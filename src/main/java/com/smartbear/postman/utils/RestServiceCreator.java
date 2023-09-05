package com.smartbear.postman.utils;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.RestURIParser;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestURIParserImpl;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.postman.VariableUtils;
import com.smartbear.postman.collection.PostmanCollection;
import com.smartbear.postman.collection.Request;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import static com.eviware.soapui.impl.actions.RestServiceBuilder.ModelCreationStrategy.REUSE_MODEL;

public class RestServiceCreator {
    private static final Logger logger = LoggerFactory.getLogger(RestServiceCreator.class);
    private final WsdlProject project;
    private String uri;
    private String rawModeData;


    public RestServiceCreator(WsdlProject project) {
        this.project = project;
    }

    public RestRequest addRestRequest(Request request) {
        uri = request.getUrl();
        rawModeData = request.getBody();

        PostmanRestServiceBuilder builder = new PostmanRestServiceBuilder();

        RestRequest restRequest;
        try {
            restRequest = builder.createRestServiceFromPostman(request.getMethod(), request.getHeaders());
        } catch (Exception e) {
            logger.error("Error while creating a REST service", e);
            return null;
        }

        String requestName = request.getName();
        if (StringUtils.hasContent(requestName)) {
            restRequest.setName(requestName);
        }

        addRequestBody(request, restRequest);
        return restRequest;
    }

    private void addRequestBody(Request request, RestRequest restRequest) {
        if (!HttpUtils.canHavePayload(restRequest.getMethod()) || !StringUtils.hasContent(rawModeData)) {
            return;
        }
        if ("formdata".equals(request.getMode())) {
            JSON dataJson = new PostmanJsonUtil().parseTrimmedText(rawModeData);
            if (dataJson instanceof JSONArray) {
                JSONArray dataArray = (JSONArray) dataJson;
                Arrays.stream(dataArray.toArray()).forEach(d -> {
                    if (d instanceof JSONObject) {
                        JSONObject data = (JSONObject) d;
                        String key = data.getString("key");
                        String value = data.getString("value");

                        restRequest.getParams().addProperty(key);
                        restRequest.getParams().setPropertyValue(key, value);
                    }
                });
            }
            restRequest.setMediaType(MediaType.MULTIPART_FORM_DATA);
            restRequest.setPostQueryString(true);
        } else {
            restRequest.setRequestContent(rawModeData);
        }
    }

    private class PostmanRestServiceBuilder extends RestServiceBuilder {
        public RestRequest createRestServiceFromPostman(String httpMethod,
                                                        List<PostmanCollection.Header> headers) throws MalformedURLException {
            RestResource restResource;
            RestURIParser uriParser = new RestURIParserImpl(uri);
            String endpoint = StringUtils.hasContent(uriParser.getScheme())
                    ? uriParser.getEndpoint()
                    : uriParser.getAuthority();

            String resourcePath = convertTemplateProperties(uriParser.getResourcePath());

            if (endpoint.contains("{{")) {
                restResource = createResource(
                        ModelCreationStrategy.REUSE_MODEL,
                        VariableUtils.convertVariables(endpoint, project),
                        resourcePath,
                        uriParser.getResourceName());
            } else {
                restResource = createResource(
                        ModelCreationStrategy.REUSE_MODEL,
                        project,
                        endpoint + resourcePath);
            }

            RestMethod restMethod = addNewMethod(
                    ModelCreationStrategy.CREATE_NEW_MODEL,
                    restResource,
                    RestRequestInterface.HttpMethod.valueOf(httpMethod));

            RestRequest restRequest = addNewRequest(restMethod);
            RestParamsPropertyHolder params = extractParams(resourcePath, uriParser.getQuery());
            addRestHeaders(params, headers);
            convertParameters(params);

            RestParamsPropertyHolder requestPropertyHolder = restMethod.getParams();
            copyParameters(params, requestPropertyHolder);

            return restRequest;
        }

        protected RestParamsPropertyHolder extractParams(String path, String queryString) {
            RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null,
                    RestParametersConfig.Factory.newInstance(), NewRestResourceActionBase.ParamLocation.METHOD);

            RestUtils.extractTemplateParamsFromResourcePath(params, path);

            if (StringUtils.hasContent(queryString)) {
                RestUtils.extractParamsFromQueryString(params, queryString);
            }

            return params;
        }

        protected RestResource createResource(ModelCreationStrategy creationStrategy, String host, String resourcePath, String resourceName) {
            RestService restService = null;

            if (creationStrategy == REUSE_MODEL) {
                AbstractInterface<?, ? extends Operation> existingInterface = project.getInterfaceByName(host);
                if (existingInterface instanceof RestService && ArrayUtils.contains(existingInterface.getEndpoints(), host)) {
                    restService = (RestService) existingInterface;
                }
            }
            if (restService == null) {
                restService = (RestService) project.addNewInterface(host, RestServiceFactory.REST_TYPE);
                restService.addEndpoint(host);
            }
            if (creationStrategy == REUSE_MODEL) {
                RestResource existingResource = restService.getResourceByFullPath(RestResource.removeMatrixParams(resourcePath));
                if (existingResource != null) {
                    return existingResource;
                }
            }

            return restService.addNewResource(resourceName, resourcePath);
        }

        private String convertTemplateProperties(String postmanUri) {
            int indexOfQuery = postmanUri.indexOf("?");
            if (indexOfQuery != -1) {
                return postmanUri.substring(0, indexOfQuery).replaceAll("\\{\\{", "{").replaceAll("\\}\\}", "}")
                       + postmanUri.substring(indexOfQuery, postmanUri.length());
            } else {
                return postmanUri.replaceAll("\\{\\{", "{").replaceAll("\\}\\}", "}");
            }
        }


        private void convertParameters(RestParamsPropertyHolder propertyHolder) {
            for (TestProperty property : propertyHolder.getPropertyList()) {
                if (property instanceof RestParamProperty && ((RestParamProperty) property).getStyle() == RestParamsPropertyHolder.ParameterStyle.TEMPLATE) {
                    property.setValue("{{" + property.getName() + "}}");
                }
                String convertedValue = VariableUtils.convertVariables(property.getValue(), project);

                property.setValue(convertedValue);
                if (property instanceof RestParamProperty && StringUtils.hasContent(property.getDefaultValue())) {
                    if (((RestParamProperty) property).getStyle() == RestParamsPropertyHolder.ParameterStyle.TEMPLATE) {
                        ((RestParamProperty) property).setDefaultValue("{{" + property.getName() + "}}");
                    }
                    convertedValue = VariableUtils.convertVariables(property.getDefaultValue(), project);
                    ((RestParamProperty) property).setDefaultValue(convertedValue);
                }
            }
        }

        private void addRestHeaders(RestParamsPropertyHolder params, List<PostmanCollection.Header> headers) {
            for (PostmanCollection.Header header : headers) {
                RestParamProperty property = params.addProperty(header.getKey());
                property.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
                property.setValue(header.getValue());
            }
        }
    }
}