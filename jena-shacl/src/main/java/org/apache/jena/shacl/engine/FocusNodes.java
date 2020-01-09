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

package org.apache.jena.shacl.engine;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

/** Calculate focus nodes. */
public class FocusNodes {

    /** Return all the focus nodes for a given set of shapes and data. */
    public static Set<Node> focusNodes(Targets shapes, Graph data) {
        Set<Node> x1 = focusTargetNode(shapes);
        Set<Node> x2 = focusTargetClass(shapes, data);
        Set<Node> x3 = focusTargetObjectsOf(shapes, data);
        Set<Node> x4 = focusTargetSubjectsOf(shapes, data);
        
        Set<Node> z = new HashSet<>();
        z.addAll(x1);
        z.addAll(x2);
        z.addAll(x3);
        z.addAll(x4);
        return z;
    }

    public static Set<Node> focusTargetNode(Targets shapes) {
        return shapes.targetNodes;
    }

    private static void focusTargetNode(Set<Node> acc, Targets shapes) {
        acc.addAll(shapes.targetNodes);
    }

    public static Set<Node> focusTargetClass(Targets shapes, Graph data) {
        // Classes
        Stream<Node> cls = targetClasses(shapes);
        // Individuals of.
        Set<Node> indiv =
            cls.flatMap(type->stream(data, null, RDF.Nodes.type, type) )
                .map(Triple::getObject)
                .collect(Collectors.toSet())
                ;
        return indiv;
    }

    static Stream<Node> targetClasses(Targets shapes) {
        return shapes.targetClasses.stream();
    }

    public static Set<Node> focusTargetObjectsOf(Targets shapes, Graph data) {
        return focusTargetOf(shapes, shapes.propertyTargetObjectsOf, data, Triple::getObject);
    }

    public static Set<Node> focusTargetSubjectsOf(Targets shapes, Graph data) {
        return focusTargetOf(shapes, shapes.propertyTargetSubjectsOf, data, Triple::getSubject);
    }

    private static Set<Node> focusTargetOf(Targets shapes, Set<Node> targetOf, Graph data, Function<Triple, Node> projection) {
        Stream <Node> z = targetOf.stream().flatMap(p->stream(data, null, p, null)).map(projection);
        return set(z);
    }

    private static Stream<Triple> stream(Graph g, Node s, Node p, Node o) {
        return Iter.asStream(g.find(s, p, o));
    }

    private static Set<Node> set(Stream<Node> z) {
        return z.collect(Collectors.toSet());
    }
}
