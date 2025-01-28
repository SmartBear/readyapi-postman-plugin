package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.fasterxml.jackson.core.JsonParseException;
import com.smartbear.ready.plugin.postman.collection.environment.PostmanEnvModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImportPostmanEnvironmentActionTest {

    private static final String ENV_WITH_DUPLICATE_VALUES = "/environments/env_with_duplicate_value_names.postman_environment.json";
    private static final String JSON_WITH_RANDOM_DATA = "/environments/invalid_format_env.json";
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
        importPostmanEnvironmentAction.createEnvironmentAndPopulateFrom(postmanEnvModel);
        assertThat(project.getEnvironmentByName(postmanEnvModel.getName()), notNullValue());
    }

    @Test
    void testIfEnvironmentWithTheSameNameWasCreatedOnlyOnce() throws IOException, URISyntaxException {
        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(ENV_WITH_DUPLICATE_VALUES);
        PostmanEnvModel postmanEnvModelCopy = loadEnvironmentFromFile(ENV_WITH_DUPLICATE_VALUES);
        importPostmanEnvironmentAction.createEnvironmentAndPopulateFrom(postmanEnvModel);
        importPostmanEnvironmentAction.createEnvironmentAndPopulateFrom(postmanEnvModelCopy);

        int environmentsWithTheSameName = (int) Arrays.stream(project.getEnvironmentNames())
                .filter(en -> en.equals(postmanEnvModel.getName()))
                .count();

        assertEquals(1, environmentsWithTheSameName);
    }

    @Test
    void invalidDefinitionDoesNotCreateEnvironment() throws IOException, URISyntaxException {
        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(JSON_WITH_RANDOM_DATA);
        importPostmanEnvironmentAction.createEnvironmentAndPopulateFrom(postmanEnvModel);
        assertNull(project.getEnvironmentByName(postmanEnvModel.getName()));
    }

    @Test
    void shouldFailOnNotAJsonFile() {
        assertThrows(JsonParseException.class, () -> loadEnvironmentFromFile(TEXT_FILE));
    }

    private PostmanEnvModel loadEnvironmentFromFile(String fileLocation) throws IOException, URISyntaxException {
        File file = new File(ImportPostmanEnvironmentActionTest.class.getResource(fileLocation).toURI());
        return importPostmanEnvironmentAction.loadFromFile(file.getPath());
    }

}
