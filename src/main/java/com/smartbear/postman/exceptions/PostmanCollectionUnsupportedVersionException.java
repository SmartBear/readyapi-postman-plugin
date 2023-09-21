package com.smartbear.postman.exceptions;

public class PostmanCollectionUnsupportedVersionException extends Exception {

    public static final String UNSUPPORTED_VERSION_MESSAGE = "Unsupported version of Postman collection was provided.\n" +
            "Make sure your collection version is at least 2.0.0. " +
            "Try importing the collection into Postman and exporting in one of the supported formats.\n" +
            "ReadyAPI Postman Plugin documentation: https://support.smartbear.com/readyapi/docs/integrations/postman.html";

    public PostmanCollectionUnsupportedVersionException(String msg) {
        super(msg);
    }
}
