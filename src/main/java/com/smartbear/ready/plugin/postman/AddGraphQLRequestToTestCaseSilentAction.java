package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.config.GraphQLOperationGroupEnumConfig;
import com.eviware.soapui.impl.graphql.GraphQLRequest;
import com.eviware.soapui.impl.graphql.actions.AddGraphQLRequestToTestCaseAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLMutationTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLQueryTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequestTestStepWithSchema;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GraphQLMutationTestStepFactory;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GraphQLQueryTestStepFactory;

import java.util.HashMap;

public class AddGraphQLRequestToTestCaseSilentAction extends AddGraphQLRequestToTestCaseAction {

    @Override
    public void perform(GraphQLRequest request, Object param) {
        WsdlProject project = request.getOperation().getInterface().getProject();
        WsdlTestCase testCase = null;
        if (param instanceof HashMap && ((HashMap<?, ?>) param).get(TEST_CASE_NAME) != null) {
            testCase = addNewTestSuiteAndTestCaseSilent(project, null, ((HashMap<?, ?>) param).get(TEST_CASE_NAME).toString());
        }

        if (testCase != null) {
            addRequest(testCase, request, -1);
        }
    }

    @Override
    public boolean addRequest(TestCase testCase, GraphQLRequest request, int position) {
        String name = request.getType() + " - " + request.getName();
        GraphQLTestRequestTestStepWithSchema testStep = null;

        if (GraphQLOperationGroupEnumConfig.QUERY.toString().equals(request.getType())) {
            GraphQLQueryTestStepFactory factory = new GraphQLQueryTestStepFactory();
            testStep = (GraphQLQueryTestStep) testCase.insertTestStep(factory.createConfig(request, name), position);
        } else if (GraphQLOperationGroupEnumConfig.MUTATION.toString().equals(request.getType())) {
            GraphQLMutationTestStepFactory factory = new GraphQLMutationTestStepFactory();
            testStep = (GraphQLMutationTestStep) testCase.insertTestStep(factory.createConfig(request, name), position);
        }

        return testStep != null;
    }
}