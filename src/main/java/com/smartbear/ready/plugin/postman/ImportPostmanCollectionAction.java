/**
 *  Copyright 2016 SmartBear Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.io.File;

import static com.smartbear.ready.plugin.postman.PostmanImporter.sendAnalytics;

public class ImportPostmanCollectionAction extends AbstractSoapUIAction<WorkspaceImpl> {
    private XFormDialog dialog;

    public ImportPostmanCollectionAction() {
        super("Import Postman Collection", "Imports a Postman collection into ReadyAPI");
    }

    @Override
    public void perform(WorkspaceImpl workspace, Object param) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        } else {
            dialog.setValue(Form.POSTMAN_COLLECTION_FILE, "");
        }

        while (dialog.show()) {
            try {
                String fieldValue = dialog.getValue(Form.POSTMAN_COLLECTION_FILE);
                if (StringUtils.hasContent(fieldValue)) {
                    String filePath = fieldValue.trim();
                    if (StringUtils.hasContent(filePath)) {
                        if (new File(filePath).exists() || PathUtils.isHttpPath(filePath)) {
                            PostmanImporter importer = new PostmanImporter(new GuiTestCreator());
                            WsdlProject project = importer.importPostmanCollection(workspace, filePath);
                            if (project != null) {
                                sendAnalytics(getTestStepsAmount(project));
                                break;
                            }
                        }

                        UISupport.showErrorMessage("The \"Postman Collection\" must be an existing file or a valid URL.");
                    }
                }
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    protected static int getTestStepsAmount(WsdlProject project) {
        int testStepsAmount = 0;
        for (WsdlTestSuite testSuite : project.getTestSuiteList()) {
            for (WsdlTestCase testCase : testSuite.getTestCaseList()) {
                testStepsAmount += testCase.getTestStepCount();
            }
        }
        return testStepsAmount;
    }

    @AForm(name = "Import Postman Collection", description = "Create a project from the specified Postman collection")
    public interface Form {
        @AField(name = "Postman Collection", description = "Location or Postman collection file", type = AFieldType.FILE)
        String POSTMAN_COLLECTION_FILE = "Postman Collection";

    }
}

