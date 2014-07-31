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

package org.apache.jena.riot;

import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.StringReader ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.adapters.RDFReaderFactoryRIOT ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.FileUtils ;

/* Test of integration with Jena via model.read.
 * Also tests triples format reading of RDFDataMgr */
public class TestJenaReaderRIOT extends BaseTest
{
    private static final String directory = "testing/RIOT/Reader" ;

    private static Context context = new Context() ;
    
    @BeforeClass static public void beforeClass()
    { 
        RIOT.init() ;
    }
    
    @AfterClass static public void afterClass()
    { 
        // Unwire?
    }

    @Test public void read_01() { jenaread("D.nt") ; }
    @Test public void read_02() { jenaread("D.ttl") ; }
    @Test public void read_03() { jenaread("D.rdf") ; }
    @Test public void read_04() { jenaread("D.rdf") ; }
    @Test public void read_05() { jenaread("D.json") ; }

    @Test public void read_11() { jenaread("D.nt",   "N-TRIPLES") ; }
    @Test public void read_12() { jenaread("D.ttl",  "TTL") ; }
    @Test public void read_13() { jenaread("D.rdf",  "RDF/XML") ; }
    @Test public void read_14() { jenaread("D.rdf",  "RDF/XML-ABBREV") ; }
    @Test public void read_15() { jenaread("D.json", "RDF/JSON") ; }

    @Test public void read_21a() { jenaread("D-nt",  "N-TRIPLES") ; }
    @Test public void read_21b() { jenaread("D-nt",  "NTRIPLES") ; }
    @Test public void read_21c() { jenaread("D-nt",  "NT") ; }
    @Test public void read_21d() { jenaread("D-nt",  "N-Triples") ; }

    @Test public void read_22a() { jenaread("D-ttl", "TURTLE") ; }
    @Test public void read_22b() { jenaread("D-ttl", "TTL") ; }
    
    @Test public void read_23a()  { jenaread("D-rdf", "RDF/XML") ; }
    @Test public void read_23b()  { jenaread("D-rdf", "RDFXML") ; }
    @Test public void read_24()   { jenaread("D-json", "RDF/JSON") ; }
    
    @Test public void read_30()
    {
        {
            TypedInputStream in = RDFDataMgr.open(filename("D-not-TTL.ttl") );
            Model m0 = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m0, in, RDFLanguages.RDFXML) ;
        }

        TypedInputStream in1 = RDFDataMgr.open(filename("D-not-TTL.ttl") );
        Model m1 = ModelFactory.createDefaultModel() ;
        // Fails until integration with jena-core as hintlang gets lost.
        m1.read(in1, null, "RDF/XML") ;
    }
    
    // test read from StringReader..
    @Test public void read_StringReader_31()
    {
        String x = "<s> <p> <p> ." ;
        
        {
            StringReader s = new StringReader(x) ;
            Model m = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m, s, null, RDFLanguages.NTRIPLES) ;
        }
        
        StringReader s1 = new StringReader("<s> <p> <p> .") ;
        Model m1 = ModelFactory.createDefaultModel() ;
        m1.read(s1, null, "N-TRIPLES") ;
    }
    
    @Test public void read_StringReader_32()
    {
        String x = StrUtils.strjoinNL(
            "<rdf:RDF", 
            "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"",
            "   xmlns:j.0=\"http://example/\">" ,
            "  <rdf:Description rdf:about=\"http://example/s\">" ,
            "     <j.0:p rdf:resource=\"http://example/o\"/>" ,
            "   </rdf:Description>" ,
            "</rdf:RDF>") ;
        {
            StringReader s = new StringReader(x) ;
            Model m = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m, s, null, RDFLanguages.RDFXML) ;
        }
        StringReader s1 = new StringReader(x) ;
        Model m = ModelFactory.createDefaultModel() ;
        m.read(s1, null, "RDF/XML") ;
    }

    // Stream opening is hardwired into jena!
    @Test public void read_base_1() 
    { jenaread("D-no-base.ttl", "TTL", "http://baseuri/") ; }
    
    @Test public void read_input_1() throws IOException
    { jenaread_stream("D.ttl", "TTL") ; }
        
    @Test public void read_input_2() throws IOException
    { jenaread_stream("D.rdf", "RDF/XML") ; }
    
    private static String filename(String filename) { return directory+"/"+filename ; }
    
    private static void jenaread_stream(String filename, String lang) throws IOException
    {
        filename = filename(filename) ;
        
        // Read with a base
        try(FileInputStream in0 = new FileInputStream(filename)) {
            Model m0 = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m0, in0, "http://example/base2", RDFLanguages.nameToLang(lang)) ;
        }

        // Read again, but without base
        try(FileInputStream in1 = new FileInputStream(filename)) {
            Model m1 = ModelFactory.createDefaultModel() ;
            RDFDataMgr.read(m1, in1, RDFLanguages.nameToLang(lang)) ;
        }
        
        // Fail because Jena core does a look up of lang with ModelCom builtin in RDFReaderF, then calls RIOReader().
        // 1/ Fix Jena - remove RDFReaderF
        // 2/ Change RDFReaderF to pass in the language name. 
        
        // Read via Jena API.
        Model m2 = ModelFactory.createDefaultModel() ;
        try(FileInputStream in2 = new FileInputStream(filename)) {
            m2.read(in2, "http://example/base3", lang) ;
        }
        
        String x = FileUtils.readWholeFileAsUTF8(filename) ;
        Model m3 = ModelFactory.createDefaultModel() ;
        m2.read(new StringReader(x), "http://example/base4", lang) ;
    }

    private static void jenaread(String dataurl)
    {
        dataurl = filename(dataurl) ; 
        Model m = ModelFactory.createDefaultModel() ;
        m.read(dataurl) ;
        assertTrue(m.size() != 0 ) ;
    }
    
    private static void jenaread(String dataurl, String lang)
    {
        // read via WebReader to make sure the test setup is right.
        dataurl = filename(dataurl) ;
        
        Model m0 = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m0, dataurl, RDFLanguages.nameToLang(lang)) ;
        assertTrue(m0.size() != 0 ) ;
        
        Model m1 = ModelFactory.createDefaultModel() ;
        new RDFReaderFactoryRIOT().getReader(lang).read(m1, dataurl) ;
        assertTrue(m1.size() != 0 ) ;
        
        // Read via Jena model API.
        Model m2 = ModelFactory.createDefaultModel() ;
        // The range of names for mode.read is less - canonicalise the language name.
        String x = RDFLanguages.nameToLang(lang).getName() ;
        m2.read(dataurl, x) ;
        assertTrue(m2.size() != 0 ) ;
    }

    private static void jenaread(String dataurl, String lang, String base)
    {
        dataurl = filename(dataurl) ;
        Model m1 = ModelFactory.createDefaultModel() ;
        Model m2 = ModelFactory.createDefaultModel() ;
        
        // This call
        RDFDataMgr.read(m1, dataurl, base, RDFLanguages.nameToLang(lang)) ;
        // should be an implementation of:
        m2.read("file:"+dataurl, base, lang) ;
        assertTrue(m1.size() != 0 ) ;
        assertTrue(m2.size() != 0 ) ;
        assertTrue(m1.isIsomorphicWith(m2)) ;
        // and check base processing ...
        Resource s = m1.listStatements().next().getSubject() ;
        assertTrue(s.getURI().startsWith("http://")) ;
        assertTrue(s.getURI().equals("http://baseuri/s")) ;
    }
}

