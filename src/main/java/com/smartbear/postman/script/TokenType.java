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

package com.smartbear.postman.script;

import java.util.regex.Pattern;

public enum TokenType {
    NEW_LINE("\\n", false),
    COMMENT("//"),
    NAME("[a-zA-Z]\\w*"),
    DOT("\\."),
    COMMA("\\,"),
    OPEN_ROUND_BRACKET("\\("),
    CLOSE_ROUND_BRACKET("\\)"),
    OPEN_SQUARE_BRACKET("\\["),
    CLOSE_SQUARE_BRACKET("\\]"),
    OPEN_CURLY_BRACKET("\\{"),
    CLOSE_CURLY_BRACKET("\\}"),
    COLON("\\:"),
    ASSIGN("=[^=]"),
    EQUALS("(===|!==|>|<)"),
    WRONG_EQUALS("=="),
    LOGIC("(&&|\\|\\|)"),
    STRING("(\"(?:[^\"\\\\]|\\\\.)*\"|'\\S*')"),
    NUMBER("\\d+"),
    END_OF_COMMAND(";"),
    END_OF_SCRIPT("");

    TokenType(String regex) {
        this(regex, true);
    }

    TokenType(String regex, boolean addSpacesBefore) {
        if (addSpacesBefore) {
            regex = "\\s*" + regex;
        }
        pattern = Pattern.compile("^" + regex);
    }

    public Pattern getPattern() {
        return pattern;
    }

    private Pattern pattern;
}
