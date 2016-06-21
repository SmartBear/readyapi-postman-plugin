package com.smartbear.postman.script;

import java.util.regex.Pattern;

public enum TokenType {
    NAME("[a-zA-Z]\\w*"),
    DOT("\\."),
    COMMA("\\,"),
    NEW_LINE("\\\\n"),
    OPEN_ROUND_BRACKET("\\("),
    CLOSE_ROUND_BRACKET("\\)"),
    OPEN_SQUARE_BRACKET("\\["),
    CLOSE_SQUARE_BRACKET("\\]"),
    OPEN_CURLY_BRACKET("\\{"),
    CLOSE_CURLY_BRACKET("\\}"),
    ASSIGN("=[^=]"),
    EQUALS("(===|!==|>|<)"),
    LOGIC("(&&|\\|\\|)"),
    STRING("(\"(?:[^\"\\\\]|\\\\.)*\"|'\\S*')"),
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
