package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.impl.wsdl.actions.environment.AbstractNewEnvironmentAction;

import com.eviware.soapui.impl.wsdl.actions.environment.NewEnvironmentPropertyWrapper;
import com.eviware.soapui.model.environment.Environment;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.FileFormField;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbear.analytics.api.ParameterType;
import com.smartbear.ready.plugin.postman.collection.environment.PostmanEnvModel;
import com.smartbear.ready.plugin.postman.collection.environment.PostmanEnvVariable;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.eviware.soapui.support.StringUtils.isNullOrEmpty;

public class ImportPostmanEnvironmentAction extends AbstractNewEnvironmentAction {

    private static final Logger log = LoggerFactory.getLogger(ImportPostmanEnvironmentAction.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final WsdlProjectPro project;

    public ImportPostmanEnvironmentAction(WsdlProjectPro project) {
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        XFormDialog dialog = ADialogBuilder.buildDialog(LoadPostmanFile.class);
        ((FileFormField) dialog.getFormField(LoadPostmanFile.FILE))
                .setFileFilter(new FileChooser.ExtensionFilter("JSON file", "json"));
        if (dialog.show()) {
            String filePath = dialog.getFormField(LoadPostmanFile.FILE).getValue();
            try {
                PostmanEnvModel postmanEnvModel = loadFromFile(filePath);
                addEnvironmentAndPopulateProperties(postmanEnvModel);
                sendAnalyticsAction(postmanEnvModel.name());
            } catch (Exception e) {
                UISupport.getDialogs().showErrorMessage("Cannot import Postman environment.\n" + e.getMessage());
            }
        }
    }

    protected PostmanEnvModel loadFromFile(String filePath) throws IOException {
        File file = Paths.get(filePath).toFile();
        if (file.exists()) {
            return OBJECT_MAPPER.readValue(file, PostmanEnvModel.class);
        } else {
            throw new FileNotFoundException("Provided import file does not exist");
        }
    }

    protected void addEnvironmentAndPopulateProperties(PostmanEnvModel postmanEnvModel) {
        if (project.getEnvironmentByName(postmanEnvModel.name()) != null) {
            UISupport.getDialogs().showErrorMessage(
                    String.format("An environment with the name %s already exists.", postmanEnvModel.name()));
            return;
        }
        Map<String, NewEnvironmentPropertyWrapper> newPropertiesMap = new HashMap<>();
        Map<String, Integer> keyCountMap = new HashMap<>();
        for (PostmanEnvVariable variable : postmanEnvModel.values()) {
            String variableName = variable.key();
            if (isNullOrEmpty(variableName)) {
                log.error("Variable name cannot be empty, it will not be imported.");
                continue;
            }
            if (newPropertiesMap.get(variableName) != null) {
                int count = keyCountMap.getOrDefault(variableName, 1);
                variableName += " " + count;
                keyCountMap.put(variable.key(), count + 1);
            }
            newPropertiesMap.put(variableName, new NewEnvironmentPropertyWrapper(variableName, variable.value(), variable.isSecret()));
            if (project.getProperty(variableName) == null) {
                project.addProperty(variableName);
            }
        }

        if (newPropertiesMap.isEmpty()) {
            UISupport.getDialogs().showErrorMessage("No variables found in provided Postman environment.");
        } else {
            addEnvironment(project, postmanEnvModel.name(), newPropertiesMap);
        }
    }

    private void sendAnalyticsAction(String envName) {
        Environment env = project.getEnvironmentByName(envName);
        if (env != null) {
            sendAddEnvironmentAction(project, ParameterType.EnvironmentSetupOptions.COPY);
        }
    }

    @AForm(name = "Load Postman Environment", description = "Load Postman environment from export file")
    private interface LoadPostmanFile {
        @AField(name = "File", description = "The environment file to load", type = AField.AFieldType.FILE)
        String FILE = "File";
    }
}