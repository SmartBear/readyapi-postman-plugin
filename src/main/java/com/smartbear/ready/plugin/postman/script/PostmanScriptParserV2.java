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
import static com.smartbear.ready.plugin.postman.utils.VariableUtils.DYNAMIC_VARIABLE_NAME_PREFIX;

public class PostmanScriptParserV2 {
    private final ScriptContext context;
    private final StringBuilder testsV1 = new StringBuilder();
    private final StringBuilder testsV2 = new StringBuilder();

    private String requestName;
    private String prescriptV1;

    private static final String POSTMAN_IMPORTED_COLLECTION_NAME = "Postman imported chai assertions - ";
    private static final String OLD_TEST_SYNTAX = "tests[";
    private static final Pattern SET_GLOBAL_VARIABLE_REGEX_PATTERN = Pattern.compile("pm.globals.set\\(['\"](.*?)['\"], ['\"](.*?)['\"]\\)");
    private static final Pattern AJV_REGEX = Pattern.compile("(var|let|const) (.*?) = require\\(['\"]ajv['\"]\\);\n*");
    private static final Map<Pattern, String> SYNTAX_TRANSLATION_MAP = new LinkedHashMap<>();
    private static final Map<Pattern, String> VAULT_VARIABLE_TRANSLATION_MAP = new LinkedHashMap<>();
    private static final Map<Pattern, String> DYNAMIC_VARIABLE_TRANSLATION_MAP = new LinkedHashMap<>();

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
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.(collectionVariables|environment|globals).get\\(['\"](.*?)['\"]\\)"), "String(context.expand(\"\\${#Project#$2}\"))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.(collectionVariables|environment|globals).get\\((.*?)\\)"), "String(context.expand(\"\\${#Project#\" + `\\${$2}` + \"}\"))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.variables.get\\(['\"](.*?)['\"]\\)"), "String(context.expand(\"\\${#TestCase#$1}\"))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.variables.get\\((.*?)\\)"), "String(context.expand(\"\\${#TestCase#\" + `\\${$1}` + \"}\"))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.(collectionVariables|environment|globals).set\\((.*?), (.*?)\\)"), "context.testCase.testSuite.project.setPropertyValue($2, $3)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.variables.set\\((.*?), (.*?)\\)"), "context.testCase.setPropertyValue($1, $2)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("require\\(['\"]xml2js['\"]\\)"), "xml2js");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("([^|\n].*?)xml2Json\\((.*?)\\)"), "var xml2jsResult;\nxml2js.parseString($2, function (err, result) {\n xml2jsResult = result;\n});\n$1xml2jsResult");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("messageExchange.response.contentAsString"), "String(messageExchange.response.contentAsString)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.response.json(\\(\\))*"), "JSON.parse(messageExchange.response.contentAsString)");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("console.(warn|debug|error)\\((.*?)\\)"), "log.$1(String($2))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("console.(info|log)\\((.*?)\\)"), "log.info(String($2))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.cookies.has\\((.*?)\\)"), "messageExchange.cookies.get($1) != null");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.cookies.get\\((.*?)\\)"), "String(messageExchange.cookies.get($1))");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.test"), "ready.test");
        SYNTAX_TRANSLATION_MAP.put(Pattern.compile("pm.expect"), "chai.expect");

        VAULT_VARIABLE_TRANSLATION_MAP.put(Pattern.compile("await pm.vault.get\\((.*?)\\)"), "String(context.expand(\"\\${#Project#\" + `\\${$1}` + \"}\"))");
        DYNAMIC_VARIABLE_TRANSLATION_MAP.put(Pattern.compile("pm.variables.replaceIn\\(['\"]\\{\\{\\$(.*?)}}['\"]\\)"), "String(context.expand(\"\\${#Project#dynamic-variable-$1}\"))");
    }

    public PostmanScriptParserV2(ScriptContext context) {
        this.context = context;
    }

    public void parse(String script) {
        parse(script, null);
    }

    public void parse(String script, String requestName) {
        this.requestName = requestName;

        script = mapAndAddDynamicVariables(mapSyntax(script, VAULT_VARIABLE_TRANSLATION_MAP));

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

    private String mapSyntax(String script, Map<Pattern, String> syntaxMap) {
        for(Map.Entry<Pattern, String> entry : syntaxMap.entrySet()) {
            Matcher matcher = entry.getKey().matcher(script);
            if (matcher.find()) {
                script = matcher.replaceAll(entry.getValue());
            }
        }
        return script;
    }

    private String mapAndAddDynamicVariables(String script) {
        for(Map.Entry<Pattern, String> entry : DYNAMIC_VARIABLE_TRANSLATION_MAP.entrySet()) {
            Matcher matcher = entry.getKey().matcher(script);
            if (matcher.find()) {
                addGlobalVariable(DYNAMIC_VARIABLE_NAME_PREFIX + matcher.group(1), "");
                script = matcher.replaceAll(entry.getValue());
            }
        }
        return script;
    }

    private String mapExternalAjvUsage(String script) {
        Matcher matcherRequireLib = AJV_REGEX.matcher(script);
        while (matcherRequireLib.find()) {
            String libraryNameInScript = matcherRequireLib.group(2);
            script = script.replace(matcherRequireLib.group(0), "");

            Matcher matcherForInit = getPatternForLibraryInitialization(libraryNameInScript).matcher(script);
            while (matcherForInit.find()) {
                libraryNameInScript = matcherForInit.group(2);
                script = script.replace(matcherForInit.group(0), "").replace(libraryNameInScript, "ajv");
            }
        }
        return script;
    }

    private Pattern getPatternForLibraryInitialization(String libraryScriptName) {
        return Pattern.compile("(var|let|const) (.*?) = new " + libraryScriptName + "\\((.|\n)*?\\);\n*");
    }

    private String translatePostmanSyntax(String postmanScript) {
        return mapSyntax(
                mapExternalAjvUsage(postmanScript),
                SYNTAX_TRANSLATION_MAP
        ).trim();
    }

    private void addChaiAssertion(String assertionBody) {
        if (context.getObject(CHAI_SCRIPTS) != null) {
            AddChaiAssertionCommand chaiAssertion = (AddChaiAssertionCommand) context.getObject(CHAI_SCRIPTS).getCommand(AddChaiAssertionCommand.NAME);
            chaiAssertion.addArgument(null, translatePostmanSyntax(assertionBody));
            chaiAssertion.addAssertionName(POSTMAN_IMPORTED_COLLECTION_NAME + requestName);
            chaiAssertion.execute();
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

    private class SplitTestsNodeVisitor implements NodeVisitor {
        @Override
        public boolean visit(AstNode astNode) {
            if (astNode.depth() == 1) {
                String jsNodeCode = astNode.toSource();
                if (jsNodeCode.startsWith(OLD_TEST_SYNTAX)) {
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
    }
}
