package com.smartbear.ready.plugin.postman.collection;

import com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import net.sf.json.JSONObject;
import java.util.Optional;
import static com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException.UNSUPPORTED_VERSION_MESSAGE;
import static com.smartbear.ready.plugin.postman.utils.PostmanCollectionUtils.getCollectionVersionFromInfo;

public class PostmanCollectionFactory {
    public static final String INFO = "info";
    public static final String COLLECTION = "collection";

    public static PostmanCollection getCollection(JSONObject collectionObject) throws PostmanCollectionUnsupportedVersionException {
        Object collectionSharedViaAPI = collectionObject.get(COLLECTION);
        if (collectionSharedViaAPI != null) {
            collectionObject = (JSONObject) collectionObject.get(COLLECTION);
        }
        return getCollectionByVersion(collectionObject);
    }

    private static PostmanCollection getCollectionByVersion(JSONObject collectionObject) throws PostmanCollectionUnsupportedVersionException {
        Object infoSection = collectionObject.get(INFO);
        return getCollectionVersionFromInfo(infoSection)
                .map(version -> new PostmanCollectionV2(collectionObject))
                .orElseThrow(() -> new PostmanCollectionUnsupportedVersionException(UNSUPPORTED_VERSION_MESSAGE));
    }
}
