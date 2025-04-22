package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.actions.environment.AbstractNewEnvironmentAction;
import com.eviware.soapui.impl.wsdl.actions.environment.ImportEnvironment;
import com.eviware.soapui.impl.wsdl.actions.environment.ImportEnvironmentFactory;
import com.eviware.soapui.support.svg.SVGCollectionManager;

public class PostmanImportEnvFactory implements ImportEnvironmentFactory {

    @Override
    public ImportEnvironment createNewEnvironmentImportMethod() {
        return new PostmanImportEnvFactory.PostmanEnvironmentImportMethod();
    }

    public static class PostmanEnvironmentImportMethod implements ImportEnvironment {
        private static final String LABEL = "Import Postman environment variables";
        private static final String SHORT_DESCRIPTION = "Adds a new Environment for the Project based on Postman export file";

        @Override
        public AbstractNewEnvironmentAction getImportEnvironmentAction(WsdlProjectPro project) {
            return new ImportPostmanEnvironmentAction(project);
        }

        @Override
        public String getShortDescription() {
            return SHORT_DESCRIPTION;
        }

        @Override
        public String getButtonIconPath() {
            return SVGCollectionManager.LOAD_FROM_FILE_PATH;
        }

        @Override
        public String getLabel() {
            return LABEL;
        }
    }
}
