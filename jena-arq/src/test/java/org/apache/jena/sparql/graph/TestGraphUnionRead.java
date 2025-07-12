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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.Item;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.builders.BuilderGraph;

public class TestGraphUnionRead
{
    private static String dataStr = StrUtils.strjoinNL(
      "(dataset" ,
      "  (graph" ,
      "   (triple <http://example/s> <http://example/p> 'dft')" ,
      "   (triple <http://example/s> <http://example/p> <http://example/o>)" ,
      " )" ,
      " (graph <http://example/g1>",
      "   (triple <http://example/s> <http://example/p> 'g1')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " )",
      " (graph <http://example/g2>",
      "   (triple <http://example/s> <http://example/p> 'g2')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " )",
      " (graph <http://example/g3>",
      "   (triple <http://example/s> <http://example/p> 'g3')",
      "   (triple <http://example/s> <http://example/p> <http://example/o>)",
      " ))");
    private static DatasetGraph dsg = null;
    static {
        Item item = SSE.parse(dataStr);
        dsg = BuilderGraph.buildDataset(item);
    }
    private static Node gn1 = SSE.parseNode("<http://example/g1>");
    private static Node gn2 = SSE.parseNode("<http://example/g2>");
    private static Node gn3 = SSE.parseNode("<http://example/g3>");
    private static Node gn9 = SSE.parseNode("<http://example/g9>");

    @Test
    public void gr_union_01() {
        List<Node> gnodes = list(gn1, gn2);
        Graph g = new GraphUnionRead(dsg, gnodes);
        long x = Iter.count(g.find(null, null, null));
        assertEquals(3, x);
    }

    @Test
    public void gr_union_02() {
        List<Node> gnodes = list(gn1, gn2);
        Graph g = new GraphUnionRead(dsg, gnodes);
        Node s = NodeFactory.createURI("http://example/s");
        long x = Iter.count(g.find(s, null, null));
        assertEquals(3, x);
    }

    @Test
    public void gr_union_03() {
        List<Node> gnodes = list(gn1, gn2, gn9);
        Graph g = new GraphUnionRead(dsg, gnodes);
        Node o = NodeFactory.createLiteralString("g2");
        long x = Iter.count(g.find(null, null, o));
        assertEquals(1, x);
    }

    @Test
    public void gr_union_04() {
        List<Node> gnodes = list(gn9);
        Graph g = new GraphUnionRead(dsg, gnodes);
        long x = Iter.count(g.find(null, null, null));
        assertEquals(0, x);
    }

    @Test
    public void gr_union_05() {
        List<Node> gnodes = list();
        Graph g = new GraphUnionRead(dsg, gnodes);
        long x = Iter.count(g.find(null, null, null));
        assertEquals(0, x);
    }

    @Test
    public void gr_union_06() {
        List<Node> gnodes = list(gn1, gn1);
        Graph g = new GraphUnionRead(dsg, gnodes);
        long x = Iter.count(g.find(null, null, null));
        assertEquals(2, x);
    }

    @Test
    public void gr_union_of_one_1() {
        List<Node> gnodes = list(gn2);
        Graph g = new GraphUnionRead(dsg, gnodes);
        long x1 = Iter.count(g.find(null, null, null));
        assertEquals(2, x1);
        Node o = NodeFactory.createLiteralString("g2");
        long x2 = Iter.count(g.find(null, null, o));
        assertEquals(1, x2);
    }

    @Test
    public void gr_union_prefixes() {
        Graph graph = setupPrefixGraph("ex", "http://example/");

        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        dsg.addGraph(gn1, graph);

        GraphUnionRead gUnionRead = new GraphUnionRead(dsg, List.of(gn1));
        assertNotNull(gUnionRead.getPrefixMapping().getNsPrefixURI("ex"));
    }

    @Test
    public void gr_union_prefixes_bad_PrefixMapping() {
        // Not valid.
        Graph graph = setupPrefixGraph("-", "http://example/");

        // Test : Must be createGeneral
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        dsg.addGraph(gn1, graph);

        // Builds a PrefixMapping when created.
        GraphUnionRead gUnionRead = new GraphUnionRead(dsg, List.of(gn1));
        // .. but skips the unacceptable prefix.
        assertTrue(gUnionRead.getPrefixMapping().hasNoMappings());
    }

    private static Graph setupPrefixGraph(String prefix, String uriStr) {
        // Must be createTxnMem
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.prefixes().add(prefix, uriStr);
        Graph graph = dsg.getDefaultGraph();
        // Check it has the prefix.
        assertFalse(graph.getPrefixMapping().hasNoMappings());
        assertTrue(graph.getPrefixMapping().getNsPrefixURI(prefix) != null);
        assertTrue(graph.getPrefixMapping().getNsPrefixMap().containsKey(prefix));
        return graph;
    }

    static List<Node> list(Node...x) {
        return Arrays.asList(x);
    }
}
