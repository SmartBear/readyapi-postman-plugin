package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestRequest;

public interface TestCreator {
    void createTest(RestRequest request);
}
