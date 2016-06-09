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
import com.smartbear.ready.core.exception.ReadyApiException;
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
    public void parsesSettingGlobalVariable() throws ReadyApiException {
        String script = "postman.setGlobalVariable(\"string1\", \"abc\");\\npostman.setGlobalVariable(\"string2\", \"def\"); ";

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
    public void parsesResponseValidCodeAssertion() throws ReadyApiException {
        String script = "tests[\"Status code is 200\"] = responseCode.code === 200;";

        ValidHttpStatusCodesAssertion assertion = mock(ValidHttpStatusCodesAssertion.class);
        when(assertable.addAssertion(ValidHttpStatusCodesAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setCodes("200");
    }

    @Test
    public void parsesResponseInvalidCodeAssertion() throws ReadyApiException {
        String script = "tests[\"Status code is not 401\"] = responseCode.code !== 401;";

        InvalidHttpStatusCodesAssertion assertion = mock(InvalidHttpStatusCodesAssertion.class);
        when(assertable.addAssertion(InvalidHttpStatusCodesAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setCodes("401");
    }

    @Test
    public void parsesResponseTwoValidCodeAssertion() throws ReadyApiException {
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
    public void parsesResponseTimeAssertion() throws ReadyApiException {
        String script = "tests[\"Response time is less than 300ms\"] = responseTime < 300;";

        ResponseSLAAssertion assertion = mock(ResponseSLAAssertion.class);
        when(assertable.addAssertion(ResponseSLAAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setSLA("300");
    }

    @Test
    public void parsesResponseBodyEqualsAssertion() throws ReadyApiException {
        String script = "tests[\"Body is correct\"] = responseBody === \"\\\"abc def\\\"\";";

        EqualsAssertion assertion = mock(EqualsAssertion.class);
        when(assertable.addAssertion(EqualsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setPatternText("\"abc def\"");
    }

    @Test
    public void parsesResponseBodyContainsAssertion() throws ReadyApiException {
        String script = "tests[\"Body matches string\"] = responseBody.has(\"abc\");";

        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("\"abc\"");
    }

    @Test
    public void parsesGlobalVariableReference() throws ReadyApiException {
        String script = "tests[\"Body matches string\"] = responseBody.has(globals[\"string1\"]);";

        SimpleContainsAssertion assertion = mock(SimpleContainsAssertion.class);
        when(assertable.addAssertion(SimpleContainsAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setToken("${#Project#string1}");
    }

    @Test
    public void parsesResponseHeaderExistsAssertion() throws ReadyApiException {
        String script = "tests[\"Content Type is present\"] = postman.getResponseHeader(\"Content-Type\");";

        GroovyScriptAssertion assertion = mock(GroovyScriptAssertion.class);
        when(assertable.addAssertion(GroovyScriptAssertion.LABEL)).thenReturn(assertion);

        ScriptContext context = ScriptContext.prepareTestScriptContext(project, assertable);

        parseScript(script, context);

        verify(assertion).setScriptText("messageExchange.responseHeaders.hasValues(\"Content-Type\")");
    }

    private void parseScript(String script, ScriptContext context) throws ReadyApiException {
        PostmanScriptTokenizer tokenizer = new PostmanScriptTokenizer();
        PostmanScriptParser parser = new PostmanScriptParser();
        LinkedList<PostmanScriptTokenizer.Token> tokens = tokenizer.tokenize(script);
        parser.parse(tokens, context);
    }
}
