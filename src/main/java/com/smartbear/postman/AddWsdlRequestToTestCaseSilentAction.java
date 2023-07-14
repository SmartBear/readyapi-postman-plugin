package com.smartbear.postman;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToTestCaseAction;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.testsuite.TestCase;

import java.util.HashMap;

public class AddWsdlRequestToTestCaseSilentAction extends AddRequestToTestCaseAction {
    @Override
    public void perform(WsdlRequest request, Object param) {
        WsdlProject project = request.getOperation().getInterface().getProject();
        WsdlTestCase testCase = null;
        if (param instanceof HashMap && ((HashMap<?, ?>) param).get(TEST_CASE_NAME) != null) {
            String testCaseName = ((HashMap<?, ?>) param).get(TEST_CASE_NAME).toString();
            testCase = addNewTestSuiteAndTestCaseSilent(project, null, testCaseName);
        }
        if (testCase != null) {
            addRequest(testCase, request, -1);
        }
    }

    @Override
    public boolean addRequest(TestCase testCase, WsdlRequest request, int position) {
        String name = request.getName();

        WsdlTestRequestStep testStep = (WsdlTestRequestStep) testCase.insertTestStep(
                WsdlTestRequestStepFactory.createConfig(request, name), position);
        return testStep != null;
    }
}
