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

package com.smartbear.postman.script;

import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.soapui.support.StringUtils;

public class AddHttpCodeAssertionCommand implements AddAssertionCommand {
    private static final String NAME = "code";
    private static final String NOT_EQUAL = "!=";

    private String condition;
    private String value;
    private final Assertable assertable;

    public AddHttpCodeAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public void addCondition(String condition) {
        this.condition = condition;
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
        if (StringUtils.hasContent(condition)) {
            if (condition.startsWith(NOT_EQUAL)) {
                return addInvalidStatusAssertion();
            }
        }
        return addValidStatusAssertion();
    }

    private Object addValidStatusAssertion() {
        ValidHttpStatusCodesAssertion assertion = getValidHttpStatusCodesAssertion();
        String codes = assertion.getCodes();
        if (StringUtils.hasContent(codes)) {
            assertion.setCodes(codes + "," + value);
        } else {
            assertion.setCodes(value);
        }
        return null;
    }

    private ValidHttpStatusCodesAssertion getValidHttpStatusCodesAssertion() {
        for (TestAssertion assertion : assertable.getAssertionList()) {
            if (assertion instanceof ValidHttpStatusCodesAssertion) {
                return (ValidHttpStatusCodesAssertion) assertion;
            }
        }
        return (ValidHttpStatusCodesAssertion) assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL);
    }

    private Object addInvalidStatusAssertion() {
        InvalidHttpStatusCodesAssertion assertion = getInvalidHttpStatusCodesAssertion();
        String codes = assertion.getCodes();
        if (StringUtils.hasContent(codes)) {
            assertion.setCodes(codes + "," + value);
        } else {
            assertion.setCodes(value);
        }
        return null;
    }

    private InvalidHttpStatusCodesAssertion getInvalidHttpStatusCodesAssertion() {
        for (TestAssertion assertion : assertable.getAssertionList()) {
            if (assertion instanceof InvalidHttpStatusCodesAssertion) {
                return (InvalidHttpStatusCodesAssertion) assertion;
            }
        }
        return (InvalidHttpStatusCodesAssertion) assertable.addAssertion(InvalidHttpStatusCodesAssertion.LABEL);
    }


}
