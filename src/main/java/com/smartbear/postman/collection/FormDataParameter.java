package com.smartbear.postman.collection;

public class FormDataParameter {
    protected enum FormDataType {
        FILE,
        TEXT
    }

    private final FormDataType type;
    private final String key;
    private final String value;

    public FormDataParameter(FormDataType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public boolean isFile() {
        return getType() == FormDataType.FILE;
    }

    public FormDataType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
