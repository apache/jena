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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jena.atlas.logging.Log;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Create XML input methods.
 * <p>
 * External DTD and entity processing is disabled to prevent
 * <a href="https://owasp.org/www-community/vulnerabilities/XML_External_Entity_(XXE)_Processing">XXE Processing</a>
 * problems.
 */
public class JenaXMLInput {

    // ---- SAX
    // RDFXMLParser
    private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    public static XMLReader createXMLReader() throws ParserConfigurationException, SAXException {
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

    // ---- StAX
    // TriX and SPARQL XML Results.
    /**
     * Initialize an XMLInputFactory to jena settings.
     */
    public static void initXMLInputFactory(XMLInputFactory xf) {
        // This disables DTDs entirely for the factory.
        // All DTDs are silently ignored; takes precedence over ACCESS_EXTERNAL_DTD
    	setXMLInputFactoryProperty(xf, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);

        // disable external entities (silently ignore)
        setXMLInputFactoryProperty(xf, XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

        // Disable external DTDs (files and HTTP) - errors unless SUPPORT_DTD is false.
        setXMLInputFactoryProperty(xf, XMLConstants.ACCESS_EXTERNAL_DTD, "");
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

    private static XMLInputFactory xf = XMLInputFactory.newInstance() ;
    static { initXMLInputFactory(xf); }

    public static XMLStreamReader newXMLStreamReader(InputStream in) throws XMLStreamException {
        return xf.createXMLStreamReader(in) ;
    }

    public static XMLStreamReader newXMLStreamReader(Reader in) throws XMLStreamException {
        return xf.createXMLStreamReader(in) ;
    }

    // ---- DocumentBuilder
    // For reference - not used in Jena src/main, but is used in src/test DOM2RDFTest and MoreDOM2RDFTest
    public static DocumentBuilderFactory newDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Causes SAXParseException if there is an external entity.
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory;
    }

//    // For reference : jdom:
//    // ---- SAXBuilder
//    public static SAXBuilder newSAXBuilder() throws ParserConfigurationException {
//        SAXBuilder builder = new SAXBuilder();
//        //builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
//        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false;)
//        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//        builder.setExpandEntities(false);
//        return builder;
//    }
}
