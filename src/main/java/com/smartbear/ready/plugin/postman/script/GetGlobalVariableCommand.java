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

import com.eviware.soapui.support.StringUtils;
import com.smartbear.ready.plugin.postman.utils.VariableUtils;

public class GetGlobalVariableCommand implements ScriptCommand {
    private static final String NAME = "[";
    private String variableName;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void prepare() {
        variableName = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        variableName = argument;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(variableName);
    }

    @Override
    public Object execute() {
        return VariableUtils.createProjectVariableExpansionString(StringUtils.unquote(variableName));
    }
}
