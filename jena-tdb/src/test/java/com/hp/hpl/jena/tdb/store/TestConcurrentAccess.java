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

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderGraph ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

public class TestConcurrentAccess extends BaseTest
{
//    static { 
//        Log.disable("com.hp.hpl.jena.tdb.exec") ;
//        Log.disable("com.hp.hpl.jena.tdb.info") ;
//    }
    
    static String data = StrUtils.strjoinNL(
       "(graph",
       "  (<x> <p> 1)" ,
       "  (<x> <p> 2)" ,
       "  (<x> <p> 3)" ,
       "  (<x> <p> 4)" ,
       "  (<x> <p> 5)" ,
       "  (<x> <p> 6)" ,
       "  (<x> <p> 7)" ,
       "  (<x> <p> 8)" ,
       "  (<x> <p> 9)" ,
        ")") ;
    
    private static Graph buildGraph()
    {
        Item item = SSE.parse(data) ;
        Graph g = BuilderGraph.buildGraph(item) ;
        return g ;
    }
    
    private static Dataset create()
    {
        Graph g = buildGraph() ;
        Dataset ds = TDBFactory.createDataset() ;
        GraphUtil.addInto(ds.getDefaultModel().getGraph(), g) ;
        return ds ;
    }
    
    @Test public void mrswGraph1()
    {
        Model m = create().getDefaultModel() ;
        Resource r = m.createResource("x") ;
        ExtendedIterator<Statement> iter1 = m.listStatements(r, null, (RDFNode)null) ;
        assertNotNull(iter1.next()) ;
        
        ExtendedIterator<Statement> iter2 = m.listStatements(r, null, (RDFNode)null) ;
        assertNotNull(iter2.next()) ;
        
        for ( ; iter2.hasNext() ; ) iter2.next() ;
        
        assertNotNull(iter1.next()) ;
    }
    
    @Test(expected=ConcurrentModificationException.class)
    public void mrswGraph2()
    {
        Model m = create().getDefaultModel() ;
        Resource r = m.createResource("x") ;
        ExtendedIterator<Statement> iter1 = m.listStatements(r, null, (RDFNode)null) ;
        assertNotNull(iter1.next()) ;
        
        Triple t = SSE.parseTriple("(<y> <p> 99)") ;
        m.getGraph().add(t) ;
        
        // Bad
        iter1.hasNext();
    }
    
    @Test(expected=ConcurrentModificationException.class)
    public void mrswGraph3()
    {
        Model m = create().getDefaultModel() ;
        Resource r = m.createResource("x") ;
        ExtendedIterator<Statement> iter1 = m.listStatements(r, null, (RDFNode)null) ;
        assertNotNull(iter1.next()) ;
        
        Triple t = SSE.parseTriple("(<y> <p> 99)") ;
        m.getGraph().delete(t) ;
        
        // Bad
        iter1.hasNext();
    }

    @Test(expected=ConcurrentModificationException.class)
    public void mrswGraph4()
    {
        Model m = create().getDefaultModel() ;
        Resource r = m.createResource("x") ;
        ExtendedIterator<Statement> iter1 = m.listLiteralStatements(r, null, 1) ;
        assertNotNull(iter1.next()) ;
        // and now the iterator has implicitly finished.
        
        Triple t = SSE.parseTriple("(<y> <p> 99)") ;
        m.getGraph().add(t) ;
        
        // Bad - modification of the dataset occurred.
        iter1.hasNext();
    }
    
    @Test
    public void mrswGraph5()
    {
        Dataset d = TDBFactory.createDataset() ;
        Model m = d.getNamedModel("http://example") ;
        GraphUtil.addInto(m.getGraph(), buildGraph()) ;
        Resource r = m.createResource("x") ;
        ExtendedIterator<Statement> iter1 = m.listStatements(r, null, (RDFNode)null) ;
        while(iter1.hasNext()) 
            iter1.next();
        Triple t = SSE.parseTriple("(<y> <p> 99)") ;
        m.getGraph().delete(t) ;
        iter1.hasNext() ;
    }

    @Test(expected=ConcurrentModificationException.class)
    public void mrswGraph6()
    {
        Dataset d = TDBFactory.createDataset() ;
        Model m = d.getNamedModel("http://example") ;
        GraphUtil.addInto(m.getGraph(), buildGraph()) ;
        Resource r = m.createResource("x") ;
        ExtendedIterator<Statement> iter1 = m.listStatements(r, null, (RDFNode)null) ;
        assertNotNull(iter1.next()) ;
        
        Triple t = SSE.parseTriple("(<y> <p> 99)") ;
        m.getGraph().delete(t) ;
        iter1.next() ;
    }
    
    @Test
    public void mrswSPARQL1()
    {
        Dataset ds = create(); 
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o}") ;
        try(QueryExecution qExec = QueryExecutionFactory.create(query, ds)) {
            ResultSet rs = qExec.execSelect() ;
            while(rs.hasNext()) 
                rs.next();
        }
        
        DatasetGraph dsg = ds.asDatasetGraph() ;
        Quad quad = SSE.parseQuad("(<g> <y> <p> 99)") ;
        dsg.add(quad) ;
        
        Iterator<Quad> iter = dsg.find() ;
        iter.hasNext() ;
        iter.next() ;
    }
    
    @Test(expected=ConcurrentModificationException.class)
    public void mrswSPARQL2()
    {
        Dataset ds = create(); 
        DatasetGraph dsg = ds.asDatasetGraph() ;
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o}") ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSet rs = qExec.execSelect() ;
        rs.hasNext() ; 
        rs.next();
        Quad quad = SSE.parseQuad("(<g> <y> <p> 99)") ;
        dsg.add(quad) ;
        rs.hasNext() ;  // <<--- Here.
        rs.next();
    }

    
    @Test(expected=ConcurrentModificationException.class)
    public void mrswDataset1()
    {
        DatasetGraph dsg = create().asDatasetGraph() ;
        Quad quad = SSE.parseQuad("(<g> <y> <p> 99)") ;
        Iterator<Quad> iter = dsg.find() ;
        dsg.add(quad) ;
        // Bad - after an update.
        iter.hasNext() ;
        iter.next() ;
    }
    
    
   
    // More DSG tests ..
}
