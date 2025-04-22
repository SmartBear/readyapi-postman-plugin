package com.smartbear.ready.plugin.postman;

import com.eviware.soapui.environmentspec.AuthProfileHolderContainer;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectPro;
import com.eviware.soapui.model.ModelItem;
import com.smartbear.ready.plugin.postman.collection.environment.PostmanEnvModel;
import com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import com.smartbear.ready.plugin.postman.utils.VaultVariableResolver;
import net.sf.json.JSONObject;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PostmanImportWithEnvironmentTest {

    private static final String AUTH_PROFILES_CLONE_ENVIRONMENT = "/import/environment_auth_profiles_clone.postman_environment.json";
    private static final String AUTH_PROFILES_CLONE_COLLECTION = "/import/collection_auth_profiles_clone.json";
    private static final String OUTPUT_FOLDER_PATH = PostmanImportWithEnvironmentTest.class.getResource("/").getPath();
    private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";

    private static File workspaceFile;
    private static WorkspaceImpl workspace;
    private static PostmanImporter importer;

    private static WsdlProjectPro projectWithCollection;
    @Mock
    private static VaultVariableResolver resolver;
    private ImportPostmanEnvironmentAction importPostmanEnvironmentAction;

    @BeforeAll
    static void setUp() throws IOException, XmlException, ExecutionException, InterruptedException {
        workspaceFile = new File(TEST_WORKSPACE_FILE_PATH);
        workspace = new WorkspaceImpl(workspaceFile.getAbsolutePath(), null);

        resolver = Mockito.mock(VaultVariableResolver.class);
        when(resolver.resolve(Mockito.any(JSONObject.class))).thenReturn(new HashMap<>());
        importer = new PostmanImporter(new DummyTestCreator(), resolver);
    }

    @Test
    void authorizationIsClonedWhenImportingEnvironment() throws IOException, URISyntaxException, PostmanCollectionUnsupportedVersionException, GeneralSecurityException {
        WsdlProject importedProject = importer.importPostmanCollection(workspace,
                ImportPostmanEnvironmentActionTest.class.getResource(AUTH_PROFILES_CLONE_COLLECTION).getPath());

        projectWithCollection = new WsdlProjectPro();
        projectWithCollection.loadProjectFromConfig(importedProject.getProjectDocument());

        importPostmanEnvironmentAction = new ImportPostmanEnvironmentAction(projectWithCollection);
        PostmanEnvModel postmanEnvModel = loadEnvironmentFromFile(AUTH_PROFILES_CLONE_ENVIRONMENT);
        importPostmanEnvironmentAction.addEnvironmentAndPopulateProperties(postmanEnvModel, true, true);

        HashMap<AuthProfileHolderContainer, List<String>> authProfiles = new HashMap<>();

        projectWithCollection.getInterfaceList().forEach(i -> {
            collectAuthProfiles(authProfiles, i);
        });

        projectWithCollection.setActiveEnvironment("Test_1");

        projectWithCollection.getInterfaceList().forEach(i -> {
            collectAuthProfiles(authProfiles, i);
        });

        authProfiles.forEach((authProfileHolderContainer, authProfileNameList) -> {
            assertEquals(authProfileNameList.get(0), authProfileNameList.get(1));
        });
    }

    private PostmanEnvModel loadEnvironmentFromFile(String fileLocation) throws IOException, URISyntaxException {
        File file = new File(ImportPostmanEnvironmentActionTest.class.getResource(fileLocation).toURI());
        return importPostmanEnvironmentAction.loadFromFile(file.getPath());
    }

    private void collectAuthProfiles(HashMap<AuthProfileHolderContainer, List<String>> authProfiles, ModelItem modelItem) {
        if (modelItem instanceof AuthProfileHolderContainer container) {
            authProfiles.compute(container, (authProfileHolderContainer, authProfileNameList) -> {
                if (authProfileNameList == null) {
                    authProfileNameList = new ArrayList<>();
                }
                authProfileNameList.add(modelItem.getName());
                return authProfileNameList;
            });
        }
        modelItem.getChildren().forEach(i -> collectAuthProfiles(authProfiles, i));
    }
}
