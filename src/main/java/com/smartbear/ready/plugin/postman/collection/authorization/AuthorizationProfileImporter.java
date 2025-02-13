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
            if (NO_AUTH_TYPE.equals(authType)) {
                objectToAttachAuth.setAuthProfile(AuthEntryTypeConfig.NO_AUTHORIZATION.toString());
            } else {
                PostmanAuthProfile profile = getProfileByType(authType, authProfileString);
                if (profile != null) {
                    createProfile(profile, profileName, objectToAttachAuth);
                }
            }
        } catch (JSONException | JsonProcessingException e) {
            log.error("Error happened while processing auth profile JSON [{}]", authProfile, e);
        }
    }

    private PostmanAuthProfile getProfileByType(String authType, String authProfileString) throws JsonProcessingException {
        return switch (authType) {
            case BASIC_AUTH_TYPE -> OBJECT_MAPPER.readValue(authProfileString, BasicAuthProfile.class);
            case AWS_SIGNATURE_AUTH_TYPE -> OBJECT_MAPPER.readValue(authProfileString, AwsSignatureProfile.class);
            case DIGEST_AUTH_TYPE -> OBJECT_MAPPER.readValue(authProfileString, DigestProfile.class);
            case NTLM_AUTH_TYPE -> OBJECT_MAPPER.readValue(authProfileString, NtlmProfile.class);
            case OAUTH1_AUTH_TYPE -> OBJECT_MAPPER.readValue(authProfileString, OAuth1Profile.class);
            case OAUTH2_AUTH_TYPE -> OBJECT_MAPPER.readValue(authProfileString, OAuth2Profile.class);
            default -> {
                log.error("Unsupported authorization profile type: {}", authType);
                yield null;
            }
        };
    }

    private void createProfile(PostmanAuthProfile authProfile, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        if (importedProfiles.containsValue(authProfile)) {
            profileName = importedProfiles.inverse().get(authProfile);
        } else {
            profileName = incrementProfileNameIfExists(profileName);
            authProfile.createAuthEntry(profileName, authRepository);
            importedProfiles.put(profileName, authProfile);
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
