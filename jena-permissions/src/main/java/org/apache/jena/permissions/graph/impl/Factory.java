/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.graph.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.permissions.graph.SecuredPrefixMapping;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.shared.PrefixMapping;

public class Factory {

    /**
     * Create an instance of SecuredPrefixMapping
     * 
     * @param graph         The SecuredGraph that contains the prefixmapping.
     * @param prefixMapping The prefixmapping returned from the base graph.
     * @return The SecuredPrefixMapping.
     */
    static SecuredPrefixMapping getInstance(final SecuredGraphImpl graph, final PrefixMapping prefixMapping) {

        final ItemHolder<PrefixMapping, SecuredPrefixMapping> holder = new ItemHolder<>(prefixMapping);
        final SecuredPrefixMappingImpl checker = new SecuredPrefixMappingImpl(graph, holder);
        // if we are going to create a duplicate proxy just return this one.
        if (prefixMapping instanceof SecuredPrefixMapping) {
            if (checker.isEquivalent((SecuredPrefixMapping) prefixMapping)) {
                return (SecuredPrefixMapping) prefixMapping;
            }
        }

        return holder.setSecuredItem(new SecuredItemInvoker(prefixMapping.getClass(), checker));
    }

    /**
     * Create an instance of the SecuredGraph
     * 
     * @param securityEvaluator The security evaluator to use
     * @param graphIRI          The IRI for the graph.
     * @param graph             The graph that we are wrapping.
     * @return the secured graph
     */
    public static SecuredGraph getInstance(final SecurityEvaluator securityEvaluator, final String graphIRI,
            final Graph graph) {

        final ItemHolder<Graph, SecuredGraphImpl> holder = new ItemHolder<>(graph);
        final SecuredGraphImpl checker = new SecuredGraphImpl(securityEvaluator, graphIRI, holder) {
        };

        // If we going to create a duplicate proxy return this one.
        if (graph instanceof SecuredGraphImpl) {
            if (checker.isEquivalent((SecuredGraphImpl) graph)) {
                return (SecuredGraph) graph;
            }
        }
        return holder.setSecuredItem(new SecuredItemInvoker(graph.getClass(), checker));
    }

}
