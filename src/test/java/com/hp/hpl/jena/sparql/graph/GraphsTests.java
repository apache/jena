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

package com.hp.hpl.jena.sparql.graph;

import java.util.Iterator ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.* ;
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
    
    private Dataset ds ;
    private Model calcUnion = ModelFactory.createDefaultModel() ;

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

    @Test public void graph_count1() 
    {
        long x = count(getDataset().getDefaultModel()) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_count2() 
    {
        long x = count(getDataset().getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph_count3() 
    {
        long x = count(getDataset().getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph_count4() 
    {
        long x = count(getDataset().getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
    }
    
    @Test public void graph_count5() 
    {
        long x = count(getDataset().getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_count6() 
    {
        long x = count(getDataset().getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_count7()
    {
        Dataset ds = getDataset() ;
        Model m = ds.getNamedModel("http://example/no-such-graph") ;
        long x = m.size() ;
        assertEquals(0, x) ;
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
    
    private long count(Model model)
    {
        return model.size() ;
    }

}
