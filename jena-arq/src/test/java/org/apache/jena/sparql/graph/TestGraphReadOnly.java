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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.sse.SSE;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphReadOnly {

    private static Graph baseGraph;
    private static Triple triple;

    @BeforeClass public static void beforeClass() {
        baseGraph = GraphFactory.createDefaultGraph();
        triple = SSE.parseTriple("(:s :p :o)");
        baseGraph.getPrefixMapping().setNsPrefix("ex", "http://example/");
    }

    @Test(expected=AddDeniedException.class)
    public void read_only_add() {
        Graph graph = new GraphReadOnly(baseGraph);
        graph.add(triple);
    }

    @Test(expected=DeleteDeniedException.class)
    public void read_only_delete() {
        Graph graph = new GraphReadOnly(baseGraph);
        graph.delete(triple);
    }

    @Test(expected=JenaException.class)
    public void read_only_remove() {
        Graph graph = new GraphReadOnly(baseGraph);
        graph.remove(null, null, null);
    }

    @Test(expected=JenaException.class)
    public void read_only_clear() {
        Graph graph = new GraphReadOnly(baseGraph);
        graph.clear();
    }

    @Test(expected=JenaException.class)
    public void read_only_prefixmapping_set() {
        Graph graph = new GraphReadOnly(baseGraph);
        // Does not matter that it is alread defined.
        graph.getPrefixMapping().setNsPrefix("ex", "http://example/");
    }

    @Test(expected=JenaException.class)
    public void read_only_prefixmapping_remove() {
        Graph graph = new GraphReadOnly(baseGraph);
        graph.getPrefixMapping().removeNsPrefix("empty");
    }

    @Test(expected=JenaException.class)
    public void read_only_prefixmapping_clear() {
        Graph graph = new GraphReadOnly(baseGraph);
        // Does not matter that it is alread defined.
        graph.getPrefixMapping().clearNsPrefixMap();
    }
}
