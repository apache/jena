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

package org.apache.jena.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;

import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.JenaXMLInput;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class TestReadXML {

    // SAX
    @Test public void sax_setup() {
        try {
            XMLReader xmlReader = JenaXMLInput.createXMLReader();
            assertFalse(xmlReader.getFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd"));
            assertFalse(xmlReader.getFeature("http://xml.org/sax/features/external-general-entities"));
            assertFalse(xmlReader.getFeature("http://xml.org/sax/features/external-parameter-entities"));
            // Allows for in-document entities.
            assertFalse(xmlReader.getFeature("http://apache.org/xml/features/disallow-doctype-decl"));
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    // SAX : When allowing DTDs in RDF/XML, and ignoring external ones.
    @Test public void rdfxml_dtd_http_migration() {
        // DTD http does not exist, no error because it was ignored
        Model model = ModelFactory.createDefaultModel();
        model.read("file:testing/xml/rdfxml-dtd-http.rdf");
    }

    // SAX : When allowing DTDs in RDF/XML, and ignoring external ones.
    @Test public void rdfxml_dtd_file_migration() {
        // DTD http does not exist, no error because it was ignored
        Model model = ModelFactory.createDefaultModel();
        model.read("file:testing/xml/rdfxml-dtd-file.rdf");
    }

    // StAX - best available option is ignore DTDs
    // srx =  SPARQL results XML
    @Test public void stax_setup() {
        XMLInputFactory xf = XMLInputFactory.newInstance() ;
        JenaXMLInput.initXMLInputFactory(xf);
        assertEquals(Boolean.FALSE, xf.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertEquals(null, xf.getProperty(XMLConstants.ACCESS_EXTERNAL_DTD));
        assertEquals(Boolean.FALSE,xf.getProperty("javax.xml.stream.isSupportingExternalEntities"));
    }

    @Test public void srx_dtd_http() {
        ResultSetFactory.load("file:testing/xml/srx-dtd-http.srx");
    }

    @Test public void srx_dtd_file() {
        ResultSetFactory.load("file:testing/xml/srx-dtd-file.srx");
    }

    // TriX uses StAX
    @Test public void trix_dtd_http() {
        Model model = ModelFactory.createDefaultModel();
        model.read("file:testing/xml/trix-dtd-http.trix");
    }

    @Test public void trix_dtd_file() {
        Model model = ModelFactory.createDefaultModel();
        model.read("file:testing/xml/trix-dtd-file.trix");
    }
}
