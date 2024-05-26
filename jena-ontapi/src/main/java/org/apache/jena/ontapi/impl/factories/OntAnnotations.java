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

package org.apache.jena.ontapi.impl.factories;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.impl.objects.OntAnnotationImpl;
import org.apache.jena.ontapi.model.OntAnnotation;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Set;

final class OntAnnotations {
    public static final Set<Node> REQUIRED_PROPERTY_NODES = StdModels.asUnmodifiableNodeSet(OntAnnotationImpl.REQUIRED_PROPERTIES);
    public static final Node AXIOM = OWL2.Axiom.asNode();
    public static final Node ANNOTATION = OWL2.Annotation.asNode();
    public static final Set<Node> EXTRA_ROOT_TYPES_AS_NODES = StdModels.asUnmodifiableNodeSet(OntAnnotationImpl.EXTRA_ROOT_TYPES);

    /**
     * Lists all root {@link Node}s of top-level {@link OntAnnotation}s in the given model.
     * In OWL2 a top-level annotation must have one of the following {@code rdf:type}s:
     * {@link OWL2#Axiom owl:Axiom}, {@link OWL2#AllDisjointClasses owl:AllDisjointClasses},
     * {@link OWL2#AllDisjointProperties owl:AllDisjointProperties}, {@link OWL2#AllDifferent owl:AllDifferent} or
     * {@link OWL2#NegativePropertyAssertion owl:NegativePropertyAssertion}
     *
     * @param eg {@link EnhGraph} model to search in
     * @return {@link ExtendedIterator} of {@link Node}s
     */
    public static ExtendedIterator<Node> listRootAnnotations(EnhGraph eg) {
        return Iterators.flatMap(Iterators.of(AXIOM).andThen(EXTRA_ROOT_TYPES_AS_NODES.iterator()),
                        t -> eg.asGraph().find(Node.ANY, RDF.Nodes.type, t))
                .mapWith(Triple::getSubject);
    }

    public static boolean testAnnotation(Node node, EnhGraph graph) {
        return testAnnotation(node, graph.asGraph());
    }

    public static boolean testAnnotation(Node node, Graph graph) {
        if (!node.isBlank()) return false;
        ExtendedIterator<Node> types = graph.find(node, RDF.Nodes.type, Node.ANY).mapWith(Triple::getObject);
        try {
            while (types.hasNext()) {
                Node t = types.next();
                if (AXIOM.equals(t) || ANNOTATION.equals(t)) {
                    // test spec
                    Set<Node> props = graph.find(node, Node.ANY, Node.ANY).mapWith(Triple::getPredicate).toSet();
                    return props.containsAll(REQUIRED_PROPERTY_NODES);
                }
                // special cases: owl:AllDisjointClasses, owl:AllDisjointProperties,
                // owl:AllDifferent or owl:NegativePropertyAssertion
                if (EXTRA_ROOT_TYPES_AS_NODES.contains(t)) {
                    return true;
                }
            }
        } finally {
            types.close();
        }
        return false;
    }
}
