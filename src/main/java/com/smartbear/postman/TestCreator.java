package com.smartbear.postman;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;

public interface TestCreator {

    void createTest(RestRequest request);

    void createTest(WsdlRequest request);
}
