package com.smartbear.postman.script;

import com.eviware.soapui.model.testsuite.Assertable;

public class AddHeaderExistsAssertionCommand extends AddScriptAssertionCommand {
    private static final String NAME = "getResponseHeader";
    private static final String HEADERS_SCRIPT_BEGIN = "messageExchange.responseHeaders.hasValues(";
    private static final String HEADERS_SCRIPT_END = ")";

    public AddHeaderExistsAssertionCommand(Assertable assertable) {
        super(assertable);
    }

    @Override
    public void addCondition(String condition) {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        StringBuffer script = new StringBuffer();
        script.append(HEADERS_SCRIPT_BEGIN);
        script.append(argument);
        script.append(HEADERS_SCRIPT_END);
        super.addArgument(tokenType, script.toString());
    }
}
