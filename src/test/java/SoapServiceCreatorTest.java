import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.support.SoapUIException;
import com.smartbear.postman.collection.PostmanCollection;
import com.smartbear.postman.collection.PostmanCollectionFactory;
import com.smartbear.postman.utils.PostmanJsonUtil;
import com.smartbear.postman.utils.SoapServiceCreator;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SoapServiceCreatorTest {
    WsdlProject project;
    SoapServiceCreator creator;

    @Before
    public void setUp() throws XmlException, IOException, SoapUIException {
        project = new WsdlProjectFactory().createNew();
        creator = new SoapServiceCreator(project);
    }

    @Test
    public void testAddingInterfaceWithOneRequestSoap12() throws IOException {
        // given
        URL collectionUrl = SoapServiceCreator.class.getResource("/soap/continents/continents_collection_12_v20.json");
        PostmanCollection.Request request = getRequests(collectionUrl).get(0);

        String continentsBody = IOUtils.toString(SoapServiceCreator.class.getResource("/soap/continents/continents_body_12.xml"), StandardCharsets.UTF_8);

        // when
        creator.addSoapRequest(request);

        TestDataHolder testDataHolder = new TestDataHolder(SoapVersion.Soap12);

        // then
        checkCreatedRequest(testDataHolder, SoapVersion.Soap12, continentsBody);
    }

    @Test
    public void testAddingInterfaceWithOneRequestSoap11() throws IOException {
        // given
        URL collectionUrl = SoapServiceCreator.class.getResource("/soap/continents/continents_collection_11_v20.json");

        PostmanCollection.Request request = getRequests(collectionUrl).get(0);

        String continentsBody = IOUtils.toString(SoapServiceCreator.class.getResource("/soap/continents/continents_body_11.xml"), StandardCharsets.UTF_8);

        // when
        creator.addSoapRequest(request);

        TestDataHolder testDataHolder = new TestDataHolder(SoapVersion.Soap11);

        // then
        checkCreatedRequest(testDataHolder, SoapVersion.Soap11, continentsBody);
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
        assertEquals(body, request.getRequestContent());
        assertEquals("Imported from Postman collection, original directory: [Continents collection/Continents]", request.getDescription());
    }
    @Test
    public void testMultipleInterfaces() throws IOException {
        // given
        URL collectionUrl = SoapServiceCreator.class.getResource("/soap/public_soap_apis/soap_apis_collection.json");

        List<PostmanCollection.Request> requests = getRequests(collectionUrl);

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

    private List<WsdlInterface> getCreatedInterfaces() {
        return project.getInterfaceList().stream().map(WsdlInterface.class::cast).collect(Collectors.toList());
    }

    private List<PostmanCollection.Request> getRequests(URL collectionUrl) throws IOException {
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
                            when(request.getUrl()).thenReturn(SoapServiceCreator.class.getResource(url).toString());
                            return request;
                        }).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
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
