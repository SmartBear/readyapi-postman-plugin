package com.smartbear.postman.script;

import java.util.HashMap;

public class PostmanObject {
    private HashMap<String, ScriptCommand> members = new HashMap<>();
    private ScriptCommand defaultCommand;

    public PostmanObject() {
    }

    public PostmanObject(ScriptCommand defaultCommand) {
        this.defaultCommand = defaultCommand;
    }

    public void addCommand(ScriptCommand command) {
        members.put(command.getName(), command);
    }

    public ScriptCommand getCommand(String name) {
        return members.get(name);
    }

    public boolean hasDefaultCommand() {
        return defaultCommand != null;
    }

    public ScriptCommand getDefaultCommand() {
        return defaultCommand;
    }
}
