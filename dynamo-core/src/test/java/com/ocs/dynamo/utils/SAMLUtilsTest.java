package com.ocs.dynamo.utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SAMLUtilsTest {

    @Test
    public void testGetAttributeValue() throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document d = builder.parse(new File("src/test/resources/saml_example.xml"));
        Assert.assertNotNull(d);

        org.dom4j.Document doc = SAMLUtils.toJDOM(d);
        Assert.assertNotNull(doc);

        String value = SAMLUtils.getSamlAttributeValue(doc,
                "http://schemas.microsoft.com/identity/claims/displayname");
        Assert.assertEquals("Patrick Deenen", value);

        value = SAMLUtils.getSamlAttributeValue(doc,
                "http://schemas.microsoft.com/identity/claims/tenantid");
        Assert.assertEquals("3d5fd6d4-0c78-4a8e-bfbd-2692babbe70c", value);

        value = SAMLUtils.getSamlAttributeValue(doc,
                "http://schemas.microsoft.com/identity/claims/not_there");
        Assert.assertNull(value);
    }

    @Test
    public void testGetAttributeValues() throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document d = builder.parse(new File("src/test/resources/saml_example.xml"));
        Assert.assertNotNull(d);

        org.dom4j.Document doc = SAMLUtils.toJDOM(d);
        Assert.assertNotNull(doc);

        Set<String> values = SAMLUtils.getSamlAttributeValues(doc,
                "http://schemas.microsoft.com/ws/2008/06/identity/claims/groups");
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.contains("b7beb37e-1aca-4a66-aead-b056c582bced"));
        Assert.assertTrue(values.contains("c670fc9b-90ee-4f49-aada-7f400769295f"));

    }

}
