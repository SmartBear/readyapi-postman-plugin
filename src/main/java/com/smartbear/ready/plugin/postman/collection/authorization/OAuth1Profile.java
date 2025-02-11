package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record OAuth1Profile(String consumerSecret, String consumerKey, String token, String tokenSecret, String callback) implements PostmanAuthProfile {

    public void createOAuth1Entry(String profileName, AuthRepository authRepository) {
        AuthEntries.OAuth10AuthEntry oAuth10AuthEntry = (AuthEntries.OAuth10AuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.O_AUTH_1_0, profileName);
        setValueIfNotNull(consumerKey(), oAuth10AuthEntry::setConsumerKey);
        setValueIfNotNull(consumerSecret(), oAuth10AuthEntry::setConsumerSecret);
        setValueIfNotNull(token(), oAuth10AuthEntry::setAccessToken);
        setValueIfNotNull(tokenSecret(), oAuth10AuthEntry::setTokenSecret);
        setValueIfNotNull(callback(), oAuth10AuthEntry::setRedirectURI);
    }
}