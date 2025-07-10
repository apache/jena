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

package org.apache.jena.sparql.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.util.iterator.ExtendedIterator;

public class TestGraphTxn {

    private static Node s = SSE.parseNode(":s");
    private static Node p = SSE.parseNode(":p");
    private static Node o1 = SSE.parseNode(":o1");
    private static Node o2 = SSE.parseNode(":o2");
    private static Triple triple1 = Triple.create(s, p, o1);
    private static Triple triple2 = Triple.create(s, p, o2);

    @Test public void graphTxn_add_find_add_01() {
        // jena-core graph transaction
        Graph graph = GraphFactory.createTxnGraph();
        graph.getTransactionHandler().execute(()->graph.add(triple1));
        assertEquals(1,  graph.size());
        ExtendedIterator<Triple> eIter = graph.find();
        assertTrue(eIter.hasNext());
        eIter.next();
        // After .next, Not yet closed
        eIter.hasNext();
        // Now closed.
        // The implementation, DatasetGraphInMemory doesn't require transactions.
        graph.add(triple2);
    }

    @Test public void graphTxn_add_find_add_02() {
        // jena-core graph transaction
        Graph graph = GraphFactory.createTxnGraph();
        graph.getTransactionHandler().execute(()->graph.add(triple1));
        assertEquals(1,  graph.size());
        ExtendedIterator<Triple> eIter = graph.find();
        try {
            assertTrue(eIter.hasNext());
            eIter.next();
        } finally { eIter.close(); }
        // Now closed.
        // The implementation, DatasetGraphInMemory doesn't require transactions.
        graph.add(triple2);
    }

    @Test public void graphSteram01() {
        GraphTxn graph = GraphFactory.createTxnGraph();
        graph.add(triple2);
        graph.stream().count();
        graph.add(triple1);
    }

    // GH-2197
    @Test public void graphForEachClose01() {
        GraphTxn graph = GraphFactory.createTxnGraph();
        graph.find().toList();
        graph.getTransactionHandler().execute(()->graph.add(triple1));
    }

    // GH-2197
    @Test public void graphForEachClose02() {
        GraphTxn graph = GraphFactory.createTxnGraph();
        graph.find().toList();
        graph.executeWrite(()->graph.add(triple2));
    }

    @Test public void graphTxn01() {
        GraphTxn graph = GraphFactory.createTxnGraph();
        graph.executeWrite(()->graph.add(triple2));
        Triple t = graph.calculate(()->graph.find(s, null, null).next());
        assertEquals(triple2, t);
    }

    @Test public void graphTxn02() {
        GraphTxn graph = GraphFactory.createTxnGraph();
        try {
            graph.calculate(() -> graph.find(s, null, null).next());
        } catch (NoSuchElementException ex) {}
        graph.add(triple2);
    }
}
