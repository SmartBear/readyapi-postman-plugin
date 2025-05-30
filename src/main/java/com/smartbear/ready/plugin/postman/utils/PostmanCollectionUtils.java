package com.smartbear.ready.plugin.postman.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostmanCollectionUtils {
    public static final String VERSION_2 = "v2.0.0";
    public static final String VERSION_2_1 = "v2.1.0";
    private static final String SCHEMA = "schema";
    private static final List<Pattern> VAULT_VARIABLE_REGEXES = List.of(
        Pattern.compile("\\{\\{vault:([^}]*)}}"),
        Pattern.compile("await pm.vault.get\\(\"(.*?)\"\\)")
    );

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

    public static Set<String> extractVaultVariables(JSONObject collectionJson) throws JSONException {
        Set<String> vaultVariables = new HashSet<>();
        ArrayDeque<Object> collectionNodesQueue = new ArrayDeque<>();
        collectionNodesQueue.add(collectionJson);

        while (!collectionNodesQueue.isEmpty()) {
            Object currentNode = collectionNodesQueue.poll();

            if (currentNode instanceof JSONObject jsonObject && !jsonObject.isNullObject()) {
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    collectionNodesQueue.add(jsonObject.get(keys.next()));
                }
            } else if (currentNode instanceof JSONArray jsonArray) {
                collectionNodesQueue.addAll(jsonArray);
            } else if (currentNode instanceof String string) {
                for (Pattern vaultVariablePattern: VAULT_VARIABLE_REGEXES) {
                    Matcher matcher = vaultVariablePattern.matcher(string);
                    while (matcher.find()) {
                        vaultVariables.add(matcher.group(1).trim());
                    }
                }
            }
        }
        return vaultVariables;
    }

}
