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

package org.apache.jena.riot.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParserRegistry;
import org.apache.jena.riot.RDFWriterRegistry;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestLangRegistration {

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of("NULL",     Lang.RDFNULL,    true, true),
                 Arguments.of("RDFXML",   Lang.RDFXML,     true, false),
                 Arguments.of("NTRIPLES", Lang.NTRIPLES,   true, false),
                 Arguments.of("NT",       Lang.NT,         true, false),
                 Arguments.of("N3",       Lang.N3,         true, false),
                 Arguments.of("TURTLE",   Lang.TURTLE,     true, false),
                 Arguments.of("TTL",      Lang.TTL,        true, false),
                 Arguments.of("JSONLD",   Lang.JSONLD,     true, true),
                 Arguments.of("RDFJSON",  Lang.RDFJSON,    true, false),
                 Arguments.of("NQUADS",   Lang.NQUADS,     false, true),
                 Arguments.of("NQ",       Lang.NQ,         false, true),
                 Arguments.of("TRIG",     Lang.TRIG,       false, true),
                 Arguments.of("TRIX",     Lang.TRIX,       true, true),
                 Arguments.of("PB RDF",   Lang.RDFPROTO,   true, true),
                 Arguments.of("TRDF",     Lang.RDFTHRIFT,  true, true)
                        );
        return x.stream();
    }

    @Parameter(0) private String name;
    @Parameter(1) private Lang lang;
    @Parameter(2) private boolean istriples;
    @Parameter(3) private boolean isquads;

    @Test public void jenaSystem_read_1() {
        assertTrue(RDFLanguages.isRegistered(lang));
        if ( istriples )
            assertTrue(RDFLanguages.isTriples(lang));
        else
            assertFalse(RDFLanguages.isTriples(lang));
        if (isquads )
            assertTrue(RDFLanguages.isQuads(lang));
        else
            assertFalse(RDFLanguages.isQuads(lang));
    }

    @Test public void jenaSystem_read_2() {
        if ( ! Lang.RDFNULL.equals(lang) ) {
            assertTrue(RDFParserRegistry.isRegistered(lang));
            assertNotNull(RDFParserRegistry.getFactory(lang));
        }
    }

    @Test public void jenaSystem_write_1() {
        assertTrue(RDFWriterRegistry.contains(lang));
    }

    @Test public void jenaSystem_write_2() {
        if ( istriples ) assertNotNull(RDFWriterRegistry.getWriterGraphFactory(lang));
        if ( isquads )   assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(lang));
        assertNotNull(RDFWriterRegistry.defaultSerialization(lang));
    }

//    @Test public void jenaSystem_write_3() {
//
//        assertEquals(jsonldFmt1, RDFWriterRegistry.defaultSerialization(JSONLD));
//
//        assertNotNull(RDFWriterRegistry.getWriterGraphFactory(jsonldFmt1));
//        assertNotNull(RDFWriterRegistry.getWriterGraphFactory(jsonldFmt2));
//
//        assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(jsonldFmt1));
//        assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(jsonldFmt2));
//
//        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(jsonldFmt1));
//        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(jsonldFmt2));
//
//        assertTrue(RDFWriterRegistry.registeredDatasetFormats().contains(jsonldFmt1));
//        assertTrue(RDFWriterRegistry.registeredDatasetFormats().contains(jsonldFmt2));
//    }
//
//    @Test public void jenaSystem_write_4() {
//        assertNotNull(RDFDataMgr.createGraphWriter(jsonldFmt1));
//        assertNotNull(RDFDataMgr.createGraphWriter(jsonldFmt2));
//        assertNotNull(RDFDataMgr.createDatasetWriter(jsonldFmt1));
//        assertNotNull(RDFDataMgr.createDatasetWriter(jsonldFmt2));
//    }
}

