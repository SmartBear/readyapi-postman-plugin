package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.graphql.GraphQLOperationGroup;
import com.eviware.soapui.impl.graphql.GraphQLService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.smartbear.ready.plugin.postman.collection.PostmanCollection;
import com.smartbear.ready.plugin.postman.collection.PostmanCollectionFactory;
import com.smartbear.ready.plugin.postman.collection.Request;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class GraphQLImporterUtilsTest {
    private static final String GRAPHQL_COLLECTION_WITH_ONE_INTERFACE = "/graphql/GraphQL_Collection.postman_collection_v2.0";
    private static final String GRAPHQL_COLLECTION_WITH_TWO_INTERFACES = "/graphql/GraphQL_Collection_2_interfaces.postman_collection_v2.1";
    private final List<String> customerMutationNames = Arrays.asList("addCustomer", "editCustomer");
    private final List<String> customerQueryNames = Arrays.asList("customer", "customers");
    private final List<String> countriesQueryNames = Arrays.asList("country PL", "country UA");
    private WsdlProject project;
    private GraphQLImporterUtils graphQLImporter;

    @Before
    public void setUp() throws Exception {
        project = new WsdlProjectFactory().createNew();
        graphQLImporter = new GraphQLImporterUtils();
    }

    @Test
    public void importCollectionWithOneInterface() throws Exception {
        importPostmanCollectionIntoProject(GRAPHQL_COLLECTION_WITH_ONE_INTERFACE);
        GraphQLService service = (GraphQLService) project.getInterfaceList().get(0);

        assertNotNull("Service is created", service);
        assertEquals("Project contains one interface",1, project.getInterfaceCount());
        serviceContainsTwoOperationGroups(service);
        assertEquals("Service contains 2 mutations", 2, service.getMutationsGroup().getOperationCount());
        assertEquals("Service contains 2 queries", 2, service.getQueriesGroup().getOperationCount());

        validateCustomersServiceOperationNames(service);
    }

    private void serviceContainsTwoOperationGroups(GraphQLService service) {
        assertEquals("Service contains 2 operation groups", 2, service.getOperationCount());
    }

    private void validateOperationNames(List<String> operationNames, GraphQLOperationGroup operationGroup) {
        operationNames.forEach(operationName ->
                assertTrue(String.format("Operation name %s matches actual operation name", operationName),
                        operationGroup.getOperationList().stream().anyMatch(it -> it.getName().equals(operationName)))
        );
    }

    @Test
    public void importCollectionWithTwoInterfaces() throws Exception {
        importPostmanCollectionIntoProject(GRAPHQL_COLLECTION_WITH_TWO_INTERFACES);
        List<GraphQLService> services = project.getInterfaceList()
                .stream()
                .map(GraphQLService.class::cast)
                .collect(Collectors.toList());

        assertEquals("Project contains 2 interfaces", 2, project.getInterfaceCount());

        assertEquals("First service contains 2 mutations", 2, services.get(0).getMutationsGroup().getOperationCount());
        assertEquals("First service contains 2 queries", 2, services.get(0).getQueriesGroup().getOperationCount());
        serviceContainsTwoOperationGroups(services.get(0));

        assertEquals("Second service contains 2 queries", 2, services.get(1).getQueriesGroup().getOperationCount());
        assertEquals("Second service has no mutations", 0, services.get(1).getMutationsGroup().getOperationCount());
        serviceContainsTwoOperationGroups(services.get(1));

        validateCustomersServiceOperationNames(services.get(0));
        validateOperationNames(countriesQueryNames, services.get(1).getQueriesGroup());
    }

    private void validateCustomersServiceOperationNames(GraphQLService service) {
        validateOperationNames(customerMutationNames, service.getMutationsGroup());
        validateOperationNames(customerQueryNames, service.getQueriesGroup());
    }

    private void importPostmanCollectionIntoProject(String interfacePath) throws Exception {
        PostmanCollection collectionWithOneInterface = getCollectionFromFile(
                getClass().getResource(interfacePath).getPath()
        );
        List<Request> graphQLRequests = getCollectionRequests(collectionWithOneInterface);

        graphQLRequests.forEach(request -> graphQLImporter.addGraphQLRequest(project, request));
    }

    private PostmanCollection getCollectionFromFile(String collectionFilePath) throws Exception {
        String postmanJson = FileUtils.readFileToString(new File(collectionFilePath), StandardCharsets.UTF_8);
        JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
        return PostmanCollectionFactory.getCollection((JSONObject) json);
    }

    private List<Request> getCollectionRequests(PostmanCollection collection) {
        return collection.getRequests().stream()
                .map(request -> {
                    String url = request.getUrl();
                    request = spy(request);
                    when(request.getUrl()).thenReturn(getClass().getResource(url).getPath());
                    return request;
                })
                .collect(Collectors.toList());
    }
}
