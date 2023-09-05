package com.smartbear.postman.utils;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.support.SoapUIException;
import com.google.common.collect.ImmutableMap;
import com.smartbear.postman.collection.PostmanCollection;
import com.smartbear.postman.collection.PostmanCollectionFactory;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RestServiceCreatorTest {
    WsdlProject project;
    RestServiceCreator creator;

    @Before
    public void setUp() throws XmlException, IOException, SoapUIException {
        project = new WsdlProjectFactory().createNew();
        creator = new RestServiceCreator(project);
    }

    @Test
    public void importFormData() throws IOException {
        // given
        PostmanCollection collection = getCollectionFromFile(RestServiceCreatorTest.class.getResource("/rest/multipart_collection.json"));

        // when
        collection.getRequests().forEach(creator::addRestRequest);

        // then
        RestService service = (RestService) project.getInterfaceAt(0);
        RestRequest request = service.getResourceList().get(0).getRestMethodAt(0).getRequestAt(0);

        Map<String, String> params = new ImmutableMap.Builder<String, String>()
                .put("pl", "'a'")
                .put("qu", "\"b\"")
                .put("sa", "d")
                .put("Special", "!@#$%&*()^_+=`~")
                .put("Not Select", "Disabled")
                .put("more", ",./';[]}{\":?><|\\\\")
                .build();

        params.forEach((name, value) -> assertThat(request.getPropertyValue(name), equalTo(value)));

        assertThat(request.getMediaType(), equalTo(MediaType.MULTIPART_FORM_DATA));
        assertThat(request.isPostQueryString(), is(true));
    }

    private PostmanCollection getCollectionFromFile(URL collectionUrl) throws IOException {
        String postmanJson = IOUtils.toString(
                collectionUrl,
                StandardCharsets.UTF_8
        );
        JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
        return PostmanCollectionFactory.getCollection((JSONObject) json);
    }
}
