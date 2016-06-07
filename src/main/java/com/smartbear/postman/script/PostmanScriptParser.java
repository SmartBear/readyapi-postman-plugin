package com.smartbear.postman.script;

import com.smartbear.postman.script.PostmanScriptTokenizer.Token;
import com.smartbear.ready.core.exception.ReadyApiException;

import java.util.LinkedList;

public class PostmanScriptParser {
    public static final String POSTMAN_OBJECT = "postman";
    public static final String TEST_LIST = "tests";

    private LinkedList<Token> tokens;
    private ScriptContext context;
    private PostmanObject currentObject;
    private ScriptCommand currentCommand;
    private String currentAssertionName;
    private Token lookahead;

    public void parse(LinkedList<Token> tokens, ScriptContext context) throws ReadyApiException {
        this.tokens = (LinkedList<Token>) tokens.clone();
        this.context = context;

        lookahead = tokens.getFirst();

        while (!tokens.isEmpty()) {
            command();
            if (lookahead.getType() == TokenType.END_OF_COMMAND) {
                nextToken();
            } else {
                break;
            }
        }

        if (lookahead.getType() != TokenType.END_OF_SCRIPT) {
            throw new ReadyApiException("Unexpected symbol is found:" + lookahead.getSequence());
        }
    }

    private void command() {
        if (TokenType.OBJECT == lookahead.getType()) {
            if (TEST_LIST.equals(lookahead.getSequence())) {
                nextToken();
                assertionDeclaration();
            } else {
                object();
            }
        }
    }

    private Object object() {
        currentObject = context.getObject(lookahead.getSequence());
        nextToken();
        return memberCall();
    }

    private Object memberCall() {
        if (TokenType.DOT == lookahead.getType()) {
            nextToken();
            memberName();
            argumentList();
            return executeCurrentCommand();
        } else {
            if (currentObject != null && currentObject.hasDefaultCommand()) {
                currentCommand = currentObject.getDefaultCommand();
                currentCommand.prepare();
            }
        }
        return null;
    }

    private Object executeCurrentCommand() {
        Object result = null;
        if (currentCommand != null && currentCommand.validate()) {
            result = currentCommand.execute();
        }
        return result;
    }

    private void memberName() {
        if (TokenType.METHOD_OR_FIELD == lookahead.getType()) {
            if (currentObject != null) {
                currentCommand = currentObject.getCommand(lookahead.getSequence());
                currentCommand.prepare();
            }
            nextToken();
        }
    }

    private void argumentList() {
        if (TokenType.OPEN_ROUND_BRACKET == lookahead.getType()) {
            nextToken();
            arguments();
            if (TokenType.CLOSE_ROUND_BRACKET == lookahead.getType()) {
                nextToken();
            }
        }
    }

    private void arguments() {
        argument();
        moreArguments();
    }

    private void argument() {
        if (TokenType.STRING == lookahead.getType() || TokenType.NUMBER == lookahead.getType()) {
            if (currentCommand != null) {
                currentCommand.addArgument(lookahead.getType(), lookahead.getSequence());
            }
        }
        nextToken();
    }

    private void moreArguments() {
        if (TokenType.COMMA == lookahead.getType()) {
            nextToken();
            arguments();
        }
    }

    private void assertionDeclaration() {
        if (TokenType.OPEN_SQUARE_BRACKET == lookahead.getType()) {
            nextToken();
            assertionName();
            if (TokenType.CLOSE_SQUARE_BRACKET == lookahead.getType()) {
                nextToken();
                if (TokenType.ASSIGN == lookahead.getType()) {
                    nextToken();
                    assertions();
                }
            }
        }
    }

    private void assertionName() {
        if (TokenType.STRING == lookahead.getType()) {
            currentAssertionName = lookahead.getSequence();
            nextToken();
        }
    }

    private void assertions() {
        assertion();
        moreAssertions();
    }

    private void assertion() {
        if (TokenType.OBJECT == lookahead.getType()) {
            object();
            condition();
            executeCurrentCommand();
        }
    }

    private void condition() {
        if (TokenType.EQUALS == lookahead.getType()) {
            if (currentCommand instanceof AddAssertionCommand) {
                ((AddAssertionCommand) currentCommand).addCondition(lookahead.getSequence());
            }
            nextToken();
            argument();
        }
    }

    private void moreAssertions() {
        if (TokenType.LOGIC == lookahead.getType()) {
            nextToken();
            assertions();
        }
    }

    private void nextToken() {
        do {
            tokens.pop();
            // at the end of input we return an epsilon token
            if (tokens.isEmpty()) {
                lookahead = new Token(TokenType.END_OF_SCRIPT, "");
            } else {
                lookahead = tokens.getFirst();
            }
        } while (lookahead.getType() == TokenType.NEW_LINE);
    }
}
