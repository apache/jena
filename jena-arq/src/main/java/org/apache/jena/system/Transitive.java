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

import java.util.*;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

public class Transitive {
    /**
     * Calculate the transitive closure of a property.
     *
     * Returns a map of node to all reachable nodes.
     */
    public static Map<Node, Collection<Node>> transitive(Graph graph, Node property) {
        Map<Node, Collection<Node>> reachable = new HashMap<>();
        ExtendedIterator<Triple> props = G.find(graph, null, property, null);
        // Old school loop.
        try {
            for (; props.hasNext(); ) {
                Triple triple = props.next();
                Node node = triple.getSubject();
                if ( ! reachable.containsKey(node) ) {
                    // Does not take advantage of intermediate results.
                    // Given cycles, that isn't so easy as it would be if it were a tree.
                    Collection<Node> subs = new HashSet<>();
                    transitiveExc(graph, true, node, property, subs);
                    reachable.put(node, subs);
                }
            }
        } finally { props.close(); }
        return reachable;
    }

    /**
     * Transitive closure of a property from a start node, and including the start node.
     */
    public static void transitiveInc(Graph graph, boolean forward, Node node, Node predicate, Collection<Node> output) {
        Set<Node> visited = new HashSet<>();
        recurse(graph, forward, 0, -1, node, predicate, visited, output);
    }

    /**
     * Transitive closure of a property from a start node, excluding the start node unless reachable via a cycle.
     */
    public static void transitiveExc(Graph graph, boolean forward, Node node, Node predicate, Collection<Node> output) {
        ExtendedIterator<Node> iter = singleStep(graph, forward, node, predicate);
        try {
            Set<Node> visited = new HashSet<>();
            for (; iter.hasNext();) {
                Node n1 = iter.next();
                recurse(graph, forward, 1, -1, n1, predicate, visited, output);
            }
        } finally { iter.close(); }
    }

    private static void recurse(Graph graph, boolean forward, int stepCount, int maxStepCount, Node node, Node predicate, Set<Node> visited, Collection<Node> output) {
        if ( maxStepCount >= 0 && stepCount > maxStepCount )
            return;
        if ( !visited.add(node) )
            return;
        output.add(node);
        ExtendedIterator<Node> iter1 = singleStep(graph, forward, node, predicate);
        try {
            // For each step, add to results and recurse.
            for (; iter1.hasNext();) {
                Node n1 = iter1.next();
                recurse(graph, forward, stepCount + 1, maxStepCount, n1, predicate, visited, output);
            }
        } finally { iter1.close(); }
    }

    // A single step of a transitive properties.
    // SP? or ?PO do not generate duplicates
    private static ExtendedIterator<Node> singleStep(Graph graph, boolean forward, Node node, Node property) {
        if ( forward )
            return G.iterSP(graph, node, property);
        else
            return G.iterPO(graph, property, node);
    }
}
