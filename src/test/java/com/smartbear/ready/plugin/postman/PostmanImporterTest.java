package com.smartbear.ready.plugin.postman;

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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.EqualsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ChaiAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import org.apache.commons.io.FileUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PostmanImporterTest {
    private static final String OUTPUT_FOLDER_PATH = PostmanImporterTest.class.getResource("/").getPath();
    private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";
    private static final WireMockServer WIREMOCK = new WireMockServer(wireMockConfig().port(28089));

    public static final String REST_GET_COLLECTION_2_1_EVENTS_PATH = "/REST_Get_Collection_events.postman_collection_v2.1";
    public static final String REST_GET_COLLECTION_2_0_PATH = "/REST_Get_Collection.postman_collection_v2.0";
    public static final String REST_GET_COLLECTION_2_1_PATH = "/REST_Get_Collection.postman_collection_v2.1";
    public static final String REST_POST_COLLECTION_2_0_PATH = "/REST_Post_Collection.postman_collection_v2.0";
    public static final String REST_POST_COLLECTION_CHAI_MIXED_2_1_PATH = "/REST_Post_Collection.postman_collection_chai_mixed_v2.1.json";
    public static final String REST_POST_COLLECTION_CHAI_2_1_PATH = "/REST_Post_Collection.postman_collection_chai_v2.1.json";
    public static final String REST_POST_COLLECTION_CHAI_2_1_EXPECTED_PATH = "/REST_Post_Collection.postman_collection_chai_v2.1_expected.txt";
    public static final String REST_POST_COLLECTION_2_1_PATH = "/REST_Post_Collection.postman_collection_v2.1";
    public static final String REST_POST_COLLECTION_2_1_EVENTS_PATH = "/REST_Post_Collection_events.postman_collection_v2.1";
    public static final String PARAMETERIZED_COLLECTION_2_1_PATH = "/Parameterized_Endpoint_Collection.postman_collection_v2.1";
    public static final String WSDL_COLLECTION_2_1_EVENTS_PATH = "/SOAP_Collection_events.postman_collection_v2.1";
    public static final String WSDL_COLLECTION_2_0_PATH = "/SOAP_Collection.postman_collection_v2.0";
    public static final String WSDL_COLLECTION_2_1_PATH = "/SOAP_Collection.postman_collection_v2.1";
    public static final String SAMPLE_COLLECTION_2_0_PATH = "/Postman_Echo.postman_collection_v2.0";
    public static final String SAMPLE_COLLECTION_2_1_PATH = "/Postman_Echo.postman_collection_v2.1";
    public static final String NEW_HTTP_METHODS_COLLECTION_2_1_PATH = "/New_Methods_Collection.postman_collection_v2.1";
    public static final String GRAPHQL_COLLECTION_OLD_ASSERTIONS_2_0_PATH = "/graphql/GraphQL_Collection_with_old_assertions.postman_collection_v2.0";
    public static final String COLLECTION_NAME = "REST Service 1 collection";
    public static final String GRAPHQL_COLLECTION_NAME = "Postman Collection (from GraphQL)";
    public static final String REST_ENDPOINT = "http://rapis02.aqa.com.ru";
    public static final String SOAP_ENDPOINT = "http://localhost:28089/SOAP/Service1.asmx";
    public static final String GET_PATH = "/WCFREST/Service.svc/ConStroka";
    public static final String POST_PATH = "/WCFREST/Service.svc/testComplexClass";
    public static final String PARAMETER1_NAME = "x";
    public static final String PARAMETER1_VALUE = "${#Project#string1}";
    public static final ParameterStyle PARAMETER1_STYLE = ParameterStyle.QUERY;
    public static final String REQUEST_NAME = "http://rapis02.aqa.com.ru/WCFREST/Service.svc/ConStroka?x=abc&y=def";
    public static final String POST_REQUEST_NAME = "http://rapis02.aqa.com.ru/WCFREST/Service.svc/testComplexClass";
    public static final String WSDL_REQUEST_NAME = "SOAP Date request";
    public static final String PROPERTY1_NAME = "string1";
    public static final String PROPERTY1_VALUE = "abc";
    public static final String PROPERTY2_NAME = "string2";
    public static final String PROPERTY2_VALUE = "def";
    private static final String OPERATION_NAME = "Date";
    private static final String HEADER1_NAME = "header1";
    private static final String HEADER1_VALUE = "af";
    private static final String HEADER2_VALUE = "er";
    private static final String REST_POST_BODY_VALUE = "{\"assd\":\"qwe\"}";
    public static final String PARAMETERIZED_REQUEST_NAME = "variableInsteadHost";
    public static final String PARAMETERIZED_ENDPOINT = "http://${#Project#host}";
    public static final String PARAMETERIZED_RESOURCE_PATH = "/{ResourceID}";
    public static final String PARAMETERIZED_RESOURCE_NAME = "ResourceID";
    public static final String TEMPLATE_PARAMETER_NAME = "ResourceID";
    public static final String TEMPLATE_PARAMETER_VALUE = "${#Project#ResourceID}";
    public static final ParameterStyle TEMPLATE_PARAMETER_STYLE = ParameterStyle.TEMPLATE;
    public static final ParameterStyle QUERY_PARAMETER_STYLE = ParameterStyle.QUERY;
    public static final String QUERY_PARAMETER_NAME = "qparam";
    public static final String QUERY_PARAMETER_VALUE = "${#Project#queryParam}";
    public static final String[] GRAPHQL_REQUESTS = {"addCustomer", "editCustomer", "customer", "customers"};
    private static final String SOAP_PUBLIC_SOAP_APIS_SERVICE_1_ASMX = "soap/public_soap_apis/Service1.asmx";
    public static final Map<String, String> GLOBAL_VARIABLES = Map.of(
      "new_key", "new_value",
      "old_key", "old_value"
    );

    private static final String EXPECTED_SCRIPT_TEXT_WHEN_OLD_AND_NEW_ASSERTIONS =
        """
        ready.test("Status code is 200 - new", () => chai.expect(messageExchange.response.getStatusCode()).to.eql(200));
        
        ready.test("Body matches string - new", () => {
          const jsonData = JSON.parse(messageExchange.response.contentAsString);
          chai.expect(jsonData.model).to.eql("SUBSCRIPTION");
        });""";

    private File workspaceFile;
    private WorkspaceImpl workspace;

    @BeforeAll
    public static void wireMockInit() throws URISyntaxException, IOException {
        URL resource = PostmanImporterTest.class.getClassLoader().getResource(SOAP_PUBLIC_SOAP_APIS_SERVICE_1_ASMX);
        Path wsdlPath = Paths.get(resource.toURI());
        String wsdlContent = Files.readString(wsdlPath);
        WIREMOCK.stubFor(get(urlEqualTo("/SOAP/Service1.asmx?WSDL"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type",
                                        "Multipart/Related; boundary=\"----=_Part_112_400566523.1602581633780\"; type=\"application/xop+xml\"; start-info=\"application/soap+xml\"")
                                .withBody(wsdlContent)
                )
        );
        WIREMOCK.start();
    }

    @AfterAll
    public static void shutdownWiremock() {
        WIREMOCK.stop();
    }

    @BeforeEach
    public void setUp() throws Exception {
        workspaceFile = new File(TEST_WORKSPACE_FILE_PATH);
        workspace = new WorkspaceImpl(workspaceFile.getAbsolutePath(), null);

        TestAssertionRegistry wsdlAssertionRegistry = TestAssertionRegistry.getInstance();
        wsdlAssertionRegistry.addAssertion(new EqualsAssertion.Factory());
    }

    @Test
    public void testImportRestGetRequestFromTestsNode() throws Exception {
        testImportRestGetRequest(REST_GET_COLLECTION_2_1_PATH);
    }

    @Test
    public void testImportRestGetRequestFromEventsNode() throws Exception {
        testImportRestGetRequest(REST_GET_COLLECTION_2_1_EVENTS_PATH);
    }

    @Test
    public void testImportRestGetRequestFromCollection20() throws Exception {
        testImportRestGetRequest(REST_GET_COLLECTION_2_0_PATH);
    }

    @Test
    public void testImportRestGetRequestFromCollection21() throws Exception {
        testImportRestGetRequest(REST_GET_COLLECTION_2_1_PATH);
    }

    private void testImportRestGetRequest(String collectionPath) throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(collectionPath).getPath());

        TestProperty property1 = postmanProject.getProperty(PROPERTY1_NAME);
        assertNotNull(property1, "Property1 is missing");
        assertEquals(PROPERTY1_VALUE, property1.getValue(), "Property1 has wrong value");

        TestProperty property2 = postmanProject.getProperty(PROPERTY2_NAME);
        assertNotNull(property2, "Property2 is missing");
        assertEquals(PROPERTY2_VALUE, property2.getValue(), "Property2 has wrong value");

        assertEquals(COLLECTION_NAME, postmanProject.getName(), "Project should be named after collection");
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals(1, interfaceMap.size(), "Project should have 1 interface");
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));

        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals(1, resources.size(), "Service should have 1 resource");
        RestResource resource = resources.get(0);
        assertEquals(makeResourceName(GET_PATH), resource.getName(), "Resource has wrong name");
        assertEquals(GET_PATH, resource.getPath(), "Resource has wrong path");


        assertEquals(1, resource.getRestMethodCount(), "Resource should have 1 method");
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals(HttpMethod.GET, method.getMethod(), "Wrong method");
        assertEquals(1, method.getRequestCount(), "Method should have 1 request");
        RestRequest request = method.getRequestAt(0);
        assertEquals(REQUEST_NAME, request.getName(), "Request has wrong name");
        assertEquals(REST_ENDPOINT, request.getEndpoint(), "Request has wrong endpoint");
        checkParams(postmanProject, request.getParams());
        List<RestParamProperty> headers = getParamsOfStyle(request.getParams(), ParameterStyle.HEADER);
        assertEquals(2, headers.size(), "Request must have 2 headers");
        for (RestParamProperty header : headers) {
            if (header.getName().equals(HEADER1_NAME)) {
                assertEquals(HEADER1_VALUE, header.getValue(), "Header1 has wrong value");
            } else {
                assertEquals(HEADER2_VALUE, header.getValue(), "Header2 has wrong value");
            }
        }

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(ValidHttpStatusCodesAssertion.class));

        checkParams(postmanProject, testStep.getTestRequest().getParams());
    }

    @Test
    public void testImportRequestWithParameterizedEndpoint() throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(PARAMETERIZED_COLLECTION_2_1_PATH).getPath());

        TestProperty hostParam = postmanProject.getProperty("host");
        assertNotNull(hostParam, "host property is missing");

        TestProperty resourceIdParam = postmanProject.getProperty("ResourceID");
        assertNotNull(resourceIdParam, "ResourceID property is missing");

        TestProperty queryParam = postmanProject.getProperty("queryParam");
        assertNotNull(queryParam, "queryParam property is missing");

        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals(1, interfaceMap.size(), "Project should have 1 interfaces");

        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));

        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals(1, resources.size(), "Service should have 1 resource");
        RestResource resource = resources.get(0);
        assertEquals(PARAMETERIZED_RESOURCE_NAME, resource.getName(), "Resource has wrong name");
        assertEquals(PARAMETERIZED_RESOURCE_PATH, resource.getPath(), "Resource has wrong path");

        assertEquals(1, resource.getRestMethodCount(), "Resource should have 1 method");
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals(HttpMethod.GET, method.getMethod(), "Wrong method");
        assertEquals(1, method.getRequestCount(), "Method should have 1 request");

        RestRequest request = method.getRequestAt(0);
        assertEquals(PARAMETERIZED_REQUEST_NAME, request.getName(), "Request has wrong name");
        assertEquals(PARAMETERIZED_ENDPOINT, request.getEndpoint(), "Request has wrong endpoint");

        assertEquals(2, request.getPropertyCount(), "Object should have 2 params");

        RestParamProperty parameter1 = request.getProperty(TEMPLATE_PARAMETER_NAME);
        assertNotNull(parameter1, "Template property has not found");
        assertEquals(TEMPLATE_PARAMETER_STYLE, parameter1.getStyle(), "Template property has wrong style");
        assertEquals(TEMPLATE_PARAMETER_VALUE, parameter1.getValue(), "Template property has wrong value");

        RestParamProperty parameter2 = request.getProperty(QUERY_PARAMETER_NAME);
        assertNotNull(parameter2, "Query property has not found");
        assertEquals(QUERY_PARAMETER_STYLE, parameter2.getStyle(), "Query property has wrong style");
        assertEquals(QUERY_PARAMETER_VALUE, parameter2.getValue(), "Query property has wrong value");
    }

    private void checkParams(WsdlProject postmanProject, RestParamsPropertyHolder propertyHolder) {
        List<RestParamProperty> params = getParamsOfStyle(propertyHolder, ParameterStyle.QUERY);
        assertEquals(2, params != null ? params.size() : 0, "Object should have 2 params");
        RestParamProperty parameter1 = propertyHolder.getProperty(PARAMETER1_NAME);
        assertNotNull(parameter1, "Property 1 has not found");
        ParameterStyle style = parameter1.getStyle();
        assertEquals(PARAMETER1_STYLE, style, "Parameter has wrong style");
        assertEquals(PARAMETER1_VALUE, parameter1.getValue(), "Property has wrong value");

        String expandedParameter1 = PropertyExpander.expandProperties(postmanProject.getContext(), parameter1.getValue());
        assertEquals(PROPERTY1_VALUE, expandedParameter1, "Expansion of parameter1 is wrong");
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
    public void testImportRestPostRequestFromTestsNode() throws Exception {
        testImportRestPostRequest(REST_POST_COLLECTION_2_1_PATH);
    }

    @Test
    public void testImportRestPostRequestFromEventsNode() throws Exception {
        testImportRestPostRequest(REST_POST_COLLECTION_2_1_EVENTS_PATH);
    }

    @Test
    public void testImportRestPostRequestFromCollectionOldAndNewAssertions() throws Exception {
        testImportRestRequestWithOldAndNewAssertions(REST_POST_COLLECTION_CHAI_MIXED_2_1_PATH);
    }

    @Test
    public void testImportRestPostRequestFromCollectionChai() throws Exception {
        testImportRestChaiRequestWithChaiAssertions(REST_POST_COLLECTION_CHAI_2_1_PATH);
    }

    @Test
    public void testImportRestPostRequestFromCollection20() throws Exception {
        testImportRestPostRequest(REST_POST_COLLECTION_2_0_PATH);
    }

    @Test
    public void testImportRestPostRequestFromCollection21() throws Exception {
        testImportRestPostRequest(REST_POST_COLLECTION_2_1_PATH);
    }

    public void testImportRestPostRequest(String collectionPath) throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(collectionPath).getPath());

        assertEquals(COLLECTION_NAME, postmanProject.getName(), "Project should be named after collection");
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals(1, interfaceMap.size(), "Project should have 1 interface");
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));

        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals(1, resources.size(), "Service should have 1 resource");
        RestResource resource = resources.get(0);

        assertEquals(makeResourceName(POST_PATH), resource.getName(), "Resource has wrong name");
        assertEquals(POST_PATH, resource.getPath(), "Resource has wrong path");
        List<RestParamProperty> params = getParamsOfStyle(resource.getParams(), ParameterStyle.QUERY);
        assertEquals(0, params != null ? params.size() : 0, "Resource should have 0 query params");


        assertEquals(1, resource.getRestMethodCount(), "Resource should have 1 method");
        RestMethod method = resource.getRestMethodAt(0);
        assertEquals(HttpMethod.POST, method.getMethod(), "Wrong method");
        assertEquals(1, method.getRequestCount(), "Method should have 1 request");
        RestRequest request = method.getRequestAt(0);
        assertEquals(POST_REQUEST_NAME, request.getName(), "Request has wrong name");
        assertEquals(REST_ENDPOINT, request.getEndpoint(), "Request has wrong endpoint");

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(SimpleContainsAssertion.class));

        RestTestRequest testRequest = testStep.getTestRequest();
        List<RestParamProperty> requestParams = getParamsOfStyle(testRequest.getParams(), ParameterStyle.QUERY);
        assertEquals(0, requestParams != null ? requestParams.size() : 0, "Request should have 0 query params");
        assertEquals(REST_POST_BODY_VALUE, request.getRequestContent(), "Request should have test body");
    }

    public void testImportRestRequestWithOldAndNewAssertions(String collectionPath) throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(
            workspace,
            PostmanImporterTest.class.getResource(collectionPath).getPath()
        );

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);

        assertThat(testStep.getAssertionAt(0), instanceOf(ChaiAssertion.class));
        assertEquals(EXPECTED_SCRIPT_TEXT_WHEN_OLD_AND_NEW_ASSERTIONS, ((GroovyScriptAssertion)testStep.getAssertionAt(0)).getScriptText());
        assertThat(testStep.getAssertionAt(1), instanceOf(ValidHttpStatusCodesAssertion.class));
        assertThat(testStep.getAssertionAt(2), instanceOf(SimpleContainsAssertion.class));

        GLOBAL_VARIABLES.forEach(
            (key, value) ->
                assertEquals(value, postmanProject.getProperty(key).getValue())
        );
    }

    public void testImportRestChaiRequestWithChaiAssertions(String collectionPath) throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(
            workspace,
            PostmanImporterTest.class.getResource(collectionPath).getPath()
        );
        String expectedChaiTests = FileUtils.readFileToString(
            new File(getClass().getResource(REST_POST_COLLECTION_CHAI_2_1_EXPECTED_PATH).getPath()),
                StandardCharsets.UTF_8
            ).trim();

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        RestTestRequestStep testStep = (RestTestRequestStep) testCase.getTestStepAt(0);
        assertEquals(expectedChaiTests, ((GroovyScriptAssertion) testStep.getAssertionAt(0)).getScriptText());
    }

    @Test
    @Disabled(GRAPHQL_COLLECTION_OLD_ASSERTIONS_2_0_PATH + " not available")
    public void testImportGraphQlRequests() throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(GRAPHQL_COLLECTION_OLD_ASSERTIONS_2_0_PATH).getPath());

        assertEquals(GRAPHQL_COLLECTION_NAME, postmanProject.getName(), "Project should be named after collection");
        assertEquals(1, postmanProject.getTestSuiteCount(), "Project should have 1 test suite");
        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        assertEquals(1, testSuite.getTestCaseCount(), "Test suite should have 1 test case");
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        assertEquals(GRAPHQL_REQUESTS.length, testCase.getTestStepCount(), "Test case should have 4 steps");
        for (int i = 0; i < 4; i++) {
            WsdlTestStep testStep = testCase.getTestStepAt(i);
            assertEquals(GRAPHQL_REQUESTS[i], testStep.getName(), "Test step '" + GRAPHQL_REQUESTS[i] + "' is missing");
        }
    }

    private String makeResourceName(String resourcePath) {
        String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/") + 1);
        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
    }

    @Test
    public void testImportWsdlRequestFromTestsNode() throws Exception {
        testImportWsdlRequest(WSDL_COLLECTION_2_1_PATH);
    }

    @Test
    public void testImportWsdlRequestFromEventsNode() throws Exception {
        testImportWsdlRequest(WSDL_COLLECTION_2_1_EVENTS_PATH);
    }

    @Test
    public void testImportWsdlRequestFromCollection20() throws Exception {
        testImportWsdlRequest(WSDL_COLLECTION_2_0_PATH);
    }

    @Test
    public void testImportWsdlRequestFromCollection21() throws Exception {
        testImportWsdlRequest(WSDL_COLLECTION_2_1_PATH);
    }

    public void testImportWsdlRequest(String collectionPath) throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(collectionPath).getPath());

        assertEquals(COLLECTION_NAME, postmanProject.getName(), "Project should be named after collection");
        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals(2, interfaceMap.size(), "Project should have 2 interface");
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(WsdlInterface.class));

        WsdlInterface wsdlInterface = (WsdlInterface) service;
        WsdlOperation operation = wsdlInterface.getOperationByName(OPERATION_NAME);
        assertNotNull(operation, "Operation is missing");

        assertEquals(1, operation.getRequestCount(), "Operation should have 1 request");
        WsdlRequest request = operation.getRequestAt(0);
        assertEquals(WSDL_REQUEST_NAME, request.getName(), "Request has wrong name");
        assertEquals(SOAP_ENDPOINT, request.getEndpoint(), "Request has wrong endpoint");

        WsdlTestSuite testSuite = postmanProject.getTestSuiteAt(0);
        WsdlTestCase testCase = testSuite.getTestCaseAt(0);
        WsdlTestRequestStep testStep = (WsdlTestRequestStep) testCase.getTestStepAt(0);
        TestAssertion assertion = testStep.getAssertionAt(0);
        assertThat(assertion, instanceOf(ValidHttpStatusCodesAssertion.class));
    }

    @Test
    public void testImportNewHttpMethods() throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject postmanProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(NEW_HTTP_METHODS_COLLECTION_2_1_PATH).getPath());

        Map<String, Interface> interfaceMap = postmanProject.getInterfaces();
        assertEquals(1, interfaceMap.size(), "Project should have 1 interface");
        Interface service = postmanProject.getInterfaceAt(0);
        assertThat(service, instanceOf(RestService.class));
        RestService restService = (RestService) service;
        List<RestResource> resources = restService.getResourceList();
        assertEquals(1, resources.size(), "Service should have 1 resource");
        RestResource resource = resources.get(0);
        assertEquals(5, resource.getRestMethodCount(), "Resource should have 5 methods");
    }

    @Test
    public void testSampleCollectionCreatesTheSameProjectFrom20and21() throws Exception {
        PostmanImporter importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject expectedProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(SAMPLE_COLLECTION_2_0_PATH).getPath());
        importer = new PostmanImporter(new DummyTestCreator());
        WsdlProject actualProject = importer.importPostmanCollection(workspace,
                PostmanImporterTest.class.getResource(SAMPLE_COLLECTION_2_1_PATH).getPath());
        compareProjects(expectedProject, actualProject);
    }

    private void compareProjects(WsdlProject expectedProject, WsdlProject actualProject) {
        assertEquals(expectedProject.getPropertyCount(), actualProject.getPropertyCount(), "Different number of properties");

        assertEquals(expectedProject.getInterfaceCount(), actualProject.getInterfaceCount(), "Different number of interfaces");
        if (expectedProject.getInterfaceCount() == 1) {
            assertThat("Wrong interface type",
                    actualProject.getInterfaceAt(0), instanceOf(expectedProject.getInterfaceAt(0).getClass()));
        }
        assertEquals(expectedProject.getInterfaceAt(0).getOperationCount(),
                actualProject.getInterfaceAt(0).getOperationCount(), "Wrong number of operations");

        assertEquals(expectedProject.getTestSuiteCount(), actualProject.getTestSuiteCount(), "Wrong number of test suites");
        assertEquals(expectedProject.getTestSuiteAt(0).getTestCaseCount(),
                actualProject.getTestSuiteAt(0).getTestCaseCount(), "Wrong number of test cases");

        WsdlTestCase expectedTestCase = expectedProject.getTestSuiteAt(0).getTestCaseAt(0);
        WsdlTestCase actualTestCase = actualProject.getTestSuiteAt(0).getTestCaseAt(0);
        assertEquals(expectedTestCase.getTestStepCount(), actualTestCase.getTestStepCount(), "Wrong number of test steps");
        for (TestStep testStep : expectedTestCase.getTestStepList()) {
            RestTestRequestStep expectedTestStep = (RestTestRequestStep) testStep;
            RestTestRequest expectedRequest = expectedTestStep.getTestRequest();
            RestTestRequestStep actualTestStep = getRestTestStepForRequest(actualTestCase,
                    expectedTestStep.getRestMethod().getRequestById(expectedRequest.getId()).getName());
            assertNotNull(actualTestStep, "No test step with name " + expectedTestStep.getName());
            assertTrue(actualTestStep.getAssertionCount() >= expectedTestStep.getAssertionCount(),
                    "Number of assertions is less than expected for step " + expectedTestStep.getName());
            RestTestRequest actualRequest = actualTestStep.getTestRequest();
            assertTrue(actualRequest.getParams().size() >= expectedRequest.getParams().size(),
                    "Number of paramters is less than expected for step " + expectedTestStep.getName());
            assertEquals(expectedRequest.getRequestContent(), actualRequest.getRequestContent(),
                    "Payloads don't match for step " + expectedTestStep.getName());
        }
    }

    private RestTestRequestStep getRestTestStepForRequest(WsdlTestCase testCase, String requestName) {
        for (TestStep testStep : testCase.getTestStepList()) {
            RestTestRequestStep restTestStep = (RestTestRequestStep) testStep;
            RestRequest restRequest = restTestStep.getRestMethod().getRequestById(restTestStep.getTestRequest().getId());
            if (restRequest.getName().equals(requestName)) {
                return restTestStep;
            }
        }
        return null;
    }

    @AfterEach
    public void tearDown() {
        if (workspaceFile.exists()) {
            workspaceFile.delete();
        }
    }
}
