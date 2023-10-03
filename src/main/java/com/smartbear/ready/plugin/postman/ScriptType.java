package com.smartbear.ready.plugin.postman;

/**
 * Enum that keeps two aliases for scripts in Postman collection files:
 *      requestElement - for "old" v1 files, where script is an element of "request" node,
 *      listenType - for "new" v1 files, where script in enclosed in "events" node with corresponding value of "listen" node
 */
public enum ScriptType {
    PRE_REQUEST("preRequestScript", "prerequest"),
    TESTS("tests", "test");

    private final String requestElement;
    private final String listenType;

    ScriptType(String requestElement, String listenType) {
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
