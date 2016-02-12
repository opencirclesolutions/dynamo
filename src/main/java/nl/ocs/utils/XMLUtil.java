package nl.ocs.utils;

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
 * 
 */
public class XMLUtil {

	private static Map<String, String> NAMESPACES = new ConcurrentHashMap<>();

	static {
		NAMESPACES.put("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
	}

	private XMLUtil() {
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
	public static Set<String> getSamlAttributeValues(org.dom4j.Document document,
			String attributeName) {

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
	 * 
	 * @param document
	 * @param xpathString
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
	 * Extracts all values that match a certain xpath expression from a document
	 * 
	 * @param document
	 *            the document
	 * @param xpathString
	 *            the xpath expression
	 * @return
	 */
	private static Set<String> getValues(Document document, String xpathString) {
		Set<String> result = new HashSet<String>();

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
