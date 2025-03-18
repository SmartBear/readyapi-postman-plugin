package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.support.SoapUIException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.smartbear.ready.plugin.postman.collection.PostmanCollection;
import com.smartbear.ready.plugin.postman.collection.PostmanCollectionFactory;
import com.smartbear.ready.plugin.postman.collection.Request;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SoapServiceCreatorTest {

    private static final WireMockServer WIREMOCK = new WireMockServer(wireMockConfig().port(28089));
    private WsdlProject project;
    private SoapServiceCreator creator;

    @BeforeAll
    public static void wireMockInit() throws URISyntaxException, IOException {
        initServiceMock("soap/public_soap_apis/number_conversion.asmx", "/webservicesserver/NumberConversion.asmx?WSDL");
        initServiceMock("soap/continents/continents.asmx", "/soap/continents.asmx?WSDL");
        initServiceMock("soap/public_soap_apis/calculator.asmx", "/calculator.asmx?WSDL");
        initServiceMock("soap/public_soap_apis/isbn.asmx", "/services/isbnservice.asmx?WSDL");
        initServiceMock("soap/public_soap_apis/continents.asmx", "/websamples.countryinfo/CountryInfoService.asxm?WSDL");
        initServiceMock("soap/public_soap_apis/tempconvert.asmx", "/xml/tempconvert.asmx?WSDL");

        WIREMOCK.start();
    }

    @AfterAll
    public static void shutdownWiremock() {
        WIREMOCK.stop();
    }


    @BeforeEach
    public void setUp() throws XmlException, IOException, SoapUIException {
        project = new WsdlProjectFactory().createNew();
        creator = new SoapServiceCreator(project);
    }

    @Test
    void testAddingInterfaceWithOneRequestSoap12() throws Exception {
        // given
        URL collectionUrl = SoapServiceCreatorTest.class.getResource("/soap/continents/continents_collection_12_v20.json");
        Request request = getRequests(collectionUrl).get(0);
        String continentsBody = IOUtils.toString(
                SoapServiceCreatorTest.class.getResource("/soap/continents/continents_body_12.xml"), StandardCharsets.UTF_8);

        // when
        creator.addSoapRequest(request);
        TestDataHolder testDataHolder = new TestDataHolder(SoapVersion.Soap12);

        // then
        checkCreatedRequest(testDataHolder, SoapVersion.Soap12, continentsBody);
    }

    @Test
    void testAddingInterfaceWithOneRequestSoap11() throws Exception {
        // given
        URL collectionUrl = SoapServiceCreatorTest.class.getResource("/soap/continents/continents_collection_11_v20.json");
        Request request = getRequests(collectionUrl).get(0);
        String continentsBody = IOUtils.toString(
                SoapServiceCreatorTest.class.getResource("/soap/continents/continents_body_11.xml"), StandardCharsets.UTF_8);

        // when
        creator.addSoapRequest(request);
        TestDataHolder testDataHolder = new TestDataHolder(SoapVersion.Soap11);

        // then
        checkCreatedRequest(testDataHolder, SoapVersion.Soap11, continentsBody);
    }

    @Test
    void testMultipleInterfaces() throws Exception {
        // given
        URL collectionUrl = SoapServiceCreatorTest.class.getResource("/soap/public_soap_apis/soap_apis_collection.json");
        List<Request> requests = getRequests(collectionUrl);

        // when
        requests.forEach(creator::addSoapRequest);
        List<WsdlInterface> interfaces = getCreatedInterfaces();
        int createdRequests = interfaces.stream()
                .flatMap(iface -> iface
                        .getAllOperations().stream()
                        .map(WsdlOperation::getRequestCount))
                .reduce(0, Integer::sum);

        // then
        assertEquals(requests.size(), createdRequests);
        assertEquals(10, interfaces.size());
        assertEquals(5, interfaces.stream().filter(iface -> iface.getWsdlContext().getSoapVersion().equals(SoapVersion.Soap11)).count());
        assertEquals(5, interfaces.stream().filter(iface -> iface.getWsdlContext().getSoapVersion().equals(SoapVersion.Soap12)).count());
    }

    private void checkCreatedRequest(TestDataHolder testDataHolder, SoapVersion soapVersion, String requestBody) {
        checkInterfaces(testDataHolder.interface11, testDataHolder.interface12);

        checkOperations(testDataHolder.operation11, testDataHolder.operation12, soapVersion);

        checkRequest(testDataHolder.request, requestBody);
    }

    private void checkInterfaces(WsdlInterface interface11, WsdlInterface interface12) {
        assertEquals(2, project.getInterfaceCount());
        assertEquals("CountryInfoServiceSoapBinding", interface11.getName());
        assertEquals("CountryInfoServiceSoapBinding12", interface12.getName());

        assertEquals(1, interface11.getOperationCount());
        assertEquals(1, interface12.getOperationCount());
    }

    private void checkOperations(WsdlOperation operation11, WsdlOperation operation12, SoapVersion soapVersion) {
        if (soapVersion.equals(SoapVersion.Soap11)) {
            assertEquals(1, operation11.getRequestCount());
            assertEquals(0, operation12.getRequestCount());
        } else {
            assertEquals(0, operation11.getRequestCount());
            assertEquals(1, operation12.getRequestCount());
        }

        assertEquals("ListOfContinentsByName", operation11.getName());
        assertEquals("ListOfContinentsByName", operation12.getName());
    }

    private void checkRequest(WsdlRequest request, String body) {
        String contentType = request.getRequestHeaders().get("Content-Type").get(0);

        assertEquals(RestRequestInterface.HttpMethod.POST, request.getMethod());
        assertEquals("List of Continents by Name", request.getName());
        assertEquals("text/xml; charset=utf-8", contentType);
        assertEquals(normalizeLineEndings(body), normalizeLineEndings(request.getRequestContent()));
        assertEquals("Imported from Postman collection, original directory: [Continents collection/Continents]", request.getDescription());
    }

    private String normalizeLineEndings(String input) {
        return input.replace("\r\n", "\n");
    }

    private List<WsdlInterface> getCreatedInterfaces() {
        return project.getInterfaceList().stream().map(WsdlInterface.class::cast).collect(Collectors.toList());
    }

    private List<Request> getRequests(URL collectionUrl) throws Exception {
        String postmanJson = IOUtils.toString(
                collectionUrl,
                StandardCharsets.UTF_8
        );
        if (PostmanJsonUtil.seemsToBeJson(postmanJson)) {
            JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
            if (json instanceof JSONObject) {
                PostmanCollection postmanCollection = PostmanCollectionFactory.getCollection((JSONObject) json);

                return postmanCollection.getRequests().stream()
                        .map(request -> {
                            String url = request.getUrl();
                            request = spy(request);
                            when(request.getUrl()).thenReturn(url);
                            return request;
                        }).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private static void initServiceMock(String resourceUrl, String stubUrl) throws URISyntaxException, IOException {
        URL resource = SoapServiceCreatorTest.class.getClassLoader().getResource(resourceUrl);
        Path wsdlPath = Paths.get(resource.toURI());
        String serviceBody = Files.readString(wsdlPath);
        WIREMOCK.stubFor(get(urlEqualTo(stubUrl))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type",
                                        "Multipart/Related; boundary=\"----=_Part_112_400566523.1602581633780\"; type=\"application/xop+xml\"; start-info=\"application/soap+xml\"")
                                .withBody(serviceBody)
                )
        );
    }

    private class TestDataHolder {
        public WsdlInterface interface11;
        public WsdlInterface interface12;
        public WsdlOperation operation11;
        public WsdlOperation operation12;
        public WsdlRequest request;

        public TestDataHolder(SoapVersion version) {
            List<WsdlInterface> interfaces = getCreatedInterfaces();

            interface11 = getInterface(interfaces, SoapVersion.Soap11);
            interface12 = getInterface(interfaces, SoapVersion.Soap12);

            operation11 = interface11.getOperationAt(0);
            operation12 = interface12.getOperationAt(0);

            if (version.equals(SoapVersion.Soap11)) {
                request = operation11.getRequestAt(0);
            } else {
                request = operation12.getRequestAt(0);
            }
        }

        private WsdlInterface getInterface(List<WsdlInterface> interfaces, SoapVersion soapVersion) {
            return interfaces.stream()
                    .filter(iface -> iface.getWsdlContext().getSoapVersion().equals(soapVersion))
                    .findFirst()
                    .orElse(null);
        }
    }
}
