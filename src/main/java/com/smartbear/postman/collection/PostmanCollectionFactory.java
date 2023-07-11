package com.smartbear.postman.collection;

import com.smartbear.postman.collection.v1.PostmanCollectionV1;
import com.smartbear.postman.collection.v2.PostmanCollectionV2;
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
