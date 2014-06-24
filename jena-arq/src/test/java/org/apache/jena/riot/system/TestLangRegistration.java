/**
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

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RDFParserRegistry ;
import org.apache.jena.riot.RDFWriterRegistry ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;


@RunWith(Parameterized.class)
public class TestLangRegistration extends BaseTest
{
    @Parameters(name = "{0} -- {1} {2} {3}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        add("NULL",     x, Lang.RDFNULL,    false, false)  ;
        add("RDFXML",   x, Lang.RDFXML,     true, false) ;
        add("NTRIPLES", x, Lang.NTRIPLES,   true, false) ;
        add("NT",       x, Lang.NT,         true, false) ;
        add("N3",       x, Lang.N3,         true, false) ;
        add("TURTLE",   x, Lang.TURTLE,     true, false) ;
        add("TTL",      x, Lang.TTL,        true, false) ;
        add("JSONLD",   x, Lang.JSONLD,     true, true) ;
        add("RDFJSON",  x, Lang.RDFJSON,    true, false) ;
        add("NQUADS",   x, Lang.NQUADS,     false, true) ;
        add("NQ",       x, Lang.NQ,         false, true) ;
        add("TRIG",     x, Lang.TRIG,       false, true) ;
        return x ;
    }
    
    private static void add(String name, List<Object[]> x, Lang lang, boolean istriples, boolean isquads) {
        x.add(new Object[] {name, lang, istriples , isquads }) ;
    }

    private String name ;
    private Lang lang ;
    private boolean istriples ;
    private boolean isquads ;

    public TestLangRegistration(String name, Lang lang, boolean istriples, boolean isquads) {
        this.name = name ;
        this.lang = lang ;
        this.istriples = istriples ;
        this.isquads = isquads ;
    }

    @Test public void jenaSystem_read_1() {
        assertTrue(RDFLanguages.isRegistered(lang)) ;
        if ( istriples ) 
            assertTrue(RDFLanguages.isTriples(lang)) ;
        else
            assertFalse(RDFLanguages.isTriples(lang)) ;
        if (isquads )
            assertTrue(RDFLanguages.isQuads(lang)) ;
        else
            assertFalse(RDFLanguages.isQuads(lang)) ;
        
    }
    @Test public void jenaSystem_read_2() {
        if ( ! Lang.RDFNULL.equals(lang) )
            assertNotNull(RDFParserRegistry.getFactory(lang)) ;
    }
    
    @Test public void jenaSystem_write_1() {
        assertTrue(RDFWriterRegistry.contains(lang)) ;
    }

    @Test public void jenaSystem_write_2() {
        if ( istriples ) assertNotNull(RDFWriterRegistry.getWriterGraphFactory(lang)) ;
        if ( isquads )   assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(lang)) ;
        assertNotNull(RDFWriterRegistry.defaultSerialization(lang)) ;
    }
    
//    @Test public void jenaSystem_write_3() {
//        
//        assertEquals(jsonldFmt1, RDFWriterRegistry.defaultSerialization(JSONLD)) ;
//        
//        assertNotNull(RDFWriterRegistry.getWriterGraphFactory(jsonldFmt1)) ;
//        assertNotNull(RDFWriterRegistry.getWriterGraphFactory(jsonldFmt2)) ;
//
//        assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(jsonldFmt1)) ;
//        assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(jsonldFmt2)) ;
//
//        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(jsonldFmt1)) ;
//        assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(jsonldFmt2)) ;
//        
//        assertTrue(RDFWriterRegistry.registeredDatasetFormats().contains(jsonldFmt1)) ;
//        assertTrue(RDFWriterRegistry.registeredDatasetFormats().contains(jsonldFmt2)) ;
//    }
//    
//    @Test public void jenaSystem_write_4() {
//        assertNotNull(RDFDataMgr.createGraphWriter(jsonldFmt1)) ;
//        assertNotNull(RDFDataMgr.createGraphWriter(jsonldFmt2)) ;
//        assertNotNull(RDFDataMgr.createDatasetWriter(jsonldFmt1)) ;
//        assertNotNull(RDFDataMgr.createDatasetWriter(jsonldFmt2)) ;
//    }
}

