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

package com.smartbear.ready.plugin.postman.script;

import com.eviware.soapui.model.testsuite.Assertable;

public class AddHeaderExistsAssertionCommand extends AddScriptAssertionCommand {
    private static final String NAME = "getResponseHeader";
    private static final String HEADERS_SCRIPT_BEGIN = "messageExchange.responseHeaders.hasValues(";
    private static final String HEADERS_SCRIPT_END = ")";

    public AddHeaderExistsAssertionCommand(Assertable assertable) {
        super(assertable);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        StringBuffer script = new StringBuffer();
        script.append(HEADERS_SCRIPT_BEGIN);
        script.append(argument);
        script.append(HEADERS_SCRIPT_END);
        super.addArgument(tokenType, script.toString());
    }
}
