package com.smartbear.postman;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PostmanImporter {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String REQUESTS = "requests";
    public static final String URL = "url";
    public static final String METHOD = "method";

    public static final String SOAP_SUFFIX = "?wsdl";

    public WsdlProject importPostmanCollection(String filePath) {
        WsdlProject project = new WsdlProjectPro();
        File jsonFile = new File(filePath);
        String postmanJson = null;
        try {
            postmanJson = FileUtils.readFileToString(jsonFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (JsonUtil.seemsToBeJson(postmanJson)) {
            try {
                JSON json = new JsonUtil().parseTrimmedText(postmanJson);
                if (json instanceof JSONObject) {
                    JSONObject postmanCollection = (JSONObject) json;
                    project.setName(getValue(postmanCollection, NAME));
                    project.setDescription(getValue(postmanCollection, DESCRIPTION));
                    JSONArray requests = postmanCollection.getJSONArray(REQUESTS);
                    for (Object requestObject : requests) {
                        if (requestObject instanceof JSONObject) {
                            JSONObject request = (JSONObject) requestObject;
                            String uri = getValue(request, URL);
                            String method = getValue(request, METHOD);
                            String serviceName = getValue(request, DESCRIPTION);
                            if (isSoapRequest(uri)) {

                            } else {
                                RestRequestInterface.HttpMethod httpMethod = RestRequestInterface.HttpMethod.valueOf(method);
                                RestService restService = (RestService) project.addNewInterface(
                                        ModelItemNamer.createName(serviceName, project.getInterfaceList()),
                                        RestServiceFactory.REST_TYPE);

                                String currentEndpoint = null;
                                if (uri.matches("http(s)?://.+")) {
                                    String host = HttpUtils.extractHost(uri);
                                    currentEndpoint = uri.substring(0, uri.indexOf("://") + 3) + host;
                                }
                                XmlBeansRestParamsTestPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null, RestParametersConfig.Factory.newInstance());
                                String path = RestUtils.extractParams(uri, params, false);
                                if (path.isEmpty()) {
                                    path = "/";
                                }
                                RestResource restResource = restService.addNewResource(path, path);
                                RestUtils.extractParams(uri, restResource.getParams(), false,
                                        RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS, true);
                                RestMethod restMethod = restResource.addNewMethod(method);
                                restMethod.setMethod(httpMethod);
                                RestRequest currentRequest = restMethod.addNewRequest(method + " Request");
                                currentRequest.setEndpoint(currentEndpoint);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
            }
        } else {
        }
        return project;
    }

    private boolean isSoapRequest(String url) {
        return StringUtils.hasContent(url) && url.endsWith(SOAP_SUFFIX);
    }

    private String getValue(JSONObject jsonObject, String name) {
        return getValue(jsonObject, name, "");
    }


    private String getValue(JSONObject jsonObject, String field, String defaultValue) {
        Object value = jsonObject.get(field);
        return value != null ? value.toString() : defaultValue;
    }
}
