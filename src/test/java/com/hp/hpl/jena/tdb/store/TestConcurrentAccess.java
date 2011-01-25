/*
 * (c) Copyright 2009 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.ConcurrentModificationException ;
import java.util.Iterator ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Graph ;
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
        ds.getDefaultModel().getGraph().getBulkUpdateHandler().add(g) ;
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
        m.getGraph().getBulkUpdateHandler().add(buildGraph()) ;
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
        m.getGraph().getBulkUpdateHandler().add(buildGraph()) ;
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
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSet rs = qExec.execSelect() ;
        while(rs.hasNext()) 
            rs.next();
        qExec.close() ;
        
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

/*
 * (c) Copyright 2009 Talis Systems Ltd.
 * (c) Copyright 2011 Epimorphics Ltd.
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