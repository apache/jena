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
import java.util.Arrays ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.* ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameters ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

@RunWith(Parameterized.class)
public class TestRiotWriterGraph extends AbstractWriterTest
{
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { RDFFormat.RDFNULL }
            
            , { RDFFormat.NTRIPLES_UTF8 }
            , { RDFFormat.NTRIPLES_ASCII }
            , { RDFFormat.NTRIPLES }
            , { RDFFormat.TURTLE }
            , { RDFFormat.TURTLE_PRETTY }
            , { RDFFormat.TURTLE_BLOCKS }
            , { RDFFormat.TURTLE_FLAT }
            , { RDFFormat.RDFXML }
            , { RDFFormat.RDFXML_PRETTY }
            , { RDFFormat.RDFXML_PLAIN }
            , { RDFFormat.JSONLD }
            , { RDFFormat.JSONLD_PRETTY }
            , { RDFFormat.JSONLD_FLAT }
            , { RDFFormat.RDFJSON }

            // graph in quad formats.
            , { RDFFormat.TRIG }
            , { RDFFormat.TRIG_PRETTY }
            , { RDFFormat.TRIG_BLOCKS }
            , { RDFFormat.TRIG_FLAT }
            , { RDFFormat.NQUADS_UTF8}
            , { RDFFormat.NQUADS_ASCII}
            , { RDFFormat.NQUADS}
        }) ; 
    }

    private RDFFormat format ;
    
    public TestRiotWriterGraph(RDFFormat format)
    {
        this.format = format ;
    }
    
    @Test public void writer00() { test("writer-rt-00.ttl") ; }
    @Test public void writer01() { test("writer-rt-01.ttl") ; }
    @Test public void writer02() { test("writer-rt-02.ttl") ; }
    @Test public void writer03() { test("writer-rt-03.ttl") ; }
    @Test public void writer04() { test("writer-rt-04.ttl") ; }
    @Test public void writer05() { test("writer-rt-05.ttl") ; }
    @Test public void writer06() { test("writer-rt-06.ttl") ; }
    @Test public void writer07() { test("writer-rt-07.ttl") ; }
    @Test public void writer08() { test("writer-rt-08.ttl") ; }
    
    @Test public void writer09() { 
        if ( format.getLang() != Lang.JSONLD )
            // Fails in jsonld-java
            test("writer-rt-09.ttl") ;
        }
    
    @Test public void writer10() { 
        if ( format.getLang() != Lang.JSONLD )
            // Fails in jsonld-java
            test("writer-rt-10.ttl") ; 
    }
    
    @Test public void writer11() { test("writer-rt-11.ttl") ; }
    @Test public void writer12() { test("writer-rt-12.ttl") ; }
    @Test public void writer13() { test("writer-rt-13.ttl") ; }
    @Test public void writer14() { test("writer-rt-14.ttl") ; }
    @Test public void writer15() { test("writer-rt-15.ttl") ; }
    @Test public void writer16() { test("writer-rt-16.ttl") ; }
    @Test public void writer17() { test("writer-rt-17.ttl") ; }

    private void test(String filename)
    {
        String displayname = filename.substring(0, filename.lastIndexOf('.')) ; 
        Model m = readModel(filename) ;
        Lang lang = format.getLang() ;

        WriterGraphRIOT rs = RDFWriterRegistry.getWriterGraphFactory(format).create(format) ;
        assertEquals(lang, rs.getLang()) ;

        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        RDFDataMgr.write(out, m, format) ;
        
        if ( lang == Lang.RDFNULL )
            return ;

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        String s = StrUtils.fromUTF8bytes(out.toByteArray()) ;
        
        Model m2 = ModelFactory.createDefaultModel() ;
        
        try {
            RDFDataMgr.read(m2, in, lang) ;
        } catch (RiotException ex)
        {
            System.out.println(format) ;
            System.out.println(s) ;
            throw ex ;
        }
        
        boolean b = m.isIsomorphicWith(m2) ;
        if ( !b )
        {
            System.out.println("------["+format+"]---------------------------------------------------") ;
            
            System.out.println("#### file="+displayname) ;
            System.out.print(s) ;
        }
        
        assertTrue("Did not round-trip file="+filename+" / format="+format,  b) ; 
        
    }
}

