/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdfs.engine;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;

/**
 * Match by S/P/O where {@code X} is the RDF term representation (Node, NodeId) and
 * {@code T} is the tuple (triple, quad, tuple) representation.
 */
public interface Match<X, T> {
    public Stream<T> match(X s, X p, X o);

    public default boolean contains(X s, X p, X o) {
        try (Stream<T> stream = match(s, p, o)) {
            return stream.findFirst().isPresent();
        }
    }

    /**
     * The mapper for reuse with wrappers.
     * Note that this indirectly ties the {@link Match} interface to the {@link Node} realm:
     * One can use the mapper to obtain X for e.g. RDF.Nodes.type.
     */
    MapperX<X, T> getMapper();
}
