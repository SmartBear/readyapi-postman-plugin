package com.smartbear.postman.exceptions;

public class PostmanCollectionUnsupportedVersionException extends Exception {

    public static final String UNSUPPORTED_VERSION_MESSAGE = "Unsupported version of Postman collection was provided";

    public PostmanCollectionUnsupportedVersionException(String msg) {
        super(msg);
    }
}
