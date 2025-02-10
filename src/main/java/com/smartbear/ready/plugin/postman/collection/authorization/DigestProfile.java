package com.smartbear.ready.plugin.postman.collection.authorization;

public record DigestProfile(String username, String password) implements PostmanAuthProfile {
}


