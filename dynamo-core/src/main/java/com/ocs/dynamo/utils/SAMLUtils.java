/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.DOMReader;

/**
 * Various utility methods for dealing with SAML and XML
 */
public final class SAMLUtils {

    private static final Map<String, String> NAMESPACES = new ConcurrentHashMap<>();

    static {
        NAMESPACES.put("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
    }

    private SAMLUtils() {
        // default hidden constructor
    }

    /**
     * Converts a W3C DOM document to a dom4j document
     * 
     * @param input
     *            the W3C document to convert
     * @return
     */
    public static org.dom4j.Document toJDOM(org.w3c.dom.Document input) {
        DOMReader reader = new DOMReader();
        return reader.read(input);
    }

    /**
     * Returns the (first) value of a SAML attribute with a certain name
     * 
     * @param document
     *            the SAML document
     * @param attributeName
     *            the name of the attribute (must be a fully qualified name)
     * @return
     */
    public static String getSamlAttributeValue(org.dom4j.Document document, String attributeName) {

        // first, try without namespaces
        String value = getFirstValue(document, "//AttributeStatement/Attribute[@Name='"
                + attributeName + "']/AttributeValue");
        if (value == null) {
            // if that does not work, try with namespaces
            value = getFirstValue(document, "//saml:AttributeStatement/saml:Attribute[@Name='"
                    + attributeName + "']/saml:AttributeValue");
        }
        return value;
    }

    /**
     * Returns the values of all SAML attributes with the specified name
     * 
     * @param document
     *            the dom4j document to scan
     * @param attributeName
     *            the name of the attribute
     * @return
     */
    public static Set<String> getSamlAttributeValues(Document document, String attributeName) {

        // first, try without namespaces
        Set<String> values = getValues(document, "//AttributeStatement/Attribute[@Name='"
                + attributeName + "']/AttributeValue");
        if (values == null || values.isEmpty()) {
            // if that does not work, try with namespaces
            values = getValues(document, "//saml:AttributeStatement/saml:Attribute[@Name='"
                    + attributeName + "']/saml:AttributeValue");
        }
        return values;
    }

    /**
     * Returns the value of the first attribute that matches the provided XPath query
     * 
     * @param document
     *            the document to query
     * @param xpathString
     *            the XPath query
     */
    private static String getFirstValue(Document document, String xpathString) {
        XPath xpath = document.createXPath(xpathString);
        xpath.setNamespaceURIs(NAMESPACES);
        List<?> nodes = xpath.selectNodes(document);
        if (!nodes.isEmpty()) {
            Element element = (Element) nodes.get(0);
            return element.getText();
        }
        return null;
    }

    /**
     * Extracts all values that match a certain XPath expression from a document
     * 
     * @param document
     *            the document
     * @param xpathString
     *            the XPath expression
     * @return
     */
    private static Set<String> getValues(Document document, String xpathString) {
        Set<String> result = new HashSet<>();

        XPath xpath = document.createXPath(xpathString);
        xpath.setNamespaceURIs(NAMESPACES);
        List<?> nodes = xpath.selectNodes(document);

        for (Object n : nodes) {
            Element element = (Element) n;
            result.add(element.getText());
        }

        return result;
    }

}
