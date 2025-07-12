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

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.mem2.GraphMem2Fast;

@SuppressWarnings("deprecation")
public class GTest {

    @Test
    public void copy() {
        // Test graph which implements Copyable<>
        {
            var graphImplementingCopyable = new GraphMem2Fast();

            graphImplementingCopyable.add(triple("s1 p1 o1"));
            graphImplementingCopyable.add(triple("s1 p2 o1"));
            graphImplementingCopyable.add(triple("s2 p1 o1"));
            graphImplementingCopyable.add(triple("s2 p1 o2"));
            graphImplementingCopyable.add(triple("s2 p1 o2"));

            var copy = G.copy(graphImplementingCopyable);

            assertEquals(graphImplementingCopyable.size(), copy.size());

            copy.delete(triple("s1 p1 o1"));
            assertEquals(graphImplementingCopyable.size() - 1, copy.size());

            copy.add(triple("s3 p3 o3"));
            copy.add(triple("s4 p4 o4"));
            assertEquals(graphImplementingCopyable.size() + 1, copy.size());
        }

        // Test graph which does not implement Copyable<>
        {
            GraphMem notCopyableGraph = new GraphMem();

            assertFalse(notCopyableGraph instanceof Copyable<?>);

            notCopyableGraph.add(triple("s1 p1 o1"));
            notCopyableGraph.add(triple("s1 p2 o1"));
            notCopyableGraph.add(triple("s2 p1 o1"));
            notCopyableGraph.add(triple("s2 p1 o2"));
            notCopyableGraph.add(triple("s2 p1 o2"));

            var copy = G.copy(notCopyableGraph);

            assertEquals(notCopyableGraph.size(), copy.size());

            copy.delete(triple("s1 p1 o1"));
            assertEquals(notCopyableGraph.size() - 1, copy.size());

            copy.add(triple("s3 p3 o3"));
            copy.add(triple("s4 p4 o4"));
            assertEquals(notCopyableGraph.size() + 1, copy.size());
        }
    }
}