package com.smartbear.ready.plugin.postman.script;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.ChaiAssertion;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.StringUtils;

public class AddChaiAssertionCommand implements AddAssertionCommand {
    public static final String NAME = "chaiAssertion";
    private final Assertable assertable;
    private String script;
    private String assertionName;

    public AddChaiAssertionCommand(Assertable assertable) {
        this.assertable = assertable;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void prepare() {
        script = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        script = argument;
    }

    public void addAssertionName(String name) {
        this.assertionName = name;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(script);
    }

    @Override
    public Object execute() {
        ChaiAssertion assertion = (ChaiAssertion) assertable.addAssertion(ChaiAssertion.LABEL);
        assertion.setScriptText(script);
        if (!StringUtils.isNullOrEmpty(assertionName)) {
            assertion.getConfig().setName(assertionName);
        }
        return null;
    }
}
