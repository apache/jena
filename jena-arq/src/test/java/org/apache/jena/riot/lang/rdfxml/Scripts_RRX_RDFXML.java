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

import static org.apache.jena.arq.junit.Scripts.withAltParserFactory;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.TestConsts;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.lang.rdfxml.rrx.ReaderRDFXML_SAX;
import org.apache.jena.riot.lang.rdfxml.rrx_stax_ev.ReaderRDFXML_StAX_EV;
import org.apache.jena.riot.lang.rdfxml.rrx_stax_sr.ReaderRDFXML_StAX_SR;

/**
 * Manifest driven tests for RRX, switching parser factory.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scripts_RRX_RDFXML {

    /** Currently RDF 1.1 */
    // See also Scripts_RIOT_rdf_tests_std.
    private static String DIR = TestConsts.RDF11_TESTS_DIR;
    private static String MANIFEST_RDFXML = DIR+"rdf-xml/manifest.ttl";

    // This is the default RDF/XML parser at Jena5.
    @TestFactory
    @Order(1)
    @DisplayName("RIOT RRX SAX")
    public Stream<DynamicNode> testFactory1() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_SAX.factory, MANIFEST_RDFXML);
    }

    @TestFactory
    @Order(2)
    @DisplayName("RIOT RRX StAXev")
    public Stream<DynamicNode> testFactory2() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_StAX_EV.factory, MANIFEST_RDFXML);
    }

    @TestFactory
    @Order(3)
    @DisplayName("RIOT RRX StAXsr")
    public Stream<DynamicNode> testFactory3() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_StAX_SR.factory, MANIFEST_RDFXML);
    }
}
