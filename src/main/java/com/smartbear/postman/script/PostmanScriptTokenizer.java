package com.smartbear.postman.script;

import com.eviware.soapui.support.StringUtils;
import com.smartbear.ready.core.exception.ReadyApiException;

import java.util.LinkedList;
import java.util.regex.Matcher;

public class PostmanScriptTokenizer {

    public LinkedList<Token> tokenize(final String scriptToParse) throws ReadyApiException {
        LinkedList<Token> tokens = new LinkedList<>();
        if (StringUtils.isNullOrEmpty(scriptToParse)) {
            return tokens;
        }
        String script = scriptToParse.trim();
        int nextTokenPosition = 0;
        int lastTokenPosition = -1;

        while (nextTokenPosition < script.length()) {
            boolean hadMatch = false;

            for (TokenType tokenType : TokenType.values()) {
                Matcher matcher = tokenType.getPattern().matcher(script.substring(nextTokenPosition));
                if (matcher.find()) {
                    hadMatch = true;
                    lastTokenPosition = nextTokenPosition;

                    String sequence = matcher.group().trim();
                    tokens.add(new Token(tokenType, sequence));

                    nextTokenPosition = lastTokenPosition + matcher.end();
                    break;
                }
            }

            if (!hadMatch) {
                throw new ReadyApiException("Unexpected character in input: " + script);
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
