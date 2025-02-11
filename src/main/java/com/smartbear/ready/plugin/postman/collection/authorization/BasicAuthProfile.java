package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record BasicAuthProfile (String username, String password) implements PostmanAuthProfile {

    public void createBasicAuthEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.BasicAuthEntry basicAuthEntry = (AuthEntries.BasicAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.BASIC, profileName);
        setValueIfNotNull(password(), basicAuthEntry::setPassword);
        setValueIfNotNull(username(), basicAuthEntry::setUsername);
    }
}