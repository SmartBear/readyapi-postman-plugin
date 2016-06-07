package com.smartbear.postman.script;

public interface ScriptCommand {
    String getName();

    void prepare();

    void addArgument(TokenType tokenType, String argument);

    boolean validate();

    Object execute();
}
