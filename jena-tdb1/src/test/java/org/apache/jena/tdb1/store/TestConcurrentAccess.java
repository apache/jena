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

package org.apache.jena.tdb1.store;

import static org.junit.Assert.assertNotNull;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.rdf.model.Statement ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.sse.builders.BuilderGraph ;
import org.apache.jena.tdb1.TDB1Factory;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.junit.Test ;

public class TestConcurrentAccess
{
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
        Dataset ds = TDB1Factory.createDataset() ;
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
        Dataset d = TDB1Factory.createDataset() ;
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
        Dataset d = TDB1Factory.createDataset() ;
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
