/**
 * Copyright 2016 SmartBear Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbear.postman;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.actions.RestServiceBuilder;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.RestURIParser;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.rest.support.RestURIParserImpl;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.smartbear.postman.collection.PostmanCollection;
import com.smartbear.postman.collection.PostmanCollectionFactory;
import com.smartbear.postman.script.PostmanScriptParser;
import com.smartbear.postman.script.PostmanScriptTokenizer;
import com.smartbear.postman.script.PostmanScriptTokenizer.Token;
import com.smartbear.postman.script.ScriptContext;
import com.smartbear.postman.utils.PostmanJsonUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.eviware.soapui.impl.actions.RestServiceBuilder.ModelCreationStrategy.REUSE_MODEL;

public class PostmanImporter {
    public static final String WSDL_SUFFIX = "?WSDL";
    private static final Logger logger = LoggerFactory.getLogger(PostmanImporter.class);
    private final TestCreator testCreator;

    public PostmanImporter(TestCreator testCreator) {
        this.testCreator = testCreator;
    }

    public WsdlProject importPostmanCollection(WorkspaceImpl workspace, String filePath) {
        WsdlProject project = null;
        File jsonFile = new File(filePath);
        String postmanJson = null;
        try {
            postmanJson = FileUtils.readFileToString(jsonFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (PostmanJsonUtil.seemsToBeJson(postmanJson)) {
            JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
            if (json instanceof JSONObject) {
                PostmanCollection postmanCollection = PostmanCollectionFactory.getCollection((JSONObject) json);
                String collectionName = postmanCollection.getName();
                String projectName = createProjectName(collectionName, workspace.getProjectList());
                try {
                    project = workspace.createProject(projectName, null);
                } catch (SoapUIException e) {
                    logger.error("Error while creating a project", e);
                    return null;
                }
                project.setDescription(postmanCollection.getDescription());
                List<PostmanCollection.Request> requests = postmanCollection.getRequests();
                for (PostmanCollection.Request request : requests) {
                    String uri = request.getUrl();
                    String requestName = request.getName();
                    String preRequestScript = request.getPreRequestScript();
                    String tests = request.getTests();
                    String rawModeData = request.getBody();

                    logger.info("Importing a request with URI [" + uri + "] - started");

                    if (StringUtils.hasContent(preRequestScript)) {
                        processPreRequestScript(preRequestScript, project);
                    }

                    Assertable assertable = null;

                    if (isWsdlRequest(uri)) {
                        String operationName = getOperationName(rawModeData);
                        WsdlRequest wsdlRequest = addWsdlRequest(project, request.getMethod(), uri,
                                operationName, rawModeData);

                        if (wsdlRequest != null) {
                            addSoapHeaders(wsdlRequest, request.getHeaders(), project);

                            if (StringUtils.hasContent(requestName)) {
                                wsdlRequest.setName(requestName);
                            }

                            if (StringUtils.hasContent(tests)) {
                                testCreator.createTest(wsdlRequest);
                                assertable = getTestRequestStep(project, WsdlTestRequestStep.class);
                            }
                        }
                    } else {
                        RestRequest restRequest = addRestRequest(project, request.getMethod(), uri, request.getHeaders());
                        if (restRequest == null) {
                            logger.error("Could not import " + request.getMethod() + " request with URI [" + uri + "]");
                            continue;
                        }

                        if (StringUtils.hasContent(requestName)) {
                            restRequest.setName(requestName);
                        }

                        if (HttpUtils.canHavePayload(restRequest.getMethod()) && StringUtils.hasContent(rawModeData)) {
                            restRequest.setRequestContent(rawModeData);
                        }

                        if (StringUtils.hasContent(tests)) {
                            testCreator.createTest(restRequest);
                            assertable = getTestRequestStep(project, RestTestRequestStep.class);
                        }
                    }

                    if (assertable != null) {
                        addAssertions(tests, project, assertable);
                    }

                    logger.info("Importing a request with URI [" + uri + "] - done");
                }
            }
        }
        return project;
    }

    private static String createProjectName(String collectionName, List<? extends Project> projectList) {
        Class clazz;
        try {
            clazz = Class.forName("com.eviware.soapui.support.ModelItemNamer$NumberSuffixStrategy");
            Method method = ModelItemNamer.class.getMethod("createName", String.class, Iterable.class, clazz);
            if (clazz.isEnum()) {
                return (String) method.invoke(null, collectionName, projectList,
                        Enum.valueOf(clazz, "SUFFIX_WHEN_CONFLICT_FOUND"));
            }
        } catch (Throwable e) {
            logger.warn("Setting number suffix strategy is only supported in ReadyAPI", e);
        }

        return ModelItemNamer.createName(collectionName, projectList);
    }

    private void addSoapHeaders(AbstractHttpRequest request, List<PostmanCollection.Header> headers,
                                WsdlProject projectToAddProperties) {
        for (PostmanCollection.Header header : headers) {
            StringToStringsMap headersMap = request.getRequestHeaders();
            headersMap.add(header.getKey(), VariableUtils.convertVariables(header.getValue(), projectToAddProperties));
            request.setRequestHeaders(headersMap);
        }
    }

    private void addRestHeaders(RestParamsPropertyHolder params, List<PostmanCollection.Header> headers) {
        for (PostmanCollection.Header header : headers) {
            RestParamProperty property = params.addProperty(header.getKey());
            property.setStyle(ParameterStyle.HEADER);
            property.setValue(header.getValue());
        }
    }

    private String getOperationName(String xml) {
        try {
            XmlObject xmlObject = XmlUtils.createXmlObject(xml, new XmlOptions());
            String xpath = "//*:Body/*[1]";
            XmlObject[] nodes = xmlObject.selectPath(xpath);
            if (nodes.length > 0) {
                return nodes[0].getDomNode().getLocalName();
            }
        } catch (XmlException e) {
            e.printStackTrace();
        }
        return null;
    }

    void addAssertions(String tests, WsdlProject project, Assertable assertable) {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParser parser = new PostmanScriptParser();
        try {
            LinkedList<Token> tokens = tokenizer.tokenize(tests);

            ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);
            parser.parse(tokens, context);
        } catch (SoapUIException e) {
            e.printStackTrace();
        }
    }

    private void processPreRequestScript(String preRequestScript, WsdlProject project) {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParser parser = new PostmanScriptParser();
        try {
            LinkedList<Token> tokens = tokenizer.tokenize(preRequestScript);

            ScriptContext context = ScriptContext.preparePreRequestScriptContext(project);
            parser.parse(tokens, context);
        } catch (SoapUIException e) {
            e.printStackTrace();
        }
    }

    private RestRequest addRestRequest(WsdlProject project, String method, String uri, List<PostmanCollection.Header> headers) {
        RestRequest currentRequest = null;
        PostmanRestServiceBuilder builder = new PostmanRestServiceBuilder();
        try {
            currentRequest = builder.createRestServiceFromPostman(project, uri,
                    RestRequestInterface.HttpMethod.valueOf(method), headers);
        } catch (Exception e) {
           logger.error("Error while creating a REST service", e);
        }
        return currentRequest;
    }

    private WsdlRequest addWsdlRequest(WsdlProject project, String method, String uri, String operationName, String requestContent) {
        WsdlRequest request = null;
        ArrayList<WsdlInterface> interfaces = new ArrayList<>();
        List<WsdlInterface> existingWsdlInterfaces = findExistingWsdlInterfaces(project, uri);
        if (existingWsdlInterfaces.size() > 0) {
            interfaces.addAll(existingWsdlInterfaces);
        } else {
            try {
                interfaces.addAll(
                        Arrays.asList(WsdlInterfaceFactory.importWsdl(project, uri, false)));
            } catch (SoapUIException e) {
                e.printStackTrace();
            }
        }

        for (WsdlInterface wsdlInterface : interfaces) {
            WsdlOperation operation = wsdlInterface.getOperationByName(operationName);
            if (operation != null) {
                request = operation.addNewRequest("Request 1");
                request.setRequestContent(requestContent);
                break;
            }
        }
        return request;
    }

    private List<WsdlInterface> findExistingWsdlInterfaces(WsdlProject project, String uri) {
        List<WsdlInterface> existingInterfaces = new ArrayList<>();
        for (Interface iface : project.getInterfaceList()) {
            if (iface instanceof WsdlInterface
                    && ((WsdlInterface) iface).getDefinition().equals(uri)) {
                existingInterfaces.add((WsdlInterface) iface);
            }
        }
        return existingInterfaces;
    }

    private <T> T getTestRequestStep(WsdlProject project, Class<T> stepClass) {
        if (project.getTestSuiteCount() > 0) {
            WsdlTestSuite testSuite = project.getTestSuiteAt(project.getTestSuiteCount() - 1);
            if (testSuite != null && testSuite.getTestCaseCount() > 0) {
                WsdlTestCase testCase = testSuite.getTestCaseAt(testSuite.getTestCaseCount() - 1);
                if (testCase != null && testCase.getTestStepCount() > 0) {
                    WsdlTestStep testStep = testCase.getTestStepAt(testCase.getTestStepCount() - 1);
                    if (stepClass.isInstance(testStep)) {
                        return (T) testStep;
                    }
                }
            }
        }
        return null;
    }

    private void convertParameters(RestParamsPropertyHolder propertyHolder, WsdlProject project) {
        for (TestProperty property : propertyHolder.getPropertyList()) {
            if (property instanceof RestParamProperty && ((RestParamProperty) property).getStyle() == ParameterStyle.TEMPLATE) {
                property.setValue("{{" + property.getName() + "}}");
            }
            String convertedValue = VariableUtils.convertVariables(property.getValue(), project);

            property.setValue(convertedValue);
            if (property instanceof RestParamProperty && StringUtils.hasContent(property.getDefaultValue())) {
                if (((RestParamProperty) property).getStyle() == ParameterStyle.TEMPLATE) {
                    ((RestParamProperty) property).setDefaultValue("{{" + property.getName() + "}}");
                }
                convertedValue = VariableUtils.convertVariables(property.getDefaultValue(), project);
                ((RestParamProperty) property).setDefaultValue(convertedValue);
            }
        }
    }

    private boolean isWsdlRequest(String url) {
        return StringUtils.hasContent(url) && url.toUpperCase().endsWith(WSDL_SUFFIX);
    }

    /**
     * https://smartbear.atlassian.net/wiki/spaces/PD/pages/172544951/ReadyAPI+analytics+home-phone+data+revision
     */
    public static void sendAnalytics() {
        Class analyticsClass;
        try {
            analyticsClass = Class.forName("com.smartbear.analytics.Analytics");
        } catch (ClassNotFoundException e) {
            return;
        }
        try {
            Method getManagerMethod = analyticsClass.getMethod("getAnalyticsManager");
            Object analyticsManager = getManagerMethod.invoke(null);
            Class analyticsCategoryClass = Class.forName("com.smartbear.analytics.AnalyticsManager$Category");
            Method trackMethod = analyticsManager.getClass().getMethod("trackAction", analyticsCategoryClass,
                    String.class, Map.class);
            Map<String, String> params = new HashMap();
            params.put("SourceModule", "Any");
            params.put("ProductArea", "MainMenu");
            params.put("Type", "REST");
            params.put("Source", "PostmanCollection");
            trackMethod.invoke(analyticsManager, Enum.valueOf(analyticsCategoryClass, "CUSTOM_PLUGIN_ACTION"),
                    "CreateProject", params);
        } catch (Throwable e) {
            logger.error("Error while sending analytics", e);
        }
    }

    private class PostmanRestServiceBuilder extends RestServiceBuilder {
        public RestRequest createRestServiceFromPostman(final WsdlProject paramWsdlProject,
                                                        String uri,
                                                        HttpMethod httpMethod,
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
                        paramWsdlProject,
                        VariableUtils.convertVariables(endpoint, paramWsdlProject),
                        resourcePath,
                        uriParser.getResourceName());
            } else {
                restResource = createResource(
                        ModelCreationStrategy.REUSE_MODEL,
                        paramWsdlProject,
                        endpoint + resourcePath);
            }

            RestMethod restMethod = addNewMethod(
                    ModelCreationStrategy.CREATE_NEW_MODEL,
                    restResource,
                    httpMethod);

            RestRequest restRequest = addNewRequest(restMethod);
            RestParamsPropertyHolder params = extractParams(resourcePath, uriParser.getQuery());
            addRestHeaders(params, headers);
            convertParameters(params, paramWsdlProject);

            RestParamsPropertyHolder requestPropertyHolder = restMethod.getParams();
            copyParameters(params, requestPropertyHolder);

            return restRequest;
        }

        protected RestParamsPropertyHolder extractParams(String path, String queryString) {
            RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null,
                    RestParametersConfig.Factory.newInstance(), ParamLocation.METHOD);

            RestUtils.extractTemplateParamsFromResourcePath(params, path);

            if (StringUtils.hasContent(queryString)) {
                RestUtils.extractParamsFromQueryString(params, queryString);
            }

            return params;
        }

        protected RestResource createResource(ModelCreationStrategy creationStrategy, WsdlProject project, String host, String resourcePath, String resourceName) {
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
}
