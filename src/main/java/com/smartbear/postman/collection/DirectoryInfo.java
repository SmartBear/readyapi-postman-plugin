package com.smartbear.postman.collection;

public class DirectoryInfo {
    private final String name;
    private final String description;

    private DirectoryInfo parent;

    protected DirectoryInfo(String name, String description, DirectoryInfo parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
    }

    protected static DirectoryInfo createRoot(String collectionName){
        return new DirectoryInfo(collectionName, "", null);
    }

    public String getName() {
        return name;
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
}
