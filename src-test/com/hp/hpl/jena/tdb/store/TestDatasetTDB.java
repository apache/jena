/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.sse.SSE;

import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.junit.GraphLocation;

import org.junit.*;
import test.BaseTest;

/** Testing the quad support for TDB */ 
public class TestDatasetTDB extends BaseTest
{
    static Node n0 = SSE.parseNode("<http://example/n0>") ; 
    static Node n1 = SSE.parseNode("<http://example/n1>") ;
    static Node n2 = SSE.parseNode("<http://example/n2>") ;
    
    static GraphLocation graphLocation = null ;
    
    @BeforeClass public static void beforeClass()
    {
        graphLocation = new GraphLocation(new Location(TS_Store.testArea), TDBFactory.stdFactory) ;
    }
        
    @Before public void before()
    {   
        graphLocation.clearDirectory() ; 
        graphLocation.createDataset() ;
    }
    
    @After public void after()
    {   
        graphLocation.release() ;
    }
    
    @AfterClass public static void afterClass()
    { 
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
    }
    
    @Test public void dataset1()
    {
        Dataset ds = graphLocation.getDataset() ;
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
        Graph g2 = ds.getNamedModel("http://example/").getGraph() ;
        
        g2.add(new Triple(n0,n1,n2) ) ;
        assertTrue(g2.contains(n0,n1,n2) ) ;
        assertFalse(g1.contains(n0,n1,n2) ) ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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