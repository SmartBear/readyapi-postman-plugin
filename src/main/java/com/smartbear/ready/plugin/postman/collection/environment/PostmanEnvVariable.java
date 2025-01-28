package com.smartbear.ready.plugin.postman.collection.environment;

public class PostmanEnvVariable {
    private String key;
    private String value;
    private String type;
    private Boolean enabled;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSecret() {
        return "secret".equals(type);
    }
}