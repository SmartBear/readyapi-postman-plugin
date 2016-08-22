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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableUtils {
    private static final String READYAPI_VARIABLE_BEGIN = "${#Project#";
    private static final String READYAPI_VARIABLE_END = "}";

    public static String convertVariables(String postmanString) {
        if (StringUtils.isNullOrEmpty(postmanString)) {
            return postmanString;
        }

        final String POSTMAN_VARIABLE_BEGIN = "{{";
        final String POSTMAN_VARIABLE_END = "}}";
        final String ESCAPING_PREFIX = "\\";

        final Pattern variableRegExp = Pattern.compile("\\{\\{.+\\}\\}");

        StringBuffer readyApiStringBuffer = new StringBuffer();
        Matcher matcher = variableRegExp.matcher(postmanString);
        while (matcher.find()) {
            String postmanVariable = matcher.group();
            String readyApiVariable = postmanVariable
                    .replace(POSTMAN_VARIABLE_BEGIN, ESCAPING_PREFIX + READYAPI_VARIABLE_BEGIN)
                    .replace(POSTMAN_VARIABLE_END, READYAPI_VARIABLE_END);
            matcher.appendReplacement(readyApiStringBuffer, readyApiVariable);
        }
        if (readyApiStringBuffer.length() > 0) {
            matcher.appendTail(readyApiStringBuffer);
            return readyApiStringBuffer.toString();
        } else {
            return postmanString;
        }
    }

    public static String createProjectVariableExpansionString(String variableName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(READYAPI_VARIABLE_BEGIN).append(variableName).append(READYAPI_VARIABLE_END);
        return buffer.toString();
    }
}
