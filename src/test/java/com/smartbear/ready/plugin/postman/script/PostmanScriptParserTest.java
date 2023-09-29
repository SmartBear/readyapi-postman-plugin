package com.smartbear.ready.plugin.postman.script;

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

    @Before
    public void prepare() {
        project = new WsdlProject();
        assertable = mock(Assertable.class);
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

        ValidHttpStatusCodesAssertion assertion = mock(ValidHttpStatusCodesAssertion.class);
        when(assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setCodes("200");
    }

    @Test
    public void parsesResponseInvalidCodeAssertion() throws SoapUIException {
        String script = "tests[\"Status code is not 401\"] = responseCode.code !== 401;";

        InvalidHttpStatusCodesAssertion assertion = mock(InvalidHttpStatusCodesAssertion.class);
        when(assertable.addAssertion(InvalidHttpStatusCodesAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setCodes("401");
    }

    @Test
    public void parsesResponseTwoValidCodeAssertion() throws SoapUIException {
        String script = "tests[\"Status code is 200 or 201\"] = responseCode.code === 200 || responseCode.code === 201;";

        final ArrayList<TestAssertion> assertions = new ArrayList<>();
        when(assertable.getAssertionList()).thenReturn(assertions);

        final ValidHttpStatusCodesAssertion assertion = mock(ValidHttpStatusCodesAssertion.class);

        when(assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL)).thenReturn(assertion);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                when(assertion.getCodes()).thenReturn((String) invocation.getArguments()[0]);
                assertions.add(assertion);
                return null;
            }
        }).when(assertion).setCodes(anyString());

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setCodes("200");

        verify(assertion).setCodes("200,201");
    }

    @Test
    public void parsesResponseTimeAssertion() throws SoapUIException {
        String script = "tests[\"Response time is less than 300ms\"] = responseTime < 300;";

        ResponseSLAAssertion assertion = mock(ResponseSLAAssertion.class);
        when(assertable.addAssertion(ResponseSLAAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setSLA("300");
    }

    @Test
    public void parsesResponseBodyEqualsAssertion() throws SoapUIException {
        String script = "tests[\"Body is correct\"] = responseBody === \"\\\"abc def\\\"\";";

        EqualsAssertion assertion = mock(EqualsAssertion.class);
        when(assertable.addAssertion(EqualsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setPatternText("\"abc def\"");
    }

    @Test
    public void parsesResponseBodyContainsAssertion() throws SoapUIException {
        String script = "tests[\"Body matches string\"] = responseBody.has(\"abc\");";

        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("abc");
    }

    @Test
    public void parsesResponseBodyContainsAssertionWithQuotes() throws SoapUIException {
        String script = "tests[\"Body matches string\"] = responseBody.has(\"\\\"abc\\\"\");";

        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("\"abc\"");
    }

    @Test
    public void parsesGlobalVariableReference() throws SoapUIException {
        String script = "tests[\"Body matches string\"] = responseBody.has(globals[\"string1\"]);";

        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("${#Project#string1}");
    }

    @Test
    public void parsesCommandInTry() throws SoapUIException {
        String script = "try { tests[\"Body matches string\"] = responseBody.has(\"abc\"); }\ncatch (e) { }";
        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("abc");
    }

    @Test
    public void parsesCommandAfterCatch() throws SoapUIException {
        String script = "try { responseJSON = JSON.parse(responseBody); }\ncatch (e) { }\n\ntests[\"Body matches string\"] = responseBody.has(\"abc\");";
        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("abc");
    }

    @Test
    public void ignoresCommentedLines() throws SoapUIException {
        String script = "//tests[\"Body matches string\"] = responseBody.has(\"def\");\ntests[\"Body matches string\"] = responseBody.has(\"abc\");";
        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("abc");
        verify(assertion, never()).setToken("def");
    }

    @Test
    public void parsesResponseHeaderExistsAssertion() throws SoapUIException {
        String script = "tests[\"Content Type is present\"] = postman.getResponseHeader(\"Content-Type\");";

        GroovyScriptAssertion assertion = mock(GroovyScriptAssertion.class);
        when(assertable.addAssertion(GroovyScriptAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setScriptText("assert messageExchange.responseHeaders.hasValues(\"Content-Type\")");
    }

    @Test
    public void parsesStringInSingleQuotes() throws SoapUIException {
        String script = "tests[\"response code is 200\"] = responseCode.code === 200;tests[\"Content Type is present\"] = postman.getResponseHeader('Content-Type');";

        ValidHttpStatusCodesAssertion statusAssertion = mock(ValidHttpStatusCodesAssertion.class);
        when(assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL)).thenReturn(statusAssertion);

        GroovyScriptAssertion groovyAssertion = mock(GroovyScriptAssertion.class);
        when(assertable.addAssertion(GroovyScriptAssertion.LABEL)).thenReturn(groovyAssertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(statusAssertion).setCodes("200");
        verify(groovyAssertion).setScriptText("assert messageExchange.responseHeaders.hasValues('Content-Type')");
    }

    @Test
    public void parsesExpressionsInRoundBrackets() throws SoapUIException {
        String script = "tests[\"response code is 200\"] = (responseCode.code === 200);";

        ValidHttpStatusCodesAssertion statusAssertion = mock(ValidHttpStatusCodesAssertion.class);
        when(assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL)).thenReturn(statusAssertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(statusAssertion).setCodes("200");
    }

    private void parseScript(String script, ScriptContext context) throws SoapUIException {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParser parser = new PostmanScriptParser();
        LinkedList<PostmanScriptTokenizer.Token> tokens = tokenizer.tokenize(script);
        parser.parse(tokens, context);
    }
}
