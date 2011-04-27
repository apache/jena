/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * Additions Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class Test_SPARQL_TDB extends BaseTest
{
    
    @Test public void sparql1()
    {
        // Test OpExecutor.execute(OpFilter)for a named graph used as a standalone model
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> 123)") ;
        Dataset ds = TDBFactory.createDataset() ;
        Graph g2 = ds.asDatasetGraph().getGraph(Node.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        Model m = ModelFactory.createModelForGraph(g2) ;
        String qs = "SELECT * { ?s ?p ?o . FILTER ( ?o < 456 ) }" ;
        Query query = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, m) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
    }
    
    @Test public void sparql2()
    {
        // Test OpExecutor.execute(OpBGP) for a named graph used as a standalone model
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> 123)") ;
        Dataset ds = TDBFactory.createDataset() ;
        Graph g2 = ds.asDatasetGraph().getGraph(Node.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        Model m = ModelFactory.createModelForGraph(g2) ;
        String qs = "SELECT * { ?s ?p ?o . }" ;
        Query query = QueryFactory.create(qs) ;
        QueryExecution qexec = QueryExecutionFactory.create(query, m) ;
        ResultSet rs = qexec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
    }
    
    @Test public void sparql3()
    {
        // Requires OpDatasetNames 
        Dataset dataset = TDBFactory.createDataset() ;
        Query query = QueryFactory.create("SELECT ?g { GRAPH ?g {} }") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(0, n) ;
    }
    
    @Test public void sparql4()
    {
        // Requires OpDatasetNames 
        Dataset dataset = TDBFactory.createDataset() ;
        
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> 123)") ;
        Graph g2 = dataset.asDatasetGraph().getGraph(Node.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        Query query = QueryFactory.create("SELECT ?g { GRAPH ?g {} }") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(1, n) ;
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