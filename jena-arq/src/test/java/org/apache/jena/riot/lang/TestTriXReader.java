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

package org.apache.jena.riot.lang;

import static org.junit.Assert.assertTrue;

import java.io.InputStream ;
import java.util.Arrays ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.util.IsoMatcher ;
import org.junit.Assert ;
import org.junit.Test ;
import org.junit.runner.RunWith ;
import org.junit.runners.Parameterized ;
import org.junit.runners.Parameterized.Parameter ;
import org.junit.runners.Parameterized.Parameters ;

@RunWith(Parameterized.class)
public class TestTriXReader {
    
    static String DIR = "testing/RIOT/Lang/TriX" ;
    
    @Parameters(name="{0}")
    public static Iterable<Object[]> data() {
              return Arrays.asList(new Object[][] { 
                  { DIR+"/trix-01.trix", DIR+"/trix-01.nq" } ,
                  { DIR+"/trix-02.trix", DIR+"/trix-02.nq" } ,
                  { DIR+"/trix-03.trix", DIR+"/trix-03.nq" } ,
                  { DIR+"/trix-04.trix", DIR+"/trix-04.nq" } ,
                  { DIR+"/trix-05.trix", DIR+"/trix-05.nq" } ,
                  { DIR+"/trix-06.trix", DIR+"/trix-06.nq" } ,
                  { DIR+"/trix-10.trix", DIR+"/trix-10.nq" } ,        
                  { DIR+"/trix-11.trix", DIR+"/trix-11.nq" } ,        
                  { DIR+"/trix-12.trix", DIR+"/trix-12.nq" } ,        
                  { DIR+"/trix-13.trix", DIR+"/trix-13.nq" } ,        
                  { DIR+"/trix-14.trix", DIR+"/trix-14.nq" } , 
                  { DIR+"/trix-15.trix", DIR+"/trix-15.nq" } , 
                  // The example from HPL-2004-56
                  { DIR+"/trix-ex-1.trix", null },
//                  //{ "trix-ex-2.trix", null },  // Contains <integer> 
                  { DIR+"/trix-ex-3.trix", null },
                  { DIR+"/trix-ex-4.trix", null },
                  { DIR+"/trix-ex-5.trix", null },
                  // W3C DTD
                  { DIR+"/trix-w3c-1.trix", DIR+"/trix-w3c-1.nq" },
                  { DIR+"/trix-w3c-2.trix", DIR+"/trix-w3c-2.nq" }
                  });
    }
    
    @Parameter(0)
    public String fInput;
        
    @Parameter(1)
    public String fExpected;
    
    @Test
    public void trix_direct() {
        ReaderRIOT r = new ReaderTriX(RiotLib.dftProfile(), ErrorHandlerFactory.errorHandlerNoWarnings);
        InputStream in = IO.openFile(fInput) ;
        DatasetGraph dsg = DatasetGraphFactory.create() ;
        //StreamRDF stream = StreamRDFLib.writer(System.out) ;
        StreamRDF stream = StreamRDFLib.dataset(dsg) ;
        stream.start();
        r.read(in, null, null, stream, null) ;
        stream.finish();
        if ( fExpected != null ) {
            DatasetGraph dsg2 = RDFDataMgr.loadDatasetGraph(fExpected) ;
            boolean b = IsoMatcher.isomorphic(dsg, dsg2) ;
            if ( ! b ) {
                Assert.fail("Not isomorphic") ;
            }
        }
    }

    @Test
    public void trix_model() {
        // Ignore warnings of skipping quads reading into a model
        Model m1 = null ;
        Model m2 = null ;
        ErrorHandler err = ErrorHandlerFactory.getDefaultErrorHandler() ;
        try {
            ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerNoWarnings) ;
            m1 = RDFDataMgr.loadModel(fInput) ;
            if ( fExpected != null )
                m2 = RDFDataMgr.loadModel(fExpected) ;
        } finally {
            ErrorHandlerFactory.setDefaultErrorHandler(err) ;
        }
        if ( m2 != null )
            assertTrue("Models not isomorphic", m1.isIsomorphicWith(m2)) ;
    }
    
    @Test
    public void trix_dataset() {
        DatasetGraph ds1 = RDFDataMgr.loadDatasetGraph(fInput) ;
        DatasetGraph ds2 = null ;
        if ( fExpected != null )
            ds2 = RDFDataMgr.loadDatasetGraph(fExpected) ;
        if ( ds2 != null )
            assertTrue("Datasets not isomorphic", IsoMatcher.isomorphic(ds1, ds2)) ;
    }
}

