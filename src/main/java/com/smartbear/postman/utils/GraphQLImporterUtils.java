package com.smartbear.postman.utils;

import com.eviware.soapui.config.GraphQLOperationGroupEnumConfig;
import com.eviware.soapui.impl.graphql.GraphQLOperation;
import com.eviware.soapui.impl.graphql.GraphQLOperationGroup;
import com.eviware.soapui.impl.graphql.GraphQLRequest;
import com.eviware.soapui.impl.graphql.GraphQLService;
import com.eviware.soapui.impl.graphql.GraphQLServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.postman.VariableUtils;
import com.smartbear.postman.collection.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GraphQLImporterUtils {

    private static final String GRAPHQL_MODE = "graphql";
    private static final Logger logger = LoggerFactory.getLogger(GraphQLImporterUtils.class);

    public static boolean isGraphQlRequest(Request request) {
        String mode = request.getMode();
        return mode != null && mode.equals(GRAPHQL_MODE);
    }

    public GraphQLRequest addGraphQLRequest(WsdlProject project, String uri, Request request, String rawModeData) {
        GraphQLRequest graphQLRequest = createGraphQLRequest(project, uri, request);

        if (graphQLRequest != null) {
            if (StringUtils.hasContent(request.getName())) {
                graphQLRequest.setName(request.getName());
            }
            if (StringUtils.hasContent(rawModeData)) {
                graphQLRequest.setRequestContent(rawModeData);
            }
            graphQLRequest.setEndpoint(VariableUtils.convertVariables(uri, project));
            graphQLRequest.setQuery(request.getGraphQlQuery());
            graphQLRequest.setVariables(request.getGraphQlVariables());
        }
        return graphQLRequest;
    }

    private GraphQLRequest createGraphQLRequest(WsdlProject project, String url, Request request) {
        String graphQLRequestString = request.getGraphQlQuery();
        if (graphQLRequestString == null) {
            return null;
        }
        GraphQLRequest graphQLRequest = null;
        ArrayList<GraphQLService> interfaces = getServices(project, url);

        for (GraphQLService service : interfaces) {
            GraphQLOperationGroup operationGroup = getGraphQLOperationGroup(graphQLRequestString, service);
            if (operationGroup != null) {
                GraphQLOperation operation = operationGroup.getOperationByName(request.getName());
                if (operation == null) {
                    operation = operationGroup.addNewOperation(request.getName());
                }
                graphQLRequest = operation.addNewRequest(request.getName());
                break;
            }
        }

        return graphQLRequest;
    }

    private List<GraphQLService> findExistingGraphQLInterfacesForURI(WsdlProject project, String url) {
        return project.getInterfaceList().stream()
                .filter(GraphQLService.class::isInstance)
                .map(GraphQLService.class::cast)
                .filter(service -> service.getDefinition().equals(url))
                .collect(java.util.stream.Collectors.toList());
    }

    private ArrayList<GraphQLService> getServices(WsdlProject project, String url) {
        ArrayList<GraphQLService> services = new ArrayList<>();
        List<GraphQLService> existingGraphQLlServices = findExistingGraphQLInterfacesForURI(project, url);
        if (!existingGraphQLlServices.isEmpty()) {
            services.addAll(existingGraphQLlServices);
        } else {
            try {
                GraphQLService graphQLService = (GraphQLService) project.addNewInterface(ModelItemNamer.createName(
                        "GraphQL Service", project.getInterfaceList()), GraphQLServiceFactory.GRAPHQL_TYPE);
                graphQLService.setDefinitionUrl(url);
                graphQLService.addEndpoint(url);
                services.add(graphQLService);
            } catch (Exception e) {
                logger.error("Error while creating a GraphQL service", e);
                return new ArrayList<>();
            }
        }
        return services;
    }

    private GraphQLOperationGroup getGraphQLOperationGroup(String graphQLRequestString, GraphQLService service) {
        GraphQLOperationGroup operationGroup;
        if (graphQLRequestString.startsWith(GraphQLOperationGroupEnumConfig.MUTATION.toString())) {
            operationGroup = service.getMutationsGroup();
            if (operationGroup == null) {
                operationGroup = service.addNewOperationGroup(GraphQLOperationGroupEnumConfig.MUTATION.toString());
            }
        } else {
            operationGroup = service.getQueriesGroup();
            if (operationGroup == null) {
                operationGroup = service.addNewOperationGroup(GraphQLOperationGroupEnumConfig.QUERY.toString());
            }
        }
        return operationGroup;
    }

}
