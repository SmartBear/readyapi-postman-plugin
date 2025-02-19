package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record NtlmProfile(String username, String password, String domain) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.NTLM;
    }

    @Override
    public void createAuthEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.NTLMAuthEntry ntlmAuthEntry = (AuthEntries.NTLMAuthEntry) authRepository
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(username, ntlmAuthEntry::setUsername);
        setValueIfNotNull(password, ntlmAuthEntry::setPassword);
        setValueIfNotNull(domain, ntlmAuthEntry::setDomain);
    }
}