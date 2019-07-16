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

package org.apache.jena.shacl.lib;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class Transitive {

    // ---- Transitive properties, including the start node.
    public static void transitiveInc(Graph graph, boolean forward, Node node, Node predicate, Collection<Node> output) {
        recurse(graph, forward, node, predicate, output);
    }

    // ---- Transitive properties, excluding the start node unless in a cycle.
    public static void transitiveExc(Graph graph, boolean forward, Node node, Node predicate, Collection<Node> output) {
        Iterator<Node> iter = singleStep(graph, forward, node, predicate);
        Set<Node> visited = new HashSet<>();
        for (; iter.hasNext();) {
            Node n1 = iter.next() ;
            recurse_1(graph, forward, 1, -1, n1, predicate, visited, output) ;
        }
    }

    // Extracted and simplified From PathEval/PathEngineSPARQL.
    private static void recurse(Graph graph, boolean forward, Node node, Node predicate, Collection<Node> output) {
        Set<Node> visited = new HashSet<>();
        recurse_1(graph, forward, 0, -1, node, predicate, visited, output);
    }

    private static void recurse_1(Graph graph, boolean forward, int stepCount, int maxStepCount, Node node, Node predicate, Set<Node> visited, Collection<Node> output) {
        if ( maxStepCount >= 0 && stepCount > maxStepCount )
            return ;
        if ( !visited.add(node) )
            return ;
        output.add(node);
        Iterator<Node> iter1 = singleStep(graph, forward, node, predicate) ;
        // For each step, add to results and recurse.
        for (; iter1.hasNext();) {
            Node n1 = iter1.next() ;
            recurse_1(graph, forward, stepCount + 1, maxStepCount, n1, predicate, visited, output) ;
        }
    }

    // A single step of a transitive properties.
    // Because for SP? or ?PO, no duplicates occur, so works for both strategies.
    private static Iterator<Node> singleStep(Graph graph, boolean forward, Node node, Node property) {
        if ( forward )
            return G.find(graph, node, property, Node.ANY).mapWith(Triple::getObject);
        else
            return G.find(graph, Node.ANY, property, node).mapWith(Triple::getSubject);
    }
}
