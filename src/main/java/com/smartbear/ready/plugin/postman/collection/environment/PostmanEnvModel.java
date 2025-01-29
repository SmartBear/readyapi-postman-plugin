package com.smartbear.ready.plugin.postman.collection.environment;

import java.util.List;

public record PostmanEnvModel (String id, String name, List<PostmanEnvVariable> values) {}