package com.smartbear.postman.script;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;

public class SetGlobalVariableCommand implements ScriptCommand {
    private static final String NAME = "setGlobalVariable";

    private final WsdlProject project;
    private int argumentCount;
    private String variableName;
    private String variableValue;

    public SetGlobalVariableCommand(WsdlProject project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void prepare() {
        variableName = null;
        variableValue = null;
        argumentCount = 0;
    }

    @Override
    public void addArgument(TokenType tokenType, String argument) {
        switch (argumentCount) {
            case 0:
                variableName = argument;
                argumentCount++;
                break;
            case 1:
                variableValue = argument;
                argumentCount++;
        }
    }

    @Override
    public boolean validate() {
        return StringUtils.hasContent(variableName) && StringUtils.hasContent(variableValue);
    }

    @Override
    public Object execute() {
        TestProperty property = project.addProperty(StringUtils.unquote(variableName.trim()));
        property.setValue(StringUtils.unquote(variableValue));
        return null;
    }
}
