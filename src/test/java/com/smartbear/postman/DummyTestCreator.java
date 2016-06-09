package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;

public class DummyTestCreator implements TestCreator {
    @Override
    public void createTest(RestRequest request) {
        WsdlProject project = request.getProject();
        WsdlTestSuite testSuite = project.addNewTestSuite("TestSuite 1");
        WsdlTestCase testCase = testSuite.addNewTestCase("TestCase 1");
        String stepName = request.getRestMethod().getName() + " - " + request.getName();
        testCase.addTestStep(RestRequestStepFactory.createConfig(request, stepName));
    }
}
