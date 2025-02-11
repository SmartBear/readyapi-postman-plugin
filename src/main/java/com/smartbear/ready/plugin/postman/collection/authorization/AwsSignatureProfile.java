package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.AuthRepository.AuthEntries;
import com.eviware.soapui.impl.AuthRepository.AuthRepository;

public record AwsSignatureProfile(String accessKey, String secretKey, String service, String region) implements PostmanAuthProfile {

    public void createAwsSignatureEntry(String profileName, AuthRepository authRepository) {
        AuthEntries.AwsSignatureAuthEntry awsSignatureAuthEntry = (AuthEntries.AwsSignatureAuthEntry) authRepository
                .createEntry(AuthEntryTypeConfig.AWS_SIGNATURE, profileName);
        setValueIfNotNull(accessKey(), awsSignatureAuthEntry::setAccessKey);
        setValueIfNotNull(secretKey(), awsSignatureAuthEntry::setSecretAccessKey);
        setValueIfNotNull(region(), awsSignatureAuthEntry::setRegion);
        setValueIfNotNull(service(), awsSignatureAuthEntry::setServiceName);
    }
}