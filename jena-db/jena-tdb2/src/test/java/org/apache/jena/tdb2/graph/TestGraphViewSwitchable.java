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

package org.apache.jena.tdb2.graph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.DatabaseMgr;
import org.junit.Assert;
import org.junit.Test;

public class TestGraphViewSwitchable {
    private DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
    
    
    @Test public void graph_txn_1() {
        Graph g = dsg.getDefaultGraph();
        Triple t = SSE.parseTriple("(:S :P :O)");
        
        g.getTransactionHandler().execute(()->g.add(t));
        
        g.getTransactionHandler().execute(()->
            Assert.assertTrue(g.contains(t))
            );
    }
    
    @Test public void graph_txn_2() {
        Graph g = dsg.getDefaultGraph();
        Triple t = SSE.parseTriple("(:S :P :O)");
        
        g.getTransactionHandler().execute(()->g.add(t));
        
        Graph g2 = dsg.getDefaultGraph();
        g2.getTransactionHandler().execute(()->
            Assert.assertTrue(g.contains(t))
            );
    }    

    @Test public void graph_txn_3() {
        Node gn = SSE.parseNode(":gn");
        Graph g = dsg.getGraph(gn);
        Triple t = SSE.parseTriple("(:S :P :O)");
        Quad q = Quad.create(gn, t);
        
        g.getTransactionHandler().execute(()->g.add(t));
        
        g.getTransactionHandler().execute(()->{
            Assert.assertTrue(g.contains(t));
            dsg.contains(q);
        });
    }
}
