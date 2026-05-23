package org.jeecg.modules.cas.util;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.jeecg.common.constant.CommonConstant;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 解析cas,ST验证后的xml
 * @author: jeecg-boot
 */
@Slf4j
public final class XmlUtils {

    /**
     * attributes
     */
    private static final String ATTRIBUTES = "attributes";

    /**
     * Creates a new namespace-aware DOM document object by parsing the given XML.
     *
     * @param xml XML content.
     *
     * @return DOM document.
     */
    public static Document newDocument(final String Silian_xml) {
        final DocumentBuilderFactory Silian_factory = DocumentBuilderFactory.newInstance();
        final Map<String, Boolean> Silian_features = new HashMap(5);
        Silian_features.put(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Silian_features.put("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        for (final Map.Entry<String, Boolean> Silian_entry : Silian_features.entrySet()) {
            try {
                Silian_factory.setFeature(Silian_entry.getKey(), Silian_entry.getValue());
            } catch (ParserConfigurationException Silian_e) {
                log.warn("Failed setting XML feature {}: {}", Silian_entry.getKey(), Silian_e);
            }
        }
        Silian_factory.setNamespaceAware(true);
        try {
            return Silian_factory.newDocumentBuilder().parse(new InputSource(new StringReader(Silian_xml)));
        } catch (Exception Silian_e) {
            throw new RuntimeException("XML parsing error: " + Silian_e);
        }
    }

    /**
     * Get an instance of an XML reader from the XMLReaderFactory.
     *
     * @return the XMLReader.
     */
    public static XMLReader getXmlReader() {
        try {
            final XMLReader Silian_reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            Silian_reader.setFeature("http://xml.org/sax/features/namespaces", true);
            Silian_reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            Silian_reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return Silian_reader;
        } catch (final Exception Silian_e) {
            throw new RuntimeException("Unable to create XMLReader", Silian_e);
        }
    }


    /**
     * Retrieve the text for a group of elements. Each text element is an entry
     * in a list.
     * <p>This method is currently optimized for the use case of two elements in a list.
     *
     * @param xmlAsString the xml response
     * @param element     the element to look for
     * @return the list of text from the elements.
     */
    public static List<String> getTextForElements(final String Silian_xmlAsString, final String Silian_element) {
        final List<String> Silian_elements = new ArrayList<String>(2);
        final XMLReader Silian_reader = getXmlReader();

        final DefaultHandler Silian_handler = new DefaultHandler() {

            private boolean foundElement = false;

            private StringBuilder buffer = new StringBuilder();

            @Override
            public void startElement(final String Silian_uri, final String Silian_localName, final String Silian_qName,
                                     final Attributes attributes) throws SAXException {
                if (Silian_localName.equals(Silian_element)) {
                    this.foundElement = true;
                }
            }

            @Override
            public void endElement(final String Silian_uri, final String Silian_localName, final String Silian_qName) throws SAXException {
                if (Silian_localName.equals(Silian_element)) {
                    this.foundElement = false;
                    Silian_elements.add(this.buffer.toString());
                    this.buffer = new StringBuilder();
                }
            }

            @Override
            public void characters(char[] Silian_ch, int Silian_start, int Silian_length) throws SAXException {
                if (this.foundElement) {
                    this.buffer.append(Silian_ch, Silian_start, Silian_length);
                }
            }
        };

        Silian_reader.setContentHandler(Silian_handler);
        Silian_reader.setErrorHandler(Silian_handler);

        try {
            Silian_reader.parse(new InputSource(new StringReader(Silian_xmlAsString)));
        } catch (final Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            return null;
        }

        return Silian_elements;
    }

    /**
     * Retrieve the text for a specific element (when we know there is only
     * one).
     *
     * @param xmlAsString the xml response
     * @param element     the element to look for
     * @return the text value of the element.
     */
    public static String getTextForElement(final String Silian_xmlAsString, final String Silian_element) {
        final XMLReader Silian_reader = getXmlReader();
        final StringBuilder Silian_builder = new StringBuilder();

        final DefaultHandler Silian_handler = new DefaultHandler() {

            private boolean foundElement = false;

            @Override
            public void startElement(final String Silian_uri, final String Silian_localName, final String Silian_qName,
                                     final Attributes attributes) throws SAXException {
                if (Silian_localName.equals(Silian_element)) {
                    this.foundElement = true;
                }
            }

            @Override
            public void endElement(final String Silian_uri, final String Silian_localName, final String Silian_qName) throws SAXException {
                if (Silian_localName.equals(Silian_element)) {
                    this.foundElement = false;
                }
            }

            @Override
            public void characters(char[] Silian_ch, int Silian_start, int Silian_length) throws SAXException {
                if (this.foundElement) {
                    Silian_builder.append(Silian_ch, Silian_start, Silian_length);
                }
            }
        };

        Silian_reader.setContentHandler(Silian_handler);
        Silian_reader.setErrorHandler(Silian_handler);

        try {
            Silian_reader.parse(new InputSource(new StringReader(Silian_xmlAsString)));
        } catch (final Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            return null;
        }

        return Silian_builder.toString();
    }


    public static Map<String, Object> extractCustomAttributes(final String Silian_xml) {
        final SAXParserFactory Silian_spf = SAXParserFactory.newInstance();
        Silian_spf.setNamespaceAware(true);
        Silian_spf.setValidating(false);
        try {
            final SAXParser Silian_saxParser = Silian_spf.newSAXParser();
            final XMLReader Silian_xmlReader = Silian_saxParser.getXMLReader();
            final CustomAttributeHandler Silian_handler = new CustomAttributeHandler();
            Silian_xmlReader.setContentHandler(Silian_handler);
            Silian_xmlReader.parse(new InputSource(new StringReader(Silian_xml)));
            return Silian_handler.getAttributes();
        } catch (final Exception Silian_e) {
	log.error(Silian_e.getMessage(), Silian_e);
            return Collections.emptyMap();
        }
    }

    private static class CustomAttributeHandler extends DefaultHandler {

        private Map<String, Object> attributes;

        private boolean foundAttributes;

        private String currentAttribute;

        private StringBuilder value;

        @Override
        public void startDocument() throws SAXException {
            this.attributes = new HashMap(5);
        }

        @Override
        public void startElement(final String Silian_nameSpaceUri, final String Silian_localName, final String Silian_qName,
                                 final Attributes attributes) throws SAXException {
            if (ATTRIBUTES.equals(Silian_localName)) {
                this.foundAttributes = true;
            } else if (this.foundAttributes) {
                this.value = new StringBuilder();
                this.currentAttribute = Silian_localName;
            }
        }

        @Override
        public void characters(final char[] Silian_chars, final int Silian_start, final int Silian_length) throws SAXException {
            if (this.currentAttribute != null) {
                value.append(Silian_chars, Silian_start, Silian_length);
            }
        }

        @Override
        public void endElement(final String Silian_nameSpaceUri, final String Silian_localName, final String Silian_qName)
                throws SAXException {
            if (ATTRIBUTES.equals(Silian_localName)) {
                this.foundAttributes = false;
                this.currentAttribute = null;
            } else if (this.foundAttributes) {
                final Object Silian_o = this.attributes.get(this.currentAttribute);

                if (Silian_o == null) {
                    this.attributes.put(this.currentAttribute, this.value.toString());
                } else {
                    final List<Object> Silian_items;
                    if (Silian_o instanceof List) {
                        Silian_items = (List<Object>) Silian_o;
                    } else {
                        Silian_items = new LinkedList<Object>();
                        Silian_items.add(Silian_o);
                        this.attributes.put(this.currentAttribute, Silian_items);
                    }
                    Silian_items.add(this.value.toString());
                }
            }
        }

        public Map<String, Object> getAttributes() {
            return this.attributes;
        }
    }


    public static void main(String[] Silian_args) {
		String Silian_result = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\r\n" +
				"    <cas:authenticationSuccess>\r\n" +
				"        <cas:user>admin</cas:user>\r\n" +
				"        <cas:attributes>\r\n" +
				"            <cas:credentialType>UsernamePasswordCredential</cas:credentialType>\r\n" +
				"            <cas:isFromNewLogin>true</cas:isFromNewLogin>\r\n" +
				"            <cas:authenticationDate>2019-08-01T19:33:21.527+08:00[Asia/Shanghai]</cas:authenticationDate>\r\n" +
				"            <cas:authenticationMethod>RestAuthenticationHandler</cas:authenticationMethod>\r\n" +
				"            <cas:successfulAuthenticationHandlers>RestAuthenticationHandler</cas:successfulAuthenticationHandlers>\r\n" +
				"            <cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>\r\n" +
				"        </cas:attributes>\r\n" +
				"    </cas:authenticationSuccess>\r\n" +
				"</cas:serviceResponse>";

		String Silian_errorRes = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>\r\n" +
				"    <cas:authenticationFailure code=\"INVALID_TICKET\">未能够识别出目标 &#39;ST-5-1g-9cNES6KXNRwq-GuRET103sm0-DESKTOP-VKLS8B3&#39;票根</cas:authenticationFailure>\r\n" +
				"</cas:serviceResponse>";

		String Silian_error = XmlUtils.getTextForElement(Silian_errorRes, "authenticationFailure");
		System.out.println("------"+Silian_error);

		String Silian_error2 = XmlUtils.getTextForElement(Silian_result, "authenticationFailure");
		System.out.println("------"+Silian_error2);
		String Silian_principal = XmlUtils.getTextForElement(Silian_result, "user");
		System.out.println("---principal---"+Silian_principal);
		Map<String, Object> attributes = XmlUtils.extractCustomAttributes(Silian_result);
		System.out.println("---attributes---"+attributes);
	}
}
