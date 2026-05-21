/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */
package org.apache.jena.mem.pattern;

import org.apache.jena.graph.Node;
import org.junit.Test;

import static org.apache.jena.junit.GraphHelper.node;
import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link PatternClassifier}: maps a triple match into one of
 * the eight {@link MatchPattern} buckets based on which of the subject,
 * predicate and object slots are concrete and which are wildcards.
 * <p>
 * Both classification overloads are tested for every combination of
 * concrete/wildcard slots, and the {@code (Node, Node, Node)} overload is
 * additionally tested with explicit {@code null} arguments and
 * {@link Node#ANY}, both of which must be treated as wildcards.
 */
public class PatternClassifierTest {

    @Test
    public void classifyTripleCoversAllEightCombinations() {
        // The wildcard "??" is parsed as a non-concrete (variable) node by
        // the test helper.
        assertEquals(MatchPattern.SUB_PRE_OBJ, PatternClassifier.classify(triple("s p o")));
        assertEquals(MatchPattern.SUB_PRE_ANY, PatternClassifier.classify(triple("s p ??")));
        assertEquals(MatchPattern.SUB_ANY_OBJ, PatternClassifier.classify(triple("s ?? o")));
        assertEquals(MatchPattern.SUB_ANY_ANY, PatternClassifier.classify(triple("s ?? ??")));
        assertEquals(MatchPattern.ANY_PRE_OBJ, PatternClassifier.classify(triple("?? p o")));
        assertEquals(MatchPattern.ANY_PRE_ANY, PatternClassifier.classify(triple("?? p ??")));
        assertEquals(MatchPattern.ANY_ANY_OBJ, PatternClassifier.classify(triple("?? ?? o")));
        assertEquals(MatchPattern.ANY_ANY_ANY, PatternClassifier.classify(triple("?? ?? ??")));
    }

    @Test
    public void classifyNodesCoversAllEightCombinations() {
        assertEquals(MatchPattern.SUB_PRE_OBJ, PatternClassifier.classify(node("s"), node("p"), node("o")));
        assertEquals(MatchPattern.SUB_PRE_ANY, PatternClassifier.classify(node("s"), node("p"), node("??")));
        assertEquals(MatchPattern.SUB_ANY_OBJ, PatternClassifier.classify(node("s"), node("??"), node("o")));
        assertEquals(MatchPattern.SUB_ANY_ANY, PatternClassifier.classify(node("s"), node("??"), node("??")));
        assertEquals(MatchPattern.ANY_PRE_OBJ, PatternClassifier.classify(node("??"), node("p"), node("o")));
        assertEquals(MatchPattern.ANY_PRE_ANY, PatternClassifier.classify(node("??"), node("p"), node("??")));
        assertEquals(MatchPattern.ANY_ANY_OBJ, PatternClassifier.classify(node("??"), node("??"), node("o")));
        assertEquals(MatchPattern.ANY_ANY_ANY, PatternClassifier.classify(node("??"), node("??"), node("??")));
    }

    @Test
    public void classifyNodesTreatsNullAsWildcard() {
        // The graph-find contract allows callers to pass null for a slot
        // they don't care about; the classifier must handle that without NPE.
        assertEquals(MatchPattern.SUB_PRE_OBJ, PatternClassifier.classify(node("s"), node("p"), node("o")));
        assertEquals(MatchPattern.SUB_PRE_ANY, PatternClassifier.classify(node("s"), node("p"), null));
        assertEquals(MatchPattern.SUB_ANY_OBJ, PatternClassifier.classify(node("s"), null, node("o")));
        assertEquals(MatchPattern.SUB_ANY_ANY, PatternClassifier.classify(node("s"), null, null));
        assertEquals(MatchPattern.ANY_PRE_OBJ, PatternClassifier.classify(null, node("p"), node("o")));
        assertEquals(MatchPattern.ANY_PRE_ANY, PatternClassifier.classify(null, node("p"), null));
        assertEquals(MatchPattern.ANY_ANY_OBJ, PatternClassifier.classify(null, null, node("o")));
        assertEquals(MatchPattern.ANY_ANY_ANY, PatternClassifier.classify(null, null, null));
    }

    @Test
    public void classifyNodesTreatsNodeAnyAsWildcard() {
        // Node.ANY is the standard wildcard sentinel used by Graph.find.
        assertEquals(MatchPattern.SUB_PRE_ANY, PatternClassifier.classify(node("s"), node("p"), Node.ANY));
        assertEquals(MatchPattern.SUB_ANY_OBJ, PatternClassifier.classify(node("s"), Node.ANY, node("o")));
        assertEquals(MatchPattern.ANY_PRE_OBJ, PatternClassifier.classify(Node.ANY, node("p"), node("o")));
        assertEquals(MatchPattern.ANY_ANY_ANY, PatternClassifier.classify(Node.ANY, Node.ANY, Node.ANY));
    }
}