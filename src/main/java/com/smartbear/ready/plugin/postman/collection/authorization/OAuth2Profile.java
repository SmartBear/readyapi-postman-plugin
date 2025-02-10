package com.smartbear.ready.plugin.postman.collection.authorization;

public record OAuth2Profile(String clientId, String clientSecret, String scope, String state, String accessTokenUrl,
                            String refreshTokenUrl, String authUrl, String redirect_uri) implements PostmanAuthProfile {
}