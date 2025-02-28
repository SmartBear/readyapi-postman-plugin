/**
 *  Copyright 2016 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.ready.plugin.postman.script;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.testsuite.Assertable;

import java.util.HashMap;

public class ScriptContext {
    public static final String POSTMAN_OBJECT = "postman";
    public static final String RESPONSE_CODE = "responseCode";
    public static final String RESPONSE_TIME = "responseTime";
    public static final String RESPONSE_BODY = "responseBody";
    public static final String CHAI_SCRIPTS = "chaiScripts";
    public static final String GLOBALS = "globals";

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
        context.addObject(POSTMAN_OBJECT, postmanObject);

        return context;
    }

    public static ScriptContext prepareTestScriptContext(WsdlProject project, Assertable assertable) {
        ScriptContext context = preparePreRequestScriptContext(project);

        AddHttpCodeAssertionCommand addHttpCodeAssertionCommand = new AddHttpCodeAssertionCommand(assertable);
        PostmanObject responseCodeObject = new PostmanObject();
        responseCodeObject.addCommand(addHttpCodeAssertionCommand);
        context.addObject(RESPONSE_CODE, responseCodeObject);

        context.addObject(RESPONSE_TIME, new PostmanObject(new AddSlaAssertionCommand(assertable)));

        PostmanObject responseBodyObject = new PostmanObject(new AddEqualsAssertionCommand(assertable));
        responseBodyObject.addCommand(new AddSimpleContainsAssertionCommand(assertable));
        context.addObject(RESPONSE_BODY, responseBodyObject);

        PostmanObject globalsObject = new PostmanObject();
        globalsObject.addCommand(new GetGlobalVariableCommand());
        context.addObject(GLOBALS, globalsObject);

        PostmanObject postmanObject = context.getObject(POSTMAN_OBJECT);
        postmanObject.addCommand(new AddHeaderExistsAssertionCommand(assertable));

        PostmanObject chaiScriptsObject = new PostmanObject();
        chaiScriptsObject.addCommand(new AddChaiAssertionCommand(assertable));
        context.addObject(CHAI_SCRIPTS, chaiScriptsObject);

        return context;
    }
}
