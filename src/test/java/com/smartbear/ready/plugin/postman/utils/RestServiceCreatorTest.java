package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlProjectFactory;
import com.eviware.soapui.support.SoapUIException;
import com.google.common.collect.ImmutableMap;
import com.smartbear.ready.plugin.postman.collection.PostmanCollection;
import com.smartbear.ready.plugin.postman.collection.PostmanCollectionFactory;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class RestServiceCreatorTest {
    WsdlProject project;
    RestServiceCreator creator;

    @BeforeEach
    public void setUp() throws XmlException, IOException, SoapUIException {
        project = new WsdlProjectFactory().createNew();
        creator = spy(new RestServiceCreator(project));
    }

    @Test
    public void importFormData() throws Exception {
        // given
        PostmanCollection collection = getCollectionFromFile(RestServiceCreatorTest.class.getResource("/rest/multipart_collection.json"));

        URL attachmentUri = RestServiceCreatorTest.class.getResource("/rest/attachment.txt");
        doReturn(new File(attachmentUri.getPath())).when(creator).getAttachmentFile(any());

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
                .put("file", attachmentUri.toString())
                .put("variable", "${#Project#variableValue}")
                .build();

        assertThat(request.getPropertyCount(), is(params.size()));
        params.forEach((name, value) -> assertThat(request.getPropertyValue(name), equalTo(value)));

        assertThat(request.getAttachmentCount(), is(1));

        assertThat(request.getMediaType(), equalTo(MediaType.MULTIPART_FORM_DATA));
        assertThat(request.isPostQueryString(), is(true));
    }

    private PostmanCollection getCollectionFromFile(URL collectionUrl) throws Exception {
        String postmanJson = IOUtils.toString(
                collectionUrl,
                StandardCharsets.UTF_8
        );
        JSON json = new PostmanJsonUtil().parseTrimmedText(postmanJson);
        return PostmanCollectionFactory.getCollection((JSONObject) json);
    }
}
