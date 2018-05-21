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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphSink;
import org.apache.jena.sparql.graph.GraphZero;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.Test;

/** Tests for
 * {@link DatasetGraphZero},
 * {@link DatasetGraphSink} and
 * {@link DatasetGraphOne}
 * and (via their use in datasets):
 * {@link GraphZero} and
 * {@link GraphSink}
*/
public class TestSpecials {
    
    private static Quad quad = SSE.parseQuad("(:g :s :p :o)");
    private static Triple triple = SSE.parseTriple("(:s :p :o)");
    private static Node gn = SSE.parseNode(":gn");
    
    // -- zero
    
    @Test public void zero_basic_1() {
        DatasetGraph dsg = DatasetGraphZero.create();
        assertFalse(dsg.find().hasNext());
        assertTrue(dsg.supportsTransactionAbort());
        assertEquals(0, dsg.getDefaultGraph().size());
        assertFalse(dsg.getDefaultGraph().getCapabilities().addAllowed());
    }
    
    @Test public void zero_basic_2() {
        DatasetGraph dsg = DatasetGraphZero.create();
        assertNull(dsg.getGraph(gn));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void zero_add_1() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.add(quad);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void zero_add_2() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.getDefaultGraph().add(triple);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void zero_add_3() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.addGraph(gn, GraphFactory.createGraphMem());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void zero_delete_1() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.delete(quad);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void zero_delete_2() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.getDefaultGraph().delete(triple);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void zero_delete_3() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.deleteAny(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    @Test public void zero_txn_1() {
        DatasetGraph dsg = DatasetGraphZero.create();
        Txn.executeRead(dsg, ()->{});
        Txn.executeWrite(dsg, ()->{});
    }

    @Test(expected=JenaTransactionException.class)
    public void zero_txn_2() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.begin(ReadWrite.READ);
        dsg.begin(ReadWrite.READ);
    }

    @Test(expected=JenaTransactionException.class)
    public void zero_txn_3() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.commit();
    }
    
    @Test(expected=JenaTransactionException.class)
    public void zero_txn_4() {
        DatasetGraph dsg = DatasetGraphZero.create();
        dsg.begin(ReadWrite.WRITE);
        dsg.commit();
        dsg.commit();
    }
    
    @Test public void zero_graph_txn_1() {
        DatasetGraph dsg = DatasetGraphZero.create();
        Graph g = dsg.getDefaultGraph();
        g.getTransactionHandler().execute(()->{});
    }

    @Test public void zero_graph_txn_2() {
        DatasetGraph dsg = DatasetGraphZero.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.begin();
        h.abort();
    }

    @Test(expected=JenaException.class)
    public void zero_graph_txn_3() {
        DatasetGraph dsg = DatasetGraphZero.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.begin();
        h.begin();
    }

    @Test(expected=JenaException.class)
    public void zero_graph_txn_4() {
        DatasetGraph dsg = DatasetGraphZero.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.begin();
        h.commit();
        h.abort();
    }

    @Test(expected=JenaException.class)
    public void zero_graph_txn_5() {
        DatasetGraph dsg = DatasetGraphZero.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.commit();
    }
    
    // -- sink
    
    @Test public void sink_basic_1() {
        DatasetGraph dsg = DatasetGraphSink.create();
        assertFalse(dsg.find().hasNext());
        assertTrue(dsg.supportsTransactionAbort());
        assertEquals(0, dsg.getDefaultGraph().size());
        assertTrue(dsg.getDefaultGraph().getCapabilities().addAllowed());
    }
    
    @Test public void sink_basic_2() {
        DatasetGraph dsg = DatasetGraphSink.create();
        assertNull(dsg.getGraph(gn));
    }
    
    @Test public void sink_add_1() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.add(quad);
    }

    @Test public void sink_add_2() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.getDefaultGraph().add(triple);
    }

    @Test
    public void sink_add_3() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.addGraph(gn, GraphFactory.createGraphMem());
    }

    @Test public void sink_delete_1() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.add(quad);
    }

    @Test public void sink_delete_2() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.getDefaultGraph().add(triple);
    }
    
    @Test
    public void sink_delete_3() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.deleteAny(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    @Test public void sink_txn_1() {
        DatasetGraph dsg = DatasetGraphSink.create();
        Txn.executeRead(dsg, ()->{});
        Txn.executeWrite(dsg, ()->{});
    }
    
    @Test(expected=JenaTransactionException.class)
    public void sink_txn_2() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.begin(ReadWrite.READ);
        dsg.begin(ReadWrite.READ);
    }

    @Test(expected=JenaTransactionException.class)
    public void sink_txn_3() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.commit();
    }
    
    @Test(expected=JenaTransactionException.class)
    public void sink_txn_4() {
        DatasetGraph dsg = DatasetGraphSink.create();
        dsg.begin(ReadWrite.WRITE);
        dsg.commit();
        dsg.commit();
    }
    
    @Test public void sink_graph_txn_1() {
        DatasetGraph dsg = DatasetGraphSink.create();
        Graph g = dsg.getDefaultGraph();
        g.getTransactionHandler().execute(()->{});
    }


    @Test public void sink_graph_txn_2() {
        DatasetGraph dsg = DatasetGraphSink.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.begin();
        h.abort();
    }

    @Test(expected=JenaException.class)
    public void sink_graph_txn_3() {
        DatasetGraph dsg = DatasetGraphSink.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.begin();
        h.begin();
    }

    @Test(expected=JenaException.class)
    public void sink_graph_txn_4() {
        DatasetGraph dsg = DatasetGraphSink.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.begin();
        h.commit();
        h.abort();
    }

    @Test(expected=JenaException.class)
    public void sink_graph_txn_5() {
        DatasetGraph dsg = DatasetGraphSink.create();
        Graph g = dsg.getDefaultGraph();
        TransactionHandler h = g.getTransactionHandler();
        h.commit();
    }
}
