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

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

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
    
    private Dataset dataset ;
    private Model calcUnion = ModelFactory.createDefaultModel() ;

    protected abstract Dataset createDataset() ;
    
    protected Dataset getDataset()
    {
        if ( dataset == null )
        {
            dataset = createDataset() ;
            // Load default model.
            // Load graph 1
            // Load graph 2.
            dataset.getDefaultModel().getGraph().add(SSE.parseTriple("(<x> <p> 'Default graph')")) ;
            
            Model m1 = dataset.getNamedModel(graph1) ;
            m1.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 1')")) ;
            m1.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')")) ;
            
            Model m2 = dataset.getNamedModel(graph2) ;
            m2.getGraph().add(SSE.parseTriple("(<x> <p> 'Graph 2')")) ;
            m2.getGraph().add(SSE.parseTriple("(<x> <p> 'ZZZ')")) ;
            
            calcUnion.add(m1) ;
            calcUnion.add(m2) ;
        }
        return dataset ;
    }
    
    String queryString =  "SELECT * {?s ?p ?o}" ;
    
    @Test public void graph1() 
    {
        Dataset ds = getDataset() ;
        int x = query(queryString, ds.getDefaultModel()) ;
        assertEquals(1,x) ;
    }
    

    @Test public void graph2() 
    {
        Dataset ds = getDataset() ;
        int x = query(queryString, ds.getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph3() 
    {
        Dataset ds = getDataset() ;
        int x = query(queryString, ds.getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph4() 
    {
        Dataset ds = getDataset() ;
        int x = query(queryString, ds.getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
        Model m = ds.getNamedModel(Quad.unionGraph.getURI()) ;
        m.isIsomorphicWith(calcUnion) ;
    }

    @Test public void graph5() 
    {
        Dataset ds = getDataset() ;
        int x = query(queryString, ds.getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph6() 
    {
        Dataset ds = getDataset() ;
        int x = query(queryString, ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_count1() 
    {
        Dataset ds = getDataset() ;
        long x = count(ds.getDefaultModel()) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_count2() 
    {
        Dataset ds = getDataset() ;
        long x = count(ds.getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph_count3() 
    {
        Dataset ds = getDataset() ;
        long x = count(ds.getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph_count4() 
    {
        Dataset ds = getDataset() ;
        long x = count(ds.getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
    }
    
    @Test public void graph_count5() 
    {
        Dataset ds = getDataset() ;
        long x = count(ds.getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_count6() 
    {
        Dataset ds = getDataset() ;
        long x = count(ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
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
        Dataset ds = getDataset() ;
        int x = api(ds.getDefaultModel()) ;
        assertEquals(1,x) ;
    }
    

    @Test public void graph_api2() 
    {
        Dataset ds = getDataset() ;
        int x = api(ds.getNamedModel(graph1)) ;
        assertEquals(2,x) ;
    }

    @Test public void graph_api3() 
    {
        Dataset ds = getDataset() ;
        int x = api(ds.getNamedModel(graph3)) ;
        assertEquals(0,x) ;
    }
    
    @Test public void graph_api4() 
    {
        Dataset ds = getDataset() ;
        int x = api(ds.getNamedModel(Quad.unionGraph.getURI())) ;
        assertEquals(3,x) ;
        Model m = ds.getNamedModel(Quad.unionGraph.getURI()) ;
        m.isIsomorphicWith(calcUnion) ;
    }

    @Test public void graph_api5() 
    {
        Dataset ds = getDataset() ;
        int x = api(ds.getNamedModel(Quad.defaultGraphIRI.getURI())) ;
        assertEquals(1,x) ;
    }

    @Test public void graph_api6() 
    {
        Dataset ds = getDataset() ;
        int x = api(ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI())) ;
        assertEquals(1,x) ;
    }
    
    private int query(String str, Model model)
    {
        Query q = QueryFactory.create(str, Syntax.syntaxARQ) ;
        try(QueryExecution qexec = QueryExecutionFactory.create(q, model)) {
            ResultSet rs = qexec.execSelect() ;
            return  ResultSetFormatter.consume(rs) ;
        }
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
