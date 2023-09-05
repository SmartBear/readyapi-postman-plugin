package com.smartbear.postman.collection;

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
import static org.junit.Assert.assertNull;

public class PostmanCollectionFactoryTest {
    private final String COLLECTION_V1_PATH = "src/test/resources/Postman_Echo.postman_collection";
    private final String COLLECTION_V2_PATH = "src/test/resources/Postman_Echo.postman_collection_v2.0";
    private final String COLLECTION_V2_1PATH = "src/test/resources/Postman_Echo.postman_collection_v2.1";
    private final String COLLECTION_FROM_URL_PATH = "src/test/resources/Postman_Echo_from_url.postman_collection_v2.1";

    @Test
    public void postmanCollectionV1ImportIsNull() throws IOException {
        PostmanCollection postmanCollection = getCollectionFromFile(COLLECTION_V1_PATH);
        assertNull(postmanCollection);
    }

    @Test
    public void postmanCollectionV2ImportsAsV2() throws IOException {
        PostmanCollection postmanCollection = getCollectionFromFile(COLLECTION_V2_PATH);
        assertThat(postmanCollection, instanceOf(PostmanCollectionV2.class));
    }

    @Test
    public void postmanCollectionV2_1ImportsAsV2_1() throws IOException {
        PostmanCollection postmanCollection = getCollectionFromFile(COLLECTION_V2_1PATH);
        assertThat(postmanCollection, instanceOf(PostmanCollectionV2_1.class));
    }

    @Test
    public void postmanCollectionV2_1FromURLImportsAsV2_1() throws IOException {
        PostmanCollection postmanCollection = getCollectionFromFile(COLLECTION_FROM_URL_PATH);
        assertThat(postmanCollection, instanceOf(PostmanCollectionV2_1.class));
    }

    private PostmanCollection getCollectionFromFile(String collectionFilePath) throws IOException {
        String postmanJson = FileUtils.readFileToString(new File(collectionFilePath), StandardCharsets.UTF_8);
        JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
        return PostmanCollectionFactory.getCollection((JSONObject) json);
    }

}
