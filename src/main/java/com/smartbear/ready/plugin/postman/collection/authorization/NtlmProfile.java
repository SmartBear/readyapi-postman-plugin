package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public record NtlmProfile(String username, String password, String domain) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.NTLM;
    }

    @Override
    public void createAuthEntry(String profileName, WsdlProject project) {
        AuthEntries.NTLMAuthEntry ntlmAuthEntry = (AuthEntries.NTLMAuthEntry) project.getAuthRepository()
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(username, ntlmAuthEntry::setUsername, project);
        setValueIfNotNull(password, ntlmAuthEntry::setPassword, project);
        setValueIfNotNull(domain, ntlmAuthEntry::setDomain, project);
    }
}