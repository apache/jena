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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.sse.SSE;

public class TestGraphReadOnly {

    private static Graph baseGraph;
    private static Triple triple;

    @BeforeAll public static void beforeClass() {
        baseGraph = GraphFactory.createDefaultGraph();
        triple = SSE.parseTriple("(:s :p :o)");
        baseGraph.getPrefixMapping().setNsPrefix("ex", "http://example/");
    }

    @Test
    public void read_only_add() {
        Graph graph = new GraphReadOnly(baseGraph);
        assertThrows(AddDeniedException.class,()-> graph.add(triple) );
    }

    @Test
    public void read_only_delete() {
        Graph graph = new GraphReadOnly(baseGraph);
        assertThrows(DeleteDeniedException.class,()-> graph.delete(triple) );
    }

    @Test
    public void read_only_remove() {
        Graph graph = new GraphReadOnly(baseGraph);
        assertThrows(JenaException.class,()-> graph.remove(null, null, null) );
    }

    @Test
    public void read_only_clear() {
        Graph graph = new GraphReadOnly(baseGraph);
        assertThrows(JenaException.class,()-> graph.clear() );
    }

    @Test
    public void read_only_prefixmapping_set() {
        Graph graph = new GraphReadOnly(baseGraph);
        // Does not matter that it is alread defined.
        assertThrows(JenaException.class,()-> graph.getPrefixMapping().setNsPrefix("ex", "http://example/") );
    }

    @Test
    public void read_only_prefixmapping_remove() {
        Graph graph = new GraphReadOnly(baseGraph);
        assertThrows(JenaException.class,()-> graph.getPrefixMapping().removeNsPrefix("empty") );
    }

    @Test
    public void read_only_prefixmapping_clear() {
        Graph graph = new GraphReadOnly(baseGraph);
        // Does not matter that it is alread defined.
        assertThrows(JenaException.class,()-> graph.getPrefixMapping().clearNsPrefixMap() );
    }
}
