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

package org.apache.jena.riot.writer;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RDFWriter;
import org.junit.Assert ;
import org.junit.Test ;

public class TestTurtleWriter {
    // Tests data.
    static String cycle1 = "_:a <urn:p> _:b . _:b <urn:q> _:a ." ;
    static String cycle2 = "_:a <urn:p> _:b . _:b <urn:q> _:a . _:a <urn:r> \"abc\" . " ;
    static String basetester = "@base <http://example.org/> .   " +
                               "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .   " +
                               "@prefix foaf:  <http://xmlns.com/foaf/0.1/> .  " +
                               "<green-goblin> rdf:type foaf:Person ." ;
    
    
    /** Read in N-Triples data, which is not empty,
     *  then write-read-compare using the format given.
     *  
     * @param testdata
     * @param lang
     */
    static void blankNodeLang(String testdata, RDFFormat lang) {
        StringReader r = new StringReader(testdata) ;
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, r, null, RDFLanguages.NTRIPLES) ;
        Assert.assertTrue(m.size() > 0);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        RDFDataMgr.write(output, m, lang);
        
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Model m2 = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m2, input, lang.getLang());
        
        Assert.assertTrue(m2.size() > 0);
        Assert.assertTrue(m.isIsomorphicWith(m2));
    }
    
    // Tests from JENA-908 
    @Test
    public void bnode_cycles_01() { blankNodeLang(cycle1, RDFFormat.TURTLE) ; }
    
    @Test
    public void bnode_cycles_02() { blankNodeLang(cycle1, RDFFormat.TURTLE_BLOCKS) ; }
    
    @Test
    public void bnode_cycles_03() { blankNodeLang(cycle1, RDFFormat.TURTLE_FLAT) ; }
    
    @Test
    public void bnode_cycles_04() { blankNodeLang(cycle1, RDFFormat.TURTLE_PRETTY) ; }

    @Test
    public void bnode_cycles_05() { blankNodeLang(cycle2, RDFFormat.TURTLE) ; }
    
    @Test
    public void bnode_cycles_06() { blankNodeLang(cycle2, RDFFormat.TURTLE_BLOCKS) ; }
    
    @Test
    public void bnode_cycles_07() { blankNodeLang(cycle2, RDFFormat.TURTLE_FLAT) ; }
    
    @Test
    public void bnode_cycles_08() { blankNodeLang(cycle2, RDFFormat.TURTLE_PRETTY) ; }

    @Test
    public void bnode_cycles() {
        Model m = RDFDataMgr.loadModel("testing/DAWG-Final/construct/data-ident.ttl");
        Assert.assertTrue(m.size() > 0);
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        RDFDataMgr.write(output, m, Lang.TURTLE);
        
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Model m2 = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m2, input, Lang.TURTLE);
        Assert.assertTrue(m2.size() > 0);
        
        Assert.assertTrue(m.isIsomorphicWith(m2));
    }
    
    @Test
    public void test_base() {
        InputStream r = new ByteArrayInputStream(TestTurtleWriter.basetester.getBytes()) ;
        String b = "http://example.org/" ;
        Model m = ModelFactory.createDefaultModel() ;
        m.read(r, b, "TTL") ;

        OutputStream o = new ByteArrayOutputStream() ;
        RDFWriter.create().source(m).format(RDFFormat.TURTLE_BLOCKS).base(b).output(o) ;
        String k = o.toString() ;
        Assert.assertTrue(k.contains("@base")) ;
    }
}

