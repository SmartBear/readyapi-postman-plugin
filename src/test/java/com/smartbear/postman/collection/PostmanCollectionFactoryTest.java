package com.smartbear.postman.collection;

import com.smartbear.postman.exceptions.PostmanCollectionUnsupportedVersionException;
import com.smartbear.postman.utils.PostmanJsonUtil;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;

public class PostmanCollectionFactoryTest {
    private final String COLLECTION_V1_PATH = "/Postman_Echo.postman_collection";
    private final String COLLECTION_V2_PATH = "/Postman_Echo.postman_collection_v2.0";
    private final String COLLECTION_V2_1PATH = "/Postman_Echo.postman_collection_v2.1";
    private final String COLLECTION_FROM_URL_PATH = "/Postman_Echo_from_url.postman_collection_v2.1";

    @Test(expected = PostmanCollectionUnsupportedVersionException.class)
    public void postmanCollectionV1ImportIsNull() throws IOException, PostmanCollectionUnsupportedVersionException {
        getCollectionFromFile(getClass().getResource(COLLECTION_V1_PATH).getPath());
    }

    @Test
    public void postmanCollectionV2ImportsAsV2() throws IOException, PostmanCollectionUnsupportedVersionException {
        PostmanCollection postmanCollection = getCollectionFromFile(
                getClass().getResource(COLLECTION_V2_PATH).getPath()
        );
        assertThat(postmanCollection, instanceOf(PostmanCollectionV2.class));
    }

    @Test
    public void postmanCollectionV2_1ImportsAsV2() throws IOException, PostmanCollectionUnsupportedVersionException {
        PostmanCollection postmanCollection = getCollectionFromFile(
                getClass().getResource(COLLECTION_V2_1PATH).getPath()
        );
        assertThat(postmanCollection, instanceOf(PostmanCollectionV2.class));
    }

    @Test
    public void postmanCollectionV2_1FromURLImportsAsV2() throws IOException, PostmanCollectionUnsupportedVersionException {
        PostmanCollection postmanCollection = getCollectionFromFile(
                getClass().getResource(COLLECTION_FROM_URL_PATH).getPath()
        );
        assertThat(postmanCollection, instanceOf(PostmanCollectionV2.class));
    }

    private PostmanCollection getCollectionFromFile(String collectionFilePath) throws IOException, PostmanCollectionUnsupportedVersionException {
        String postmanJson = FileUtils.readFileToString(new File(collectionFilePath), StandardCharsets.UTF_8);
        JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
        return PostmanCollectionFactory.getCollection((JSONObject) json);
    }

}
