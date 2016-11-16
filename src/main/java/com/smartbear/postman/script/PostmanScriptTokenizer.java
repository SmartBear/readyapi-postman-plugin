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

import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;

import java.util.LinkedList;
import java.util.regex.Matcher;

public class PostmanScriptTokenizer {

    public LinkedList<Token> tokenize(final String scriptToParse) throws SoapUIException {
        LinkedList<Token> tokens = new LinkedList<>();
        if (StringUtils.isNullOrEmpty(scriptToParse)) {
            return tokens;
        }
        String script = scriptToParse.trim();
        int nextTokenPosition = 0;
        int lastTokenPosition = -1;

        while (nextTokenPosition < script.length()) {
            String remainedScript = script.substring(nextTokenPosition);

            for (TokenType tokenType : TokenType.values()) {
                Matcher matcher = tokenType.getPattern().matcher(remainedScript);
                if (matcher.find()) {
                    lastTokenPosition = nextTokenPosition;

                    String sequence = matcher.group().trim();
                    tokens.add(new Token(tokenType, sequence));

                    nextTokenPosition = lastTokenPosition + matcher.end();
                    if (nextTokenPosition == lastTokenPosition) {
                        throw new SoapUIException("Unexpected character in input: " + script);
                    }
                    break;
                }
            }
        }

        return tokens;
    }

    public static class Token {
        private final TokenType type;
        private final String sequence;

        public Token(TokenType type, String sequence) {
            this.type = type;
            this.sequence = sequence;
        }

        public TokenType getType() {
            return type;
        }

        public String getSequence() {
            return sequence;
        }
    }

}
