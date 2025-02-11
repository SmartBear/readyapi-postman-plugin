package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.environmentspec.AuthProfileHolderContainer;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbear.ready.plugin.postman.collection.PostmanCollectionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthorizationProfileImporter {

    private static final String AWS_SIGNATURE_AUTH_TYPE = "awsv4";
    private static final String BASIC_AUTH_TYPE = "basic";
    private static final String DIGEST_AUTH_TYPE = "digest";
    private static final String NO_AUTH_TYPE = "noauth";
    private static final String NTLM_AUTH_TYPE = "ntlm";
    private static final String OAUTH1_AUTH_TYPE = "oauth1";
    private static final String OAUTH2_AUTH_TYPE = "oauth2";
    private static final String PROFILE_TYPE = "type";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Logger log = LoggerFactory.getLogger(AuthorizationProfileImporter.class);

    private final AuthRepository authRepository;
    private final String collectionVersion;
    private final BiMap<String, PostmanAuthProfile> importedProfiles = HashBiMap.create();
    private final Map<String, Integer> profileNamesCounter = new HashMap<>();

    public AuthorizationProfileImporter(AuthRepository authRepository, String collectionVersion) {
        this.authRepository = authRepository;
        this.collectionVersion = collectionVersion;
    }

    public void importAuthorizationProfile(String authProfile, String profileName, AuthProfileHolderContainer objectToAttachAuth) {
        try {
            JSONObject authProfileJson = new JSONObject(authProfile);
            String authType = authProfileJson.getString(PROFILE_TYPE);
            String authProfileString = getAuthProfileString(authProfileJson, authType);
            switch (authType) {
                case NO_AUTH_TYPE -> objectToAttachAuth.setAuthProfile(AuthEntryTypeConfig.NO_AUTHORIZATION.toString());
                case BASIC_AUTH_TYPE -> createBasicAuthProfile(authProfileString, profileName, objectToAttachAuth);
                case AWS_SIGNATURE_AUTH_TYPE -> createAwsSignatureProfile(authProfileString, profileName, objectToAttachAuth);
                case DIGEST_AUTH_TYPE -> createDigestProfile(authProfileString, profileName, objectToAttachAuth);
                case NTLM_AUTH_TYPE -> createNtlmProfile(authProfileString, profileName, objectToAttachAuth);
                case OAUTH1_AUTH_TYPE -> createOAuth1Profile(authProfileString, profileName, objectToAttachAuth);
                case OAUTH2_AUTH_TYPE -> createOAuth2Profile(authProfileString, profileName, objectToAttachAuth);
                default -> log.error("Unsupported authorization profile type: {}", authType);
            }
        } catch (JSONException | JsonProcessingException e) {
            log.error("Error happened while processing auth profile JSON [{}]", authProfile, e);
        }
    }

    private void createBasicAuthProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        BasicAuthProfile basicAuthProfile = OBJECT_MAPPER.readValue(authString, BasicAuthProfile.class);
        if (importedProfiles.containsValue(basicAuthProfile)) {
            profileName = importedProfiles.inverse().get(basicAuthProfile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            basicAuthProfile.createBasicAuthEntry(profileName, authRepository);
            importedProfiles.put(profileName, basicAuthProfile);
        }
        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createAwsSignatureProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        AwsSignatureProfile awsSignatureProfile = OBJECT_MAPPER.readValue(authString, AwsSignatureProfile.class);
        if (importedProfiles.containsValue(awsSignatureProfile)) {
            profileName = importedProfiles.inverse().get(awsSignatureProfile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            awsSignatureProfile.createAwsSignatureEntry(profileName, authRepository);
            importedProfiles.put(profileName, awsSignatureProfile);
        }
        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createOAuth1Profile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        OAuth1Profile oAuth1Profile = OBJECT_MAPPER.readValue(authString, OAuth1Profile.class);
        if (importedProfiles.containsValue(oAuth1Profile)) {
            profileName = importedProfiles.inverse().get(oAuth1Profile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            oAuth1Profile.createOAuth1Entry(profileName, authRepository);
            importedProfiles.put(profileName, oAuth1Profile);
        }
        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createOAuth2Profile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        OAuth2Profile oAuth2Profile = OBJECT_MAPPER.readValue(authString, OAuth2Profile.class);
        if (importedProfiles.containsValue(oAuth2Profile)) {
            profileName = importedProfiles.inverse().get(oAuth2Profile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            oAuth2Profile.createOAuth2Entry(profileName, authRepository);
            importedProfiles.put(profileName, oAuth2Profile);
        }
        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createNtlmProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        NtlmProfile ntlmProfile = OBJECT_MAPPER.readValue(authString, NtlmProfile.class);
        if (importedProfiles.containsValue(ntlmProfile)) {
            profileName = importedProfiles.inverse().get(ntlmProfile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            ntlmProfile.createNtlmEntry(profileName, authRepository);
            importedProfiles.put(profileName, ntlmProfile);
        }
        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createDigestProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        DigestProfile digestProfile = OBJECT_MAPPER.readValue(authString, DigestProfile.class);
        if (importedProfiles.containsValue(digestProfile)) {
            profileName = importedProfiles.inverse().get(digestProfile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            digestProfile.createDigestAuthEntry(profileName, authRepository);
            importedProfiles.put(profileName, digestProfile);
        }
        objectToAttachAuth.setAuthProfile(profileName);
    }

    private String getAuthProfileString(JSONObject authProfileJson, String authType) throws JSONException, JsonProcessingException {
        if (NO_AUTH_TYPE.equals(authType)) {
            return "";
        }
        if (collectionVersion.equals(PostmanCollectionFactory.VERSION_2_1)) {
            String authProfileContent = authProfileJson.getJSONArray(authType).toString();
            return mapKeyValueAuthProfileToObject(authProfileContent);
        }
        return authProfileJson.getJSONObject(authType).toString();
    }

    private String incrementProfileNameIfExists(String profileName) {
        if (importedProfiles.get(profileName) != null) {
            int count = profileNamesCounter.getOrDefault(profileName, 1);
            profileNamesCounter.put(profileName, count + 1);
            profileName += " " + count;
        }
        return profileName;
    }

    private String mapKeyValueAuthProfileToObject(String json) throws JsonProcessingException {
        List<JsonNode> nodeList = OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        Map<String, String> resultMap = nodeList.stream()
                .collect(Collectors.toMap(
                        node -> node.get("key").asText(),
                        node -> node.get("value").asText()
                ));
        return OBJECT_MAPPER.writeValueAsString(resultMap);
    }
}
