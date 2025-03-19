package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VariableUtilsTest {

    private static final String ENDPOINT = "http://test:1234?time=";
    private static final String DYNAMIC_VARIABLE_ENDPOINT = ENDPOINT + "{{$timestamp}}";
    private static final String DYNAMIC_VARIABLE_NAME = "dynamic-variable-timestamp";
    private static final String REGULAR_VARIABLE_ENDPOINT = ENDPOINT + "{{timestamp}}";
    private static final String REGULAR_VARIABLE_NAME = "timestamp";

    private WsdlProject project;

    @BeforeEach
    void setUp() {
        project = new WsdlProject();
    }

    @Test
    void convertPostmanVariableToPropertyExpansion() {
        String convertedUrl = VariableUtils.convertVariables(REGULAR_VARIABLE_ENDPOINT, project);
        assertEquals(ENDPOINT + "${#Project#" + REGULAR_VARIABLE_NAME + "}", convertedUrl);
        assertNotNull(project.getProperty(REGULAR_VARIABLE_NAME));
    }

    @Test
    void convertPostmanDynamicVariableToPropertyExpansion() {
        String convertedUrl = VariableUtils.convertVariables(DYNAMIC_VARIABLE_ENDPOINT, project);
        assertEquals(ENDPOINT + "${#Project#" + DYNAMIC_VARIABLE_NAME + "}", convertedUrl);
        assertNotNull(project.getProperty(DYNAMIC_VARIABLE_NAME));
    }
}
