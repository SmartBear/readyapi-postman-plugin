package com.smartbear.ready.plugin.postman.collection;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostmanCollectionV2 extends PostmanCollection {
    public static final String INFO = "info";
    public static final String ITEM = "item";
    public static final String REQUEST = "request";
    public static final String HEADER = "header";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String BODY = "body";
    public static final String MODE = "mode";
    public static final String RAW = "raw";
    public static final String GRAPHQL_QUERY = "query";
    public static final String GRAPHQL_VARIABLES = "variables";
    public static final String EVENT = "event";
    public static final String VARIABLE = "variable";
    public static final String VARIABLE_ID = "id";
    public static final String VARIABLE_KEY = "key";
    public static final String VARIABLE_VALUE = "value";
    public static final String VARIABLE_TYPE = "type";

    private final JSONObject info;

    public PostmanCollectionV2(JSONObject postmanCollection) {
        super(postmanCollection);
        info = postmanCollection.getJSONObject(INFO);
    }

    @Override
    public String getName() {
        return getValue(info, NAME);
    }

    @Override
    public String getDescription() {
        return getValue(info, DESCRIPTION);
    }

    @Override
    public List<Request> getRequests() {
        ArrayList<Request> requestList = new ArrayList<>();
        extractRequestsFromItems(postmanCollection.getJSONArray(ITEM), requestList, DirectoryInfo.createRoot(getName()));
        return requestList;
    }

    @Override
    public List<JSONObject> getFolders() {
        ArrayList<JSONObject> foldersList = new ArrayList<>();
        extractFoldersFromItems(postmanCollection.getJSONArray(ITEM), foldersList);
        return foldersList;
    }

    @Override
    public List<Variable> getVariables() {
        ArrayList<Variable> variablesList = new ArrayList<>();
        try {
            extractVariablesFromItems(postmanCollection.getJSONArray(VARIABLE), variablesList);
        } catch (JSONException e) {

        }
        return variablesList;
    }

    private boolean isFolder(JSONObject item) {
        return item.containsKey(ITEM);
    }

    private void extractRequestsFromItems(JSONArray items, List<Request> requestList, DirectoryInfo directoryInfo) {
        for (Object itemObject : items) {
            if (itemObject instanceof JSONObject) {
                JSONObject item = (JSONObject) itemObject;
                if (isFolder(item)) {
                    extractRequestsFromItems(item.getJSONArray(ITEM), requestList, new DirectoryInfo(item.getString(NAME), item.get(DESCRIPTION) != null ? item.getString(DESCRIPTION) : "", directoryInfo));
                } else {
                    requestList.add(new RequestV2(item, directoryInfo));
                }
            }
        }
    }

    private void extractFoldersFromItems(JSONArray items, List<JSONObject> foldersList) {
        for (Object itemObject : items) {
            if (itemObject instanceof JSONObject) {
                JSONObject item = (JSONObject) itemObject;
                if (isFolder(item)) {
                    foldersList.add(item);
                    extractFoldersFromItems(item.getJSONArray(ITEM), foldersList);
                }
            }
        }
    }

    private void extractVariablesFromItems(JSONArray items, List<Variable> variablesList) {
        for (Object itemObject : items) {
            if (itemObject instanceof JSONObject) {
                JSONObject item = (JSONObject) itemObject;
                variablesList.add(new VariableImpl(item));
            }
        }
    }

    private class VariableImpl implements Variable {
        private final JSONObject item;

        public VariableImpl(JSONObject item) {
            this.item = item;
        }

        @Override
        public String getId() {
            return PostmanCollectionV2.this.getValue(item, VARIABLE_ID);
        }

        @Override
        public String getKey() {
            return PostmanCollectionV2.this.getValue(item, VARIABLE_KEY);
        }

        @Override
        public String getValue() {
            return PostmanCollectionV2.this.getValue(item, VARIABLE_VALUE);
        }

        @Override
        public String getType() {
            return PostmanCollectionV2.this.getValue(item, VARIABLE_TYPE);
        }
    }
}
