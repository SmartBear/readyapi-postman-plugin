package com.smartbear.postman.collection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostmanCollectionV2 extends PostmanCollection {
    public static final String INFO = "info";
    public static final String ITEM = "item";
    public static final String REQUEST = "request";
    public static final String HEADER = "header";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String BODY = "body";
    public static final String RAW = "raw";

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
        JSONArray requests = postmanCollection.getJSONArray(ITEM);
        for (Object requestObject : requests) {
            if (requestObject instanceof JSONObject) {
                requestList.add(new RequestV2((JSONObject) requestObject));
            }
        }
        return requestList;
    }

    private static class RequestV2 implements Request {
        private final JSONObject item;
        private final JSONObject request;
        private String url;

        public RequestV2(JSONObject item) {
            this.item = item;
            Object requestObject = item.get(REQUEST);
            if (requestObject instanceof JSONObject) {
                request = (JSONObject) requestObject;
            } else {
                request = null;
                url = requestObject.toString();
            }
        }

        @Override
        public String getUrl() {
            return request != null ? getValue(request, URL) : url;
        }

        @Override
        public String getName() {
            return getValue(item, NAME);
        }

        @Override
        public String getMethod() {
            return request != null ? getValue(request, METHOD) : "GET";
        }

        @Override
        public String getDescription() {
            return request != null ? getValue(request, DESCRIPTION) : "";
        }

        @Override
        public String getPreRequestScript() {
            return null;
        }

        @Override
        public String getTests() {
            return null;
        }

        @Override
        public List<Header> getHeaders() {
            if (request != null) {
                Object headersObject = request.get(HEADER);
                if (headersObject instanceof JSONArray) {
                    ArrayList<Header> headerList = new ArrayList<>();
                    JSONArray headers = (JSONArray) headersObject;
                    for (Object header : headers) {
                        if (header instanceof JSONObject) {
                            JSONObject headerObject = (JSONObject) header;
                            headerList.add(new Header(getValue(headerObject, KEY), getValue(headerObject, VALUE)));
                        }
                    }
                    return headerList;
                } else if (headersObject != null){
                    String headerString = headersObject.toString();
                    return createHeaderList(headerString);
                }
            }
            return Collections.emptyList();
        }

        @Override
        public String getBody() {
            JSONObject body = null;
            if (request != null) {
                body = request.getJSONObject(BODY);
            }
            return body != null ? getValue(body, RAW) : "";
        }
    }
}
