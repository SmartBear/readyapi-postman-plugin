package com.smartbear.postman.utils;

import com.eviware.soapui.support.JsonUtil;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.groovy.JsonSlurper;
import org.apache.commons.lang3.StringUtils;

public class PostmanJsonUtil {
    static final String WHILE_1 = "while(1);";
    static final String CLOSING_BRACKETS_WITH_COMMA = ")]}',";
    static final String CLOSING_BRACKETS = ")]}'";
    static final String EMPTY_FOR = "for(;;);";
    static final String D_PREFIXED = "{\"d\":";

    private static final String[] VULNERABILITY_TOKENS = {WHILE_1, CLOSING_BRACKETS_WITH_COMMA, CLOSING_BRACKETS, EMPTY_FOR};

    public static boolean seemsToBeJson(String content) {
        if (StringUtils.isBlank(content)) {
            return false;
        }
        try {
            new JsonSlurper().parseText(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static JSONArray getJsonArraySafely(JSONObject node, String arrayNodeName) {
        Object jsonObject = node.get(arrayNodeName);
        if (jsonObject instanceof JSONArray) {
            return (JSONArray) jsonObject;
        } else {
            return new JSONArray();
        }
    }

    public JSON parseTrimmedText(String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = removeVulnerabilityTokens(text).trim();
        JsonConfig config = new JsonConfig();
        config.setIgnoreDefaultExcludes(true);
        return JSONSerializer.toJSON(trimmedText, config);
    }

    String removeVulnerabilityTokens(String inputJsonString) {
        if (inputJsonString == null) {
            return null;
        }
        String outputString = inputJsonString.trim();
        for (String vulnerabilityToken : VULNERABILITY_TOKENS) {
            if (outputString.startsWith(vulnerabilityToken)) {
                outputString = outputString.substring(vulnerabilityToken.length()).trim();
            }
        }

        if (outputString.startsWith(D_PREFIXED) && outputString.endsWith("}")) {
            outputString = outputString.substring(D_PREFIXED.length(), outputString.length() - 1).trim();
        }
        return outputString;
    }
}
