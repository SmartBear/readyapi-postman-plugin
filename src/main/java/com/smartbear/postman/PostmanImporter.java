package com.smartbear.postman;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.postman.script.PostmanScriptParser;
import com.smartbear.postman.script.PostmanScriptTokenizer;
import com.smartbear.postman.script.PostmanScriptTokenizer.Token;
import com.smartbear.postman.script.ScriptContext;
import com.smartbear.ready.core.exception.ReadyApiException;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class PostmanImporter {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String REQUESTS = "requests";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String PRE_REQUEST_SCRIPT = "preRequestScript";
    public static final String TESTS = "tests";

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
                            String preRequestScript = getValue(request, PRE_REQUEST_SCRIPT);
                            String tests = getValue(request, TESTS);

                            if (StringUtils.hasContent(preRequestScript)) {
                                processPreRequestScript(preRequestScript, project);
                            }

                            if (isSoapRequest(uri)) {

                            } else {
                                RestRequest restRequest = addRestRequest(project, serviceName, method, uri);

                                if (StringUtils.hasContent(tests)) {
                                    AddRestRequestToTestCaseAction addRestRequestToTestCaseAction = new AddRestRequestToTestCaseAction();
                                    addRestRequestToTestCaseAction.perform(restRequest, null);

                                    if (project.getTestSuiteCount() > 0) {
                                        WsdlTestSuite testSuite = project.getTestSuiteAt(project.getTestSuiteCount() - 1);
                                        if (testSuite != null && testSuite.getTestCaseCount() > 0) {
                                            WsdlTestCase testCase = testSuite.getTestCaseAt(testSuite.getTestCaseCount() - 1);
                                            if (testCase != null && testCase.getTestStepCount() > 0) {
                                                WsdlTestStep testStep = testCase.getTestStepAt(testCase.getTestStepCount() - 1);
                                                if (testStep instanceof RestTestRequestStep) {
                                                    addAssertions(tests, project, (RestTestRequestStep) testStep);
                                                }
                                            }
                                        }
                                    }

                                }
                            }
//                                    GenericAddRequestToTestCaseAction.perform
                        }
                    }
                }
            } catch (JSONException e) {
            }
        } else {
        }
        return project;
    }

    void addAssertions(String tests, WsdlProject project, Assertable assertable) {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParser parser = new PostmanScriptParser();
        try {
            LinkedList<Token> tokens = tokenizer.tokenize(tests);

            ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);
            parser.parse(tokens, context);
        } catch (ReadyApiException e) {
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
        } catch (ReadyApiException e) {
            e.printStackTrace();
        }
    }

    private RestRequest addRestRequest(WsdlProject project, String serviceName, String method, String uri) {
        RestRequestInterface.HttpMethod httpMethod = RestRequestInterface.HttpMethod.valueOf(method);
        RestService restService = (RestService) project.addNewInterface(
                ModelItemNamer.createName(serviceName, project.getInterfaceList()),
                RestServiceFactory.REST_TYPE);

        String currentEndpoint = null;
        if (uri.matches("http(s)?://.+")) {
            String host = HttpUtils.extractHost(uri);
            currentEndpoint = uri.substring(0, uri.indexOf("://") + 3) + host;
        }
        XmlBeansRestParamsTestPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder(null,
                RestParametersConfig.Factory.newInstance());
        String path = RestUtils.extractParams(uri, params, false);
        if (path.isEmpty()) {
            path = "/";
        }
        RestResource restResource = restService.addNewResource(path, path);
        RestUtils.extractParams(uri, restResource.getParams(), false,
                RestUtils.TemplateExtractionOption.EXTRACT_TEMPLATE_PARAMETERS, true);
        convertParameters(restResource.getParams());
        RestMethod restMethod = restResource.addNewMethod(method);
        restMethod.setMethod(httpMethod);
        RestRequest currentRequest = restMethod.addNewRequest(method + " Request");
        currentRequest.setEndpoint(currentEndpoint);

        return currentRequest;
    }

    private void convertParameters(RestParamsPropertyHolder propertyHolder) {
        for (TestProperty property : propertyHolder.getPropertyList()) {
            String convertedValue = VariableUtils.convertVariables(property.getValue());
            property.setValue(convertedValue);
            if (property instanceof RestParamProperty && StringUtils.hasContent(property.getDefaultValue())) {
                convertedValue = VariableUtils.convertVariables(property.getDefaultValue());
                ((RestParamProperty) property).setDefaultValue(convertedValue);
            }
        }
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
