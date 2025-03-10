package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record AwsSignatureProfile(String accessKey, String secretKey, String service, String region, String sessionToken) implements PostmanAuthProfile {

    @Override
    public AuthEntryTypeConfig.Enum getAuthEntryType() {
        return AuthEntryTypeConfig.AWS_SIGNATURE;
    }

    @Override
    public void createAuthEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.AwsSignatureAuthEntry awsSignatureAuthEntry = (AuthEntries.AwsSignatureAuthEntry) authRepository
                .createEntry(getAuthEntryType(), profileName);
        setValueIfNotNull(accessKey, awsSignatureAuthEntry::setAccessKey);
        setValueIfNotNull(secretKey, awsSignatureAuthEntry::setSecretAccessKey);
        setValueIfNotNull(region, awsSignatureAuthEntry::setRegion);
        setValueIfNotNull(service, awsSignatureAuthEntry::setServiceName);
        setValueIfNotNull(sessionToken, awsSignatureAuthEntry::setSecurityToken);
    }
}