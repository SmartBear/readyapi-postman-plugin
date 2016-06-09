package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;

public class GuiTestCreator implements TestCreator {
    @Override
    public void createTest(RestRequest request) {
        AddRestRequestToTestCaseAction addRestRequestToTestCaseAction = new AddRestRequestToTestCaseAction();
        addRestRequestToTestCaseAction.perform(request, null);
    }
}
