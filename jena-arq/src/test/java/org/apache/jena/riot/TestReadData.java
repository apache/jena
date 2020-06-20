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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.FileInputStream ;
import java.io.IOException ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context ;
import org.junit.Test ;

/* Tests of RDFDataMgr.
 * See also TestJenaReaderRIOT (which covers reading triples formats)
 */
public class TestReadData
{
    private static final String directory = "testing/RIOT/Reader" ;

    private static Context context = new Context() ;
    
    // Model, graph
    
    @Test public void read_01() { read("D.nq") ; }
    @Test public void read_02() { read("D.trig") ; }
    @Test public void read_03() { read("D.nq",   RDFLanguages.NQUADS) ; }
    @Test public void read_04() { read("D.trig", RDFLanguages.TRIG) ; }
    
    @Test public void read_11() { read("D.nq", "N-Quads") ; }

    @Test public void read_12() { read("D.nq", "NQuads") ; }
    @Test public void read_13() { read("D.nq", "NQ") ; }
    @Test public void read_14() { read("D.trig", "TriG") ; }
    @Test public void read_15() { read("D.trig", "trig") ; } 

    @Test public void read_21() { read("D-nq",  RDFLanguages.NQUADS) ; }
    @Test public void read_22() { read("D-trig", RDFLanguages.TRIG) ; }
    @Test public void read_23() { read("D-nq",  "NQuads") ; }
    @Test public void read_24() { read("D-trig", "TRIG") ; }

    @Test public void read_input_1() throws IOException
    { read_stream("D.nq", "NQ") ; }
        
    @Test public void read_input_2() throws IOException
    { read_stream("D.trig", "trig") ; }
    
    @Test public void load_01() { RDFDataMgr.loadModel(filename("D.nt")) ; }
    @Test public void load_02() { RDFDataMgr.loadModel(filename("D.nt"), RDFLanguages.NTRIPLES)  ; }
    @Test public void load_03() { RDFDataMgr.loadModel(filename("D-nt"), RDFLanguages.NTRIPLES)  ; }

    @Test public void load_04() { RDFDataMgr.loadDataset(filename("D.nq")) ; }
    @Test public void load_05() { RDFDataMgr.loadDataset(filename("D.nq"), RDFLanguages.NQUADS)  ; }
    @Test public void load_06() { RDFDataMgr.loadDataset(filename("D-nq"), RDFLanguages.NQUADS)  ; }
    
    @Test public void load_11() { RDFDataMgr.loadGraph(filename("D.nt")) ; }
    @Test public void load_12() { RDFDataMgr.loadGraph(filename("D.nt"), RDFLanguages.NTRIPLES)  ; }
    @Test public void load_13() { RDFDataMgr.loadGraph(filename("D-nt"), RDFLanguages.NTRIPLES)  ; }

    @Test public void load_14() { RDFDataMgr.loadDatasetGraph(filename("D.nq")) ; }
    @Test public void load_15() { RDFDataMgr.loadDatasetGraph(filename("D.nq"), RDFLanguages.NQUADS)  ; }
    @Test public void load_16() { RDFDataMgr.loadDatasetGraph(filename("D-nq"), RDFLanguages.NQUADS)  ; }
    
    // Load triples into DSG 
    @Test public void load_17() { 
        DatasetGraph dsg = RDFDataMgr.loadDatasetGraph(filename("D.ttl")) ;
        assertFalse(dsg.getDefaultGraph().isEmpty()) ;
        assertEquals(0, Iter.count(dsg.listGraphNodes())) ;
    }
    
    // Load quads into graph - warning on named graphs
    @Test
    public void load_18()
    {
        ErrorHandler err = ErrorHandlerFactory.getDefaultErrorHandler() ;
        try
        {
            ErrorHandlerFactory.setDefaultErrorHandler(new ErrorHandlerTestLib.ErrorHandlerEx()) ;
            try {
                Graph g = RDFDataMgr.loadGraph(filename("D.trig")) ;
                fail("No expection generated") ;
            } catch (ErrorHandlerTestLib.ExWarning e) { }
            ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerNoLogging) ;
            Graph g = RDFDataMgr.loadGraph(filename("D.trig")) ;
            assertFalse(g.isEmpty()) ;
            assertEquals(1, g.size()) ;
        } finally
        {
            ErrorHandlerFactory.setDefaultErrorHandler(err) ;
        }
    }

    private static String filename(String filename) { return directory+"/"+filename ; }

    // Base.
    
    private static void read(String dataurl) { read(dataurl, (Lang)null) ; }
    
    private static void read(String dataurl, String lang)
    {
        read(dataurl, RDFLanguages.nameToLang(lang)) ;
    }
    
    private static void read(String dataurl, Lang lang)
    {
        dataurl = filename(dataurl) ;
        @SuppressWarnings("deprecation")
        Dataset ds = DatasetFactory.createMem() ;
        RDFDataMgr.read(ds, dataurl, lang) ;
    }

    private static void read_stream(String filename, String lang) throws IOException
    {
        read_stream(filename, RDFLanguages.nameToLang(lang)) ;
    }
    
    private static void read_stream(String filename, Lang lang) throws IOException
    {
        filename = filename(filename) ;
        
        // Read with a base
        @SuppressWarnings("deprecation")
        Dataset ds0 = DatasetFactory.createMem() ;
        try(FileInputStream in0 = new FileInputStream(filename)) {
            RDFDataMgr.read(ds0, in0, "http://example/base2", lang) ;
        }
        
        // Read again, but without base
        @SuppressWarnings("deprecation")
        Dataset ds1 = DatasetFactory.createMem() ;
        try(FileInputStream in1 = new FileInputStream(filename)) {
            RDFDataMgr.read(ds1, in1, null, lang) ;
        }
    }
    
    private static Model loadModel(String uri)
    {
        return RDFDataMgr.loadModel(filename(uri)) ;
    }

}

