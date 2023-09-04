package com.smartbear.postman.collection;

import com.eviware.soapui.support.UISupport;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostmanCollectionFactory {
    public static final String INFO = "info";
    public static final String COLLECTION = "collection";
    public static final String VERSION_2 = "2.0.0";
    public static final String VERSION_2_1 = "2.1.0";
    private static final String V1_IS_DEPRECATED_ERROR_MESSAGE =
            "Postman Collections in v1 format are no longer supported. " +
            "Convert your collection to version v2 or higher and try importing again.";
    private static final Logger logger = LoggerFactory.getLogger(PostmanCollectionFactory.class);

    public static PostmanCollection getCollection(JSONObject collectionObject) {
        Object collectionSharedViaAPI = collectionObject.get(COLLECTION);
        if (collectionSharedViaAPI != null) {
            collectionObject = (JSONObject) collectionObject.get(COLLECTION);
        }

        Object infoSection = collectionObject.get(INFO);
        if (infoSection != null) {
            return getCollectionByVersion(collectionObject, (JSONObject) infoSection);
        } else {
            // info section is not present for collections of version 1.0.0
            UISupport.showErrorMessage(V1_IS_DEPRECATED_ERROR_MESSAGE);
            return null;
        }
    }

    private static PostmanCollection getCollectionByVersion(JSONObject collectionObject, JSONObject infoSection) {
        Object schemaField = infoSection.get("schema");
        if (schemaField instanceof String) {
            String collectionVersion = StringUtils
                    .substringBetween((String) schemaField, "collection/v", "/collection.json");
            if (VERSION_2.equals(collectionVersion)) {
                return new PostmanCollectionV2(collectionObject);
            } else if (VERSION_2_1.equals(collectionVersion)) {
                return new PostmanCollectionV2_1(collectionObject);
            } else {
                logger.error("Unsupported version {} of Postman collection is provided", collectionVersion);
            }
        }
        return null;
    }
}
