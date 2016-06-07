package com.smartbear.postman.script;

import java.util.HashMap;

public class PostmanObject {
    private HashMap<String, ScriptCommand> members = new HashMap<>();

    public void addCommand(ScriptCommand command) {
        members.put(command.getName(), command);
    }

    public ScriptCommand getCommand(String name) {
        return members.get(name);
    }
}
