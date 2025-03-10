package com.smartbear.ready.plugin.postman.utils;

import net.sf.json.JSONObject;

import java.util.Arrays;
import java.util.Optional;

public class PostmanCollectionUtils {
    public static final String SCHEMA = "schema";
    public static final String VERSION_2 = "v2.0.0";
    public static final String VERSION_2_1 = "v2.1.0";

    private PostmanCollectionUtils() {}

    public static Optional<String> getCollectionVersionFromInfo(Object infoObject) {
        if (infoObject != null) {
            Object schemaField = ((JSONObject) infoObject).get(SCHEMA);
            if (schemaField instanceof String schemaString) {
                return Arrays.stream(schemaString.split("/"))
                        .filter(schemaPart -> (VERSION_2.equals(schemaPart) || VERSION_2_1.equals(schemaPart)))
                        .findAny();
            }
        }
        return Optional.empty();
    }

}
