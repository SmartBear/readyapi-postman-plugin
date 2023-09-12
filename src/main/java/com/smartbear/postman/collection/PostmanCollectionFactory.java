package com.smartbear.postman.collection;

import com.smartbear.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import static com.smartbear.postman.exceptions.PostmanCollectionUnsupportedVersionException.UNSUPPORTED_VERSION_MESSAGE;

public class PostmanCollectionFactory {
    public static final String INFO = "info";
    public static final String COLLECTION = "collection";
    public static final String SCHEMA = "schema";
    public static final String VERSION_2 = "2.0.0";
    public static final String VERSION_2_1 = "2.1.0";

    public static PostmanCollection getCollection(JSONObject collectionObject) throws PostmanCollectionUnsupportedVersionException {
        Object collectionSharedViaAPI = collectionObject.get(COLLECTION);
        if (collectionSharedViaAPI != null) {
            collectionObject = (JSONObject) collectionObject.get(COLLECTION);
        }
        return getCollectionByVersion(collectionObject);
    }

    private static PostmanCollection getCollectionByVersion(JSONObject collectionObject) throws PostmanCollectionUnsupportedVersionException {
        Object infoSection = collectionObject.get(INFO);
        if (infoSection != null) {
            Object schemaField = ((JSONObject) infoSection).get(SCHEMA);
            if (schemaField instanceof String) {
                String collectionVersion = StringUtils
                        .substringBetween((String) schemaField, "collection/v", "/collection.json");
                if (VERSION_2.equals(collectionVersion) || VERSION_2_1.equals(collectionVersion)) {
                    return new PostmanCollectionV2(collectionObject);
                }
            }
        }
        throw new PostmanCollectionUnsupportedVersionException(UNSUPPORTED_VERSION_MESSAGE);
    }
}
