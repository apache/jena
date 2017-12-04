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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;
import static org.junit.Assert.*; 

public class TestGraphView {

    @Test public void graphDSG_view_txn_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Graph graph = dsg.getDefaultGraph();
        assertTrue(graph instanceof GraphView);
        assertTrue(graph.getTransactionHandler().transactionsSupported());
    }
    
    @Test public void graphDSG_view_txn_2() {
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        // NOT dsg.getDefaultGraph()
        Graph graph = GraphView.createDefaultGraph(dsg);
        assertTrue( graph instanceof GraphView );
        assertFalse(graph.getTransactionHandler().transactionsSupported());
    }
    
    @Test public void graphDSG_view_txn_3() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Graph graph = dsg.getDefaultGraph();
        Triple triple = SSE.parseTriple("(<s> <p> 0)") ;
        assertFalse(graph.contains(triple));
        graph.getTransactionHandler().execute(()->graph.add(triple) );
        graph.getTransactionHandler().execute(()->assertTrue(graph.contains(triple)) );
    }
}
