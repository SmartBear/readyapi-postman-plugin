package com.smartbear.postman.script;

import com.smartbear.postman.script.PostmanScriptTokenizer.Token;
import com.smartbear.ready.core.exception.ReadyApiException;

import java.util.LinkedList;
import java.util.Stack;

public class PostmanScriptParser {
    public static final String POSTMAN_OBJECT = "postman";
    public static final String TEST_LIST = "tests";

    private LinkedList<Token> tokens;
    private ScriptContext context;
    private Stack<ParserState> stateStack = new Stack<>();
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
        pushState();
        setCurrentObject(context.getObject(lookahead.getSequence()));
        nextToken();
        return memberCall();
    }

    private Object memberCall() {
        if (TokenType.DOT == lookahead.getType()) {
            nextToken();
            memberName();
            argumentList();
            return executeCurrentCommand();
        } else if (TokenType.OPEN_SQUARE_BRACKET == lookahead.getType()) {
            prepareCommand(lookahead.getSequence());
            nextToken();
            argument();
            if (TokenType.CLOSE_SQUARE_BRACKET == lookahead.getType()) {
                nextToken();
                return executeCurrentCommand();
            }
        } else {
            prepareDefaultCommand();
        }
        return null;
    }

    private Object executeCurrentCommand() {
        Object result = null;
        ScriptCommand command = getCurrentCommand();
        if (command != null && command.validate()) {
            result = command.execute();
            popState();
        }
        return result;
    }

    private void memberName() {
        if (TokenType.METHOD_OR_FIELD == lookahead.getType()) {
            prepareCommand(lookahead.getSequence());
            nextToken();
        }
    }

    private void prepareCommand(String commandName) {
        PostmanObject object = getCurrentObject();
        if (object != null) {
            ScriptCommand command = object.getCommand(commandName);
            setCurrentCommand(command);
            command.prepare();
        }
    }

    private void prepareDefaultCommand() {
        PostmanObject object = getCurrentObject();
        if (object != null && object.hasDefaultCommand()) {
            ScriptCommand command = object.getDefaultCommand();
            setCurrentCommand(command);
            command.prepare();
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
            if (getCurrentCommand() != null) {
                getCurrentCommand().addArgument(lookahead.getType(), lookahead.getSequence());
            }
        } else if (TokenType.OBJECT == lookahead.getType()) {
            Object result = object();
            if (getCurrentCommand() != null && result != null) {
                getCurrentCommand().addArgument(lookahead.getType(), result.toString());
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
            setCurrentAssertionName(lookahead.getSequence());
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
            if (getCurrentCommand() instanceof AddAssertionCommand) {
                ((AddAssertionCommand) getCurrentCommand()).addCondition(lookahead.getSequence());
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

    private void pushState() {
        stateStack.push(new ParserState());
    }

    private ParserState popState() {
        return stateStack.pop();
    }

    public PostmanObject getCurrentObject() {
        if (stateStack.isEmpty()) {
            return null;
        }
        return stateStack.peek().object;
    }

    public void setCurrentObject(PostmanObject currentObject) {
        if (!stateStack.isEmpty()) {
            stateStack.peek().object = currentObject;
        }
    }

    public ScriptCommand getCurrentCommand() {
        if (stateStack.isEmpty()) {
            return null;
        }
        return stateStack.peek().command;
    }

    public void setCurrentCommand(ScriptCommand currentCommand) {
        if (!stateStack.isEmpty()) {
            ParserState currentState = stateStack.peek();
            if (currentState.command == null) {
                currentState.command = currentCommand;
            } else {
                ParserState state = new ParserState();
                state.object = currentState.object;
                state.command = currentCommand;
                stateStack.push(state);
            }
        }
    }

    public String getCurrentAssertionName() {
        if (stateStack.isEmpty()) {
            return null;
        }
        return stateStack.peek().currentAssertionName;
    }

    public void setCurrentAssertionName(String currentAssertionName) {
        if (!stateStack.isEmpty()) {
            stateStack.peek().currentAssertionName = currentAssertionName;
        }
    }

    private static class ParserState {
        public PostmanObject object;
        public ScriptCommand command;
        public String currentAssertionName;
    }
}
