package com.smartbear.postman.script;

import com.smartbear.ready.core.exception.ReadyApiException;
import org.junit.Test;

import java.util.LinkedList;

public class PostmanScriptParserTest {

    @Test
    public void parsesSettingGlobalVariable() throws ReadyApiException {
        PostmanScriptParser parser = new PostmanScriptParser();
        String script = "postman.setGlobalVariable(\\\"string1\\\", \\\"abc\\\");\\npostman.setGlobalVariable(\\\"string2\\\", \\\"def\\\"); ";
        LinkedList<PostmanScriptParser.Token> tokens = parser.parse(script);
    }
}
