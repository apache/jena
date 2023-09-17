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

package org.apache.jena.riot.lang.rdfxml.rrx;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.JenaXMLInput;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * RDF/XML parser.
 * <p>
 * This implementation uses SAX.
 *
 * @see <a href="https://www.w3.org/TR/rdf-xml/">https://www.w3.org/TR/rdf-xml/</a>
 */
public class ReaderRDFXML_SAX implements ReaderRIOT
{
    public static ReaderRIOTFactory factory = (Lang language, ParserProfile parserProfile) -> {
        return new ReaderRDFXML_SAX(parserProfile);
    };

    private final ParserProfile parserProfile;

    public ReaderRDFXML_SAX(ParserProfile parserProfile) {
        this.parserProfile = parserProfile;
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        InputSource input = new InputSource(in) ;
        parse(input, baseURI, ct, output, context);
    }

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        InputSource input = new InputSource(reader) ;
        parse(input, baseURI, ct, output, context);
    }

    private void parse(InputSource inputSource, String xmlBase, ContentType ct, StreamRDF destination, Context context) {
        ParserRDFXML_SAX sax2rdf = new ParserRDFXML_SAX(xmlBase, parserProfile, destination, RIOT.getContext().copy());
        // Configured to avoid XXE
        XMLReader xmlReader;
        try {
            xmlReader = createXMLReader();
            // 4 call backs.
            xmlReader.setDTDHandler(sax2rdf);
            xmlReader.setEntityResolver(sax2rdf);
            xmlReader.setErrorHandler(sax2rdf);
            xmlReader.setContentHandler(sax2rdf);
            xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", sax2rdf);

            destination.start();
            try {
                xmlReader.parse(inputSource);
            } finally { destination.finish(); }
        } catch (RiotException ex) {
            throw ex;
        } catch (IOException ex) {
            throw IOX.exception(ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RiotException(ex);
        }
    }

    private static XMLReader createXMLReader() throws Exception {
        XMLReader xmlreader = JenaXMLInput.createXMLReader();
        xmlreader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlreader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        return xmlreader;
    }
}
