package com.smartbear.ready.plugin.postman.collection.environment;

public record PostmanEnvVariable (String key, String value, String type, Boolean enabled) {

    public boolean isSecret() {
        return "secret".equals(type);
    }
}