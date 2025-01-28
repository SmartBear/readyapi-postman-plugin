package com.smartbear.ready.plugin.postman.collection.environment;

import java.util.List;

public class PostmanEnvModel {
    private String id;
    private String name;
    private List<PostmanEnvVariable> values;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PostmanEnvVariable> getValues() {
        return values;
    }

    public void setValues(List<PostmanEnvVariable> values) {
        this.values = values;
    }
}