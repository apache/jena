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

package org.apache.jena.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMemValue;
import org.apache.jena.mem2.GraphMem2Fast;
import org.apache.jena.sparql.sse.SSE;

public class GTest {

    @Test
    public void copy() {
        // Test graph which implements Copyable<>
        {
            var graphImplementingCopyable = new GraphMem2Fast();
            test(graphImplementingCopyable);
        }

        // Test graph which does not implement Copyable<>
        {
            @SuppressWarnings("deprecation")
            GraphMemValue notCopyableGraph = new GraphMemValue();
            assertFalse(notCopyableGraph instanceof Copyable<?>);
            test(notCopyableGraph);
        }
    }

    private void test(Graph graph) {
        graph.add(triple("(:s1 :p1 :o1)"));
        graph.add(triple("(:s1 :p2 :o1)"));
        graph.add(triple("(:s2 :p1 :o1)"));
        graph.add(triple("(:s2 :p1 :o2)"));
        graph.add(triple("(:s2 :p1 :o2)"));

        var copy = G.copy(graph);

        assertEquals(graph.size(), copy.size());

        copy.delete(triple("(:s1 :p1 :o1)"));
        assertEquals(graph.size() - 1, copy.size());

        copy.add(triple("(:s3 :p3 :o3)"));
        copy.add(triple("(:s4 :p4 :o4)"));
        assertEquals(graph.size() + 1, copy.size());
    }

    private Triple triple(String str) {
        return SSE.parseTriple(str);
    }
}
