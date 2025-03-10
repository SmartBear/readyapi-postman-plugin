package com.smartbear.ready.plugin.postman.script;

import com.eviware.soapui.support.StringUtils;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Context;
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
    private static final Map<Pattern, String> SYNTAX_TRANSLATION_MAP = new LinkedHashMap<>();

    static {
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.code"), "messageExchange.response.getStatusCode()");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.text\\(\\)"), "messageExchange.response.contentAsString");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.to.have.jsonSchema\\((.*?)\\)"), "chai.expect(ajv.validate($1, JSON.parse(messageExchange.response.contentAsString))).to.be.true");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("tv4.validate\\((.*?), (.*?)\\)"), "ajv.validate($2, $1)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.to.have.status\\((.*?)\\)"), "chai.expect(String(messageExchange.responseHeaders.get(\"#status#\"))).to.include($1)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.to.have.body\\((.*?)\\)"), "chai.expect(messageExchange.response.contentAsString).to.eql($1)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.to.have.header\\((.*?)\\)"), "chai.expect(messageExchange.responseHeaders.hasValues($1)).to.be.true");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.headers.get\\((.*?)\\)"), "String(messageExchange.responseHeaders.get($1))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.body"), "messageExchange.response.contentAsString");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.headers"), "messageExchange.response.responseHeaders");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.responseTime"), "messageExchange.response.timeTaken");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.(environment|globals).get\\(['\"](.*?)['\"]\\)"), "String(context.expand(\"\\${#Project#$2}\"))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.(environment|globals).get\\((.*?)\\)"), "String(context.expand(\"\\${#Project#\" + `\\${$2}` + \"}\"))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("require\\(['\"]xml2js['\"]\\)"), "xml2js");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("([^|\n].*?)xml2Json\\((.*?)\\)"), "var xml2jsResult;\nxml2js.parseString($2, function (err, result) {\n xml2jsResult = result;\n});\n$1xml2jsResult");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("messageExchange.response.contentAsString"), "String(messageExchange.response.contentAsString)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.json(\\(\\))*"), "JSON.parse(messageExchange.response.contentAsString)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("console.(error|debug)\\((.*?)\\)"), "log.$1(String($2))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("console.(log|info)\\((.*?)\\)"), "log.info(String($2))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.cookies.has\\((.*?)\\)"), "messageExchange.cookies.get($1) != null");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.cookies.get\\((.*?)\\)"), "String(messageExchange.cookies.get($1))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.test"), "ready.test");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.expect"), "chai.expect");
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
        env.setLanguageVersion(Context.VERSION_ES6);
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
        for(Map.Entry<Pattern, String> entry : SYNTAX_TRANSLATION_MAP.entrySet()){
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
                String jsNodeCode = astNode.toSource();
                if (jsNodeCode.startsWith(OLD_TEST_SYNTAX)){
                    testsV1.append(jsNodeCode);
                } else {
                    testsV2.append(jsNodeCode);
                    testsV2.append('\n');
                }
            }
            return astNode.depth() <= 1;
        }
    }

    private class LookForGlobalsNodeVisitor implements NodeVisitor {
        @Override
        public boolean visit(AstNode astNode) {
            String jsNodeCode = astNode.toSource();
            if (astNode.depth() == 0) {
                prescriptV1 = jsNodeCode;
            } else {
                Matcher matcher = SET_GLOBAL_VARIABLE_REGEX_PATTERN.matcher(jsNodeCode);
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
