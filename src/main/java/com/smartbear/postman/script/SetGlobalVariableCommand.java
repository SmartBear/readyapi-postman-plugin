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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;

public class SetGlobalVariableCommand implements ScriptCommand {
    private static final String NAME = "setGlobalVariable";

    private final WsdlProject project;
    private int argumentCount;
    private String variableName;
    private String variableValue;

    public SetGlobalVariableCommand(WsdlProject project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void prepare() {
        variableName = null;
        variableValue = null;
        argumentCount = 0;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        switch (argumentCount) {
            case 0:
                variableName = argument;
                argumentCount++;
                break;
            case 1:
                variableValue = argument;
                argumentCount++;
        }
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(variableName) && StringUtils.hasContent(variableValue);
    }

    @Override
    public Object execute() {
        String propertyName = StringUtils.unquote(variableName.trim());
        if (!project.hasProperty(propertyName)) {
            TestProperty property = project.addProperty(propertyName);
            property.setValue(StringUtils.unquote(variableValue));
        }

        return null;
    }
}
