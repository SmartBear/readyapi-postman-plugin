package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.config.GraphQLOperationGroupEnumConfig;
import com.eviware.soapui.impl.graphql.GraphQLRequest;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GraphQLMutationTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GraphQLQueryTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;

public class DummyTestCreator implements TestCreator {
    private WsdlTestCase testCase;
    @Override
    public void createTest(RestRequest request, String testCaseName) {
        testCase = createTestCase(request.getProject(), testCaseName);
        String stepName = request.getRestMethod().getName() + " - " + request.getName();
        testCase.addTestStep(RestRequestStepFactory.createConfig(request, stepName));
    }

    @Override
    public void createTest(WsdlRequest request, String testCaseName) {
        WsdlTestCase testCase = createTestHierarchyForRequest(request.getProject());
        String stepName = request.getOperation().getName() + " - " + request.getName();
        testCase.addTestStep(WsdlTestRequestStepFactory.createConfig(request, stepName));
    }

    @Override
    public void createTest(GraphQLRequest request, String testCaseName) {
        testCase = createTestCase(request.getProject(), testCaseName);
        String stepName = request.getType() + " - " + request.getName();
        if (GraphQLOperationGroupEnumConfig.MUTATION.toString().equals(request.getType())) {
            GraphQLMutationTestStepFactory mutationTestStepFactory = new GraphQLMutationTestStepFactory();
            testCase.addTestStep(mutationTestStepFactory.createConfig(request, stepName));
        } else if (GraphQLOperationGroupEnumConfig.QUERY.toString().equals(request.getType())) {
            GraphQLQueryTestStepFactory queryTestStepFactory = new GraphQLQueryTestStepFactory();
            testCase.addTestStep(queryTestStepFactory.createConfig(request, stepName));
        }
    }

    @Override
    public WsdlTestCase createTestCase(WsdlProject project, String testCaseName) {
        if (testCase == null) {
            testCase = createTestHierarchyForRequest(project);
        }
        return testCase;
    }

    private WsdlTestCase createTestHierarchyForRequest(WsdlProject project) {
        WsdlTestSuite testSuite = project.addNewTestSuite("TestSuite 1");
        return testSuite.addNewTestCase("TestCase 1");
    }
}
