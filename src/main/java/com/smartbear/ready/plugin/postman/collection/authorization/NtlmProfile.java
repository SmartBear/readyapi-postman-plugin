package com.smartbear.ready.plugin.postman.collection.authorization;

public record NtlmProfile(String username, String password, String domain) implements PostmanAuthProfile {
}