package com.smartbear.ready.plugin.postman.collection.authorization;

import com.eviware.soapui.config.AuthEntryTypeConfig;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.smartbear.ready.plugin.postman.utils.VariableUtils;

import java.util.Optional;
import java.util.function.Consumer;

public interface PostmanAuthProfile {

    void createAuthEntry(String profileName, WsdlProject project);

    AuthEntryTypeConfig.Enum getAuthEntryType();

    default <T> void setValueIfNotNull(T value, Consumer<T> setter) {
        Optional.ofNullable(value).ifPresent(setter);
    }

    @SuppressWarnings("unchecked")
    default <T> void setValueIfNotNull(T value, Consumer<T> setter, WsdlProject project) {
        if (value instanceof String string) {
            value = (T) VariableUtils.convertVariables(string, project);
        }
        setValueIfNotNull(value, setter);
    }

}
