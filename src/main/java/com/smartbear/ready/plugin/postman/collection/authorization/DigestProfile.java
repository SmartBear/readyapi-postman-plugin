package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record DigestProfile(String username, String password) implements PostmanAuthProfile {

    public void createDigestAuthEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.DigestAuthEntry digestAuthEntry = (AuthEntries.DigestAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.DIGEST, profileName);
        setValueIfNotNull(username(), digestAuthEntry::setUsername);
        setValueIfNotNull(password(), digestAuthEntry::setPassword);
    }
}


