package com.smartbear.ready.plugin.postman.collection.authorization;

public record AwsSignatureProfile(String accessKey, String secretKey, String service, String region) implements PostmanAuthProfile {

}