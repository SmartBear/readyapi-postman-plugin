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
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.StringUtils;
import groovy.json.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AddEqualsAssertionCommand implements AddAssertionCommand {
    private static final Logger logger = LoggerFactory.getLogger(AddEqualsAssertionCommand.class);
    private final Assertable assertable;
    private String value;

    public AddEqualsAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public String getName() {
        return null;
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
        Class clazz;
        try{
            clazz = Class.forName("com.eviware.soapui.impl.wsdl.teststeps.assertions.EqualsAssertion");
            Field labelField = clazz.getField("LABEL");
            TestAssertion assertion = assertable.addAssertion((String) labelField.get(null));
            if (clazz.isInstance(assertion)) {
                Method method = clazz.getMethod("setPatternText", String.class);
                method.invoke(assertion, StringUtils.unquote(StringEscapeUtils.unescapeJava(value)));
            }
        }
        catch (Throwable e){
            logger.warn("Creating EqualsAssertion is only supported in ReadyAPI", e);
        }
        return null;
    }
}
