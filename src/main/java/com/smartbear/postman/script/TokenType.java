package com.smartbear.postman.script;

import java.util.regex.Pattern;

public enum TokenType {
    OBJECT("(postman|tests|responseCode)"),
    METHOD_OR_FIELD("(setGlobalVariable|code)"),
    DOT("\\."),
    COMMA("\\,"),
    NEW_LINE("\\\\n"),
    OPEN_ROUND_BRACKET("\\("),
    CLOSE_ROUND_BRACKET("\\)"),
    OPEN_SQUARE_BRACKET("\\["),
    CLOSE_SQUARE_BRACKET("\\]"),
    ASSIGN("=[^=]"),
    EQUALS("(===|!==|>|<)"),
    LOGIC("(&&|\\|\\|)"),
    STRING("\"[^\"]*\""),
    NUMBER("\\d+"),
    END_OF_COMMAND(";"),
    END_OF_SCRIPT("");

    TokenType(String regex) {
        pattern = Pattern.compile("^\\s*" + regex);
    }

    public Pattern getPattern() {
        return pattern;
    }

    private Pattern pattern;
}
