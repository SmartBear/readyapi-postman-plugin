package com.smartbear.ready.plugin.postman.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONTokener;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
            Object parsed = parseJson(content);
            return parsed instanceof JSONObject || parsed instanceof JSONArray;
        } catch (Exception e) {
            return false;
        }
    }

    public static JSONArray getJsonArraySafely(JSONObject node, String arrayNodeName) {
        Object jsonObject = node.opt(arrayNodeName);
        if (jsonObject instanceof JSONArray) {
            return (JSONArray) jsonObject;
        } else {
            return new JSONArray();
        }
    }

    public Object parseTrimmedText(String text) {
        if (text == null) {
            return null;
        }
        String trimmedText = removeVulnerabilityTokens(text).trim();
        return parseJson(trimmedText);
    }

    private static Object parseJson(String text) {
        try {
            return new JSONTokener(text).nextValue();
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    public static List<Object> toList(JSONArray array) {
        List<Object> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.opt(i));
            }
        }
        return list;
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
