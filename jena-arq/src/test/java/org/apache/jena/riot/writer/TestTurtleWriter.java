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
import java.io.StringReader ;

import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.RDFLanguages ;
import org.junit.Assert ;
import org.junit.Ignore ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class TestTurtleWriter {
    // Tests data.
    static String cycle = "_:a <urn:p> _:b . _:b <urn:q> _:a ." ;
    
    
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
    

    @Test
    @Ignore // Currently ignored due to JENA-908
    public void bnode_cycles_01() { blankNodeLang(cycle, RDFFormat.TURTLE) ; }
    
    @Test
    public void bnode_cycles_02() { blankNodeLang(cycle, RDFFormat.TURTLE_BLOCKS) ; }
    
    @Test
    public void bnode_cycles_03() { blankNodeLang(cycle, RDFFormat.TURTLE_FLAT) ; }
    
    @Test
    @Ignore // Currently ignored due to JENA-908
    public void bnode_cycles_04() { blankNodeLang(cycle, RDFFormat.TURTLE_PRETTY) ; }

}

