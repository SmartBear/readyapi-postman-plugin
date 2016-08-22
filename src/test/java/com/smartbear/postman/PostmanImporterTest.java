package com.smartbear.postman;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequest;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.EqualsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


public class PostmanImporterTest {
    private static final String OUTPUT_FOLDER_PATH = PostmanImporterTest.class.getResource("/").getPath();
    private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";

    public static final String REST_GET_COLLECTION_PATH = "/REST_Get_Collection.postman_collection";
    public static final String REST_POST_COLLECTION_PATH = "/REST_Post_Collection.postman_collection";
    public static final String WSDL_COLLECTION_PATH = "/SOAP_Collection.postman_collection";
    public static final String SAMPLE_COLLECTION_PATH = "/Postman_Echo.postman_collection";
    public static final String COLLECTION_NAME = "REST Service 1 collection";
    public static final String REST_ENDPOINT = "http://rapis02.aqa.com.ru";
    public static final String SOAP_ENDPOINT = "http://www.webservicex.com/globalweather.asmx";
    public static final String GET_PATH = "/WCFREST/Service.svc/ConStroka";
    public static final String POST_PATH = "/WCFREST/Service.svc/testComplexClass";
    public static final String PARAMETER1_NAME = "x";
    public static final String PARAMETER1_VALUE = "${#Project#string1}";
    public static final ParameterStyle PARAMETER1_STYLE = ParameterStyle.QUERY;
    public static final String PARAMETER2_NAME = "y";
    public static final String PARAMETER2_VALUE = "${#Project#string2}";
    public static final String REQUEST_NAME = "Request 1";
    public static final String WSDL_REQUEST_NAME = "Request 1";
    public static final String PROPERTY1_NAME = "string1";
    public static final String PROPERTY1_VALUE = "abc";
    public static final String PROPERTY2_NAME = "string2";
    public static final String PROPERTY2_VALUE = "def";
    private static final String OPERATION_NAME = "GetWeather";
    private static final String HEADER1_NAME = "header1";
    private static final String HEADER1_VALUE = "af";
    private static final String HEADER2_NAME = "header2";
    private static final String HEADER2_VALUE = "er";
    private static final String REST_POST_BODY_VALUE = "{\"assd\":\"qwe\"}";

    private File workspaceFile;
    private WorkspaceImpl workspace;

    @Before
    public void setUp() throws Exception {
        workspaceFile = new File(TEST_WORKSPACE_FILE_PATH);
        workspace = new WorkspaceImpl(workspaceFile.getAbsolutePath(), null);

        TestAssertionRegistry wsdlAssertionRegistry = TestAssertionRegistry.getInstance();
        wsdlAssertionRegistry.addAssertion(new EqualsAssertion.Factory());
    }

