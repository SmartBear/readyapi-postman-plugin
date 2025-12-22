package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.rest.RestURIParser;

public class RestURIPostmanParser implements RestURIParser {
    private String resourcePath = "";
    private String query = "";
    private String scheme = "";
    private String authority = "";

    public RestURIPostmanParser(String uriString) {
        parseManually(uriString);
    }

    @Override
    public String getEndpoint() {
        String endpoint;
        if (this.authority.isEmpty()) {
            endpoint = "";
        } else if (this.scheme.isEmpty()) {
            endpoint = DEFAULT_SCHEME + SCHEME_SEPARATOR + authority;
        } else {
            endpoint = scheme + SCHEME_SEPARATOR + authority;
        }

        return endpoint;
    }

    @Override
    public String getResourceName() {
        String path = this.getResourcePath();
        if (path.isEmpty()) {
            return path;
        }

        String[] splitResourcePath = path.split("/");
        if (splitResourcePath.length == 0) {
            return "";
        }

        String resourceName = splitResourcePath[splitResourcePath.length - 1];
        if (resourceName.startsWith(";")) {
            return "";
        }

        resourceName = resourceName.replaceAll("\\{", "").replaceAll("\\}", "");
        if (resourceName.contains(";")) {
            resourceName = resourceName.substring(0, resourceName.indexOf(";"));
        }

        return resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String getResourcePath() {
        String path = resourcePath;
        path = addPrefixSlash(path);

        return path;
    }

    @Override
    public String getQuery() {
        return query;
    }

    private String addPrefixSlash(String path) {
        if (!path.startsWith("/") && !path.isEmpty()) {
            path = "/" + path;
        }
        return path;
    }

    private void parseManually(String uriString) {
        int endIndexOfScheme = uriString.indexOf(SCHEME_SEPARATOR);
        if (endIndexOfScheme >= 0 ) {
            scheme = uriString.substring(0, endIndexOfScheme);
        }

        int startIndexOfQuery = uriString.indexOf("?");
        if (startIndexOfQuery >= 0) {
            query = uriString.substring(startIndexOfQuery + 1);
            resourcePath = uriString.substring(endIndexOfScheme + SCHEME_SEPARATOR.length(), startIndexOfQuery);
        }

        int startIndexOfResource = resourcePath.indexOf("/");
        if (startIndexOfResource >= 0) {
            authority = resourcePath.substring(0, startIndexOfResource);
            resourcePath = resourcePath.substring(startIndexOfResource);
        } else {
            authority = resourcePath;
            resourcePath = "";
        }
    }
}
