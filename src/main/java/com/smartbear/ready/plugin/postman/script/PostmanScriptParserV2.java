package com.smartbear.ready.plugin.postman.script;

import com.eviware.soapui.support.StringUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.NodeVisitor;

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
    private static final Pattern SET_GLOBAL_VARIABLE_REGEX_PATTERN = Pattern.compile("pm.globals.set\\(\"(.*?)\", \"(.*?)\"\\);");
    private static final Map<Pattern, String> tokenMap = new LinkedHashMap<>();

    static {
        tokenMap.put(Pattern.compile("pm.response.json\\(\\)"), "JSON.parse(messageExchange.response.contentAsString)");
        tokenMap.put(Pattern.compile("pm.response.json"), "JSON.parse(messageExchange.response.contentAsString)");
        tokenMap.put(Pattern.compile("pm.response.code"), "messageExchange.response.getStatusCode()");
        tokenMap.put(Pattern.compile("pm.response.text\\(\\)"), "String(messageExchange.response.contentAsString)");
        tokenMap.put(Pattern.compile("pm.response.to.have.status"), "chai.expect(messageExchange.response.getStatusCode()).to.eql");
        tokenMap.put(Pattern.compile("pm.response.to.have.header *\\((.*?)\\)"), "chai.expect(messageExchange.responseHeaders.hasValues($1)).to.be.true");
        tokenMap.put(Pattern.compile("pm.response.headers.get\\((.*?)\\)"), "String(messageExchange.responseHeaders.get($1))");
        tokenMap.put(Pattern.compile("pm.response.responseTime"), "messageExchange.response.timeTaken");
        tokenMap.put(Pattern.compile("pm.test"), "ready.test");
        tokenMap.put(Pattern.compile("pm"), "chai");
    }

    public PostmanScriptParserV2(ScriptContext context) {
        this.context = context;
    }

    public void parse(String script) {
        parse(script, null);
    }

    public void parse(String script, String requestName) {
        this.requestName = requestName;

        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecordingLocalJsDocComments(true);
        env.setAllowSharpComments(true);
        env.setRecordingComments(true);
        Parser parser = new Parser(env);
        AstRoot astRoot = parser.parse(script, null, 1);

        if (requestName == null) {
            astRoot.visit(new LookForGlobalsNodeVisitor());
        } else {
            astRoot.visit(new SplitTestsNodeVisitor());
        }

        String chaiTests = testsV2.toString();
        if (StringUtils.hasContent(chaiTests)) {
            addChaiAssertion(chaiTests);
        }
    }

    public String getTestsV1() {
        return testsV1.toString();
    }

    private String mapSyntax(String assertionBody) {
        for(Map.Entry<Pattern, String> entry : tokenMap.entrySet()){
            Matcher matcher = entry.getKey().matcher(assertionBody);
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

    public String getPrescriptV1() {
        return prescriptV1;
    }

    private class SplitTestsNodeVisitor implements NodeVisitor {
        @Override
        public boolean visit(AstNode astNode) {
            if (astNode.depth() == 1){
                if (astNode.toSource().startsWith(OLD_TEST_SYNTAX)){
                    testsV1.append(astNode.toSource());
                } else {
                    testsV2.append(astNode.toSource());
                    testsV2.append('\n');
                }
            }
            return astNode.depth() <= 1;
        }
    }

    private class LookForGlobalsNodeVisitor implements NodeVisitor {
        @Override
        public boolean visit(AstNode astNode) {
            if (astNode.depth() == 0) {
                prescriptV1 = astNode.toSource();
            } else {
                Matcher matcher = SET_GLOBAL_VARIABLE_REGEX_PATTERN.matcher(astNode.toSource());
                while (matcher.find()) {
                    addGlobalVariable(matcher.group(1), matcher.group(2));
                    prescriptV1 = prescriptV1.replace(matcher.group(0), "");
                }
            }
            return astNode.depth() <= 1;
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
}
