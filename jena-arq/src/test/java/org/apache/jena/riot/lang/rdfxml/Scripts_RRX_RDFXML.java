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

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.junit5.ScriptsLib;
import org.apache.jena.arq.junit5.riot.ParsingStepForTest;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.riot.lang.rdfxml.rrx.ReaderRDFXML_SAX;
import org.apache.jena.riot.lang.rdfxml.rrx_stax_ev.ReaderRDFXML_StAX_EV;
import org.apache.jena.riot.lang.rdfxml.rrx_stax_sr.ReaderRDFXML_StAX_SR;
import org.apache.jena.sys.JenaSystem;

/**
 * Manifest driven tests for RRX (and the same tests for ARP1).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scripts_RRX_RDFXML {

    @BeforeAll
    public static void beforeAll() {
        JenaSystem.init();
    }

    // This is the default RDF/XML parser at Jena5.
    // Run test again in the same style as the other RRX parsers.
    @TestFactory
    @Order(1)
    @DisplayName("RIOT RRX SAX")
    public Stream<DynamicNode> testFactory1() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_SAX.factory, "testing/RIOT/rdf11-xml/manifest.ttl");
    }

    @TestFactory
    @Order(2)
    @DisplayName("RIOT RRX StAXev")
    public Stream<DynamicNode> testFactory2() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_StAX_EV.factory, "testing/RIOT/rdf11-xml/manifest.ttl");
    }

    @TestFactory
    @Order(3)
    @DisplayName("RIOT RRX StAXsr")
    public Stream<DynamicNode> testFactory3() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_StAX_SR.factory, "testing/RIOT/rdf11-xml/manifest.ttl");
    }

    @TestFactory
    @Order(9)
    @DisplayName("ARP1 (legacy)")
    public Stream<DynamicNode> testFactory4() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_ARP1.factory, "testing/RIOT/rdf11-xml/manifest.ttl");
    }

    /** Run with a specific RIOT Factory for a language. */
    /*package*/ static Stream<DynamicNode> withAltParserFactory(Lang lang, ReaderRIOTFactory factory, String filename) {
        ParsingStepForTest.registerAlternative(lang, factory);
        try {
        return ScriptsLib.manifestTestFactoryRIOT("testing/RIOT/rdf11-xml/manifest.ttl");
        } finally {
            ParsingStepForTest.unregisterAlternative(lang);
        }
    }
}
