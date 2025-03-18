package com.smartbear.ready.plugin.postman.utils;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import com.smartbear.ready.plugin.postman.VariableUtils;
import com.smartbear.ready.plugin.postman.collection.PostmanCollection;
import com.smartbear.ready.plugin.postman.collection.Request;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SoapServiceCreator {
    private static final String WSDL_SUFFIX = "?WSDL";
    private static final Logger logger = LoggerFactory.getLogger(SoapServiceCreator.class);

    private final WsdlProject project;

    public SoapServiceCreator(WsdlProject project) {
        this.project = project;
    }

    public WsdlRequest addSoapRequest(Request request) {
        final String url = getRequestWsdlUrl(request.getUrl());

        List<WsdlInterface> wsdlInterfaces = getWsdlInterfaces(url);

        WsdlInterface bindingWithCorrectVersion = wsdlInterfaces.stream()
                .filter(iface -> iface.getWsdlContext().getSoapVersion().equals(request.getSoapVersion()))
                .findFirst()
                .orElseThrow(() -> new SoapImportingException("Could not find interface with soap version: " + request.getSoapVersion() + " for url: " + url));

        String operationName = getOperationName(request.getBody());
        WsdlOperation operation = bindingWithCorrectVersion.getOperationByName(operationName);

        return createWsdlRequest(request, operation);
    }

    private String getRequestWsdlUrl(String url) {
        return shouldAppendWsdl(url) ? url + WSDL_SUFFIX : url;
    }

    private boolean shouldAppendWsdl(String url) {
        return StringUtils.hasContent(url) && !url.toUpperCase().endsWith(WSDL_SUFFIX);
    }

    private List<WsdlInterface> getWsdlInterfaces(String url) {
        List<WsdlInterface> wsdlInterfaces = new LinkedList<>();

        if (!interfaceAlreadyExists(url)) {
            logger.info("Interface with url: {} does not exist, importing it", url);
            try {
                WsdlInterface[] wsdlInterfacesArray = WsdlInterfaceFactory.importWsdl(project, url, false);

                wsdlInterfaces.addAll(Arrays.asList(wsdlInterfacesArray));
            } catch (InvalidDefinitionException e) {
                throw new SoapImportingException("Error importing WSDL from: " + url + " error message: " + e.getDetailedMessage());
            } catch (SoapUIException e) {
                throw new SoapImportingException("Error importing WSDL from: " + url + " error message: " + e.getMessage());
            }
        } else {
            logger.info("Interface with url: {} already exists", url);
            project.getInterfaceList().stream()
                    .filter(WsdlInterface.class::isInstance)
                    .map(WsdlInterface.class::cast)
                    .filter(iface -> iface.getWsdlContext().getUrl().equals(url))
                    .forEach(wsdlInterfaces::add);
        }
        return wsdlInterfaces;
    }

    private boolean interfaceAlreadyExists(String uri) {
        return project.getInterfaceList().stream()
                .filter(WsdlInterface.class::isInstance)
                .map(WsdlInterface.class::cast)
                .anyMatch(iface -> iface.getDefinition().equals(uri));
    }

    private String getOperationName(String xml) {
        try {
            XmlObject xmlObject = XmlUtils.createXmlObject(xml, new XmlOptions());
            String xpath = "//*:Body/*[1]";
            XmlObject[] nodes = xmlObject.selectPath(xpath);
            if (nodes.length > 0) {
                return nodes[0].getDomNode().getLocalName();
            }
        } catch (XmlException e) {
            e.printStackTrace();
        }
        return null;
    }

    private WsdlRequest createWsdlRequest(Request request, WsdlOperation operation) {
        WsdlRequest wsdlRequest = null;
        if (operation != null) {
            logger.info("Creating [ {} ] wsdl request", request.getName());
            wsdlRequest = operation.addNewRequest(request.getName());
            wsdlRequest.setRequestContent(request.getBody());

            wsdlRequest.setDescription("Imported from Postman collection, original directory: [" + request.getDirectoryPath() + "]");
            addHttpHeaders(wsdlRequest, request.getHeaders(), project);
        }
        return wsdlRequest;
    }

    private void addHttpHeaders(WsdlRequest request, List<PostmanCollection.Header> headers,
                                WsdlProject projectToAddProperties) {
        for (PostmanCollection.Header header : headers) {
            StringToStringsMap headersMap = request.getRequestHeaders();
            headersMap.add(header.getKey(), VariableUtils.convertVariables(header.getValue(), projectToAddProperties));
            request.setRequestHeaders(headersMap);
        }
    }

    private static class SoapImportingException extends RuntimeException {
        public SoapImportingException(String message) {
            super(message);
        }
    }
}
