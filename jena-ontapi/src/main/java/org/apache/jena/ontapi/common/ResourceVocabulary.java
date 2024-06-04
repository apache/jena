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
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import java.util.Set;

/**
 * This is a resource type mapper.
 * It is a technical interface that is included into the {@link OntPersonality}.
 * Note: all its methods must return a IRIs (as {@code String}s), not literals or blank-nodes.
 *
 * @param <T> any subtype of {@link Resource}
 * @see OntVocabulary
 */
public interface ResourceVocabulary<T extends Resource> {

    /**
     * Returns a {@code Set} of {@link Node Jena Graph Node}s for the given {@code Class}-type.
     *
     * @param type {@link Class}, any subtype of {@code T}
     * @return Set of {@link Node node}s (immutable!), can be empty (if no mapping or type is not supported)
     */
    Set<Node> get(Class<? extends T> type) throws OntJenaException;

    /**
     * Answers {@code true} if the given type is supported by the vocabulary.
     *
     * @param type {@link Class}, any subtype of {@code T}
     * @return boolean
     */
    boolean supports(Class<? extends T> type);

}
