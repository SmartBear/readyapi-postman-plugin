package com.smartbear.ready.plugin.postman.collection.authorization;

public record BasicAuthProfile (String username, String password) implements PostmanAuthProfile {

}