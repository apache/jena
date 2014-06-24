
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
import org.apache.jena.riot.* ;
import org.junit.Test ;
// Test system integration / registration
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;


@RunWith(Parameterized.class)
public class TestFormatRegistration extends BaseTest
{
    @Parameters(name = "{0} -- {1} {2} {3}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        add("NULL",     x, RDFFormat.RDFNULL,           false, false)  ;
        add("RDFXML",   x, RDFFormat.RDFXML,            true, false) ;
        add("RDFXML",   x, RDFFormat.RDFXML_ABBREV,     true, false) ;
        add("RDFXML",   x, RDFFormat.RDFXML_PLAIN,     true, false) ;
        add("RDFXML",   x, RDFFormat.RDFXML_PLAIN,     true, false) ;

        add("NTRIPLES", x, RDFFormat.NTRIPLES,   true, false) ;
        add("NT",       x, RDFFormat.NT,         true, false) ;
        add("TURTLE",   x, RDFFormat.TURTLE,     true, false) ;
        add("TTL",      x, RDFFormat.TTL,        true, false) ;
        add("JSONLD",   x, RDFFormat.JSONLD,     true, true) ;
        add("RDFJSON",  x, RDFFormat.RDFJSON,    true, false) ;
        add("NQUADS",   x, RDFFormat.NQUADS,     true, true) ;
        add("NQ",       x, RDFFormat.NQ,         true, true) ;
        add("TRIG",     x, RDFFormat.TRIG,       true, true) ;
        return x ;
    }
    
    private static void add(String name, List<Object[]> x, RDFFormat format, boolean istriples, boolean isquads) {
        x.add(new Object[] {name, format, istriples , isquads }) ;
    }

    private String name ;
    private RDFFormat format ;
    private boolean istriples ;
    private boolean isquads ;

    public TestFormatRegistration(String name, RDFFormat format, boolean istriples, boolean isquads) {
        this.name = name ;
        this.format = format ;
        this.istriples = istriples ;
        this.isquads = isquads ;
    }

    @Test public void jenaSystem_write_1() {
        assertTrue(RDFWriterRegistry.contains(format)) ;
    }

    @Test public void jenaSystem_write_2() {
        assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(format)) ;
        if ( istriples ) assertNotNull(RDFWriterRegistry.getWriterGraphFactory(format)) ;
        if ( isquads )   assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(format)) ;
    }
    
  @Test public void jenaSystem_write_3() {
      if ( istriples ) assertNotNull(RDFDataMgr.createGraphWriter(format)) ;
      if ( isquads )   assertNotNull(RDFDataMgr.createDatasetWriter(format)) ;
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
}

