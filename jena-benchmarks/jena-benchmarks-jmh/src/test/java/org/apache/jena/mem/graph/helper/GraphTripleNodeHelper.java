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
package org.apache.jena.mem.graph.helper;

import java.util.List;

/**
 * Helper interface to create graphs, read the triples from files and clone triples and nodes.
 *
 * @param <G> the graph type
 * @param <T> the triple type
 * @param <N> the node type
 */
public interface GraphTripleNodeHelper<G, T, N> {

    /**
     * Creates a graph of the given type.
     *
     * @param graphClass the graph type
     * @return the created graph
     */
    G createGraph(Context.GraphClass graphClass);

    /**
     * Reads the triples from the given file.
     *
     * @param graphUri the file to read the triples from
     * @return the triples
     */
    List<T> readTriples(String graphUri);

    /**
     * Clones the given list of triples.
     *
     * @param triples the triples to clone
     * @return the cloned triples
     */
    default List<T> cloneTriples(List<T> triples) {
        var list = new java.util.ArrayList<T>();
        triples.forEach(triple -> list.add(cloneTriple(triple)));
        return list;
    }

    /**
     * Clones the given triple.
     *
     * @param triple the triple to clone
     * @return the cloned triple
     */
    T cloneTriple(T triple);

    /**
     * Clones the given node.
     *
     * @param node the node to clone
     * @return the cloned node
     */
    N cloneNode(N node);
}
