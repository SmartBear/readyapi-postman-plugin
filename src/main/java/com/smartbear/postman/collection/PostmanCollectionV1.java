package com.smartbear.postman.collection;

import com.eviware.soapui.support.StringUtils;
import com.smartbear.postman.ScriptType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PostmanCollectionV1 extends PostmanCollection {
    public static final String REQUESTS = "requests";
    public static final String EVENTS = "events";
    public static final String HEADERS = "headers";
    public static final String RAW_MODE_DATA = "rawModeData";

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

    private static String getScript(JSONObject request, ScriptType scriptType) {
        String eventScript = getEventScript(request, scriptType, EVENTS);
        if (StringUtils.hasContent(eventScript)) {
            return eventScript;
        } else {
            return getValue(request, scriptType.getRequestElement());
        }
    }

    private static class RequestV1 implements Request {
        private final JSONObject request;

        public RequestV1(JSONObject request) {
            this.request = request;
        }

        @Override
        public String getUrl() {
            return getValue(request, URL);
        }

        @Override
        public String getName() {
            return getValue(request, NAME);
        }

        @Override
        public String getMethod() {
            return getValue(request, METHOD);
        }

        @Override
        public String getDescription() {
            return getValue(request, DESCRIPTION);
        }

        @Override
        public String getPreRequestScript() {
            return getScript(request, ScriptType.PRE_REQUEST);
        }

        @Override
        public String getTests() {
            return getScript(request, ScriptType.TESTS);
        }

        @Override
        public List<Header> getHeaders() {
            return createHeaderList(getValue(request, HEADERS));
        }

        @Override
        public String getBody() {
            return getValue(request, RAW_MODE_DATA);
        }
    }
}
