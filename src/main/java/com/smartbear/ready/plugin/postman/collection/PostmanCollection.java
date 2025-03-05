package com.smartbear.ready.plugin.postman.collection;

import com.eviware.soapui.support.StringUtils;
import com.smartbear.ready.plugin.postman.ScriptType;
import com.smartbear.ready.plugin.postman.utils.PostmanJsonUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PostmanCollection {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String URL = "url";
    public static final String METHOD = "method";

    public static final String LISTEN = "listen";
    public static final String SCRIPT = "script";
    public static final String EXEC = "exec";
    public static final char SCRIPT_LINE_DELIMITER = ';';

    protected final JSONObject postmanCollection;

    public PostmanCollection(JSONObject postmanCollection) {
        this.postmanCollection = postmanCollection;
    }

    public abstract String getName();
    public abstract String getDescription();
    public abstract List<Request> getRequests();
    public abstract List<JSONObject> getFolders();
    public abstract List<Variable> getVariables();
    public abstract String getAuth();
    public abstract String getVersion();

    protected static String getEventScript(JSONObject request, ScriptType scriptType, String nodeName) {
        JSONArray events = PostmanJsonUtil.getJsonArraySafely(request, nodeName);
        List<Pattern> commentRegexPatterns = List.of(Pattern.compile("(.*?)(//.*)"), Pattern.compile("(.*?)(/\\*.*)"));
        Pattern onlyCommentPattern = Pattern.compile("^ *(//|/\\*).*");
        Pattern continuationInCurrentLinePattern = Pattern.compile("^ *[!-/:-@\\[-`{-~].*");
        Pattern continuationInNextLinePattern = Pattern.compile(".*[!-(*-/:-@\\[-`{-~] *$");

        for (Object eventObject : events) {
            if (eventObject instanceof JSONObject event) {
                String listen = getValue(event, LISTEN);
                if (!StringUtils.sameString(listen, scriptType.getListenType())) {
                    continue;
                }
                JSONObject script = event.getJSONObject(SCRIPT);
                if (script != null) {
                    StringBuilder scriptBuilder = new StringBuilder();
                    JSONArray scriptLines = PostmanJsonUtil.getJsonArraySafely(script, EXEC);
                    for (Object scriptLine : scriptLines) {
                        String line = scriptLine.toString();
                        removeSemicolonFromPreviousLineIfNeeded(
                            line,
                            scriptBuilder,
                            continuationInCurrentLinePattern,
                            onlyCommentPattern
                        );
                        appendNewLineAndComment(
                            line,
                            scriptBuilder,
                            commentRegexPatterns,
                            continuationInNextLinePattern
                        );
                    }
                    if (!scriptBuilder.isEmpty()) {
                        return scriptBuilder.toString();
                    }
                }
            }
        }
        return null;
    }

    private static void appendNewLineAndComment(
        String currentLine,
        StringBuilder scriptBuilder,
        List<Pattern> commentRegexPatterns,
        Pattern continuationInNextLinePattern) {

        String comment = "";

        for (Pattern commentRegex : commentRegexPatterns) {
            Matcher commentMatcher = commentRegex.matcher(currentLine);

            if (commentMatcher.find()) {
                currentLine = commentMatcher.group(1);
                comment = commentMatcher.group(2);
                break;
            }
        }

        scriptBuilder.append(currentLine);
        if (StringUtils.hasContent(currentLine)) {
            addSemicolonIfNeeded(currentLine, scriptBuilder, continuationInNextLinePattern);
        }
        scriptBuilder.append(comment);
        scriptBuilder.append('\n');
    }

    private static void removeSemicolonFromPreviousLineIfNeeded(
        String currentLine,
        StringBuilder scriptBuilder,
        Pattern continuationInCurrentLinePattern,
        Pattern onlyCommentPattern) {

        if (scriptBuilder.length() > 1 &&
                scriptBuilder.charAt(scriptBuilder.length() - 2) == SCRIPT_LINE_DELIMITER &&
                continuationInCurrentLinePattern.matcher(currentLine).find() &&
                !onlyCommentPattern.matcher(currentLine).find()) {
            scriptBuilder.deleteCharAt(scriptBuilder.length() - 2);
        }
    }

    private static void addSemicolonIfNeeded(
        String currentLine,
        StringBuilder scriptBuffer,
        Pattern continuationInNextLinePattern) {

        if (!scriptBuffer.isEmpty() && !currentLine.isEmpty() &&
                !continuationInNextLinePattern.matcher(currentLine).find()) {
            scriptBuffer.append(SCRIPT_LINE_DELIMITER);
        }
    }

    protected static String getValue(JSONObject jsonObject, String name) {
        return getValue(jsonObject, name, "");
    }

    protected static String getValue(JSONObject jsonObject, String field, String defaultValue) {
        final String NULL_STRING = "null";
        Object value = jsonObject.get(field);
        if (value != null) {
            String valueString = value.toString();
            if (!valueString.equals(NULL_STRING)) {
                return valueString;
            }
        }
        return defaultValue;
    }

    protected static String getValueFromObjectOrString(JSONObject jsonObject, String firstLevelField, String secondLevelField) {
        Object firstLevelObject = jsonObject.get(firstLevelField);
        if (firstLevelObject instanceof JSONObject) {
            return getValue((JSONObject) firstLevelObject, secondLevelField);
        } else if (firstLevelObject != null) {
            return firstLevelObject.toString();
        }
        return null;
    }

    protected static List<Header> createHeaderList(String headersString) {
        if (StringUtils.isNullOrEmpty(headersString)) {
            return Collections.emptyList();
        }

        ArrayList<Header> headersList = new ArrayList<>();
        String[] headers = headersString.split("\\n");
        for (String header : headers) {
            String[] headerParts = header.split(":");
            if (headerParts.length == 2) {
                headersList.add(new Header(headerParts[0].trim(), headerParts[1].trim()));
            }
        }
        return headersList;
    }

    public interface Variable {
        String getId();

        String getKey();

        String getValue();

        String getType();
    }

    public static class Header {
        private final String key;
        private final String value;

        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
