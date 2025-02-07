package com.smartbear.ready.plugin.postman.collection;

public class DirectoryInfo {
    private final String name;
    private final String description;

    private final DirectoryInfo parent;
    private final String authProfile;

    protected DirectoryInfo(String name, String description, DirectoryInfo parent, String authProfile) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.authProfile = authProfile;
    }

    protected static DirectoryInfo createRoot(String collectionName) {
        return new DirectoryInfo(collectionName, "", null, "");
    }

    public String getName() {
        return name;
    }

    public String getAuthProfile() {
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
