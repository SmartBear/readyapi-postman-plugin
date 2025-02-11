package com.smartbear.ready.plugin.postman.collection.authorization;

import java.util.Optional;
import java.util.function.Consumer;

public interface PostmanAuthProfile {

    default <T> void setValueIfNotNull(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }
}
