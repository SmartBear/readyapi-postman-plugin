package com.smartbear.ready.plugin.postman.collection;

import javafx.util.Pair;
import net.sf.json.JSONObject;

public class RequestAuthProfile {

    private final String profileName;
    private final JSONObject authProfile;

    public RequestAuthProfile(Request request) {
        if (request.getRequestAuth() == null) {
            Pair<String, JSONObject> directoryAuthAndName = getParentFolderAuthProfile(request.getDirectory());
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

    public JSONObject getAuthProfile() {
        return authProfile;
    }

    private Pair<String, JSONObject> getParentFolderAuthProfile(DirectoryInfo directory) {
        if (directory == null) {
            return new Pair<>("", null);
        }
        if ((directory.getAuthProfile() == null)) {
            return getParentFolderAuthProfile(directory.getParent());
        }
        return new Pair<>(directory.getPath(), directory.getAuthProfile());
    }
}
