package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToTestCaseAction;

public class GuiTestCreator implements TestCreator {
    @Override
    public void createTest(RestRequest request) {
        AddRestRequestToTestCaseAction addRestRequestToTestCaseAction = new AddRestRequestToTestCaseAction();
        addRestRequestToTestCaseAction.perform(request, null);
    }

    @Override
    public void createTest(WsdlRequest request) {
        AddRequestToTestCaseAction addRequestToTestCaseAction = new AddRequestToTestCaseAction();
        addRequestToTestCaseAction.perform(request, null);
    }
}
