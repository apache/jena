/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.tdb.TDBFactory;


public class TestBulkUpdateTDB extends BaseTest
{
    //private static PrefixMapping 
    
    private static String graphName = "http://graph/";
    
    private static Node n0 = NodeFactory.parseNode("<http://example/n0>") ; 
    private static Node n1 = NodeFactory.parseNode("<http://example/n1>") ;
    private static Node n2 = NodeFactory.parseNode("<http://example/n2>") ;
    
    private static Triple t1 = SSE.parseTriple("(<x> <y> <z>)") ;
    
    private static Graph create()
    {
        Graph g = TDBFactory.createGraph() ;
        return g ;
    }
    
    @Test public void update1()
    {
        Graph g = create() ;
        g.getBulkUpdateHandler().add(new Triple[]{t1}) ;
        assertTrue(g.contains(t1)) ;
        g.getBulkUpdateHandler().delete(new Triple[]{t1}) ;
        assertFalse(g.contains(t1)) ;
    }

    @Test public void update2()
    {
        Graph g = create() ;
        g.getBulkUpdateHandler().add(new Triple[]{t1}) ;
        assertTrue(g.contains(t1)) ;
        g.getBulkUpdateHandler().removeAll() ;
        assertFalse(g.contains(t1)) ;
    }


    @Test public void update3()
    {
        Dataset ds = TDBFactory.createDataset() ;
        
        ds.asDatasetGraph().getDefaultGraph().add(t1) ;
        ds.getNamedModel(graphName).getGraph().add(t1) ;
        
        Model m = ds.getDefaultModel() ;
        m.removeAll() ;
        assertEquals(0, m.size()) ;
        
        // But still in the other graph
        assertTrue(ds.getNamedModel(graphName).getGraph().contains(t1)) ;
    }

    @Test public void update4()
    {
        Dataset ds = TDBFactory.createDataset() ;
        
        ds.asDatasetGraph().getDefaultGraph().add(t1) ;
        ds.getNamedModel(graphName).getGraph().add(t1) ;
        
        Model m = ds.getNamedModel(graphName) ;
        m.removeAll() ;
        assertEquals(0, m.size()) ;
        
        // But still in the other graph
        assertTrue(ds.getDefaultModel().getGraph().contains(t1)) ;
    }

    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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