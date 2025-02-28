package com.smartbear.ready.plugin.postman.script;

import com.eviware.soapui.support.StringUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.smartbear.ready.plugin.postman.script.ScriptContext.CHAI_SCRIPTS;
import static com.smartbear.ready.plugin.postman.script.ScriptContext.POSTMAN_OBJECT;

public class PostmanScriptParserV2 {
    private final ScriptContext context;
    private final StringBuilder testsV1 = new StringBuilder();
    private final StringBuilder testsV2 = new StringBuilder();

    private String requestName;
    private String prescriptV1;

    private static final String POSTMAN_IMPORTED_COLLECTION_NAME = "Postman imported chai assertions - ";
    private static final String OLD_TEST_SYNTAX = "tests[";
    private static final String SET_GLOBAL_VARIABLE_REGEX = "pm *. *globals. *set *\\(\"(.*?)\" *, *\"(.*?)\"\\);";
    private static final Map<String, String> tokenMap = new LinkedHashMap<>();

    static {
        tokenMap.put("pm.response.json\\(\\)", "JSON.parse(messageExchange.response.contentAsString)");
        tokenMap.put("pm.response.json", "JSON.parse(messageExchange.response.contentAsString)");
        tokenMap.put("pm.response.code", "messageExchange.response.getStatusCode()");
        tokenMap.put("pm.response.text\\(\\)", "String(messageExchange.response.contentAsString)");
        tokenMap.put("pm.response.to.have.status", "chai.expect(messageExchange.response.getStatusCode()).to.eql");
        tokenMap.put("pm.response.to.have.header *\\((.*?)\\)", "chai.expect(messageExchange.responseHeaders.hasValues($1)).to.be.true");
        tokenMap.put("pm.response.headers.get\\((.*?)\\)", "String(messageExchange.responseHeaders.get($1))");
        tokenMap.put("pm.response.responseTime", "messageExchange.response.timeTaken");
        tokenMap.put("pm.test", "ready.test");
        tokenMap.put("pm", "chai");
    }

    public PostmanScriptParserV2(ScriptContext context) {
        this.context = context;
    }

    public void parse(String script, String requestName) {
        this.requestName = requestName;

        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecordingLocalJsDocComments(true);
        env.setAllowSharpComments(true);
        env.setRecordingComments(true);
        Parser parser = new Parser(env);

        AstRoot astRoot = parser.parse(script, null, 1);
        for (Node node : astRoot) {
            AstNode astNode = (AstNode) node;
            if (astNode.toSource().startsWith(OLD_TEST_SYNTAX)){
                testsV1.append(astNode.toSource());
            } else {
                testsV2.append(astNode.toSource());
                testsV2.append('\n');
            }
        }
        if (StringUtils.hasContent(testsV2.toString())) {
            addChaiAssertion(testsV2.toString());
        }
    }

    public String getTestsV1() {
        return testsV1.toString();
    }

    private String mapSyntax(String assertionBody) {
        for(Map.Entry<String,String> entry : tokenMap.entrySet()){
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(assertionBody);
            if (matcher.find()) {
                assertionBody = matcher.replaceAll(entry.getValue());
            }
        }
        return assertionBody;
    }

    private void addChaiAssertion(String assertionBody) {
        if (context.getObject(CHAI_SCRIPTS) != null) {
            AddChaiAssertionCommand chaiAssertion = (AddChaiAssertionCommand) context.getObject(CHAI_SCRIPTS).getCommand(AddChaiAssertionCommand.NAME);
            chaiAssertion.addArgument(null, mapSyntax(assertionBody).trim());
            chaiAssertion.addAssertionName(POSTMAN_IMPORTED_COLLECTION_NAME + requestName);
            chaiAssertion.execute();
        }
    }

    public void findAndAddSettingGlobalVariables(String script) {
        prescriptV1 = script;
        Pattern pattern = Pattern.compile(SET_GLOBAL_VARIABLE_REGEX);
        Matcher matcher;
        matcher = pattern.matcher(script);
        while (matcher.find()) {
            addGlobalVariable(matcher.group(1), matcher.group(2));
            prescriptV1 = script.replace(matcher.group(0), "");
        }
    }

    public String getPrescriptV1() {
        return prescriptV1;
    }

    private void addGlobalVariable(String key, String value) {
        if (context.getObject(POSTMAN_OBJECT) != null) {
            SetGlobalVariableCommand setGlobalVariableCommand =
                (SetGlobalVariableCommand) context.getObject(POSTMAN_OBJECT).getCommand(SetGlobalVariableCommand.NAME);
            setGlobalVariableCommand.addVariable(key, value);
            setGlobalVariableCommand.execute();
        }
    }
}
