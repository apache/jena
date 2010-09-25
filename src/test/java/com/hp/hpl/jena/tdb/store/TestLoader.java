/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

