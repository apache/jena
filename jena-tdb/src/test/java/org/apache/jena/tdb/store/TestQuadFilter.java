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

package org.apache.jena.tdb.store;

import java.util.function.Predicate;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestQuadFilter extends BaseTest
{
    private static String graphToHide = "http://example/g2" ;
    private static Dataset ds = setup() ;  
    

    @BeforeClass public static void beforeClass()
    {
        
    }
    
    @AfterClass public static void afterClass() {}
    
    /** Example setup - in-memory dataset with two graphs, one triple in each */
    private static Dataset setup()
    {
        Dataset ds = TDBFactory.createDataset() ;
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;
        Quad q1 = SSE.parseQuad("(<http://example/g1> <http://example/s> <http://example/p> <http://example/o1>)") ;
        Quad q2 = SSE.parseQuad("(<http://example/g2> <http://example/s> <http://example/p> <http://example/o2>)") ;
        dsg.add(q1) ;
        dsg.add(q2) ;
        return ds ;
    }
    
    /** Create a filter to exclude the graph http://example/g2 */
    private static Predicate<Tuple<NodeId>> createFilter(Dataset ds)
    {
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;
        final NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable() ;
        final NodeId target = nodeTable.getNodeIdForNode(NodeFactory.createURI(graphToHide)) ;
        return item -> !( item.len() == 4 && item.get(0).equals(target) );
    }            

    @Test public void quad_filter_1()   { test("SELECT * { GRAPH ?g { ?s ?p ?o } }", 1, 2) ; }
    @Test public void quad_filter_2()   { test("SELECT * { ?s ?p ?o }", 1, 2) ; }
    @Test public void quad_filter_3()   { test("SELECT * { GRAPH ?g { } }", 1, 2) ; }
    
    private void test(String qs, int withFilter, int withoutFilter)
    {
        Predicate<Tuple<NodeId>> filter = createFilter(ds) ;
        
//    private static void example(Dataset ds, Filter<Tuple<NodeId>> filter)
//    {
//        String[] x = {
//            "SELECT * { GRAPH ?g { ?s ?p ?o } }",
//            "SELECT * { ?s ?p ?o }",
//            // THis filter does not hide the graph itself, just the quads associated with the graph.
//            "SELECT * { GRAPH ?g {} }"
//            } ;
        Query query = QueryFactory.create(qs) ;
        
        try(QueryExecution qExec = QueryExecutionFactory.create(query, ds)) {
            // Install filter for this query only.
            qExec.getContext().set(SystemTDB.symTupleFilter, filter) ;
            qExec.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            long x1 = ResultSetFormatter.consume(qExec.execSelect()) ;
            assertEquals(withFilter, x1) ;
        }
        // No filter.
        try(QueryExecution qExec = QueryExecutionFactory.create(query, ds)) {
            qExec.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            long x2 = ResultSetFormatter.consume(qExec.execSelect()) ;
            assertEquals(withoutFilter, x2) ;
        }

    }
        
    
}
