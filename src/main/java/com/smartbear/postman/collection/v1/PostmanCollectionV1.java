package com.smartbear.postman.collection.v1;

import com.eviware.soapui.support.StringUtils;
import com.smartbear.postman.ScriptType;
import javax.annotation.Nullable;

import com.smartbear.postman.collection.PostmanCollection;
import com.smartbear.postman.collection.Request;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostmanCollectionV1 extends PostmanCollection {
    public static final String REQUESTS = "requests";
    public static final String EVENTS = "events";
    public static final String HEADERS = "headers";
    public static final String RAW_MODE_DATA = "rawModeData";
    public static final String FOLDERS = "folders";

    public PostmanCollectionV1(JSONObject postmanCollection) {
        super(postmanCollection);
    }

    @Override
    public String getName() {
        return getValue(postmanCollection, NAME);
    }

    @Override
    public String getDescription() {
        return getValue(postmanCollection, DESCRIPTION);
    }

    @Override
    public List<Request> getRequests() {
        ArrayList<Request> requestList = new ArrayList<>();
        JSONArray requests = postmanCollection.getJSONArray(REQUESTS);
        for (Object requestObject : requests) {
            if (requestObject instanceof JSONObject) {
                requestList.add(new RequestV1((JSONObject) requestObject));
            }
        }
        return requestList;
    }

    @Override
    public List<JSONObject> getFolders() {
        ArrayList<JSONObject> foldersList = new ArrayList<>();
        JSONArray folders = postmanCollection.getJSONArray(FOLDERS);
        for (Object folder : folders) {
            if (folder instanceof JSONObject) {
                foldersList.add((JSONObject) folder);
            }
        }
        return foldersList;
    }

    @Override
    @Nullable
    public List<Variable> getVariables() {
        return null;
    }

    public static String getScript(JSONObject request, ScriptType scriptType) {
        String eventScript = getEventScript(request, scriptType, EVENTS);
        if (StringUtils.hasContent(eventScript)) {
            return eventScript;
        } else {
            return getValue(request, scriptType.getRequestElement());
        }
    }

}
