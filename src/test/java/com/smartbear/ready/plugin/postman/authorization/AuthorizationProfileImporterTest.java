package com.smartbear.ready.plugin.postman.authorization;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.config.OAuth2FlowConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthProfileHolder;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.smartbear.ready.plugin.postman.DummyTestCreator;
import com.smartbear.ready.plugin.postman.PostmanImporter;
import com.smartbear.ready.plugin.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import com.smartbear.ready.plugin.postman.utils.VaultVariableResolver;
import net.sf.json.JSONObject;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AuthorizationProfileImporterTest {

    private static final String AUTH_COLLECTION_V20 = "/authorization/test-auth-profiles-V20.json";
    private static final String AUTH_COLLECTION_V21 = "/authorization/test-auth-profiles-V21.json";
    private static final String COLLECTION_NAME = "test-auth-profiles";
    private static final String NESTED_FOLDER_NAME = COLLECTION_NAME + "/folder 1 [Digest]";
    private static final String UNSUPPORTED_PROFILE = "unsupported profile";

    private static final String OUTPUT_FOLDER_PATH = AuthorizationProfileImporterTest.class.getResource("/").getPath();
    private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";

    private static File workspaceFile;
    private static WorkspaceImpl workspace;
    private static PostmanImporter importer;
    private static WsdlProject collectionV20;
    private static WsdlProject collectionV21;

    @Mock
    private static VaultVariableResolver resolver;

    @BeforeAll
    static void setUp() throws XmlException, IOException, ExecutionException, InterruptedException, PostmanCollectionUnsupportedVersionException {
        workspaceFile = new File(TEST_WORKSPACE_FILE_PATH);
        workspace = new WorkspaceImpl(workspaceFile.getAbsolutePath(), null);

        resolver = Mockito.mock(VaultVariableResolver.class);
        when(resolver.resolve(Mockito.any(JSONObject.class))).thenReturn(new HashMap<>());
        importer = new PostmanImporter(new DummyTestCreator(), resolver);

        collectionV20 = importer.importPostmanCollection(workspace,
                AuthorizationProfileImporterTest.class.getResource(AUTH_COLLECTION_V20).getPath());
        collectionV21 = importer.importPostmanCollection(workspace,
                AuthorizationProfileImporterTest.class.getResource(AUTH_COLLECTION_V21).getPath());
    }

    @Test
    void importAuthorizationCollectionLevelV20_V21() {
        assertEquals(COLLECTION_NAME, collectionV20.getAuthProfile());
        assertEquals(COLLECTION_NAME, collectionV21.getAuthProfile());
    }

    @Test
    void unsupportedProfileShouldNotBeCreatedV20_V21() {
        assertNull(collectionV20.getAuthRepository().getEntry(UNSUPPORTED_PROFILE));
        assertNull(collectionV21.getAuthRepository().getEntry(UNSUPPORTED_PROFILE));
    }

    @Test
    void importNestedFolderAuthProfileToRequestV20_V21() {
        verifyNestedFolderAuthProfile(collectionV20);
        verifyNestedFolderAuthProfile(collectionV21);
    }

    @Test
    void duplicateProfileImportedOnceButAppliedToBothRequestsV20_V21() {
        verifyDuplicateProfileImportedOnce(collectionV20);
        verifyDuplicateProfileImportedOnce(collectionV21);
    }

    @Test
    void duplicateProfileNamesAreIncrementedV20_V21() {
        verifyDuplicateProfileNameIsIncremented(collectionV20);
        verifyDuplicateProfileNameIsIncremented(collectionV21);
    }

    @Test
    void importBasicAuthProfileToRequestV20_V21() {
        verifyBasicAuthProfileCreation(collectionV20);
        verifyBasicAuthProfileCreation(collectionV21);
    }

    @Test
    void importDigestAuthProfileV20_V21() {
        verifyDigestProfileImport(collectionV20);
        verifyDigestProfileImport(collectionV21);
    }

    @Test
    void importNtlmAuthProfileV20_V21() {
        verifyNtlmProfileImport(collectionV20);
        verifyNtlmProfileImport(collectionV21);
    }

    @Test
    void importOAuth1AuthProfileV20_V21() {
        verifyOAuth1ProfileImport(collectionV20);
        verifyOAuth1ProfileImport(collectionV21);
    }

    @Test
    void importOAuth2AuthProfileV20_V21() {
        verifyOAuth2ProfileImport(collectionV20);
        verifyOAuth2ProfileImport(collectionV21);
    }

    @Test
    void importAwsSignatureAuthProfileV20_V21() {
        verifyAwsProfileImport(collectionV20);
        verifyAwsProfileImport(collectionV21);
    }

    @Test
    void importNoAuthAuthProfileV20_V21() {
        verifyIfNoAuthProfileIsSet(collectionV20);
        verifyIfNoAuthProfileIsSet(collectionV21);
    }

    @Test
    void checkPropertyExpansionsCreatedV20_V21() {
        verifyPropertyExpansionsCreatedForBasicAuth(collectionV20);
        verifyPropertyExpansionsCreatedForBasicAuth(collectionV21);
    }

    @AfterAll
    public static void tearDown() {
        if (workspaceFile.exists()) {
            workspaceFile.delete();
        }
    }

    private void verifyNestedFolderAuthProfile(WsdlProject project) {
        List<AuthProfileHolder> profileHolders = project.getAuthRepository()
                .getAuthProfileHolderListByEntryName(NESTED_FOLDER_NAME);

        assertNotNull(project.getAuthRepository().getEntry(NESTED_FOLDER_NAME));
        assertEquals(1, profileHolders.size());

        RestRequest requestWithAuth = (RestRequest) profileHolders.get(0);
        assertEquals("request in folder", requestWithAuth.getName());
        assertEquals(NESTED_FOLDER_NAME, requestWithAuth.getAuthProfile());
    }

    private void verifyDuplicateProfileImportedOnce(WsdlProject project) {
        long sameNameProfileCount = project.getAuthRepository().getEntryList()
                .stream()
                .filter(entry -> entry.getName().equals("same profile"))
                .count();
        List<AuthProfileHolder> profileHolders = project.getAuthRepository()
                .getAuthProfileHolderListByEntryName("same profile");

        assertEquals(1, sameNameProfileCount);
        assertEquals(2, profileHolders.size());
    }

    private void verifyDuplicateProfileNameIsIncremented(WsdlProject project) {
        List<AuthProfileHolder> profileHolders = project.getAuthRepository()
                .getAuthProfileHolderListByEntryName("same name");
        assertEquals(1, profileHolders.size());
        assertNotNull(project.getAuthRepository().getEntry("same name 1"));
    }

    private void verifyBasicAuthProfileCreation(WsdlProject project) {
        AuthEntries.BaseAuthEntry authEntry = project.getAuthRepository().getEntry(COLLECTION_NAME);
        assertNotNull(authEntry);
        assertInstanceOf(AuthEntries.BasicAuthEntry.class, authEntry);
        assertEquals("collection-name", ((AuthEntries.BasicAuthEntry) authEntry).getUsername());
        assertEquals("collection-password", ((AuthEntries.BasicAuthEntry) authEntry).getPassword());
    }

    private void verifyDigestProfileImport(WsdlProject project) {
        AuthEntries.BaseAuthEntry authEntry = project.getAuthRepository().getEntry(NESTED_FOLDER_NAME);
        assertNotNull(authEntry);
        assertInstanceOf(AuthEntries.DigestAuthEntry.class, authEntry);
        assertEquals("digest-user", ((AuthEntries.DigestAuthEntry) authEntry).getUsername());
        assertEquals("digest-password", ((AuthEntries.DigestAuthEntry) authEntry).getPassword());
    }

    private void verifyNtlmProfileImport(WsdlProject project) {
        AuthEntries.BaseAuthEntry authEntry = project.getAuthRepository().getEntry("ntlm");
        assertNotNull(authEntry);
        assertInstanceOf(AuthEntries.NTLMAuthEntry.class, authEntry);
        assertEquals("ntlm-user", ((AuthEntries.NTLMAuthEntry) authEntry).getUsername());
        assertEquals("ntlm-pass", ((AuthEntries.NTLMAuthEntry) authEntry).getPassword());
        assertEquals("sampledomain.com", ((AuthEntries.NTLMAuthEntry) authEntry).getDomain());
    }

    private void verifyAwsProfileImport(WsdlProject project) {
        AuthEntries.BaseAuthEntry authEntry = project.getAuthRepository().getEntry("aws request");
        assertNotNull(authEntry);
        assertInstanceOf(AuthEntries.AwsSignatureAuthEntry.class, authEntry);
        assertEquals("us-east-1", ((AuthEntries.AwsSignatureAuthEntry) authEntry).getRegion());
        assertEquals("s3", ((AuthEntries.AwsSignatureAuthEntry) authEntry).getServiceName());
        assertEquals("session-token", ((AuthEntries.AwsSignatureAuthEntry) authEntry).getSecurityToken());
        assertEquals("secret-key", ((AuthEntries.AwsSignatureAuthEntry) authEntry).getSecretAccessKey());
        assertEquals("access-key", ((AuthEntries.AwsSignatureAuthEntry) authEntry).getAccessKey());
    }

    private void verifyOAuth1ProfileImport(WsdlProject project) {
        AuthEntries.BaseAuthEntry authEntry = project.getAuthRepository().getEntry("oauth1");
        assertNotNull(authEntry);
        assertInstanceOf(AuthEntries.OAuth10AuthEntry.class, authEntry);
        assertEquals("consumer-key-value", ((AuthEntries.OAuth10AuthEntry) authEntry).getConsumerKey());
        assertEquals("consumer-secret-value", ((AuthEntries.OAuth10AuthEntry) authEntry).getConsumerSecret());
        assertEquals("access-token", ((AuthEntries.OAuth10AuthEntry) authEntry).getAccessToken());
        assertEquals("token-secret-value", ((AuthEntries.OAuth10AuthEntry) authEntry).getTokenSecret());
        assertEquals("https://callback-url.com", ((AuthEntries.OAuth10AuthEntry) authEntry).getRedirectURI());
        assertEquals(AccessTokenPositionConfig.QUERY, ((AuthEntries.OAuth10AuthEntry) authEntry).getAccessTokenPosition());
    }

    private void verifyOAuth2ProfileImport(WsdlProject project) {
        AuthEntries.BaseAuthEntry authEntry = project.getAuthRepository().getEntry("oauth2");
        assertNotNull(authEntry);
        assertInstanceOf(AuthEntries.OAuth20AuthEntry.class, authEntry);
        assertEquals("some-client-id", ((AuthEntries.OAuth20AuthEntry) authEntry).getClientID());
        assertEquals("some-client-secret", ((AuthEntries.OAuth20AuthEntry) authEntry).getClientSecret());
        assertEquals("test-state", ((AuthEntries.OAuth20AuthEntry) authEntry).getState());
        assertEquals("scope:email", ((AuthEntries.OAuth20AuthEntry) authEntry).getScope());
        assertEquals("http://accesstokenurl", ((AuthEntries.OAuth20AuthEntry) authEntry).getAccessTokenURI());
        assertEquals("http://authurl", ((AuthEntries.OAuth20AuthEntry) authEntry).getAuthorizationURI());
        assertEquals("http://test.test", ((AuthEntries.OAuth20AuthEntry) authEntry).getRedirectURI());
        assertEquals(OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT, ((AuthEntries.OAuth20AuthEntry) authEntry).getOAuth2Flow());
        assertEquals(AccessTokenPositionConfig.QUERY, ((AuthEntries.OAuth20AuthEntry) authEntry).getAccessTokenPosition());
        assertTrue(((AuthEntries.OAuth20AuthEntry) authEntry).isEnablePKCE());
        assertTrue(((AuthEntries.OAuth20AuthEntry) authEntry).getUseAuthHeader());
    }

    private void verifyIfNoAuthProfileIsSet(WsdlProject project) {
        RestRequest request = (RestRequest) project
                .getInterfaceByName("https://localhost:222")
                .getOperationAt(0)
                .getRequestByName("no auth");
        assertEquals(AuthEntryTypeConfig.NO_AUTHORIZATION.toString(), request.getAuthProfile());
    }

    private void verifyPropertyExpansionsCreatedForBasicAuth(WsdlProject project) {
        assertNotNull(project.getProperty("vault:vault var"));
        assertNotNull(project.getProperty("vault:basicAuthUsername"));

        AuthEntries.BaseAuthEntry vaultEntry = project.getAuthRepository().getEntry("basic with vault");
        assertEquals("${#Project#vault:basicAuthUsername}", ((AuthEntries.BasicAuthEntry) vaultEntry).getUsername());
        assertEquals("${#Project#vault:vault var}", ((AuthEntries.BasicAuthEntry) vaultEntry).getPassword());

        assertNotNull(project.getProperty("global_username"));
        assertNotNull(project.getProperty("global_password"));

        AuthEntries.BaseAuthEntry globalsEntry = project.getAuthRepository().getEntry("basic with globals");
        assertEquals("${#Project#global_username}", ((AuthEntries.BasicAuthEntry) globalsEntry).getUsername());
        assertEquals("${#Project#global_password}", ((AuthEntries.BasicAuthEntry) globalsEntry).getPassword());
    }
}
