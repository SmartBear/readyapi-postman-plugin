package com.smartbear.postman;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.model.ModelItem;
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
    public void perform(Workspace workspace, Object o) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
//            dialog.setValue(Form.TYPE, RESOURCE_LISTING_TYPE);
        } else {
            dialog.setValue(Form.POSTMAN_COLLECTION_FILE, "");
        }

        while (dialog.show()) {
            try {
                // get the specified URL
                String fieldValue = dialog.getValue(Form.POSTMAN_COLLECTION_FILE);
                if (StringUtils.hasContent(fieldValue)) {
                    String filePath = fieldValue.trim();
                    if (StringUtils.hasContent(filePath)) {
//                    // expand any property-expansions
//                    String expUrl = PathUtils.expandPath(filePath, project);

                        // if this is a file - convert it to a file URL
                        if (new File(filePath).exists()) {
                            PostmanImporter importer = new PostmanImporter();
                            WsdlProject project = importer.importPostmanCollection(filePath);
                            workspace.addProject(project);
                        }

//                    SwaggerImporter importer = SwaggerUtils.importSwaggerFromUrl(project, expUrl, dialog.getValue(Form.TYPE).equals(RESOURCE_LISTING_TYPE));
//                    Analytics.trackAction("ImportSwagger", "Importer", importer.getClass().getSimpleName());
                        break;
                    }
                }
            } catch (Exception ex) {
                UISupport.showErrorMessage(ex);
            }
        }
    }

    @AForm(name = "Import Postman Collection", description = "Creates a project from the specified Postman collection")
    public interface Form {
        @AField(name = "Postman Collection", description = "Location or Postman collection file", type = AFieldType.FILE)
        String POSTMAN_COLLECTION_FILE = "Postman Collection";

//        @AField(name = "Definition Type", description = "Resource Listing or API Declaration",
//                type = AFieldType.RADIOGROUP, values = {RESOURCE_LISTING_TYPE, API_DECLARATION_TYPE})
//        public final static String TYPE = "Definition Type";
    }
}

