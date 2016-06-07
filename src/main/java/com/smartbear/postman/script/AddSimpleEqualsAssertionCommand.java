package com.smartbear.postman.script;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.StringUtils;

public class AddSimpleEqualsAssertionCommand implements AddAssertionCommand {
    private final Assertable assertable;
    private String value;

    public AddSimpleEqualsAssertionCommand(Assertable assertable) {
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
        SimpleContainsAssertion assertion = (SimpleContainsAssertion) assertable.addAssertion(SimpleContainsAssertion.LABEL);
        assertion.setToken(value);
        return null;
    }
}