    @Test
    public void testImportRestGetRequest() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(REST_GET_COLLECTION_PATH).getPath());

        TestProperty property1 = postmanProject.getProperty(PROPERTY1_NAME);
        assertNotNull("Property1 is missing", property1);
        assertEquals("Property1 has wrong value", PROPERTY1_VALUE, property1.getValue());

        TestProperty property2 = postmanProject.getProperty(PROPERTY2_NAME);
        assertNotNull("Property2 is missing", property2);
        assertEquals("Property2 has wrong value", PROPERTY2_VALUE, property2.getValue());

        assertEquals("Project should be named after collection", COLLECTION_NAME, postmanProject.getName());
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals("Project should have 1 interface", 1, interfaceMap.size());
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));

        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals("Service should have 1 resource", 1, resources.size());
        RestResource resource = resources.get(0);
        assertEquals("Resource has wrong name", makeResourceName(GET_PATH), resource.getName());
        assertEquals("Resource has wrong path", GET_PATH, resource.getPath());


        assertEquals("Resource should have 1 method", 1, resource.getRestMethodCount());
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals("Wrong method", HttpMethod.GET, method.getMethod());
        assertEquals("Method should have 1 request", 1, method.getRequestCount());
        RestRequest request = method.getRequestAt(0);
        assertEquals("Request has wrong name", REQUEST_NAME, request.getName());
        assertEquals("Request has wrong endpoint", REST_ENDPOINT, request.getEndpoint());
        checkParams(postmanProject, request.getParams());
        List<RestParamProperty> headers = getParamsOfStyle(request.getParams(), ParameterStyle.HEADER);
        assertEquals("Request must have 2 headers", 2, headers.size());
        for (RestParamProperty header : headers) {
            if (header.getName().equals(HEADER1_NAME)) {
                assertEquals("Header1 has wrong value", HEADER1_VALUE, header.getValue());
            } else {
                assertEquals("Header2 has wrong value", HEADER2_VALUE, header.getValue());
            }
        }

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(ValidHttpStatusCodesAssertion.class));

        checkParams(postmanProject, testStep.getTestRequest().getParams());
    }

    private void checkParams(WsdlProject postmanProject, RestParamsPropertyHolder propertyHolder) {
        List<RestParamProperty> params = getParamsOfStyle(propertyHolder, ParameterStyle.QUERY);
        assertEquals("Object should have 2 params", 2, params != null ? params.size() : 0);
        RestParamProperty parameter1 = propertyHolder.getProperty(PARAMETER1_NAME);
        assertNotNull("Property 1 has not found", parameter1);
        ParameterStyle style = parameter1.getStyle();
        assertEquals("Parameter has wrong style", PARAMETER1_STYLE, style);
        assertEquals("Property has wrong value", PARAMETER1_VALUE, parameter1.getValue());

        String expandedParameter1 = PropertyExpander.expandProperties(postmanProject.getContext(), parameter1.getValue());
        assertEquals("Expansion of parameter1 is wrong", PROPERTY1_VALUE, expandedParameter1);
    }

    private List<RestParamProperty> getParamsOfStyle(RestParamsPropertyHolder propertyHolder, ParameterStyle style) {
        ArrayList<RestParamProperty> params = new ArrayList<>();
        for (TestProperty param : propertyHolder.values()) {
            if (param instanceof RestParamProperty) {
                RestParamProperty restParam = (RestParamProperty) param;
                if (restParam.getStyle() == style) {
                    params.add(restParam);
                }
            }
        }
        return params;
    }

    @Test
    public void testImportRestPostRequest() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(REST_POST_COLLECTION_PATH).getPath());

        assertEquals("Project should be named after collection", COLLECTION_NAME, postmanProject.getName());
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals("Project should have 1 interface", 1, interfaceMap.size());
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));

        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals("Service should have 1 resource", 1, resources.size());
        RestResource resource = resources.get(0);

        assertEquals("Resource has wrong name", makeResourceName(POST_PATH), resource.getName());
        assertEquals("Resource has wrong path", POST_PATH, resource.getPath());
        List<RestParamProperty> params = getParamsOfStyle(resource.getParams(), ParameterStyle.QUERY);
        assertEquals("Resource should have 0 query params", 0, params != null ? params.size() : 0);


        assertEquals("Resource should have 1 method", 1, resource.getRestMethodCount());
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals("Wrong method", HttpMethod.POST, method.getMethod());
        assertEquals("Method should have 1 request", 1, method.getRequestCount());
        RestRequest request = method.getRequestAt(0);
        assertEquals("Request has wrong name", REQUEST_NAME, request.getName());
        assertEquals("Request has wrong endpoint", REST_ENDPOINT, request.getEndpoint());

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(SimpleContainsAssertion.class));

        RestTestRequest testRequest = testStep.getTestRequest();
        List<RestParamProperty> requestParams = getParamsOfStyle(testRequest.getParams(), ParameterStyle.QUERY);
        assertEquals("Request should have 0 query params", 0, requestParams != null ? requestParams.size() : 0);
        assertEquals("Request should have test body", REST_POST_BODY_VALUE, request.getRequestContent());
    }

    private String makeResourceName(String resourcePath) {
        String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
    }

    @Test
    public void testImportWsdlRequest() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(WSDL_COLLECTION_PATH).getPath());

        assertEquals("Project should be named after collection", COLLECTION_NAME, postmanProject.getName());
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals("Project should have 2 interface", 2, interfaceMap.size());
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(WsdlInterface.class));

        WsdlInterface wsdlInterface = (WsdlInterface) service;
        WsdlOperation operation = wsdlInterface.getOperationByName(OPERATION_NAME);
        assertNotNull("Operation is missing", operation);

        assertEquals("Operation should have 1 request", 1, operation.getRequestCount());
        WsdlRequest request = operation.getRequestAt(0);
        assertEquals("Request has wrong name", WSDL_REQUEST_NAME, request.getName());
        assertEquals("Request has wrong endpoint", SOAP_ENDPOINT, request.getEndpoint());

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        WsdlTestRequestStep testStep = (WsdlTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(GroovyScriptAssertion.class));
    }

    @Test
    public void testImportSampleCollectionDoesNotHangUp() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(SAMPLE_COLLECTION_PATH).getPath());
    }

    @After
    public void tearDown() {
        if (workspaceFile.exists()) {
            workspaceFile.delete();
        }
    }

}
