package com.smartbear.postman;

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
import com.eviware.soapui.support.types.StringToStringsMap;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;


public class PostmanImporterTest {
    public static final String REST_GET_COLLECTION_PATH = "D:\\issues\\SOAP-5525\\REST_Get_Collection.postman_collection";
    public static final String REST_POST_COLLECTION_PATH = "D:\\issues\\SOAP-5525\\REST_Post_Collection.postman_collection";
    public static final String WSDL_COLLECTION_PATH = "D:\\issues\\SOAP-5525\\SOAP_Collection.postman_collection";
    public static final String COLLECTION_NAME = "REST Service 1 collection";
    public static final String REST_ENDPOINT = "http://rapis02.aqa.com.ru";
    public static final String SOAP_ENDPOINT = "http://rapis02.aqa.com.ru/SOAP/Service1.asmx";
    public static final String GET_PATH = "/WCFREST/Service.svc/ConStroka";
    public static final String POST_PATH = "/WCFREST/Service.svc/testComplexClass";
    public static final String PARAMETER1_NAME = "x";
    public static final String PARAMETER1_VALUE = "${#Project#string1}";
    public static final ParameterStyle PARAMETER1_STYLE = ParameterStyle.QUERY;
    public static final String PARAMETER2_NAME = "y";
    public static final String PARAMETER2_VALUE = "${#Project#string2}";
    public static final String GET_REQUEST_NAME = "GET Request";
    public static final String POST_REQUEST_NAME = "POST Request";
    public static final String WSDL_REQUEST_NAME = "Request 1";
    public static final String PROPERTY1_NAME = "string1";
    public static final String PROPERTY1_VALUE = "abc";
    public static final String PROPERTY2_NAME = "string2";
    public static final String PROPERTY2_VALUE = "def";
    private static final String OPERATION_NAME = "Con_Stroka";
    private static final String HEADER1_NAME = "header1";
    private static final String HEADER1_VALUE = "af";
    private static final String HEADER2_NAME = "header2";
    private static final String HEADER2_VALUE = "er";


    @Before
    public void setUp() throws Exception {
        TestAssertionRegistry wsdlAssertionRegistry = TestAssertionRegistry.getInstance();
        wsdlAssertionRegistry.addAssertion(new EqualsAssertion.Factory());
    }

    @Test
    public void testImportRestGetRequest() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(REST_GET_COLLECTION_PATH);

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
        assertEquals("Resource has wrong name", GET_PATH, resource.getName());
        assertEquals("Resource has wrong path", GET_PATH, resource.getPath());
        checkParams(postmanProject, resource.getParams());


        assertEquals("Resource should have 1 method", 1, resource.getRestMethodCount());
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals("Wrong method", HttpMethod.GET, method.getMethod());
        assertEquals("Method should have 1 request", 1, method.getRequestCount());
        RestRequest request = method.getRequestAt(0);
        assertEquals("Request has wrong name", GET_REQUEST_NAME, request.getName());
        assertEquals("Request has wrong endpoint", REST_ENDPOINT, request.getEndpoint());
        StringToStringsMap headers = request.getRequestHeaders();
        assertEquals("Request must have 2 headers", 2, headers.size());
        List<String> values = headers.get(HEADER1_NAME);
        assertEquals("Header1 value must have 1 element", 1, values.size());
        assertEquals("Header1 has wrong value", HEADER1_VALUE, values.get(0));
        values = headers.get(HEADER2_NAME);
        assertEquals("Header2 value must have 1 element", 1, values.size());
        assertEquals("Header2 has wrong value", HEADER2_VALUE, values.get(0));

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(ValidHttpStatusCodesAssertion.class));

        checkParams(postmanProject, testStep.getTestRequest().getParams());
    }

    private void checkParams(WsdlProject postmanProject, RestParamsPropertyHolder propertyHolder) {
        assertEquals("Resource should have 2 params", 2, propertyHolder.getPropertyCount());
        RestParamProperty parameter1 = propertyHolder.getProperty(PARAMETER1_NAME);
        assertNotNull("Property 1 has not found", parameter1);
        ParameterStyle style = parameter1.getStyle();
        assertEquals("Parameter has wrong style", PARAMETER1_STYLE, style);
        assertEquals("Property has wrong value", PARAMETER1_VALUE, parameter1.getValue());

        String expandedParameter1 = PropertyExpander.expandProperties(postmanProject.getContext(), parameter1.getValue());
        assertEquals("Expansion of parameter1 is wrong", PROPERTY1_VALUE, expandedParameter1);
    }

    @Test
    public void testImportRestPostRequest() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(REST_POST_COLLECTION_PATH);

        assertEquals("Project should be named after collection", COLLECTION_NAME, postmanProject.getName());
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals("Project should have 1 interface", 1, interfaceMap.size());
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));

        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals("Service should have 1 resource", 1, resources.size());
        RestResource resource = resources.get(0);
        assertEquals("Resource has wrong name", POST_PATH, resource.getName());
        assertEquals("Resource has wrong path", POST_PATH, resource.getPath());
        assertEquals("Resource should have 0 params", 0, resource.getParams().getPropertyCount());


        assertEquals("Resource should have 1 method", 1, resource.getRestMethodCount());
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals("Wrong method", HttpMethod.POST, method.getMethod());
        assertEquals("Method should have 1 request", 1, method.getRequestCount());
        RestRequest request = method.getRequestAt(0);
        assertEquals("Request has wrong name", POST_REQUEST_NAME, request.getName());
        assertEquals("Request has wrong endpoint", REST_ENDPOINT, request.getEndpoint());

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(SimpleContainsAssertion.class));

        assertEquals("Resource should have 0 params", 0, testStep.getTestRequest().getParams().getPropertyCount());
    }

    @Test
    public void testImportWsdlRequest() {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(WSDL_COLLECTION_PATH);

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

//        assertEquals("Resource should have 0 params", 0, testStep.getTestRequest().getParams().getPropertyCount());
    }

}
