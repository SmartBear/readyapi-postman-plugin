package com.smartbear.postman;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.io.File;

@ActionConfiguration(actionGroup = "WorkspaceImplActions", afterAction = "ImportProjectFromVcsAction", separatorBefore = true)
public class ImportPostmanCollectionAction extends AbstractSoapUIAction<Workspace> {
    private XFormDialog dialog;

    public ImportPostmanCollectionAction() {
        super("Import Postman Collection", "Imports a Postman collection into Ready! API");
    }

    @Override
    public void perform(Workspace workspace, Object param) {
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
                        if (new File(filePath).exists()) {
                            PostmanImporter importer = new PostmanImporter(new GuiTestCreator());
                            WsdlProject project = importer.importPostmanCollection(filePath);
                            workspace.addProject(project);
                        }
                        break;
                    }
                }
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    @AForm(name = "Import Postman Collection", description = "Create a project from the specified Postman collection")
    public interface Form {
        @AField(name = "Postman Collection", description = "Location or Postman collection file", type = AFieldType.FILE)
        String POSTMAN_COLLECTION_FILE = "Postman Collection";

    }
}

