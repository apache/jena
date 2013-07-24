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

package com.hp.hpl.jena.tdb.store;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.update.* ;

/**
 * Test SPARQL
 */
public class Test_SPARQL_TDB extends BaseTest
{
    private static Dataset create() 
    {
        return TDBFactory.createDataset() ;
    }
    
    private static Dataset create(Location location) 
    {
        return TDBFactory.createDataset(location) ;
    }

    
    @Test public void sparql1()
    {
        // Test OpExecutor.execute(OpFilter)for a named graph used as a standalone model
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> 123)") ;
        Dataset ds = create() ;
        Graph g2 = ds.asDatasetGraph().getGraph(NodeFactory.createURI(graphName)) ;
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
        Dataset ds = create() ;
        Graph g2 = ds.asDatasetGraph().getGraph(NodeFactory.createURI(graphName)) ;
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
        Dataset dataset = create() ;
        Query query = QueryFactory.create("SELECT ?g { GRAPH ?g {} }") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(0, n) ;
    }
    
    @Test public void sparql4()
    {
        // OpDatasetNames 
        Dataset dataset = create() ;
        
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> 123)") ;
        Graph g2 = dataset.asDatasetGraph().getGraph(NodeFactory.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        Query query = QueryFactory.create("SELECT ?g { GRAPH ?g {} }") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(1, n) ;
    }
    
    @Test public void sparql5()
    {
        Dataset dataset = create() ;
        
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<x> <y> 123)") ;
        Graph g2 = dataset.asDatasetGraph().getGraph(NodeFactory.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        Query query = QueryFactory.create("ASK { GRAPH <"+graphName+"> {} }") ;
        boolean b = QueryExecutionFactory.create(query, dataset).execAsk() ;
        assertEquals(true, b) ;
    }
    
    @Test public void sparql6()
    {
        Dataset dataset = create() ;
        
        String graphName = "http://example/" ;
        Triple triple = SSE.parseTriple("(<http://example/x> <http://example/y> 123)") ;
        Graph g2 = dataset.asDatasetGraph().getGraph(NodeFactory.createURI(graphName)) ;
        // Graphs only exists if they have a triple in them
        g2.add(triple) ;
        
        Query query = QueryFactory.create("ASK { GRAPH <http://example/x> {} }") ;
        boolean b = QueryExecutionFactory.create(query, dataset).execAsk() ;
        assertEquals(false, b) ;
    }

    // Test transactions effective.
    
    @Test public void sparql_txn_1()
    {
        Dataset dataset = create() ;
        update(dataset, "INSERT DATA { <x:s> <x:p> <x:o> }") ;

        dataset.begin(ReadWrite.READ) ;
        try {
            int n = count(dataset) ;
            assertEquals(1, n) ;
            n = count(dataset, "SELECT * { <x:s> <x:p> <x:o>}") ;
            assertEquals(1, n) ;
        } finally { dataset.end() ; }
    }

    @Test public void sparql_txn_2()
    {
        Dataset dataset1 = create(Location.mem("foo")) ;
        Dataset dataset2 = create(Location.mem("foo")) ;
        
        // Test the test setup.
        update(dataset1, "INSERT DATA { <x:s> <x:p> <x:o> }") ;
        //TDB.sync(dataset1) ;
        assertEquals(1, count(dataset2)) ;
        
        dataset1.begin(ReadWrite.READ) ;
        
        dataset2.begin(ReadWrite.WRITE) ;
        update(dataset2, "INSERT DATA { <x:s> <x:p> <x:o2> }") ;
        
        assertEquals(1, count(dataset1)) ;
        assertEquals(2, count(dataset2)) ;
        dataset2.commit();
        dataset2.end() ;
        
        // This is 2 if dataset1 is not in a transaction
        // but that replies on dataset2 commit doing the write back.
        assertEquals(1, count(dataset1)) ;  
        
        dataset1.end() ;
        
        dataset1.begin(ReadWrite.READ) ;
        assertEquals(2, count(dataset1)) ;
        dataset1.end() ;
    }

    @Test public void sparql_update_unionGraph()
    {
        // JENA-344
        Dataset ds = TDBFactory.createDataset() ;
        ds.asDatasetGraph().add(SSE.parseQuad("(<g> <s> <p> 123)")) ;
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        String us = StrUtils.strjoinNL(
            "INSERT { GRAPH <http://example/g2> { ?s ?p 'NEW' } }",
            "WHERE { ",
                 "?s ?p 123",
            " }" ) ;
                                       
        UpdateRequest req = UpdateFactory.create(us) ;
        UpdateAction.execute(req, ds) ;
        
        Model m = ds.getNamedModel("http://example/g2") ;
        assertEquals("Did not find 1 statement in named graph", 1, m.size()) ;
    }
    
    private int count(Dataset dataset)
    { return count(dataset, "SELECT * { ?s ?p ?o }") ; }
    
    private int count(Dataset dataset, String queryString)
    
    {
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
        ResultSet rs = qExec.execSelect() ;
        return ResultSetFormatter.consume(rs) ;
    }
    private void update(Dataset dataset, String string)
    {
        UpdateRequest req = UpdateFactory.create(string) ;
        GraphStore gs = GraphStoreFactory.create(dataset) ;
        UpdateProcessor proc = UpdateExecutionFactory.create(req, gs) ;
        proc.execute() ;
    }
}
