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

package org.apache.jena.riot.lang.rdfxml;

import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.rdfxml.rrx.ReaderRDFXML_SAX;
import org.apache.jena.riot.lang.rdfxml.rrx_stax_ev.ReaderRDFXML_StAX_EV;
import org.apache.jena.riot.lang.rdfxml.rrx_stax_sr.ReaderRDFXML_StAX_SR;

/**
 * Addition registration of RDF/XML parsers to given each it's own {@link Lang} name.
 * Each parser has it's own short names for use with {@code --syntax} argument of the
 * {@code riot} command. NB Each Content-Type must be unique.
 */
public class RRX {
    /** <a href="http://www.w3.org/TR/rdf-xml/">RDF/XML</a> implemented by RRX-SAX */
    public static final Lang RDFXML_SAX = LangBuilder.create("RDFXML-SAX", "application/rdf+sax")
            .addAltNames("RRX-SAX", "rrxsax")
            .addFileExtensions("rdfsax")
            .build();

    /** <a href="http://www.w3.org/TR/rdf-xml/">RDF/XML</a> implemented by RRX-StAXev */
    public static final Lang RDFXML_StAX_ev = LangBuilder.create("RDFXML-StAX-EV", "application/rdf+stax-ev")
            .addAltNames("RRX-StAX-ev", "rrxstaxev")
            .addFileExtensions("rdfstax")
            .build();

    /** <a href="http://www.w3.org/TR/rdf-xml/">RDF/XML</a> implemented by RRX-StAXsr */
    public static final Lang RDFXML_StAX_sr = LangBuilder.create("RDFXML-StAX-SR", "application/rdf+stax-sr")
            .addAltNames("RRX-StAX-sr", "rrxstaxsr")
            .addFileExtensions("rdfstaxev")
            .build();

    /** <a href="http://www.w3.org/TR/rdf-syntax-grammar/">RDF/XML</a> implemented by ARP1 */
    public static final Lang RDFXML_ARP1 = LangBuilder.create("RDFXML-ARP1", "application/rdf+arp1")
            .addAltContentTypes("application/rdf+arp")
            .addAltNames("arp1", "arp")
            .addFileExtensions("arp1", "arp")
            .build();

    /** <a href="http://www.w3.org/TR/rdf-syntax-grammar/">RDF/XML</a> implemented by ARP1 */
    public static final Lang RDFXML_ARP0 = LangBuilder.create("RDFXML-ARP0", "application/rdf+arp0")
            .addAltNames("arp0")
            .addFileExtensions("arp0")
            .build();

    /**
     * Register direct Lang constants for RDF/XML parsers.
     */
    public static void register() {
        // RRX
        register(RDFXML_SAX,     ReaderRDFXML_SAX.factory);
        register(RDFXML_StAX_ev, ReaderRDFXML_StAX_EV.factory);
        register(RDFXML_StAX_sr, ReaderRDFXML_StAX_SR.factory);

        // Names for ARP
        register(RDFXML_ARP1,    ReaderRDFXML_ARP1.factory);
        register(RDFXML_ARP0,    ReaderRDFXML_ARP0.factory);
    }

    private static void register(Lang lang, ReaderRIOTFactory factory) {
        RDFLanguages.register(lang);
        RDFParserRegistry.registerLangTriples(lang, factory);
    }

    /**
     * Remove the direct registrations of RDF/XML parsers.
     */
    public static void unregister() {
        unregister(RDFXML_SAX);
        unregister(RDFXML_StAX_ev);
        unregister(RDFXML_StAX_sr);
        unregister(RDFXML_ARP1);
        unregister(RDFXML_ARP0);
    }

    private static void unregister(Lang lang) {
        RDFParserRegistry.removeRegistration(lang);
        RDFLanguages.unregister(lang);
    }
}
