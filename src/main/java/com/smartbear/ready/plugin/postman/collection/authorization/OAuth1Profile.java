package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AccessTokenPositionConfig;
import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public record OAuth1Profile(String consumerSecret, String consumerKey, String token, String tokenSecret, String callback,
                            Boolean addParamsToHeader) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.O_AUTH_1_0;
    }

    @Override
    public void createAuthEntry(String profileName, WsdlProject project) {
        AuthEntries.OAuth10AuthEntry oAuth10AuthEntry = (AuthEntries.OAuth10AuthEntry) project.getAuthRepository()
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(consumerKey, oAuth10AuthEntry::setConsumerKey, project);
        setValueIfNotNull(consumerSecret, oAuth10AuthEntry::setConsumerSecret, project);
        setValueIfNotNull(token, oAuth10AuthEntry::setAccessToken, project);
        setValueIfNotNull(tokenSecret, oAuth10AuthEntry::setTokenSecret, project);
        setValueIfNotNull(callback, oAuth10AuthEntry::setRedirectURI, project);
        if (Boolean.TRUE.equals(addParamsToHeader)) {
            oAuth10AuthEntry.setAccessTokenPosition(AccessTokenPositionConfig.HEADER);
        } else {
            oAuth10AuthEntry.setAccessTokenPosition(AccessTokenPositionConfig.QUERY);
        }
    }
}