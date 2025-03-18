package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public record AwsSignatureProfile(String accessKey, String secretKey, String service, String region, String sessionToken) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.AWS_SIGNATURE;
    }

    @Override
    public void createAuthEntry(String profileName, WsdlProject project) {
        AuthEntries.AwsSignatureAuthEntry awsSignatureAuthEntry = (AuthEntries.AwsSignatureAuthEntry) project.getAuthRepository()
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(accessKey, awsSignatureAuthEntry::setAccessKey, project);
        setValueIfNotNull(secretKey, awsSignatureAuthEntry::setSecretAccessKey, project);
        setValueIfNotNull(region, awsSignatureAuthEntry::setRegion, project);
        setValueIfNotNull(service, awsSignatureAuthEntry::setServiceName, project);
        setValueIfNotNull(sessionToken, awsSignatureAuthEntry::setSecurityToken, project);
    }
}