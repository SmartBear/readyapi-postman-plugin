package com.smartbear.ready.plugin.postman.collection.authorization;

public record OAuth1Profile(String consumerSecret, String consumerKey, String token, String tokenSecret, String callback) implements PostmanAuthProfile {
}