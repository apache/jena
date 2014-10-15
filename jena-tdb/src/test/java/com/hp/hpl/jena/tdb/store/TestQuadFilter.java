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

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Tuple ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TestQuadFilter extends BaseTest
{
    private static String graphToHide = "http://example/g2" ;
    private static Dataset ds = setup() ;  
    

    @BeforeClass public static void beforeClass()
    {
        
    }
    
    @AfterClass public static void afterClass() {}
    
//    public static void main(String ... args)
//    {
//        // This also works for default union graph ....
//        TDB.getContext().setTrue(TDB.symUnionDefaultGraph) ;
//        
//        Dataset ds = setup() ;
//        Filter<Tuple<NodeId>> filter = createFilter(ds) ;
//        example(ds, filter) ;
//    }
    
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
    private static Filter<Tuple<NodeId>> createFilter(Dataset ds)
    {
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;
        final NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable() ;
        final NodeId target = nodeTable.getNodeIdForNode(NodeFactory.createURI(graphToHide)) ;
        Filter<Tuple<NodeId>> filter = new Filter<Tuple<NodeId>>() {
            @Override
            public boolean accept(Tuple<NodeId> item)
            {
                // Reverse the lookup as a demo
                //Node n = nodeTable.getNodeForNodeId(target) ;
                //System.err.println(item) ;
                if ( item.size() == 4 && item.get(0).equals(target) )
                    return false ;
                return true ;
            } } ;
        return filter ;
    }            

    @Test public void quad_filter_1()   { test("SELECT * { GRAPH ?g { ?s ?p ?o } }", 1, 2) ; }
    @Test public void quad_filter_2()   { test("SELECT * { ?s ?p ?o }", 1, 2) ; }
    @Test public void quad_filter_3()   { test("SELECT * { GRAPH ?g { } }", 1, 2) ; }
    
    private void test(String qs, int withFilter, int withoutFilter)
    {
        Filter<Tuple<NodeId>> filter = createFilter(ds) ;
        
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
