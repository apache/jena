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

package org.apache.jena.tdb2.store;

import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.util.NodeFactoryExtra ;
import org.apache.jena.tdb2.junit.TL;
import org.apache.jena.tdb2.store.GraphViewSwitchable;
import org.junit.Test ;

/** Testing persistence  */ 
public class TestDatasetTDBPersist
{
    static Node n0 = NodeFactoryExtra.parseNode("<http://example/n0>") ; 
    static Node n1 = NodeFactoryExtra.parseNode("<http://example/n1>") ;
    static Node n2 = NodeFactoryExtra.parseNode("<http://example/n2>") ;
    
    @Test
    public void dataset1() {
        TL.exec((ds) -> {
            assertTrue(ds.getDefaultModel().getGraph() instanceof GraphViewSwitchable) ;
            assertTrue(ds.getNamedModel("http://example/").getGraph() instanceof GraphViewSwitchable) ;
        }) ;
    }
    
    @Test
    public void dataset2() {
        TL.exec((ds) -> {
            Graph g1 = ds.getDefaultModel().getGraph() ;
            Graph g2 = ds.getNamedModel("http://example/").getGraph() ;

            g1.add(new Triple(n0, n1, n2)) ;
            assertTrue(g1.contains(n0, n1, n2)) ;
            assertFalse(g2.contains(n0, n1, n2)) ;
        }) ;
    }

//    @Test
//    public void dataset3() {
//        TL.exec((ds) -> {
//            Graph g1 = ds.getDefaultModel().getGraph() ;
//            // Sometimes, under windows, deleting the files by
//            // clearDirectory does not work.
//            // Needed for safe tests on windows.
//            g1.clear() ;
//
//            Graph g2 = ds.getNamedModel("http://example/").getGraph() ;
//            g2.add(new Triple(n0, n1, n2)) ;
//            assertTrue(g2.contains(n0, n1, n2)) ;
//            assertFalse(g1.contains(n0, n1, n2)) ;
//        }) ;
//    }
//
//    @Test
//    public void dataset4() {
//        String graphName = "http://example/" ;
//        Triple triple = SSE.parseTriple("(<x> <y> <z>)") ;
//        Node gn = org.apache.jena.graph.NodeFactory.createURI(graphName) ;
//
//        TL.exec((ds) -> {
//            // ?? See TupleLib.
//            ds.asDatasetGraph().deleteAny(gn, null, null, null) ;
//
//            Graph g2 = ds.asDatasetGraph().getGraph(gn) ;
//
//            // Graphs only exists if they have a triple in them
//            assertFalse(ds.containsNamedModel(graphName)) ;
//
//            List<String> names = Iter.toList(ds.listNames()) ;
//            assertEquals(0, names.size()) ;
//            assertEquals(0, ds.asDatasetGraph().size()) ;
//        }) ;
//    }
//
//    @Test
//    public void dataset5() {
//        String graphName = "http://example/" ;
//        Triple triple = SSE.parseTriple("(<x> <y> <z>)") ;
//        TL.exec((ds) -> {
//            Graph g2 = ds.asDatasetGraph().getGraph(org.apache.jena.graph.NodeFactory.createURI(graphName)) ;
//            // Graphs only exists if they have a triple in them
//            g2.add(triple) ;
//
//            assertTrue(ds.containsNamedModel(graphName)) ;
//            List<String> x = Iter.toList(ds.listNames()) ;
//            List<String> y = Arrays.asList(graphName) ;
//            assertEquals(x, y) ;
//
//            assertEquals(1, ds.asDatasetGraph().size()) ;
//        }) ;
//    }
}
