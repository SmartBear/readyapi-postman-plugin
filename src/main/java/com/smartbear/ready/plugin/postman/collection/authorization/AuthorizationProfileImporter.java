package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.environmentspec.AuthProfileHolderContainer;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
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
                case AWS_SIGNATURE_AUTH_TYPE ->
                        createAwsSignatureProfile(authProfileString, profileName, objectToAttachAuth);
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

        AuthEntries.BasicAuthEntry basicAuthEntry = (AuthEntries.BasicAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.BASIC, profileName);
        setIfNotNull(basicAuthProfile.password(), basicAuthEntry::setPassword);
        setIfNotNull(basicAuthProfile.username(), basicAuthEntry::setUsername);

        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createAwsSignatureProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        AwsSignatureProfile awsSignatureProfile = OBJECT_MAPPER.readValue(authString, AwsSignatureProfile.class);

        AuthEntries.AwsSignatureAuthEntry awsSignatureAuthEntry = (AuthEntries.AwsSignatureAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.AWS_SIGNATURE, profileName);
        setIfNotNull(awsSignatureProfile.accessKey(), awsSignatureAuthEntry::setAccessKey);
        setIfNotNull(awsSignatureProfile.secretKey(), awsSignatureAuthEntry::setSecretAccessKey);
        setIfNotNull(awsSignatureProfile.region(), awsSignatureAuthEntry::setRegion);
        setIfNotNull(awsSignatureProfile.service(), awsSignatureAuthEntry::setServiceName);

        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createOAuth1Profile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        OAuth1Profile oAuth1Profile = OBJECT_MAPPER.readValue(authString, OAuth1Profile.class);

        AuthEntries.OAuth10AuthEntry oAuth10AuthEntry = (AuthEntries.OAuth10AuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.O_AUTH_1_0, profileName);

        setIfNotNull(oAuth1Profile.consumerKey(), oAuth10AuthEntry::setConsumerKey);
        setIfNotNull(oAuth1Profile.consumerSecret(), oAuth10AuthEntry::setConsumerSecret);
        setIfNotNull(oAuth1Profile.token(), oAuth10AuthEntry::setAccessToken);
        setIfNotNull(oAuth1Profile.tokenSecret(), oAuth10AuthEntry::setTokenSecret);
        setIfNotNull(oAuth1Profile.callback(), oAuth10AuthEntry::setRedirectURI);

        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createOAuth2Profile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        OAuth2Profile oAuth2Profile = OBJECT_MAPPER.readValue(authString, OAuth2Profile.class);

        AuthEntries.OAuth20AuthEntry oAuth20AuthEntry = (AuthEntries.OAuth20AuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.O_AUTH_2_0, profileName);
        setIfNotNull(oAuth2Profile.clientId(), oAuth20AuthEntry::setClientID);
        setIfNotNull(oAuth2Profile.clientSecret(), oAuth20AuthEntry::setClientSecret);
        setIfNotNull(oAuth2Profile.accessTokenUrl(), oAuth20AuthEntry::setAccessTokenURI);
        setIfNotNull(oAuth2Profile.authUrl(), oAuth20AuthEntry::setAuthorizationURI);
        setIfNotNull(oAuth2Profile.redirect_uri(), oAuth20AuthEntry::setRedirectURI);
        setIfNotNull(oAuth2Profile.scope(), oAuth20AuthEntry::setScope);
        setIfNotNull(oAuth2Profile.state(), oAuth20AuthEntry::setState);

        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createNtlmProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        NtlmProfile ntlmProfile = OBJECT_MAPPER.readValue(authString, NtlmProfile.class);

        AuthEntries.NTLMAuthEntry ntlmAuthEntry = (AuthEntries.NTLMAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.NTLM, profileName);
        setIfNotNull(ntlmProfile.username(), ntlmAuthEntry::setUsername);
        setIfNotNull(ntlmProfile.password(), ntlmAuthEntry::setPassword);
        setIfNotNull(ntlmProfile.domain(), ntlmAuthEntry::setDomain);

        objectToAttachAuth.setAuthProfile(profileName);
    }

    private void createDigestProfile(String authString, String profileName, AuthProfileHolderContainer objectToAttachAuth) throws JsonProcessingException {
        DigestProfile digestProfile = OBJECT_MAPPER.readValue(authString, DigestProfile.class);

        AuthEntries.DigestAuthEntry digestAuthEntry = (AuthEntries.DigestAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.DIGEST, profileName);
        setIfNotNull(digestProfile.username(), digestAuthEntry::setUsername);
        setIfNotNull(digestProfile.password(), digestAuthEntry::setPassword);

        objectToAttachAuth.setAuthProfile(profileName);
    }

    private String getAuthProfileString(JSONObject authProfileJson, String authType) throws JSONException, JsonProcessingException {
        if (collectionVersion.equals(PostmanCollectionFactory.VERSION_2_1)) {
            String authProfileContent = authProfileJson.getJSONArray(authType).toString();
            return mapKeyValueAuthProfileToObject(authProfileContent);
        }
        return authProfileJson.getJSONObject(authType).toString();
    }

    private <T> void setIfNotNull(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
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
