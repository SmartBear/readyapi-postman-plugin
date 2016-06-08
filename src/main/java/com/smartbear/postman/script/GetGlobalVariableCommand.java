package com.smartbear.postman.script;

import com.eviware.soapui.support.StringUtils;
import com.smartbear.postman.VariableUtils;

public class GetGlobalVariableCommand implements ScriptCommand {
    private static final String NAME = "[";
    private String variableName;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void prepare() {
        variableName = null;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        variableName = argument;
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(variableName);
    }

    @Override
    public Object execute() {
        return VariableUtils.createProjectVariableExpansionString(StringUtils.unquote(variableName));
    }
}
