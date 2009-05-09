/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import org.junit.BeforeClass;
import org.junit.Test;
import atlas.test.BaseTest;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

/** Test API use of models */

public class TestGraphs extends BaseTest
{
    static final String graph1 = "http://example/g1" ;
    static final String graph2 = "http://example/g2" ;
    static final String graph3 = "http://example/g3" ;
    
    static Dataset ds ;
    @BeforeClass public static void setupClass()
    {
        SystemTDB.defaultOptimizer = ReorderLib.identity() ;
        ds = TDBFactory.createDataset() ;
        // Load default mdoel.
        // Load graph 1
        // Load graph 2.
        ds.getDefaultModel().getGraph().add(SSE.parseTriple("(<x> <p> 'Default graph')")) ;
        
        Model m1 = ds.getNamedModel(graph1) ;
        m1.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 1')")) ;
        m1.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')")) ;
        
        Model m2 = ds.getNamedModel(graph2) ;
        m2.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 2')")) ;
        m2.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')")) ;
    }
    
    String queryString =  "SELECT * {?s ?p ?o}" ;
    
    @Test public void graph1() 
    {
        int x = query(queryString, ds.getDefaultModel()) ;
        assertEquals(1,x) ;
    }
    

    @Test public void graph2() 
    {
        int x = query(queryString, ds.getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph3() 
    {
        int x = query(queryString, ds.getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph4() 
    {
        int x = query(queryString, ds.getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
    }

    @Test public void graph5() 
    {
        int x = query(queryString, ds.getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph6() 
    {
        int x = query(queryString, ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
        assertEquals(1,x) ;
    }

    
    // Named model.
//    Quad.unionGraph ;
//    Quad.defaultGraphIRI ;
//    Quad.defaultGraphNodeGenerated ;
   
    
    private int query(String str, Model model)
    {
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        int x = ResultSetFormatter.consume(rs) ;
        qexec.close() ;
        return x ;
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