package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.impl.AuthRepository.AuthRepository;

import java.util.Optional;
import java.util.function.Consumer;

public interface PostmanAuthProfile {

    void createAuthEntry(String profileName, AuthRepository authRepository);

    default <T> void setValueIfNotNull(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }
}
