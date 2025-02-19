package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.config.OAuth2FlowConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

import java.util.Map;

public record OAuth2Profile(String clientId, String clientSecret, String scope, String state, String accessTokenUrl,
                            String refreshTokenUrl, String authUrl, String redirect_uri, String grant_type,
                            String client_authentication, String addTokenTo) implements PostmanAuthProfile {

    private static final String AUTH_CODE_WITH_PKCE = "authorization_code_with_pkce";

    private static final Map<String, OAuth2FlowConfig.Enum> GRANT_TYPE_MAP = Map.of(
            "implicit", OAuth2FlowConfig.IMPLICIT_GRANT,
            AUTH_CODE_WITH_PKCE, OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT,
            "authorization_code", OAuth2FlowConfig.AUTHORIZATION_CODE_GRANT,
            "password_credentials", OAuth2FlowConfig.RESOURCE_OWNER_PASSWORD_CREDENTIALS,
            "client_credentials", OAuth2FlowConfig.CLIENT_CREDENTIALS_GRANT);

    private static final Map<String, AccessTokenPositionConfig.Enum> ADD_TOKEN_TO = Map.of(
            "queryParams", AccessTokenPositionConfig.QUERY,
            "header", AccessTokenPositionConfig.HEADER);


    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.O_AUTH_2_0;
    }

    @Override
    public void createAuthEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.OAuth20AuthEntry oAuth20AuthEntry = (AuthEntries.OAuth20AuthEntry) authRepository
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(clientId, oAuth20AuthEntry::setClientID);
        setValueIfNotNull(clientSecret, oAuth20AuthEntry::setClientSecret);
        setValueIfNotNull(accessTokenUrl, oAuth20AuthEntry::setAccessTokenURI);
        setValueIfNotNull(authUrl, oAuth20AuthEntry::setAuthorizationURI);
        setValueIfNotNull(redirect_uri, oAuth20AuthEntry::setRedirectURI);
        setValueIfNotNull(scope, oAuth20AuthEntry::setScope);
        setValueIfNotNull(state, oAuth20AuthEntry::setState);
        if (grant_type != null) {
            setValueIfNotNull(GRANT_TYPE_MAP.get(grant_type), oAuth20AuthEntry::setOAuth2Flow);
        }
        if (addTokenTo != null) {
            setValueIfNotNull(ADD_TOKEN_TO.get(addTokenTo), oAuth20AuthEntry::setAccessTokenPosition);
        }
        if (AUTH_CODE_WITH_PKCE.equals(grant_type)) {
            oAuth20AuthEntry.setEnablePKCE(true);
        }
        if ("header".equals(client_authentication)) {
            oAuth20AuthEntry.setUseAuthHeader(true);
        }
    }
}