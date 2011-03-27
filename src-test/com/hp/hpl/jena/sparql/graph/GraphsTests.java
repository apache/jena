/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.graph;

import java.util.Iterator ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Test API use of models, including some union graph cases : see also DatasetGraphTests */

public abstract class GraphsTests extends BaseTest
{
    // These graphs must exist.
    protected static final String graph1 = "http://example/g1" ;
    protected static final String graph2 = "http://example/g2" ;
    protected static final String graph3 = "http://example/g3" ;
    
    static Dataset ds ;
    static Model calcUnion = ModelFactory.createDefaultModel() ;

    protected abstract Dataset createDataset() ;
    
    protected Dataset getDataset()
    {
        if ( ds == null )
        {
            ds = createDataset() ;
            // Load default model.
            // Load graph 1
            // Load graph 2.
            ds.getDefaultModel().getGraph().add(SSE.parseTriple("(<x> <p> 'Default graph')")) ;
            
            Model m1 = ds.getNamedModel(graph1) ;
            m1.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 1')")) ;
            m1.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')")) ;
            
            Model m2 = ds.getNamedModel(graph2) ;
            m2.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 2')")) ;
            m2.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')")) ;
            
            calcUnion.add(m1) ;
            calcUnion.add(m2) ;
        }
        return ds ;
    }
    
    String queryString =  "SELECT * {?s ?p ?o}" ;
    
    @Test public void graph1() 
    {
        int x = query(queryString, getDataset().getDefaultModel()) ;
        assertEquals(1,x) ;
    }
    

    @Test public void graph2() 
    {
        int x = query(queryString, getDataset().getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph3() 
    {
        int x = query(queryString, getDataset().getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph4() 
    {
        int x = query(queryString, getDataset().getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
        Model m = getDataset().getNamedModel(Quad.unionGraph.getURI()) ;
        m.isIsomorphicWith(calcUnion) ;
    }

    @Test public void graph5() 
    {
        int x = query(queryString, getDataset().getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph6() 
    {
        int x = query(queryString, getDataset().getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
        assertEquals(1,x) ;
    }

    
    @Test public void graph_api1() 
    {
        int x = api(getDataset().getDefaultModel()) ;
        assertEquals(1,x) ;
    }
    

    @Test public void graph_api2() 
    {
        int x = api(getDataset().getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph_api3() 
    {
        int x = api(getDataset().getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph_api4() 
    {
        int x = api(getDataset().getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
        Model m = getDataset().getNamedModel(Quad.unionGraph.getURI()) ;
        m.isIsomorphicWith(calcUnion) ;
    }

    @Test public void graph_api5() 
    {
        int x = api(getDataset().getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_api6() 
    {
        int x = api(getDataset().getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
        assertEquals(1,x) ;
    }
    
    private int query(String str, Model model)
    {
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        QueryExecution qexec = QueryExecutionFactory.create(q, model) ;
        ResultSet rs = qexec.execSelect() ;
        int x = ResultSetFormatter.consume(rs) ;
        qexec.close() ;
        return x ;
    }
    
    private int api(Model model)
    {
        Iterator<Triple> iter = model.getGraph().find(Node.ANY, Node.ANY, Node.ANY) ;
        int x = (int)Iter.count(iter) ;
        return x ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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