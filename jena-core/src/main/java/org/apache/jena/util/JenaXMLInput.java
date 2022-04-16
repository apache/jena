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
 * <p>
 * DTDs are, by default, not processed. These may be enabled with {@link #allowLocalDTDs}.
 */
public class JenaXMLInput {
    // ---- SAX
    // RDFXMLParser
    private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

    /** Whether to allow DTD processing. This applies to reading RDF/XML
     * and SPARQL XML Results - these formats do not need DTD processing
     * to be read into Jena.
     * <p>
     * External DTDs are always prohibited.
     * <p>
     * The default configuration is to not process DTDs.
     * An application may enable local DTD processing if necessary.
     *
     * @deprecated Future releases remove the ability to enable local DTDs processing.
     */
    @Deprecated
    public static boolean allowLocalDTDs = false;

    public static XMLReader createXMLReader() throws ParserConfigurationException, SAXException {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlreader = saxParser.getXMLReader();

            if ( !allowLocalDTDs ) {
                // XXE : disable all DTD processing.
                // Effect: JenaException if a DTD is found.
                xmlreader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            }
            // Always disable remote DTDs (silently ignore if DTDs are allowed at all)
            xmlreader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // and ignore external entities (silently ignore)
            xmlreader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            xmlreader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            return xmlreader;
            // WAS: only:
//          xmlreader.setFeature("http://xml.org/sax/features/external-general-entities", false);
//          xmlreader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    }


    // ---- StAX
    // TriX and results.
    private static XMLInputFactory xf = XMLInputFactory.newInstance() ;

    static {
        try {
            if ( !allowLocalDTDs ) {
                // This disables DTDs entirely for that factory.
                // DTDs are silently ignored.
                xf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            }
            // Disable external DTDs (files and HTTP) - silent ignore.
            xf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // disable external entities (silently ignore)
            xf.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
//            // WAS only:
//             comment out -- xf.setProperty(XMLInputFactory.SUPPORT_DTD, false);
//            xf.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

        } catch(IllegalArgumentException ex){
            Log.error(JenaXMLInput.class, "Problem setting StAX property", ex);
        }
    }

    public static XMLStreamReader newXMLStreamReader(InputStream in) throws XMLStreamException {
        return xf.createXMLStreamReader(in) ;
    }

    public static XMLStreamReader newXMLStreamReader(Reader in) throws XMLStreamException {
        return xf.createXMLStreamReader(in) ;
    }

    // ---- DocumentBuilder
    // For reference - not used in Jena src/main, but is in src/test DOM2RDFTest and MoreDOM2RDFTest
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
//        if ( !allowLocalDTDs ) {
//            // XXE : either disable all DTD processing ...
//            builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//        }
//        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
//        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false;)
//        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
//        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
//        builder.setExpandEntities(false);
//        return builder;
//    }
}
