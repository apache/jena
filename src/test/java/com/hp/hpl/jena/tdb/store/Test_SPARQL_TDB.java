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
