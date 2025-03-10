package com.smartbear.ready.plugin.postman.collection;

import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.smartbear.ready.plugin.postman.ScriptType;
import com.smartbear.ready.plugin.postman.utils.PostmanJsonUtil;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class RequestV2 implements Request {
    private final JSONObject item;
    private final JSONObject request;
    private final DirectoryInfo directoryInfo;
    private final RequestAuthProfile authProfileWithName;
    private String url;

    public RequestV2(JSONObject item, DirectoryInfo directoryInfo) {
        this.item = item;
        Object requestObject = item.get(PostmanCollectionV2.REQUEST);
        if (requestObject instanceof JSONObject) {
            request = (JSONObject) requestObject;
        } else {
            request = null;
            url = requestObject.toString();
        }
        this.directoryInfo = directoryInfo;
        this.authProfileWithName = new RequestAuthProfile(this);
    }

    @Override
    public String getUrl() {
        if (request == null) {
            return url;
        }
        return PostmanCollection.getValueFromObjectOrString(request, PostmanCollection.URL, PostmanCollectionV2.RAW);
    }

    @Override
    public String getName() {
        return PostmanCollection.getValue(item, PostmanCollection.NAME);
    }

    @Override
    public String getMethod() {
        return request != null ? PostmanCollection.getValue(request, PostmanCollection.METHOD) : "GET";
    }

    @Override
    public String getDescription() {
        return request != null ? PostmanCollection.getValue(request, PostmanCollection.DESCRIPTION) : "";
    }

    @Override
    public String getPreRequestScript() {
        return PostmanCollection.getEventScript(item, ScriptType.PRE_REQUEST, PostmanCollectionV2.EVENT);
    }

    @Override
    public String getTests() {
        return PostmanCollection.getEventScript(item, ScriptType.TESTS, PostmanCollectionV2.EVENT);
    }

    @Override
    public List<PostmanCollection.Header> getHeaders() {
        if (request != null) {
            Object headersObject = request.get(PostmanCollectionV2.HEADER);
            if (headersObject instanceof JSONArray) {
                ArrayList<PostmanCollection.Header> headerList = new ArrayList<>();
                JSONArray headers = (JSONArray) headersObject;
                for (Object header : headers) {
                    if (header instanceof JSONObject) {
                        JSONObject headerObject = (JSONObject) header;
                        headerList.add(new PostmanCollection.Header(PostmanCollection.getValue(headerObject, PostmanCollectionV2.KEY), PostmanCollection.getValue(headerObject, PostmanCollectionV2.VALUE)));
                    }
                }
                return headerList;
            } else if (headersObject != null) {
                String headerString = headersObject.toString();
                return PostmanCollection.createHeaderList(headerString);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public String getMode() {
        if (request == null) {
            return "";
        }
        return PostmanCollection.getValueFromObjectOrString(request, PostmanCollectionV2.BODY, PostmanCollectionV2.MODE);
    }

    @Override
    public String getBody() {
        if (request == null) {
            return "";
        }
        return PostmanCollection.getValueFromObjectOrString(request, PostmanCollectionV2.BODY, getMode());
    }

    @Override
    public String getGraphQlQuery() {
        Object body = request.get(PostmanCollectionV2.BODY);
        if (body instanceof JSONObject) {
            return PostmanCollection.getValueFromObjectOrString((JSONObject) body, getMode(), PostmanCollectionV2.GRAPHQL_QUERY);
        }

        return "";
    }

    @Override
    public String getGraphQlVariables() {
        Object body = request.get(PostmanCollectionV2.BODY);
        if (body instanceof JSONObject) {
            return PostmanCollection.getValueFromObjectOrString((JSONObject) body, getMode(), PostmanCollectionV2.GRAPHQL_VARIABLES);
        }

        return "";
    }

    @Override
    public String getRequestAuth() {
        if (request == null) {
            return "";
        }
        return PostmanCollection.getValue(request, PostmanCollectionV2.AUTH_PROFILE);
    }

    @Override
    public List<FormDataParameter> getFormDataParameters() {
        List<FormDataParameter> parameters = new LinkedList<>();
        JSON dataJson = new PostmanJsonUtil().parseTrimmedText(getBody());

        Map<String, String> typeToValue = new HashMap<>();
        typeToValue.put("file", "src");
        typeToValue.put("text", "value");

        Map<String, FormDataParameter.FormDataType> stringToFormDataType = new HashMap<>();
        stringToFormDataType.put("text", FormDataParameter.FormDataType.TEXT);
        stringToFormDataType.put("file", FormDataParameter.FormDataType.FILE);

        if (dataJson instanceof JSONArray) {
            JSONArray dataArray = (JSONArray) dataJson;
            Arrays.stream(dataArray.toArray())
                    .filter(JSONObject.class::isInstance)
                    .map(d -> (JSONObject) d)
                    .forEach(data -> {
                        String key = data.getString("key");
                        String type = data.getString("type");

                        String value = data.getString(typeToValue.get(type));
                        parameters.add(new FormDataParameter(stringToFormDataType.get(type), key, value));
                    });
        }
        return parameters;
    }

    @Override
    public boolean isFormDataMode() {
        return "formdata".equals(getMode());
    }

    @Override
    public boolean isSoap() {
        return isSoapVersion(SoapVersion.Soap11) || isSoapVersion(SoapVersion.Soap12);
    }

    private boolean isSoapVersion(SoapVersion soapVersion) {
        String body = getBody();
        List<XmlError> errors = new ArrayList<>();
        soapVersion.validateSoapEnvelope(body, errors, new XmlOptions());
        return errors.isEmpty();
    }

    @Override
    public SoapVersion getSoapVersion() {
        if (isSoapVersion(SoapVersion.Soap11)) {
            return SoapVersion.Soap11;
        }
        if (isSoapVersion(SoapVersion.Soap12)) {
            return SoapVersion.Soap12;
        }

        throw new IllegalStateException("Request is not a valid SOAP request");
    }

    @Override
    public String getDirectoryPath() {
        return directoryInfo.getPath();
    }

    @Override
    public DirectoryInfo getDirectory() {
        return directoryInfo;
    }

    @Override
    public RequestAuthProfile getAuthProfileWithName() {
        return authProfileWithName;
    }
}
