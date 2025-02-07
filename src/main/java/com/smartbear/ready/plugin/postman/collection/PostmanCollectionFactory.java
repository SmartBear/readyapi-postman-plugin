package com.smartbear.ready.plugin.postman.collection;

import com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import net.sf.json.JSONObject;
import java.util.Arrays;
import java.util.Optional;
import static com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException.UNSUPPORTED_VERSION_MESSAGE;

public class PostmanCollectionFactory {
    public static final String INFO = "info";
    public static final String COLLECTION = "collection";
    public static final String SCHEMA = "schema";
    public static final String VERSION_2 = "v2.0.0";
    public static final String VERSION_2_1 = "v2.1.0";
    public static final String VERSION_REGEX = "v(?!\\.)(\\d+(\\.\\d+)+)(?![\\d\\.])$";

    public static PostmanCollection getCollection(JSONObject collectionObject) throws PostmanCollectionUnsupportedVersionException {
        Object collectionSharedViaAPI = collectionObject.get(COLLECTION);
        if (collectionSharedViaAPI != null) {
            collectionObject = (JSONObject) collectionObject.get(COLLECTION);
        }
        return getCollectionByVersion(collectionObject);
    }

    public static Optional<String> getCollectionVersionFromInfo(Object infoObject) {
        if (infoObject != null) {
            Object schemaField = ((JSONObject) infoObject).get(SCHEMA);
            if (schemaField instanceof String schemaString) {
                return Arrays.stream(schemaString.split("/"))
                        .filter(schemaPart -> schemaPart.matches(VERSION_REGEX)
                                && (VERSION_2.equals(schemaPart) || VERSION_2_1.equals(schemaPart)))
                        .findAny();
            }
        }
        return Optional.empty();
    }

    private static PostmanCollection getCollectionByVersion(JSONObject collectionObject) throws PostmanCollectionUnsupportedVersionException {
        Object infoSection = collectionObject.get(INFO);
        Optional<String> collectionVersion = getCollectionVersionFromInfo(infoSection);
        if (collectionVersion.isPresent()) {
            return new PostmanCollectionV2(collectionObject);
        }
        throw new PostmanCollectionUnsupportedVersionException(UNSUPPORTED_VERSION_MESSAGE);
    }
}
