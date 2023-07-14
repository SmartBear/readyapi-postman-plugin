package com.smartbear.postman.collection;

import com.smartbear.postman.ScriptType;
import net.sf.json.JSONObject;

import java.util.List;

class RequestV1 implements Request {
    private final JSONObject request;

    public RequestV1(JSONObject request) {
        this.request = request;
    }

    @Override
    public String getUrl() {
        return PostmanCollection.getValue(request, PostmanCollection.URL);
    }

    @Override
    public String getName() {
        return PostmanCollection.getValue(request, PostmanCollection.NAME);
    }

    @Override
    public String getMethod() {
        return PostmanCollection.getValue(request, PostmanCollection.METHOD);
    }

    @Override
    public String getDescription() {
        return PostmanCollection.getValue(request, PostmanCollection.DESCRIPTION);
    }

    @Override
    public String getPreRequestScript() {
        return PostmanCollectionV1.getScript(request, ScriptType.PRE_REQUEST);
    }

    @Override
    public String getTests() {
        return PostmanCollectionV1.getScript(request, ScriptType.TESTS);
    }

    @Override
    public List<PostmanCollection.Header> getHeaders() {
        return PostmanCollection.createHeaderList(PostmanCollection.getValue(request, PostmanCollectionV1.HEADERS));
    }

    @Override
    public String getBody() {
        return PostmanCollection.getValue(request, PostmanCollectionV1.RAW_MODE_DATA);
    }

    @Override
    public String getMode() {
        return null;
    }

    @Override
    public String getGraphQlQuery() {
        return null;
    }

    @Override
    public String getGraphQlVariables() {
        return null;
    }
}
