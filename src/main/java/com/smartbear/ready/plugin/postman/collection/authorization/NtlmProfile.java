package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record NtlmProfile(String username, String password, String domain) implements PostmanAuthProfile {

    public void createNtlmEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.NTLMAuthEntry ntlmAuthEntry = (AuthEntries.NTLMAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.NTLM, profileName);
        setValueIfNotNull(username(), ntlmAuthEntry::setUsername);
        setValueIfNotNull(password(), ntlmAuthEntry::setPassword);
        setValueIfNotNull(domain(), ntlmAuthEntry::setDomain);
    }

}