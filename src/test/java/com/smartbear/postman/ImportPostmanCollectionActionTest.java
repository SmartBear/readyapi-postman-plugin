package com.smartbear.postman;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.PropertiesStepFactory;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;

import java.io.IOException;

import static com.smartbear.postman.ImportPostmanCollectionAction.getTestStepsAmount;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ImportPostmanCollectionActionTest {

    @Test
    public void testStepsAmountCalculation() throws XmlException, IOException, SoapUIException {
        WsdlProject project = new WsdlProjectFactory().createNew();
        createTestSteps(project);

        assertThat(getTestStepsAmount(project), is(8));
    }

    private static void createTestSteps(WsdlProject project) {
        for (int ts = 0; ts < 2; ts++) {
            WsdlTestSuite testSuite = project.addNewTestSuite(String.format("test suite %d", ts));
            for (int tc = 0; tc < 2; tc++) {
                WsdlTestCase testCase1 = testSuite.addNewTestCase(String.format("test case %d", tc));
                for (int testStep = 0; testStep < 2; testStep++) {
                    testCase1.addTestStep(PropertiesStepFactory.PROPERTIES_TYPE, String.format("test step %d", testStep));
                }
            }
        }
    }
}