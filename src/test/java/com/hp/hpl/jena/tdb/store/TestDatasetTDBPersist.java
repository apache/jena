/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;


import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.After ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.junit.GraphLocation ;
import com.hp.hpl.jena.tdb.sys.TDBMaker ;

/** Testing persistence  */ 
public class TestDatasetTDBPersist extends BaseTest
{
    static Node n0 = NodeFactory.parseNode("<http://example/n0>") ; 
    static Node n1 = NodeFactory.parseNode("<http://example/n1>") ;
    static Node n2 = NodeFactory.parseNode("<http://example/n2>") ;
    
    static GraphLocation graphLocation = null ;
    
    @BeforeClass public static void beforeClass()
    {
        graphLocation = new GraphLocation(new Location(ConfigTest.getTestingDirDB())) ;
    }

    @AfterClass public static void afterClass()
    { 
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
    }
    
    @Before public void before()
    {   
        // Windows/memory mapped does not allow deleting memory mapped files.
        if ( false )
        {
            if ( graphLocation.getDataset() == null )
                graphLocation.createDataset() ;
            else
                graphLocation.getDataset().asDatasetGraph().deleteAny(null, null, null, null) ;
        }
        else
        {
            graphLocation.clearDirectory() ; 
            graphLocation.createDataset() ;
        }
    }
    
    @After public void after()
    {   
        graphLocation.release() ;
        TDBMaker.clearDatasetCache() ;
    }
    
    @Test public void dataset1()
    {
        Dataset ds = graphLocation.getDataset() ;
        assertTrue( ds.asDatasetGraph() instanceof DatasetGraphTDB ) ;
        assertTrue( ds.getDefaultModel().getGraph() instanceof GraphTriplesTDB ) ;
        assertTrue( ds.getNamedModel("http://example/").getGraph() instanceof GraphNamedTDB ) ;
    }
    
    @Test public void dataset2()
    {
        Dataset ds = graphLocation.getDataset() ;
        Graph g1 = ds.getDefaultModel().getGraph() ;
        Graph g2 = ds.getNamedModel("http://example/").getGraph() ;
        
        g1.add(new Triple(n0,n1,n2) ) ;
        assertTrue(g1.contains(n0,n1,n2) ) ;
        assertFalse(g2.contains(n0,n1,n2) ) ;
    }

    @Test public void dataset3()
    {
        Dataset ds = graphLocation.getDataset() ;
        Graph g1 = ds.getDefaultModel().getGraph() ;
        // Sometimes, under windows, deleting the files by 
        // graphLocation.clearDirectory does not work.  
        // Needed for safe tests on windows.
        g1.getBulkUpdateHandler().removeAll() ;
        
        Graph g2 = ds.getNamedModel("http://example/").getGraph() ;
        g2.add(new Triple(n0,n1,n2) ) ;
        assertTrue(g2.contains(n0,n1,n2) ) ;
        assertFalse(g1.contains(n0,n1,n2) ) ;
    }

    @Test public void dataset4()
    {
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> <z>)") ;
        Node gn = Node.createURI(graphName) ;

        Dataset ds = graphLocation.getDataset() ;
        // ?? See TupleLib.
        ds.asDatasetGraph().deleteAny(gn, null, null, null) ;
        
        Graph g2 = ds.asDatasetGraph().getGraph(gn) ;
        
//        if ( true )
//        {
//            PrintStream ps = System.err ;
//            ps.println("Dataset names: ") ;
//            Iter.print(ps, ds.listNames()) ;
//        }
        
        // Graphs only exists if they have a triple in them
        assertFalse(ds.containsNamedModel(graphName)) ;
        
        Iterator<String> iter = ds.listNames() ;
        assertFalse(iter.hasNext()) ;
        
        assertEquals(0, ds.asDatasetGraph().size()) ;
    }
    
    @Test public void dataset5()
    {
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> <z>)") ;
        Dataset ds = graphLocation.getDataset() ;
        Graph g2 = ds.asDatasetGraph().getGraph(Node.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        assertTrue(ds.containsNamedModel(graphName)) ;
        Iterator<String> iter = ds.listNames() ;
        List<String> x = Iter.toList(iter) ;
        List<String> y = Arrays.asList(graphName) ;
        assertEquals(x,y) ;
        
        assertEquals(1, ds.asDatasetGraph().size()) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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