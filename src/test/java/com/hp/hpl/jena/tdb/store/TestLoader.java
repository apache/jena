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

package com.hp.hpl.jena.tdb.store;

import java.io.InputStream ;
import java.util.List ;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.TDBLoader ;

public class TestLoader extends BaseTest
{
    private static final String DIR = "testing/Loader/" ;
    private static final Node g = Node.createURI("g") ;
    private static final Node s = Node.createURI("s") ;
    private static final Node p = Node.createURI("p") ;
    private static final Node o = Node.createURI("o") ;
    
    @BeforeClass static public void beforeClass()   { Log.disable(ARQ.logExecName) ; Log.disable(TDB.logLoaderName) ; }
    @AfterClass  static public void afterClass()    { Log.enable(ARQ.logExecName) ; Log.enable(TDB.logLoaderName) ; }
    
    @Test public void load_dataset_01()
    {
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph() ;
        TDBLoader.load(dsg, DIR+"data-1.nq", false) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertEquals(1, dsg.getGraph(g).size()) ;
    }

    @Test public void load_dataset_02()
    {
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph() ;
        InputStream in = IO.openFile(DIR+"data-1.nq") ;
        TDBLoader.load(dsg, in, false) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertEquals(1, dsg.getGraph(g).size()) ;
    }

    @Test public void load_graph_01()
    {
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph() ;
        TDBLoader.load(dsg, DIR+"data-2.nt", false) ;
        assertEquals(1, dsg.getDefaultGraph().size()) ;
    }

    @Test public void load_graph_02()
    {
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph() ;
        TDBLoader.load(dsg.getDefaultGraphTDB(), DIR+"data-2.nt", false) ;
        assertEquals(1, dsg.getDefaultGraph().size()) ;
    }

    @Test public void load_graph_03()
    {
        DatasetGraphTDB dsg = TDBFactory.createDatasetGraph() ;
        TDBLoader.load(dsg.getGraphTDB(g), DIR+"data-2.nt", false) ;
        assertEquals(0, dsg.getDefaultGraph().size()) ;
        assertEquals(1, dsg.getGraph(g).size()) ;
        
        // Check indexes.
        List<Triple> x = Iter.toList(dsg.getDefaultGraph().find(null, null, null)) ; 
        assertEquals(0, x.size()) ;
            
        x = Iter.toList(dsg.getGraph(g).find(null, null, null)) ;
        assertEquals(1, x.size()) ;
        x = Iter.toList(dsg.getGraph(g).find(s, null, null)) ;
        assertEquals(1, x.size()) ;
        x = Iter.toList(dsg.getGraph(g).find(null, p, null)) ;
        assertEquals(1, x.size()) ;
        x = Iter.toList(dsg.getGraph(g).find(null, null, o)) ;
        assertEquals(1, x.size()) ;
        
        List<Quad> z = Iter.toList(dsg.find(null, null, null, null)) ;
        assertEquals(1, z.size()) ;
        z = Iter.toList(dsg.find(g, null, null, null)) ;
        assertEquals(1, z.size()) ;
        z = Iter.toList(dsg.find(null, s, null, null)) ;
        assertEquals(1, z.size()) ;
        z = Iter.toList(dsg.find(null, null, p, null)) ;
        assertEquals(1, z.size()) ;
        z = Iter.toList(dsg.find(null, null, null, o)) ;
        assertEquals(1, z.size()) ;
    }
}
