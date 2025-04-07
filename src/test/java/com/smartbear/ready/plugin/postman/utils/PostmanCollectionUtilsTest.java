package com.smartbear.ready.plugin.postman.utils;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

import static com.smartbear.ready.plugin.postman.collection.PostmanCollectionFactory.INFO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostmanCollectionUtilsTest {

    private static final String COLLECTION_WITH_VAULT_V20 = "/utils/collection_with_vault_variables_V20.json";
    private static final String COLLECTION_WITH_VAULT_V21 = "/utils/collection_with_vault_variables_V21.json";
    private static final String COLLECTION_WITH_VAULT_IN_SCRIPT_V21 = "/utils/collection_with_vault_variables_in_script_V21.json";
    private static final String COLLECTION_WITH_UNSUPPORTED_VERSION = "/utils/collection_with_unsupported_version.json";

    private static final Set<String> EXPECTED_VAULT_VARIABLES = Set.of(
            "form-data-vault", "basic_user", "basic_password", "the secret key", "ntlm username", "ntlm password"
    );

    @Test
    void versionIsV20ForCollectionWithVersionV20() throws Exception {
        verifyCollectionVersionExtractedCorrectly(COLLECTION_WITH_VAULT_V20, PostmanCollectionUtils.VERSION_2);
    }

    @Test
    void versionIsV21ForCollectionWithVersionV21() throws Exception {
        verifyCollectionVersionExtractedCorrectly(COLLECTION_WITH_VAULT_V21, PostmanCollectionUtils.VERSION_2_1);
    }

    @Test
    void versionIsBlankForUnsupportedCollectionVersion() throws Exception {
        JSONObject collectionJson = getCollectionFromFile(COLLECTION_WITH_UNSUPPORTED_VERSION);
        Optional<String> collectionVersion = PostmanCollectionUtils.getCollectionVersionFromInfo(collectionJson.get(INFO));

        assertFalse(collectionVersion.isPresent());
    }

    @Test
    void vaultVariableExtractUtilExtractsCorrectValuesV20() throws Exception {
        verifyVaultVariablesExtractedCorrectly(COLLECTION_WITH_VAULT_V20);
    }

    @Test
    void vaultVariableExtractUtilExtractsCorrectValuesV21() throws Exception {
        verifyVaultVariablesExtractedCorrectly(COLLECTION_WITH_VAULT_V21);
    }

    @Test
    void vaultVariableExtractUtilExtractsCorrectValuesInScriptV21() throws Exception {
        verifyVaultVariablesExtractedCorrectly(COLLECTION_WITH_VAULT_IN_SCRIPT_V21);
    }

    private void verifyCollectionVersionExtractedCorrectly(String pathToCollection, String version) throws Exception {
        JSONObject collectionJson = getCollectionFromFile(pathToCollection);
        Optional<String> collectionVersion = PostmanCollectionUtils.getCollectionVersionFromInfo(collectionJson.get(INFO));

        assertTrue(collectionVersion.isPresent());
        assertEquals(version, collectionVersion.get());
    }

    private void verifyVaultVariablesExtractedCorrectly(String pathToCollection) throws Exception {
        JSONObject collectionJson = getCollectionFromFile(pathToCollection);
        Set<String> vaultVariableNames = PostmanCollectionUtils.extractVaultVariables(collectionJson);

        assertEquals(6, vaultVariableNames.size());
        assertEquals(EXPECTED_VAULT_VARIABLES, vaultVariableNames);
    }

    private JSONObject getCollectionFromFile(String filePath) throws Exception{
        File file = new File(PostmanCollectionUtilsTest.class.getResource(filePath).toURI());
        return JSONObject.fromObject(Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

}