/**
 *  Copyright 2016 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseSilentAction;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToTestCaseSilentAction;

import java.util.HashMap;
import java.util.Map;

import static com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction.TEST_CASE_NAME;

public class GuiTestCreator implements TestCreator {
    @Override
    public void createTest(RestRequest request, String testCaseName) {
        Map<String, String> param = new HashMap<>();
        param.put(TEST_CASE_NAME, testCaseName);
        AddRestRequestToTestCaseSilentAction addRestRequestToTestCaseSilentAction = new AddRestRequestToTestCaseSilentAction();
        addRestRequestToTestCaseSilentAction.perform(request, param);
    }

    @Override
    public void createTest(WsdlRequest request, String testCaseName) {
        Map<String, String> param = new HashMap<>();
        param.put(TEST_CASE_NAME, testCaseName);
        AddRequestToTestCaseSilentAction addRequestToTestCaseSilentAction = new AddRequestToTestCaseSilentAction();
        addRequestToTestCaseSilentAction.perform(request, param);
    }
}
