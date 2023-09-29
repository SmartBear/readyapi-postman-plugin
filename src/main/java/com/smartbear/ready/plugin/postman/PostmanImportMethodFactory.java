package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportMethod;
import com.eviware.soapui.impl.actions.ImportMethodFactory;
import com.eviware.soapui.support.action.SoapUIAction;

public class PostmanImportMethodFactory implements ImportMethodFactory {

    @Override
    public ImportMethod createNewImportMethod() {
        return new PostmanImportMethod();
    }

    public static class PostmanImportMethod implements ImportMethod {
        public static final String LABEL = "Postman collection";

        @Override
        public SoapUIAction<WorkspaceImpl> getImportAction() {
            return new ImportPostmanCollectionAction();
        }

        @Override
        public String getLabel() {
            return LABEL;
        }
    }
}