/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.util;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jena.atlas.logging.Log;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Create XML input methods.
 * <p>
 * External DTDs and remote entity processing is disabled to prevent XXE attacks.
 * <br/>
 * <a href="https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing">XXE Processing Problems</a>
 * <p>
 * For advice in preventing these attacks, see
 * <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet</a>
 * which has a <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java">Java section</a>.
 */
public class JenaXMLInput {

    // ---- SAX
    // RDFXMLParser
    private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    static {
        // XMLConstants.FEATURE_SECURE_PROCESSING defaults to true.
        // This can result in problems occasionally.
        //
        // It limits internal resources - one case: system property "entityExpansionLimit"
        // The default setting Java17 is 64000. RDF/XML that uses a lot of "&ent;" as a form of URI management
        // might lead to exceeding this limit.
        //
        // This limit applies to SAX/XMLreader, StAX/XMLStreamReader and StAX/XMLEventReader.
        // Illustrative code that sets the default higher if it is not already been set.
        if ( false ) {
            String keyExpansionLimit = "entityExpansionLimit";
            if ( System.getProperty(keyExpansionLimit) == null ) {
                // The system default (no setting) is 64000
                System.setProperty(keyExpansionLimit, "200000");
            }
        }
    }

    public static XMLReader createXMLReader() throws ParserConfigurationException, SAXException {
        /*
         * https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#xmlreader
         * ----
         * XMLReader
         *
         *    To protect a Java org.xml.sax.XMLReader from XXE, do this:
         *
         *    XMLReader reader = XMLReaderFactory.createXMLReader();
         *    reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         *    // This may not be strictly required as DTDs shouldn't be allowed at all, per previous line.
         *    reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         *    reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
         *    reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
         */

        SAXParser saxParser = saxParserFactory.newSAXParser();
        XMLReader xmlreader = saxParser.getXMLReader();

        // XXE : disable all DTD processing.
        // Effect: RiotException if a DTD is found.
        // However, OWL WG test files, and others, have internal entity
        // declarations in internal DTD subset ("the "[ ]"in a DOCTYPE).
        // xmlreader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // instead, silently ignore external DTDs.

        // Always disable remote DTDs (silently ignore if DTDs are allowed at all)
        xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        // and ignore external entities (silently ignore)
        xmlreader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlreader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        return xmlreader;
    }

    private static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance() ;

    static { initXMLInputFactory(xmlInputFactory); }

    // ---- StAX
    // RRX SR and RRX EV, TriX and SPARQL XML Results.
    /**
     * Initialize an XMLInputFactory to Jena settings such as protecting against XXE.
     * Return the XMLInputFactory.
     */
    public static XMLInputFactory initXMLInputFactory(XMLInputFactory xmlInputFactory) {
        /*
         * https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
         * ---
         *     // This disables DTDs entirely for that factory
         *     xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
         *
         * or if you can't completely disable DTDs:
         *
         *     // This causes XMLStreamException to be thrown if external DTDs are accessed.
         *     xmlInputFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
         *     // disable external entities
         *     xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
         */

        String name = xmlInputFactory.getClass().getName();
        boolean isWoodstox = name.startsWith("com.ctc.wstx.stax.");
        boolean isJDK = name.contains("sun.xml.internal");
        boolean isXerces = name.startsWith("org.apache.xerces");

        // This disables DTDs entirely for the factory.
        // DTDs are silently ignored except for xmlEventReader.nextTag() which throws an exception on a "DTD" event.
        // Code can peek and skip the DTD.
        // This takes precedence over ACCESS_EXTERNAL_DTD
    	setXMLInputFactoryProperty(xmlInputFactory, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        // disable external entities (silently ignore)
        setXMLInputFactoryProperty(xmlInputFactory, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

        // Not supported by Woodstox. IS_SUPPORTING_EXTERNAL_ENTITIES = false is enough.
        // Disable external DTDs (files and HTTP) - errors unless SUPPORT_DTD is false.
        if ( ! isWoodstox )
            setXMLInputFactoryProperty(xmlInputFactory, XMLConstants.ACCESS_EXTERNAL_DTD, "");

        return xmlInputFactory;
        // RRX
//        private static XMLInputFactory createXMLInputFactory() {
//            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
//            JenaXMLInput.initXMLInputFactory(xmlInputFactory);
//            // Enable character entity support.
//            xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
//            xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
//            // Set merging.
//            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
//            return xmlInputFactory;
//        }


    }

    /**
     * Catch any {@link IllegalArgumentException}, log it, and continue.
     */
    private static void setXMLInputFactoryProperty(XMLInputFactory xf, String name, Object value) {
        try {
            xf.setProperty(name, value);
        } catch(IllegalArgumentException ex) {
            Log.error(JenaXMLInput.class, "Problem setting StAX property - name: \"" +
            		name + "\" - value: \"" + value + "\" - error: " + ex.getMessage());
        }
    }

    public static XMLStreamReader newXMLStreamReader(InputStream in) throws XMLStreamException {
        return xmlInputFactory.createXMLStreamReader(in) ;
    }

    public static XMLStreamReader newXMLStreamReader(Reader in) throws XMLStreamException {
        return xmlInputFactory.createXMLStreamReader(in) ;
    }

    public static XMLEventReader newXMLEventReader(InputStream in) throws XMLStreamException {
        return xmlInputFactory.createXMLEventReader(in) ;
    }

    public static XMLEventReader newXMLEventReader(Reader in) throws XMLStreamException {
        return xmlInputFactory.createXMLEventReader(in) ;
    }

    // ---- DocumentBuilder
    // For reference - not used in Jena src/main, but is used in src/test DOM2RDFTest and MoreDOM2RDFTest
    public static DocumentBuilderFactory newDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Causes SAXParseException if there is an external entity.
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory;
    }

    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#saxbuilder
    // For reference : org.jdom2.input
//    // ---- SAXBuilder
//    public static SAXBuilder newSAXBuilder() throws ParserConfigurationException {
//        SAXBuilder builder = new SAXBuilder();
//
//        // Completely disable
//        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
//
//        // Or disable external entities and entity expansion:
//        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//        builder.setExpandEntities(false);
//
//        return builder;
//    }
}
