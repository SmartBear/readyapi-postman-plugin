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

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.StringUtils;

public abstract class AddScriptAssertionCommand implements AddAssertionCommand {
    private static final String ASSERT_COMMAND = "assert ";
    private final Assertable assertable;
    private String script;

    public AddScriptAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public void prepare() {
        script = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        script = argument;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(script);
    }

    @Override
    public Object execute() {
        GroovyScriptAssertion assertion = (GroovyScriptAssertion) assertable.addAssertion(GroovyScriptAssertion.LABEL);
        assertion.setScriptText(ASSERT_COMMAND + script);
        return null;
    }
}
