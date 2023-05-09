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

package org.apache.jena.ontapi.common;

import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * An {@link EnhNode} factory abstraction,
 * that is an ONT-API analogue of the {@link Implementation Jena Implementation Factory}.
 * Used to bind a concrete implementation (i.e. {@link EnhNode enhanced node}) and
 * an interface ({@link OntObject OWL Object}) with a {@link Node Graph Node}.
 * It is a part of {@link OntPersonality} mechanism to manipulate
 * {@link OntModel OWL2 RDF-model} objects (which happens through {@link OntEnhGraph}).
 * Also note, explicit links between different {@link EnhNodeFactory} factories are undesirable
 * (for more details see {@link OntObjectPersonalityBuilder#add(Class, EnhNodeFactory)} and
 * {@link OntEnhGraph}).
 * <p>
 * TODO: consider a possibility to replace {@link EnhGraph} with {@link OntEnhGraph}.
 *
 * @see OntPersonality
 * @see OntEnhGraph
 */
public interface EnhNodeFactory {

    /**
     * Represents the given implementation factory as {@link Implementation Jena Implementation Factory}.
     *
     * @param f {@link OntPersonality}, not {@code null}
     * @return {@link Implementation}, not {@code null}
     * @throws OntJenaException in case the conversion is not possible
     * @see OntPersonality#asJenaPersonality(OntPersonality)
     * @see OntEnhGraph#asPersonalityModel(EnhGraph)
     */
    static Implementation asJenaImplementation(EnhNodeFactory f) throws OntJenaException {
        if (f instanceof Implementation) {
            return (Implementation) f;
        }
        return OntJenaException.TODO("The given EnhNodeFactory is not an instance of Jena Implementation.");
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} over the {@link Node node}s found in the {@link EnhGraph}
     * in the form of {@link EnhNode} with the interface that this factory encapsulates.
     * It is assumed that the returned iterator is distinct (does not answer with duplicates).
     * At least, it must be distinct if the {@code EnhGraph} encapsulates the distinct graph
     * (see also {@link Graphs#isDistinct(Graph)}).
     *
     * @param eg {@link EnhGraph}
     * @return {@link ExtendedIterator} of {@link EnhNode}s
     */
    ExtendedIterator<EnhNode> iterator(EnhGraph eg);

    /**
     * Checks that the wrapping ({@code node}, {@code eg}) would succeed.
     *
     * @param node node the node to test for suitability
     * @param eg   the enhanced graph the node appears in
     * @return {@code true} iff the {@code node} can represent the type in the graph
     */
    boolean canWrap(Node node, EnhGraph eg);

    /**
     * Wraps the given pair of {@link Node node} and {@link EnhGraph graph} to a fresh {@link EnhNode} instance
     * without any checking or throwing any exception.
     * Some implementations may return {@code null} if the {@link #canWrap(Node, EnhGraph)} returns {@code false}.
     * Unlike the method {@link #createInGraph(Node, EnhGraph)}, this method does not make any changes to the graph.
     *
     * @param node {@link Node}, not {@code null}
     * @param eg   {@link EnhGraph}, not {@code null}
     * @return {@link EnhNode} or {@code null}
     */
    EnhNode createInstance(Node node, EnhGraph eg);

    /**
     * Creates a new {@link EnhNode} wrapping the given {@link Node} node in the context of the graph {@link EnhGraph}.
     * Unlike the method {@link #createInGraph(Node, EnhGraph)}, this method does not make any changes to the graph.
     *
     * @param node {@link Node}, the node to be wrapped as {@link EnhNode}, not {@code null}
     * @param eg   {@link EnhGraph}, the graph containing the node, not {@code null}
     * @return {@link EnhNode} a new enhanced node presenting the interface that this factory encapsulates
     * @throws OntJenaException in case wrapping is impossible
     */
    default EnhNode wrap(Node node, EnhGraph eg) {
        if (!canWrap(node, eg))
            throw new OntJenaException.Conversion("Can't wrap node " + node + ". Use direct factory.");
        return createInstance(node, eg);
    }

    /**
     * Makes interface-defined changes in the given graph
     * and returns a new enhanced node, wrapping the given {@link Node}.
     *
     * @param node {@link Node}, the node to be wrapped as {@link EnhNode}, not {@code null}
     * @param eg   {@link EnhGraph}, the graph which would contain the result {@link EnhNode}, not {@code null}
     * @return a new enhanced node
     * @throws OntJenaException in case modification of graph is not allowed for the specified node
     */
    default EnhNode createInGraph(Node node, EnhGraph eg) {
        throw new OntJenaException.IllegalCall("Creation is not allowed: " + node);
    }

    /**
     * Answers {@code true} iff the modifying of the {@link EnhGraph graph} is allowed
     * by this implementation for the encapsulated object's interface.
     *
     * @param node {@link Node} the node to test that changes are permitted.
     * @param eg   {@link EnhGraph} the graph to be changed.
     * @return true if creation is allowed.
     */
    default boolean canCreateInGraph(Node node, EnhGraph eg) {
        return false;
    }

    /**
     * Lists all enhanced nodes from the graph, with the interface that this factory encapsulates.
     *
     * @param eg the graph containing the node
     * @return the stream of enhanced and suitability nodes
     */
    default Stream<EnhNode> find(EnhGraph eg) {
        return Iterators.asStream(iterator(eg));
    }
}
