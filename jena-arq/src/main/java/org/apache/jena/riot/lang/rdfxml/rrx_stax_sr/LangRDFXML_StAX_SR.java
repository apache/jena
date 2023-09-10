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

package org.apache.jena.riot.lang.rdfxml.rrx_stax_sr;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.JenaXMLInput;

/**
 * RDF/XML parser.
 * <p>
 * This implementation uses StAX via {@link XMLStreamReader}.
 *
 * @see <a href="https://www.w3.org/TR/rdf-xml/">https://www.w3.org/TR/rdf-xml/</a>
 */

public class LangRDFXML_StAX_SR implements ReaderRIOT
{
    public static ReaderRIOTFactory factory = (Lang language, ParserProfile parserProfile) -> {
        return new LangRDFXML_StAX_SR(parserProfile);
    };

    private final ParserProfile parserProfile;

    public LangRDFXML_StAX_SR(ParserProfile parserProfile) {
        this.parserProfile = parserProfile;
    }

    @Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
        parse(in, null, baseURI, ct, output, context);
    }

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        parse(null, reader, baseURI, ct, output, context);
    }

    private void parse(InputStream input, Reader reader, String xmlBase, ContentType ct, StreamRDF destination, Context context) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        // Make safe.
        JenaXMLInput.initXMLInputFactory(xmlInputFactory);

        // Enable character entity support.
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        // Set merging.
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);

        XMLStreamReader xmlStreamReader = null;
        try {
            if ( input != null )
                xmlStreamReader = xmlInputFactory.createXMLStreamReader(input);
            else
                xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);
        } catch (XMLStreamException ex) {
            throw new RiotException("Failed to create the XMLStreamReader", ex);
        }

        RDFXMLParser_StAX_SR parser = new RDFXMLParser_StAX_SR(xmlStreamReader, xmlBase, parserProfile, destination, context);
        destination.start();
        try {
            parser.parse();
        } catch (RiotException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RiotException(ex);
        }
        finally {
            destination.finish();
        }
    }
}
