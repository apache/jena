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

package org.apache.jena.sparql.core;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public abstract class AbstractTestDynamicDataset extends BaseTest
{
    protected abstract Dataset createDataset() ;
    protected abstract void releaseDataset(Dataset ds) ;
    protected Dataset  dataset ;
    
    @Before public void before()
    {
        dataset = createDataset() ;
        // Named graphs
        for ( int i = 0 ; i < 5 ; i++ )
            addGraph(dataset, i) ;
        // Default model.
        Model m = dataset.getDefaultModel() ;
        Triple t1 = SSE.parseTriple("(<uri:x> <uri:p> 0)") ;
        Triple t2 = SSE.parseTriple("(<uri:y> <uri:q> 'ABC')") ; 
        Triple t3 = SSE.parseTriple("(<uri:z> <uri:property> 'DEF')") ; 
        m.getGraph().add(t1) ;
        m.getGraph().add(t2) ;
        m.getGraph().add(t3) ;
    }
    
    @After public void after() {
        releaseDataset(dataset) ; 
    }

    private static void addGraph(Dataset dataset, int i)
    {
        // Not a very interesting model
        String x = "graph:"+i ;
        Model m = dataset.getNamedModel(x) ;
        Triple t1 = SSE.parseTriple("(<uri:x> <uri:p> "+i+")") ;
        Triple t2 = SSE.parseTriple("(<uri:y> <uri:q> 'ABC')") ; 
        m.getGraph().add(t1) ;
        m.getGraph().add(t2) ;
    }
    
    @Test public void dynamic01()    { testCount("SELECT * {?s ?p ?o}", 3, dataset) ; }
    
    @Test public void dynamic02()    { testCount("SELECT ?g { GRAPH ?g {} }", 5, dataset) ; }
    
    @Test public void dynamic03()    { testCount("SELECT * FROM <graph:1> {?s <uri:p> ?o}", 1, dataset) ; }

    @Test public void dynamic04()    { testCount("SELECT * FROM <graph:1> { GRAPH ?g { ?s ?p ?o} }", 0, dataset) ; }
    
    @Test public void dynamic05()    { testCount("SELECT * FROM <graph:1> FROM <graph:2> {?s <uri:p> ?o}", 2, dataset) ; }

    // Duplicate surpression
    @Test public void dynamic06()    { testCount("SELECT ?s FROM <graph:1> FROM <graph:2> {?s <uri:q> ?o}", 1, dataset) ; }
    
    @Test public void dynamic07()    { testCount("SELECT ?s FROM NAMED <graph:1> {?s <uri:q> ?o}", 0, dataset) ; }
    
    @Test public void dynamic08()    { testCount("SELECT ?s FROM <graph:2> FROM NAMED <graph:1> {?s <uri:q> ?o}", 1, dataset) ; }

    @Test public void dynamic09()    { testCount("SELECT * "+
                                                "FROM <graph:1> FROM <graph:2> "+
                                                "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                2, dataset) ; 
                                    }
    
    @Test public void dynamic10()    { testCount("SELECT * "+
                                                "FROM <graph:1> FROM <graph:2>"+
                                                "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                2, dataset) ; 
                                    }

    @Test public void dynamic11()    { testCount("SELECT * "+
                                                "FROM <x:unknown>"+
                                                "{ GRAPH ?g { ?s <uri:q> ?o }}",
                                                0, dataset) ; 
                                    }

    @Test public void dynamic12()    { testCount("SELECT * "+
                                                 "FROM  <graph:1>"+
                                                 "{ GRAPH ?g { }}",
                                                 0, dataset) ; 
                                     }

    @Test public void dynamic13()    { testCount("SELECT * "+
                                                 "FROM NAMED <graph:1>"+
                                                 "{ GRAPH ?g { }}",
                                                 1, dataset) ; 
                                     }

    @Test public void dynamic14()    { testCount("SELECT * "+
                                                 "FROM NAMED <graph:1> FROM NAMED <graph:2>"+
                                                 "FROM <graph:3> "+
                                                 "{ GRAPH ?g { }}",
                                                 2, dataset) ; 
                                     }

    // -- Union graph.
    
    // No FROM <union> the underlying dataset.
    @Test public void dynamic_union_1() { 
        testCount("SELECT * FROM <urn:x-arq:UnionGraph> { ?s <uri:p> ?o }", 5, dataset) ; 
    } 
    
    // Should be able to see two graphs in the union.
    @Test public void dynamic_union_2() {
        testCount("SELECT * FROM NAMED <graph:1> FROM NAMED <graph:2> FROM <graph:3>" + 
                  "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
            2, dataset);
    }

    @Test public void dynamic_union_3() {
        testCount("SELECT * FROM NAMED <urn:x-arq:UnionGraph> { GRAPH <urn:x-arq:UnionGraph> { } }", 1, dataset);
    }
    
    // The union graph isn't in the named set, even if placed there explicitly.
    @Test
    public void dynamic_union_4() {
        testCount("SELECT * FROM NAMED <urn:x-arq:UnionGraph> { GRAPH ?g { } }", 0, dataset);
    }

    @Test
    public void dynamic_union5() {
        testCount("SELECT * " + "FROM NAMED <urn:x-arq:UnionGraph> " + "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }", 0, dataset);
    }

    // GRAPH <union> is the union over the view dataset (FROM NAMED)
    @Test
    public void dynamic_union_6() {
        testCount("SELECT * " + "FROM NAMED <urn:x-arq:UnionGraph> " + "FROM NAMED <graph:4> "
                  + "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
            1, dataset);
    }

    // -- dft graph

    @Test
    public void dynamic_dft_1() {
        testCount("SELECT * FROM <urn:x-arq:DefaultGraph> { ?s <uri:p> 0 }", 1, dataset);
    }

    @Test
    public void dynamic_dft_2() {
        testCount("SELECT * FROM NAMED <urn:x-arq:DefaultGraph> { ?s <uri:p> 0 }", 0, dataset);
    }

    @Test
    public void dynamic_dft_3() {
        testCount("SELECT * FROM NAMED <urn:x-arq:DefaultGraph> { GRAPH ?g { } }",
            1, dataset);
    }

    // No FROM -> empty default.
    @Test
    public void dynamic_dft_4() {
        testCount("SELECT * FROM NAMED <urn:x-arq:DefaultGraph> { GRAPH ?g { ?s <uri:p> 0 } }",
            0, dataset);
    }

    // No FROM -> empty default.
    @Test
    public void dynamic_dft_5() {
        testCount("SELECT * FROM NAMED <urn:x-arq:DefaultGraph> "+
                  "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> 0 } }",
                  0, dataset);
    }

    @Test
    public void dynamic_dft_6() {
        testCount("SELECT * " + "FROM <graph:1> FROM <graph:2> " + "FROM NAMED <urn:x-arq:DefaultGraph> "+
                  "{ GRAPH ?g { ?s <uri:p> ?o .FILTER ( ?o IN ( 1, 2) ) } }",
                  2, dataset);
    }

    @Test
    public void dynamic_dft_7() {
        testCount("SELECT * " + "FROM <graph:1> FROM <graph:2> " + "FROM NAMED <urn:x-arq:DefaultGraph> "+
                  "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o . FILTER ( ?o IN ( 1, 2) ) } }",
                  2, dataset);
    }
    
    private static void testCount(String queryString, int expected, Dataset ds)
    {
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(expected, n) ;
    }
}
