package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface.HttpMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.EqualsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
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
    public static final String COLLECTION_NAME = "REST Service 1 collection";
    public static final String ENDPOINT = "http://rapis02.aqa.com.ru";
    public static final String PATH = "/WCFREST/Service.svc/ConStroka";
    public static final String PARAMETER1_NAME = "x";
    public static final String PARAMETER1_VALUE = "${#Project#string1}";
    public static final ParameterStyle PARAMETER1_STYLE = ParameterStyle.QUERY;
    public static final String PARAMETER2_NAME = "y";
    public static final String PARAMETER2_VALUE = "${#Project#string2}";
    public static final HttpMethod REQUEST_METHOD = HttpMethod.GET;
    public static final String REQUEST_NAME = "GET Request";
    public static final String PROPERTY1_NAME = "string1";
    public static final String PROPERTY1_VALUE = "abc";
    public static final String PROPERTY2_NAME = "string2";
    public static final String PROPERTY2_VALUE = "def";

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
        assertEquals("Resource has wrong name", PATH, resource.getName());
        assertEquals("Resource has wrong path", PATH, resource.getPath());
        checkParams(postmanProject, resource.getParams());


        assertEquals("Resource should have 1 method", 1, resource.getRestMethodCount());
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals("Wrong method", REQUEST_METHOD, method.getMethod());
        assertEquals("Method should have 1 request", 1, method.getRequestCount());
        RestRequest request = method.getRequestAt(0);
        assertEquals("Request has wrong name", REQUEST_NAME, request.getName());
        assertEquals("Request has wrong endpoint", ENDPOINT, request.getEndpoint());

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

}
