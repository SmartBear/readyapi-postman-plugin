package com.smartbear.postman.collection;

import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;

import java.util.List;

public interface Request {
    String getMode();

    String getUrl();

    String getName();

    String getMethod();

    String getDescription();

    String getPreRequestScript();

    String getTests();

    List<PostmanCollection.Header> getHeaders();

    String getBody();

    String getGraphQlQuery();

    String getGraphQlVariables();

    default boolean isSoap() {
        return false;
    }

    default SoapVersion getSoapVersion() {
        return null;
    }

    default DirectoryInfo getFolderInfo() {
        return null;
    }
}
