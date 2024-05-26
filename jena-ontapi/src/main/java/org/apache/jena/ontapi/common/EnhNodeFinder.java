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

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A class-helper to perform the preliminary resource search in a model.
 * Subsequently, the search result Stream will be filtered by the {@link EnhNodeFilter} instance.
 * Used as a component in {@link CommonEnhNodeFactoryImpl default factory} and {@link CompositeEnhNodeFactoryImpl} implementations
 */
@FunctionalInterface
public interface EnhNodeFinder {
    EnhNodeFinder ANY_SUBJECT = eg -> Graphs.listSubjects(eg.asGraph());
    EnhNodeFinder ANY_BLANK_SUBJECT = eg -> Iterators.distinct(eg.asGraph().find().mapWith(Triple::getSubject).filterKeep(Node::isBlank));
    EnhNodeFinder ANY_SUBJECT_AND_OBJECT = eg -> Graphs.listSubjectsAndObjects(eg.asGraph());
    EnhNodeFinder ANYTHING = eg -> Graphs.listAllNodes(eg.asGraph());
    EnhNodeFinder NOTHING = eg -> NullIterator.instance();
    EnhNodeFinder ANY_TYPED = new ByPredicate(RDF.type);

    /**
     * Returns an iterator over the nodes in the given model, which satisfy some criterion,
     * specific to this {@link EnhNodeFinder}.
     * It is expected that the result does not contain duplicates.
     *
     * @param eg {@link EnhGraph}, model
     * @return {@link ExtendedIterator} of {@link Node}s
     */
    ExtendedIterator<Node> iterator(EnhGraph eg);

    /**
     * Lists the nodes from the specified model by the encapsulated criterion.
     *
     * @param eg {@link EnhGraph}, model
     * @return {@link Stream} of {@link Node}s
     */
    default Stream<Node> find(EnhGraph eg) {
        return Iterators.asStream(iterator(eg));
    }

    default EnhNodeFinder restrict(EnhNodeFilter filter) {
        if (Objects.requireNonNull(filter, "Null restriction filter.").equals(EnhNodeFilter.TRUE)) return this;
        if (filter.equals(EnhNodeFilter.FALSE)) return eg -> NullIterator.instance();
        return eg -> iterator(eg).filterKeep(n -> filter.test(n, eg));
    }

    class ByType implements EnhNodeFinder {
        protected final Node type;

        public ByType(Resource type) {
            this(Objects.requireNonNull(type, "Null type.").asNode());
        }

        public ByType(Node type) {
            this.type = Objects.requireNonNull(type, "Null type.");
        }

        @Override
        public ExtendedIterator<Node> iterator(EnhGraph eg) {
            return eg.asGraph().find(Node.ANY, RDF.Nodes.type, type).mapWith(Triple::getSubject);
        }
    }

    class ByTypes implements EnhNodeFinder {
        protected final List<Node> types;

        public ByTypes(Collection<Resource> types) {
            if (types.isEmpty()) {
                throw new IllegalStateException();
            }
            this.types = types.stream().map(FrontsNode::asNode).distinct().toList();
        }

        @Override
        public ExtendedIterator<Node> iterator(EnhGraph eg) {
            if (types.size() == 1) {
                return eg.asGraph().find(Node.ANY, RDF.Nodes.type, types.get(0)).mapWith(Triple::getSubject);
            }
            return Iterators.distinct(Iterators.flatMap(WrappedIterator.create(types.iterator()),
                    type -> eg.asGraph().find(Node.ANY, RDF.Nodes.type, type)).mapWith(Triple::getSubject));
        }
    }

    class ByPredicate implements EnhNodeFinder {
        protected final Node predicate;

        public ByPredicate(Property predicate) {
            this.predicate = Objects.requireNonNull(predicate, "Null predicate.").asNode();
        }

        @Override
        public ExtendedIterator<Node> iterator(EnhGraph eg) {
            return Iterators.distinct(eg.asGraph().find(Node.ANY, predicate, Node.ANY).mapWith(Triple::getSubject));
        }
    }
}
