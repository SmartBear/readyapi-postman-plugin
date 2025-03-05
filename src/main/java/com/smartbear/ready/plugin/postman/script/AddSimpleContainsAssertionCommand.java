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

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.StringUtils;
import groovy.json.StringEscapeUtils;

public class AddSimpleContainsAssertionCommand implements AddAssertionCommand {
    private static final String NAME = "has";
    private final Assertable assertable;
    private String value;

    public AddSimpleContainsAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void prepare() {
        value = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        value = argument;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(value);
    }

    @Override
    public Object execute() {
        SimpleContainsAssertion assertion = (SimpleContainsAssertion) assertable.addAssertion(SimpleContainsAssertion.LABEL);
        assertion.setToken(StringUtils.unquote(StringEscapeUtils.unescapeJava(value)));
        return null;
    }
}
