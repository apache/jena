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
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Objects;

/**
 * A factory wrapper.
 * It is a facility to provide implicit links between {@link EnhNodeFactory object factories}.
 * For more details see the {@link OntEnhGraph} description and also the description for
 * the method {@link OntObjectPersonalityBuilder#add(Class, EnhNodeFactory)}.
 *
 * @see OntEnhNodeFactories
 */
@SuppressWarnings("WeakerAccess")
public class WrappedEnhNodeFactory extends BaseEnhNodeFactoryImpl {
    private final Class<? extends RDFNode> type;

    public WrappedEnhNodeFactory(Class<? extends RDFNode> type) {
        this.type = Objects.requireNonNull(type);
    }

    public static WrappedEnhNodeFactory of(Class<? extends RDFNode> type) {
        return new WrappedEnhNodeFactory(type);
    }

    /**
     * Finds and returns the {@link EnhNodeFactory} instance for the encapsulated {@link OntObject object} type.
     * This factory and the returned one are synonymous: both have the same behaviour.
     *
     * @param g {@link EnhGraph}, the model. Must be instance of {@link OntEnhGraph}
     * @return {@link EnhNodeFactory}, not {@code null}
     * @throws OntJenaException in case nothing is found
     */
    public EnhNodeFactory getDelegate(EnhGraph g) throws OntJenaException {
        return getFactory(g);
    }

    protected EnhNodeFactory getFactory(EnhGraph g) throws OntJenaException {
        return OntEnhGraph.asPersonalityModel(g).getOntPersonality().getObjectFactory(type);
    }

    @Override
    public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
        return getDelegate(eg).iterator(eg);
    }

    @Override
    public boolean canWrap(Node node, EnhGraph eg) {
        return getDelegate(eg).canWrap(node, eg);
    }

    @Override
    public EnhNode createInstance(Node node, EnhGraph eg) {
        return getDelegate(eg).createInstance(node, eg);
    }

    @Override
    public EnhNode wrap(Node node, EnhGraph eg) {
        return getDelegate(eg).wrap(node, eg);
    }

    @Override
    public String toString() {
        return String.format("Factory[%s]", OntEnhNodeFactories.viewAsString(type));
    }
}
