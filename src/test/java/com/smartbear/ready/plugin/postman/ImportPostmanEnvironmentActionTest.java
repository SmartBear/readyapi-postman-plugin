package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.model.environment.EnvironmentImpl;
import com.eviware.soapui.model.testsuite.EncryptableTestProperty;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.settings.ProjectSettings;
import com.fasterxml.jackson.core.JsonParseException;
import com.smartbear.ready.plugin.postman.collection.environment.PostmanEnvModel;
import com.smartbear.ready.plugin.postman.collection.environment.PostmanEnvVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportPostmanEnvironmentActionTest {

    private static final String ENV_WITH_DUPLICATE_VALUES = "/environments/env_with_duplicate_value_names.postman_environment.json";
    private static final String ENV_WITH_SECRET_VALUES = "/environments/env_with_secret_values.postman_environment.json";
    private static final String JSON_WITH_RANDOM_DATA = "/environments/invalid_format_env.json";
    private static final String FIRST_ENV_WITH_THE_SAME_VALUE = "/environments/first_environment_with_the_same_value.json";
    private static final String SECOND_ENV_WITH_THE_SAME_VALUE = "/environments/second_environment_with_the_same_value.json";
    private static final String TEXT_FILE = "/environments/random.txt";

    private ImportPostmanEnvironmentAction importPostmanEnvironmentAction;
    private WsdlProjectPro project;

    @BeforeEach
    public void setUp() {
        project = new WsdlProjectPro();
        importPostmanEnvironmentAction = new ImportPostmanEnvironmentAction(project);
    }

    @Test
    void testIfEnvironmentWasCreated() throws IOException, URISyntaxException {
        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(ENV_WITH_DUPLICATE_VALUES);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(postmanEnvModel);
        assertNotNull(project.getEnvironmentByName(postmanEnvModel.name()));
    }

    @Test
    void testIfEnvironmentWithTheSameNameWasCreatedOnlyOnce() throws IOException, URISyntaxException {
        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(ENV_WITH_DUPLICATE_VALUES);
        PostmanEnvModel postmanEnvModelCopy = loadEnvironmentFromFile(ENV_WITH_DUPLICATE_VALUES);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(postmanEnvModel);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(postmanEnvModelCopy);

        int environmentsWithTheSameName = (int) Arrays.stream(project.getEnvironmentNames())
                .filter(en -> en.equals(postmanEnvModel.name()))
                .count();

        assertEquals(1, environmentsWithTheSameName);
    }

    @Test
    void invalidDefinitionDoesNotCreateEnvironment() throws IOException, URISyntaxException {
        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(JSON_WITH_RANDOM_DATA);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(postmanEnvModel);
        assertNull(project.getEnvironmentByName(postmanEnvModel.name()));
    }

    @Test
    void shouldFailOnNotAJsonFile() {
        assertThrows(JsonParseException.class, () -> loadEnvironmentFromFile(TEXT_FILE));
    }

    @Test
    void testSecretPropertiesEncryption() throws IOException, URISyntaxException {
        project.getSettings().setString(ProjectSettings.SHADOW_PASSWORD, "password");

        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(ENV_WITH_SECRET_VALUES);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(postmanEnvModel);

        postmanEnvModel.values().forEach(variable -> {
            TestProperty testProperty = project.getProjectProperty(variable.key());
            if (variable.isSecret()) {
                assertTrue(((EncryptableTestProperty) testProperty).isEncrypted());
            }
        });
        project.getSettings().setString(ProjectSettings.SHADOW_PASSWORD, null);
    }

    @Test
    void propertiesWithTheSameNameAddedOnlyOnce() throws IOException, URISyntaxException {
        PostmanEnvModel firstPostmanEnvModel = loadEnvironmentFromFile(FIRST_ENV_WITH_THE_SAME_VALUE);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(firstPostmanEnvModel);
        PostmanEnvVariable duplicateVariable = firstPostmanEnvModel.values().get(0);

        PostmanEnvModel secondPostmanEnvModel = loadEnvironmentFromFile(SECOND_ENV_WITH_THE_SAME_VALUE);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(secondPostmanEnvModel);

        long duplicateVariableNameCount = Arrays.stream(project.getPropertyNames())
                .filter(p -> p.equals(duplicateVariable.key()))
                .count();
        int firstEnvPropertiesCount = ((EnvironmentImpl) project.getEnvironmentByName(firstPostmanEnvModel.name()))
                .getPropertiesCount();

        assertEquals(1, duplicateVariableNameCount);
        assertEquals(1, firstEnvPropertiesCount);
    }

    private PostmanEnvModel loadEnvironmentFromFile(String fileLocation) throws IOException, URISyntaxException {
        File file = new File(ImportPostmanEnvironmentActionTest.class.getResource(fileLocation).toURI());
        return importPostmanEnvironmentAction.loadFromFile(file.getPath());
    }

}
