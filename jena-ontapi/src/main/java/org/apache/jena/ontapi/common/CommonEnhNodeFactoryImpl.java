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
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Objects;

/**
 * Default implementation of {@link EnhNodeFactory}.
 * This is a kind of constructor that consists of three modules:
 * <ul>
 * <li>{@link EnhNodeProducer} for initialization and physical creation a node {@link EnhNode} in the graph {@link EnhGraph}.</li>
 * <li>{@link EnhNodeFilter} to test the presence of a node in the graph.</li>
 * <li>{@link EnhNodeFinder} to search for nodes in the graph.</li>
 * </ul>
 */
public class CommonEnhNodeFactoryImpl extends BaseEnhNodeFactoryImpl {
    private final EnhNodeProducer maker;
    private final EnhNodeFinder finder;
    private final EnhNodeFilter filter;

    public CommonEnhNodeFactoryImpl(EnhNodeProducer maker, EnhNodeFinder finder, EnhNodeFilter filter) {
        this.maker = Objects.requireNonNull(maker, "Null maker.");
        this.finder = Objects.requireNonNull(finder, "Null finder.");
        this.filter = Objects.requireNonNull(filter, "Null filter.");
    }

    public EnhNodeProducer getMaker() {
        return maker;
    }

    public EnhNodeFinder getFinder() {
        return finder;
    }

    public EnhNodeFilter getFilter() {
        return filter;
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        if (!canWrap(node, eg)) {
            throw new OntJenaException.Conversion(
                    String.format("Can't wrap node to impl %s. Node: %s", maker.targetName(), OntEnhNodeFactories.toPrintString(node, eg))
            );
        }
        return createInstance(node, eg);
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return filter.test(node, eg);
    }

    @Override
    public EnhNode createInGraph(Node node, EnhGraph eg) {
        if (!canCreateInGraph(node, eg)) {
            throw new OntJenaException.Creation(
                    String.format("Can't modify graph for impl %s. Node: %s", maker.targetName(), OntEnhNodeFactories.toPrintString(node, eg))
            );
        }
        maker.doInsert(node, eg);
        return createInstance(node, eg);
    }

    @Override
    public boolean canCreateInGraph(Node node, EnhGraph eg) {
        return maker.canInsert(node, eg);
    }

    @Override
    public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
        return finder.restrict(filter).iterator(eg).mapWith(n -> maker.newInstance(n, eg));
    }

    @Override
    public EnhNode createInstance(Node node, EnhGraph eg) {
        return maker.newInstance(node, eg);
    }
}
