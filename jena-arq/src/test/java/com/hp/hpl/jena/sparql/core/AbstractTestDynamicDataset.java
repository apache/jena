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

package com.hp.hpl.jena.sparql.core;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public abstract class AbstractTestDynamicDataset extends BaseTest
{
    protected abstract Dataset createDataset() ;
    Dataset  dataset ;
    
    @After public void after() {}

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
//    
//
//    // If  context.isTrue(TDB.symUnionDefaultGraph)
//    
//    protected abstract void startDynamicAndUnionTest() ; // TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
//    protected abstract void finishDynamicAndUnionTest() ; // TDB.getContext().unset(TDB.symUnionDefaultGraph) ;
//    
//    @Test public void dynamicAndUnion1() {
//        try { 
//            startDynamicAndUnionTest() ;
//        testCount("SELECT * "+
//                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                  "{ GRAPH ?g { ?s <uri:q> ?o }}",
//                  2, dataset) ; 
//        } finally { finishDynamicAndUnionTest() ; }
//    }    
//
//    @Test public void dynamicAndUnion2() {
//        try { startDynamicAndUnionTest() ;
//        testCount("SELECT * "+
//                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                  "{ ?s <uri:q> ?o }",    // Same in each graph
//                  1, dataset) ; 
//        } finally { finishDynamicAndUnionTest() ; } 
//    }    
//
//    @Test public void dynamicAndUnion3() {
//        try { startDynamicAndUnionTest() ;
//        testCount("SELECT * "+
//                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                  "{ ?s <uri:p> ?o }",    // Different in each graph
//                  2, dataset) ; 
//        } finally { finishDynamicAndUnionTest() ; } 
//    }    
//
//    @Test public void dynamicAndUnion4() {
//        try { startDynamicAndUnionTest() ;
//        testCount("SELECT * "+
//                  "FROM <graph:1> FROM <graph:2>"+
//                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                  "{ ?s <uri:p> ?o }",
//                  2, dataset) ;         // Only see <graph:1> and <graph:2> as default graph.
//        } finally { finishDynamicAndUnionTest() ; } 
//    }  
//
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion5() {
//        testCount("SELECT * "+
//                  "FROM <graph:1>"+
//                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                  "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o } }",    // Different in each graph
//                  1, dataset) ;
//    }  
//    
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion6() {
//        try {
//            startDynamicAndUnionTest() ;
//            testCount("SELECT * "+
//                      "FROM <graph:1>"+
//                      "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                      "{ GRAPH <urn:x-arq:DefaultGraph> { ?s <uri:p> ?o } }",
//                      1, dataset) ;
//        } finally { finishDynamicAndUnionTest() ; } 
//    }  
//    
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion7() {
//        testCount("SELECT * "+
//                  "FROM <graph:1>"+
//                  "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                  "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
//                  2, dataset) ;
//    }  
//    
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion8() {
//        try {
//            startDynamicAndUnionTest() ;
//            testCount("SELECT * "+
//                      "FROM <graph:1>"+
//                      "FROM NAMED <graph:3> FROM NAMED <graph:4> "+
//                      "{ GRAPH <urn:x-arq:UnionGraph> { ?s <uri:p> ?o } }",
//                      2, dataset) ;
//        } finally { finishDynamicAndUnionTest() ; } 
//    }  
//
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion10() {
//            testCount("SELECT * "+
//                      "FROM <urn:x-arq:DefaultGraph>" +
//                      "{ ?s ?p ?o }",
//                      3, dataset) ;
//    }  
//
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion10a() {
//            testCount("SELECT * "+
//                      "FROM <urn:x-arq:DefaultGraph>" +
//                      "{ GRAPH ?g { ?s ?p ?o } }",
//                      0, dataset) ;
//    }  
//
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion11() {
//            testCount("SELECT * "+
//                      "FROM <urn:x-arq:UnionGraph>" +
//                      "{ ?s ?p ?o }",
//                      6, dataset) ;
//    } 
//    
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion11a() {
//            testCount("SELECT * "+
//                      "FROM <urn:x-arq:UnionGraph>" +
//                      "{ GRAPH ?g { ?s ?p ?o } }",
//                      0, dataset) ;
//    }  
//
//
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion12() {
//            testCount("SELECT * "+
//                      "FROM <urn:x-arq:DefaultGraph>" +
//                      "FROM <urn:x-arq:UnionGraph>" +
//                      "{ ?s ?p ?o }",
//                      7, dataset) ;
//    }  
//
//    //@Ignore("Test of dynamic datasets with named default or union graph")
//    @Test public void dynamicAndUnion12a() {
//        testCount("SELECT * "+
//                  "FROM <urn:x-arq:DefaultGraph>" +
//                  "FROM <urn:x-arq:UnionGraph>" +
//                  "{ GRAPH ?g { ?s ?p ?o } }",
//                  0, dataset) ;
//    }
//    @Test public void dynamic99() {
//        // Check we did not mess with the global context in getting previous tests to pass.
//        testCount("SELECT * FROM NAMED <graph:3> { ?s ?p ?o }", 0, dataset) ;
//    }
//    
//    // Tests of patterns and paths across graphs.
//    
//    private static String dataStr = StrUtils.strjoinNL(
//       "(dataset" ,
//       "  (graph" ,
//       "   (triple <http://example/s> <http://example/p> 'dft')" ,
//       "   (triple <http://example/s> <http://example/p> <http://example/x>)" ,
//       "   (triple <http://example/x> <http://example/p> <http://example/o>)" ,
//       " )" ,
//       " (graph <http://example/g1>",
//       "   (triple <http://example/s> <http://example/p> 'g1')",
//       "   (triple <http://example/s> <http://example/p1> <http://example/x>)",
//       "   (triple <http://example/x> <http://example/p2> <http://example/o>)",
//       " )",
//       " (graph <http://example/g2>", 
//       "   (triple <http://example/s> <http://example/p> 'g2')",
//       "   (triple <http://example/x> <http://example/p1> <http://example/z>)",
//       "   (triple <http://example/x> <http://example/p2> <http://example/o>)",
//       "   (triple <http://example/x> <http://example/p2> <http://example/o2>)",
//       " )",
//       " (graph <http://example/g3>",
//       "   (triple <http://example/s> <http://example/p> 'g3')",
//       "   (triple <http://example/s> <http://example/p1> <http://example/y>)",
//       " ))") ;
//    
//    private static Dataset dataset2 = TDBFactory.createDataset() ; 
//    static {
//        Item item = SSE.parse(dataStr) ;
//        DatasetGraph dsg = BuilderGraph.buildDataset(item) ;
//        
//        Iterator<Quad> iter = dsg.find() ;
//        for ( ; iter.hasNext(); )
//            dataset2.asDatasetGraph().add(iter.next()) ;    
//    }
//    private static Node gn1 = SSE.parseNode("<http://example/g1>") ;
//    private static Node gn2 = SSE.parseNode("<http://example/g2>") ;
//    private static Node gn3 = SSE.parseNode("<http://example/g3>") ;
//    private static Node gn9 = SSE.parseNode("<http://example/g9>") ;
//
//    private static final String prefix = "PREFIX : <http://example/> " ; 
//    
//    // g1+g2 { ?s :p1 ?x . ?x :p2 ?o } ==> 1
//    // g1+g2 { ?s :p1* ?o } ==> 1
//    
//    @Test public void pattern_01()
//    {
//        testCount(prefix + "SELECT * FROM :g1 FROM :g2 { ?s :p1 ?x . ?x :p2 ?o }", 2, dataset2) ; 
//    }
//    
//    @Test public void pattern_02()
//    {
//        String qs = prefix + "SELECT * FROM :g1 FROM :g2 { ?s :p1+ ?x }" ;
////        Query query = QueryFactory.create(qs) ;
////        Dataset ds = DatasetFactory.create(DynamicDatasets.dynamicDataset(query, dataset2.asDatasetGraph())) ;
////        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
////        ResultSetFormatter.out(qExec.execSelect()) ;
//        testCount(qs, 3, dataset2) ; 
//    }
//    
//    @Test public void pattern_03()
//    {
//        // Do it externally to the TDB query engine.
//        String qs = prefix + "SELECT * FROM :g1 FROM :g2 { ?s :p1+ ?x }" ;
//        Query query = QueryFactory.create(qs) ;
//        DatasetDescription dsDesc = DatasetDescription.create(query) ;
//        Dataset ds = DynamicDatasets.dynamicDataset(dsDesc, dataset2, false) ;
//        testCount(qs, 3, ds) ; 
//    }

    
    private static void testCount(String queryString, int expected, Dataset ds)
    {
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(expected, n) ;
    }
}
