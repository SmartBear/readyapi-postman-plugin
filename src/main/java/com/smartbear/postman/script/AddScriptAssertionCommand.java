package com.smartbear.postman.script;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.GroovyScriptAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.StringUtils;

public abstract class AddScriptAssertionCommand implements AddAssertionCommand {
    private final Assertable assertable;
    private String script;

    public AddScriptAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public void prepare() {
        script = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        script = argument;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(script);
    }

    @Override
    public Object execute() {
        GroovyScriptAssertion assertion = (GroovyScriptAssertion) assertable.addAssertion(GroovyScriptAssertion.LABEL);
        assertion.setScriptText(script);
        return null;
    }
}
