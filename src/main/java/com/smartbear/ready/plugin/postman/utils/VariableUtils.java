/**
 * Copyright 2016 SmartBear Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableUtils {
    private static final String READYAPI_VARIABLE_BEGIN = "${#Project#";
    private static final String READYAPI_VARIABLE_END = "}";
    private static final String VARIABLE_NAME_REG_GROUP = "name";
    private static final Pattern VARIABLE_REG = Pattern.compile("\\{\\{(?<" + VARIABLE_NAME_REG_GROUP + ">.*?)\\}\\}");
    private static final String ESCAPING_PREFIX = "\\";
    private static final String VAULT_PREFIX = "vault:";
    private static final String DYNAMIC_VARIABLE_PREFIX = "$";
    private static final String DYNAMIC_VARIABLE_NAME_PREFIX = "dynamic-variable-";
    private static boolean isDynamicVariablePresent;

    private VariableUtils() {}

    public static String convertVariables(String postmanString, WsdlProject projectToAddProperties) {
        if (StringUtils.isNullOrEmpty(postmanString)) {
            return postmanString;
        }

        StringBuilder readyApiStringBuilder = new StringBuilder();
        Matcher matcher = VARIABLE_REG.matcher(postmanString);
        while (matcher.find()) {
            String propertyName = matcher.group(VARIABLE_NAME_REG_GROUP);
            propertyName = removeVaultPrefixIfPresent(propertyName);
            if (propertyName.startsWith(DYNAMIC_VARIABLE_PREFIX)) {
                propertyName = DYNAMIC_VARIABLE_NAME_PREFIX + propertyName.substring(1);
                isDynamicVariablePresent = true;
            }
            if (projectToAddProperties != null && !projectToAddProperties.hasProperty(propertyName)) {
                projectToAddProperties.addProperty(propertyName);
            }
            matcher.appendReplacement(readyApiStringBuilder,
                    ESCAPING_PREFIX + READYAPI_VARIABLE_BEGIN + propertyName + READYAPI_VARIABLE_END);
        }
        if (!readyApiStringBuilder.isEmpty()) {
            matcher.appendTail(readyApiStringBuilder);
            return readyApiStringBuilder.toString();
        } else {
            return postmanString;
        }
    }

    public static String createProjectVariableExpansionString(String variableName) {
        variableName = removeVaultPrefixIfPresent(variableName);
        return READYAPI_VARIABLE_BEGIN + variableName + READYAPI_VARIABLE_END;
    }

    public static void showDynamicVariablesInfoIfPresent() {
        if (isDynamicVariablePresent) {
            isDynamicVariablePresent = false;
            UISupport.showInfoMessage("Dynamic variables were converted to ReadyAPI property expansions. " +
                    "Their values can be set in custom project properties.");
        }
    }

    private static String removeVaultPrefixIfPresent(String propertyName) {
        if (propertyName.startsWith(VAULT_PREFIX)) {
            propertyName = propertyName.substring(VAULT_PREFIX.length());
        }
        return propertyName;
    }
}
