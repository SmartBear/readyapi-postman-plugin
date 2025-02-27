package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public record BasicAuthProfile (String username, String password) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.BASIC;
    }

    @Override
    public void createAuthEntry(String profileName, WsdlProject project) {
        AuthEntries.BasicAuthEntry basicAuthEntry = (AuthEntries.BasicAuthEntry) project.getAuthRepository()
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(password, basicAuthEntry::setPassword, project);
        setValueIfNotNull(username, basicAuthEntry::setUsername, project);
    }
}