package com.smartbear.postman.script;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ResponseSLAAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.StringUtils;

public class AddSlaAssertionCommand implements AddAssertionCommand {
    private final Assertable assertable;
    private String value;

    public AddSlaAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public void addCondition(String condition) {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void prepare() {
        value = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        value = argument;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(value);
    }

    @Override
    public Object execute() {
        ResponseSLAAssertion assertion = getSlaAssertion();
        assertion.setSLA(value);
        return null;
    }

    private ResponseSLAAssertion getSlaAssertion() {
        for (TestAssertion assertion : assertable.getAssertionList()) {
            if (assertion instanceof ResponseSLAAssertion) {
                return (ResponseSLAAssertion) assertion;
            }
        }
        return (ResponseSLAAssertion) assertable.addAssertion(ResponseSLAAssertion.LABEL);
    }
}
