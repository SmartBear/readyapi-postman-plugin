package com.smartbear.postman.script;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.testsuite.Assertable;

import java.util.HashMap;

public class ScriptContext {
    public static final String RESPONSE_CODE = "responseCode";
    public static final String RESPONSE_TIME = "responseTime";
    public static final String RESPONSE_BODY = "responseBody";

    private HashMap<String, PostmanObject> objects = new HashMap<>();

    public void addObject(String name, PostmanObject postmanObject) {
        objects.put(name, postmanObject);
    }

    public PostmanObject getObject(String name) {
        return objects.get(name);
    }

    public static ScriptContext preparePreRequestScriptContext(WsdlProject project) {
        SetGlobalVariableCommand setGlobalVariableCommand = new SetGlobalVariableCommand(project);
        PostmanObject postmanObject = new PostmanObject();
        postmanObject.addCommand(setGlobalVariableCommand);

        ScriptContext context = new ScriptContext();
        context.addObject(PostmanScriptParser.POSTMAN_OBJECT, postmanObject);

        return context;
    }

    public static ScriptContext prepareTestScriptContext(WsdlProject project, Assertable assertable) {
        ScriptContext context = preparePreRequestScriptContext(project);

        AddHttpCodeAssertionCommand addHttpCodeAssertionCommand = new AddHttpCodeAssertionCommand(assertable);
        PostmanObject responseCodeObject = new PostmanObject();
        responseCodeObject.addCommand(addHttpCodeAssertionCommand);
        context.addObject(RESPONSE_CODE, responseCodeObject);

        context.addObject(RESPONSE_TIME, new PostmanObject(new AddSlaAssertionCommand(assertable)));
        context.addObject(RESPONSE_BODY, new PostmanObject(new AddSimpleEqualsAssertionCommand(assertable)));

        return context;
    }
}
