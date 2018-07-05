package com.smartbear.postman;

public enum ScriptType {
    PRE_REQUEST("preRequestScript", "prerequest"),
    TESTS("tests", "test");

    private final String requestElement;
    private final String listenType;

    private ScriptType(String requestElement, String listenType) {
        this.requestElement = requestElement;
        this.listenType = listenType;
    }

    public String getRequestElement() {
        return requestElement;
    }

    public String getListenType() {
        return listenType;
    }
}
