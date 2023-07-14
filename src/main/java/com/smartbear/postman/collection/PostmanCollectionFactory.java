package com.smartbear.postman.collection;

import net.sf.json.JSONObject;

public class PostmanCollectionFactory {
    public static final String INFO = "info";

    public static PostmanCollection getCollection(JSONObject collectionObject) {
        Object value = collectionObject.get(INFO);
        if (value == null) {
            return new PostmanCollectionV1(collectionObject);
        } else {
            return new PostmanCollectionV2(collectionObject);
        }
    }
}
