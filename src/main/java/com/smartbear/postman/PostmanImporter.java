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
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostmanImporter {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String REQUESTS = "requests";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String PRE_REQUEST_SCRIPT = "preRequestScript";
    public static final String TESTS = "tests";
    public static final String POSTMAN_OBJECT = "postman.";
    public static final String TEST_LIST = "tests[";
    public static final String SET_GLOBAL_VARIABLE = "setGlobalVariable(";

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

                                    WsdlTestSuite testSuite = project.getTestSuiteAt(project.getTestSuiteCount() - 1);
                                    if (testSuite != null) {
                                        WsdlTestCase testCase = testSuite.getTestCaseAt(testSuite.getTestCaseCount() - 1);
                                        if (testCase != null) {
                                            WsdlTestStep testStep = testCase.getTestStepAt(testCase.getTestStepCount() - 1);
                                            if (testStep instanceof RestTestRequestStep) {
                                                addAssertions(tests, (RestTestRequestStep) testStep);
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

    void addAssertions(String tests, Assertable assertable) {
        final Pattern statusCodePattern = Pattern.compile("(?<=responseCode\\.code\\s*===)\\s*d+");
        String[] commands = tests.split(";");
        for (String commandLine : commands) {
            String command = commandLine.trim();
            if (command.startsWith(TEST_LIST)) {
                int closeBracketPosition = command.indexOf(")", TEST_LIST.length());
                if (closeBracketPosition > 0) {
                    String assertionName = StringUtils.unquote(command.substring(TEST_LIST.length(), closeBracketPosition));
                    int equalsPosition = command.indexOf("=", closeBracketPosition);
                    if (equalsPosition > 0) {
                        String assertionString = command.substring(equalsPosition);
                        Matcher matcher = statusCodePattern.matcher(assertionString);
                        if (matcher.find()) {
                            ValidHttpStatusCodesAssertion assertion = (ValidHttpStatusCodesAssertion)
                                    assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL);
                            assertion.setCodes(matcher.group().trim());
                        }
                    }
                }
            }
        }
    }

    private void processPreRequestScript(String preRequestScript, WsdlProject project) {
        String[] commands = preRequestScript.split(";");
        for (String commandLine : commands) {
            String command = commandLine.trim();
            if (command.startsWith(POSTMAN_OBJECT)) {
                if (command.startsWith(SET_GLOBAL_VARIABLE, POSTMAN_OBJECT.length())) {
                    int methodNameLength = POSTMAN_OBJECT.length() + SET_GLOBAL_VARIABLE.length();
                    int closeBracketPosition = command.indexOf(")", methodNameLength);
                    if (closeBracketPosition > 0) {
                        String argumentsString = command.substring(methodNameLength, closeBracketPosition);
                        String[] arguments = argumentsString.split(",");
                        if (arguments.length == 2) {
                            TestProperty property = project.addProperty(StringUtils.unquote(arguments[0].trim()));
                            property.setValue(StringUtils.unquote(arguments[1].trim()));
                        }
                    }
                }
            }
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
        convertParameters(params);
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

    private String convertVariables(String postmanString, WsdlProject project) {
        if (StringUtils.isNullOrEmpty(postmanString)) {
            return postmanString;
        }

        final String POSTMAN_VARIABLE_BEGIN = "{{";
        final String POSTMAN_VARIABLE_END = "}}";
        final String READYAPI_VARIABLE_BEGIN = "\\${#Project#";
        final String READYAPI_VARIABLE_END = "}";
        final Pattern variableRegExp = Pattern.compile("\\{\\{.+\\}\\}");

        StringBuffer readyApiStringBuffer = new StringBuffer();
        Matcher matcher = variableRegExp.matcher(postmanString);
        while (matcher.find()) {
            String postmanVariable = matcher.group();
            String readyApiVariable = postmanVariable
                    .replace(POSTMAN_VARIABLE_BEGIN, READYAPI_VARIABLE_BEGIN)
                    .replace(POSTMAN_VARIABLE_END, READYAPI_VARIABLE_END);
            matcher.appendReplacement(readyApiStringBuffer, readyApiVariable);
        }
        if (readyApiStringBuffer.length() > 0) {
            matcher.appendTail(readyApiStringBuffer);
            return readyApiStringBuffer.toString();
        } else {
            return postmanString;
        }
    }

    private void convertParameters(RestParamsPropertyHolder propertyHolder) {
        for (TestProperty property : propertyHolder.getPropertyList()) {
            String convertedValue = convertVariables(property.getValue(), null);
            property.setValue(convertedValue);
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
