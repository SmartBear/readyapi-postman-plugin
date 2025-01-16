package com.smartbear.ready.plugin.postman.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.smartbear.ready.plugin.postman.script.ScriptContext.RESPONSE_BODY;

public class PostmanScriptParserV2 {
    private static final String TEST_REGEX = "pm\\.test\\(\"[^\"]*\", \\( *\\) *=> (.|\n)*?\\);";
    private static final String TEST_REGEX_START = "pm\\.test *\\( *\".*\" *, *\\( *\\) *=> *\\{";
    private static final String TEST_REGEX_END = "}\\);";

    private final HashMap<String, String> tokenMap = new HashMap<>();

    public PostmanScriptParserV2() {
        tokenMap.put("pm", "chai");
        tokenMap.put("pm.response.json", "JSON.parse(messageExchange.response.contentAsString)");
        tokenMap.put("pm.response.code", "messageExchange.response.getStatusCode()");
    }

    public void parse(String tests, ScriptContext context) {

        List<String> testList = splitTests(tests);
        for(String test : testList) {
            ScriptCommand chaiAssertion = context.getObject(RESPONSE_BODY).getCommand(AddChaiAssertionCommand.NAME);
            chaiAssertion.addArgument(null, test);
            chaiAssertion.execute();
        }
    }

    private List<String> splitTests(String tests) {
        List<String> testList = new ArrayList<>();

        Pattern pattern = Pattern.compile(TEST_REGEX);
        Matcher match = pattern.matcher(tests);
        String currentTest;

        while(match.find()){
            currentTest = match.group().replaceFirst(TEST_REGEX_START, "").replaceAll(TEST_REGEX_END, "");

            for(Map.Entry<String,String> entry : tokenMap.entrySet()){
                currentTest = currentTest.replace(entry.getKey(), entry.getValue());
            }
            testList.add(currentTest);
        }

        return testList;
    }
}
