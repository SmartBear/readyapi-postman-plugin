package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record BasicAuthProfile (String username, String password) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.BASIC;
    }

    @Override
    public void createAuthEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.BasicAuthEntry basicAuthEntry = (AuthEntries.BasicAuthEntry) authRepository
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(password, basicAuthEntry::setPassword);
        setValueIfNotNull(username, basicAuthEntry::setUsername);
    }
}