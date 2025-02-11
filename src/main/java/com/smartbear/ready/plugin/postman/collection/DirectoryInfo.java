package com.smartbear.ready.plugin.postman.collection;

import net.sf.json.JSONObject;

public class DirectoryInfo {
    private final String name;
    private final String description;

    private final DirectoryInfo parent;
    private final JSONObject authProfile;

    protected DirectoryInfo(String name, String description, DirectoryInfo parent, JSONObject authProfile) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.authProfile = authProfile;
    }

    protected static DirectoryInfo createRoot(String collectionName) {
        return new DirectoryInfo(collectionName, "", null, null);
    }

    public String getName() {
        return name;
    }

    public JSONObject getAuthProfile() {
        return authProfile;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        if (parent == null) {
            return name;
        }
        return parent.getPath() + "/" + name;
    }

    public DirectoryInfo getParent() {
        return parent;
    }
}
