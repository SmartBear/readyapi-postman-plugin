package com.smartbear.ready.plugin.postman.collection;

import com.eviware.soapui.support.StringUtils;
import org.apache.commons.math3.util.Pair;

public class RequestAuthProfile {

    private final String profileName;
    private final String authProfile;

    public RequestAuthProfile(Request request) {
        if (StringUtils.isNullOrEmpty(request.getRequestAuth())) {
            Pair<String, String> directoryAuthAndName = getParentFolderAuthProfile(request.getDirectory());
            this.profileName = directoryAuthAndName.getKey();
            this.authProfile = directoryAuthAndName.getValue();
        } else {
            this.profileName = request.getName();
            this.authProfile = request.getRequestAuth();
        }
    }

    public String getProfileName() {
        return profileName;
    }

    public String getAuthProfile() {
        return authProfile;
    }

    private Pair<String, String> getParentFolderAuthProfile(DirectoryInfo directory) {
        if (directory == null) {
            return new Pair<>("", "");
        }
        if (StringUtils.isNullOrEmpty(directory.getAuthProfile())) {
            return getParentFolderAuthProfile(directory.getParent());
        }
        return new Pair<>(directory.getPath(), directory.getAuthProfile());
    }
}
