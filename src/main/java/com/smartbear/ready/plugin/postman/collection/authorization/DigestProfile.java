package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public record DigestProfile(String username, String password) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.DIGEST;
    }

    @Override
    public void createAuthEntry(String profileName, WsdlProject project) {
        AuthEntries.DigestAuthEntry digestAuthEntry = (AuthEntries.DigestAuthEntry) project.getAuthRepository()
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(username, digestAuthEntry::setUsername, project);
        setValueIfNotNull(password, digestAuthEntry::setPassword, project);
    }
}


