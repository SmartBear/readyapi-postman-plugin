package com.smartbear.postman.script;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.EqualsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.assertion.InvalidHttpStatusCodesAssertion;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.soapui.support.SoapUIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostmanScriptParserTest {

    public static final String PROPERTY1_NAME = "string1";
    public static final String PROPERTY1_VALUE = "abc";
    public static final String PROPERTY2_NAME = "string2";
    public static final String PROPERTY2_VALUE = "def";

    private WsdlProject project;
    private Assertable assertable;

    List<TestAssertion> assertions;

    GroovyScriptAssertion groovyAssertion;
    ValidHttpStatusCodesAssertion validHttpStatusCodesAssertion;
    InvalidHttpStatusCodesAssertion invalidHttpStatusCodesAssertion;
    ResponseSLAAssertion responseSLAAssertion;
    EqualsAssertion equalsAssertion;
    SimpleContainsAssertion simpleContainsAssertion;

    @Before
    public void prepare() {
        project = new WsdlProject();
        assertable = mock(Assertable.class);

        groovyAssertion = mock(GroovyScriptAssertion.class);
        validHttpStatusCodesAssertion = mock(ValidHttpStatusCodesAssertion.class);
        invalidHttpStatusCodesAssertion = mock(InvalidHttpStatusCodesAssertion.class);
        responseSLAAssertion = mock(ResponseSLAAssertion.class);
        equalsAssertion = mock(EqualsAssertion.class);
        simpleContainsAssertion = mock(SimpleContainsAssertion.class);

        when(assertable.addAssertion(GroovyScriptAssertion.LABEL)).thenReturn(groovyAssertion);
        when(assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL)).thenReturn(validHttpStatusCodesAssertion);
        when(assertable.addAssertion(InvalidHttpStatusCodesAssertion.LABEL)).thenReturn(invalidHttpStatusCodesAssertion);
        when(assertable.addAssertion(ResponseSLAAssertion.LABEL)).thenReturn(responseSLAAssertion);
        when(assertable.addAssertion(EqualsAssertion.LABEL)).thenReturn(equalsAssertion);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(simpleContainsAssertion);

        assertions = new ArrayList<>();
        when(assertable.getAssertionList()).thenReturn(assertions);
    }

    @Test
    public void newLineTokenIsFirst() {
        //???
        //Each token regular expression appended in the beginning with "\s*" pattern which includes new line pattern,
        //so the NEW_LINE pattern must be first in the pattern check list.
        assert (TokenType.values()[0] == TokenType.NEW_LINE);
    }

    @Test
    public void parsesSettingGlobalVariable() throws SoapUIException {
        String script = "postman.setGlobalVariable(\"string1\", \"abc\");\\npostman.setGlobalVariable(\"string2\", \"def\"); ";
        parseSettingGlobalVariables(script);
    }

    @Test
    public void skipsUnknownCommands() throws SoapUIException {
        String script = "postman.setGlobalVariable(\"string1\", \"abc\");\\nvar jsonObject = xml2Json(responseBody);\\n" +
                "tests[\"last record ingested is 13\"] = jsonData.last_record_ingested == 13;\\n" +
                "var schema = {\\n" +
                "  \"files_not_found_records\": { \\n" +
                "                \"type\": \"string\" \\n" +
                "            }\\n" +
                "}\\n" +
                "postman.setGlobalVariable(\"string2\", \"def\");";
        parseSettingGlobalVariables(script);
    }

    private void parseSettingGlobalVariables(String script) throws SoapUIException {
        ScriptContext context = ScriptContext.preparePreRequestScriptContext(project);

        parseScript(script, context);

        TestProperty property1 = project.getProperty(PROPERTY1_NAME);
        assertNotNull("Property1 is missing", property1);
        assertEquals("Property1 has wrong value", PROPERTY1_VALUE, property1.getValue());

        TestProperty property2 = project.getProperty(PROPERTY2_NAME);
        assertNotNull("Property2 is missing", property2);
        assertEquals("Property2 has wrong value", PROPERTY2_VALUE, property2.getValue());
    }

    @Test
    public void parsesResponseValidCodeAssertion() throws SoapUIException {
        String script = "tests[\"Status code is 200\"] = responseCode.code === 200;";

        createContextAndParse(script);

        verify(validHttpStatusCodesAssertion).setCodes("200");
    }

    @Test
    public void parsesResponseInvalidCodeAssertion() throws SoapUIException {
        String script = "tests[\"Status code is not 401\"] = responseCode.code !== 401;";

        createContextAndParse(script);

        verify(invalidHttpStatusCodesAssertion).setCodes("401");
    }

    @Test
    public void parsesResponseTwoValidCodeAssertion() throws SoapUIException {
        String script = "tests[\"Status code is 200 or 201\"] = responseCode.code === 200 || responseCode.code === 201;";

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                when(validHttpStatusCodesAssertion.getCodes()).thenReturn((String) invocation.getArguments()[0]);
                assertions.add(validHttpStatusCodesAssertion);
                return null;
            }
        }).when(validHttpStatusCodesAssertion).setCodes(anyString());

        createContextAndParse(script);

        verify(validHttpStatusCodesAssertion).setCodes("200");
        verify(validHttpStatusCodesAssertion).setCodes("200,201");
    }

    @Test
    public void parsesResponseTimeAssertion() throws SoapUIException {
        String script = "tests[\"Response time is less than 300ms\"] = responseTime < 300;";

        createContextAndParse(script);

        verify(responseSLAAssertion).setSLA("300");
    }

    @Test
    public void parsesResponseBodyEqualsAssertion() throws SoapUIException {
        String script = "tests[\"Body is correct\"] = responseBody === \"\\\"abc def\\\"\";";

        createContextAndParse(script);

        verify(equalsAssertion).setPatternText("\"abc def\"");
    }

    @Test
    public void parsesResponseBodyContainsAssertion() throws SoapUIException {
        String script = "tests[\"Body matches string\"] = responseBody.has(\"abc\");";

        createContextAndParse(script);

        verify(simpleContainsAssertion).setToken("abc");
    }

    @Test
    public void parsesResponseBodyContainsAssertionWithQuotes() throws SoapUIException {
        String script = "tests[\"Body matches string\"] = responseBody.has(\"\\\"abc\\\"\");";

        createContextAndParse(script);

        verify(simpleContainsAssertion).setToken("\"abc\"");
    }

    @Test
    public void parsesGlobalVariableReference() throws SoapUIException {
        String script = "tests[\"Body matches string\"] = responseBody.has(globals[\"string1\"]);";

        createContextAndParse(script);

        verify(simpleContainsAssertion).setToken("${#Project#string1}");
    }

    @Test
    public void parsesCommandInTry() throws SoapUIException {
        String script = "try { tests[\"Body matches string\"] = responseBody.has(\"abc\"); }\ncatch (e) { }";

        createContextAndParse(script);

        verify(simpleContainsAssertion).setToken("abc");
    }

    @Test
    public void parsesCommandAfterCatch() throws SoapUIException {
        String script = "try { responseJSON = JSON.parse(responseBody); }\ncatch (e) { }\n\ntests[\"Body matches string\"] = responseBody.has(\"abc\");";

        createContextAndParse(script);

        verify(simpleContainsAssertion).setToken("abc");
    }

    @Test
    public void ignoresCommentedLines() throws SoapUIException {
        String script = "//tests[\"Body matches string\"] = responseBody.has(\"def\");\ntests[\"Body matches string\"] = responseBody.has(\"abc\");";

        createContextAndParse(script);

        verify(simpleContainsAssertion).setToken("abc");
        verify(simpleContainsAssertion, never()).setToken("def");
    }

    @Test
    public void parsesResponseHeaderExistsAssertion() throws SoapUIException {
        String script = "tests[\"Content Type is present\"] = postman.getResponseHeader(\"Content-Type\");";

        createContextAndParse(script);

        verify(groovyAssertion).setScriptText("assert messageExchange.responseHeaders.hasValues(\"Content-Type\")");
    }

    @Test
    public void parsesStringInSingleQuotes() throws SoapUIException {
        String script = "tests[\"response code is 200\"] = responseCode.code === 200;tests[\"Content Type is present\"] = postman.getResponseHeader('Content-Type');";

        createContextAndParse(script);

        verify(validHttpStatusCodesAssertion).setCodes("200");
        verify(groovyAssertion).setScriptText("assert messageExchange.responseHeaders.hasValues('Content-Type')");
    }

    @Test
    public void parsesExpressionsInRoundBrackets() throws SoapUIException {
        String script = "tests[\"response code is 200\"] = (responseCode.code === 200);";

        createContextAndParse(script);

        verify(validHttpStatusCodesAssertion).setCodes("200");
    }

    @Test
    public void testMultiline() throws SoapUIException {
        final String TESTS = "tests[\"response code is 200\"] = responseCode.code === 200\n" +
                "tests[\"Status code is not 401\"] = responseCode.code !== 401\n" +
                "tests[\"Response time is less than 500ms\"] = responseTime < 500";
        createContextAndParse(TESTS);
        verify(validHttpStatusCodesAssertion).setCodes("200");
        verify(invalidHttpStatusCodesAssertion).setCodes("401");
        verify(responseSLAAssertion).setSLA("500");
    }

    private void createContextAndParse(String script) throws SoapUIException {
        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);
        parseScript(script, context);
    }

    private void parseScript(String script, ScriptContext context) throws SoapUIException {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParser parser = new PostmanScriptParser();
        LinkedList<PostmanScriptTokenizer.Token> tokens = tokenizer.tokenize(script);
        parser.parse(tokens, context);
    }
}
