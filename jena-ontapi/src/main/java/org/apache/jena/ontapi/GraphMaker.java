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

package org.apache.jena.ontapi;

import org.apache.jena.graph.Graph;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DoesNotExistException;

import java.util.stream.Stream;

/**
 * A factory for providing instances of named graphs with appropriate storage models.
 * It represents a directory, or a database, or a mapping: names map to graphs for the
 * lifetime of the GraphMaker. Names can be "arbitrary" character sequences.
 */
public interface GraphMaker {

    /**
     * Create a new graph associated with the given name.
     * If there is no such association, create one and return it.
     * Otherwise, throw an AlreadyExistsException.
     *
     * @param name graph identifier, not {@code null}
     * @return {@link Graph}, not {@code null}
     * @throws AlreadyExistsException if graph already exists in the underlying storage
     */
    Graph createGraph(String name) throws AlreadyExistsException;

    /**
     * Finds an existing graph that this factory knows about under the given name.
     * If such a graph exists, return it.
     * Otherwise, throw a DoesNotExistException.
     *
     * @param name graph identifier, not {@code null}
     * @return {@link Graph}, not {@code null}
     * @throws DoesNotExistException if there is no such graph in the underlying storage
     */
    Graph openGraph(String name) throws DoesNotExistException;

    /**
     * Removes the association between the name and the graph.
     * The method {@link #createGraph(String)} will now be able to create a graph with that name,
     * and {@link #openGraph(String)} will no longer be able to find it.
     * The graph itself is not touched.
     *
     * @param name graph identifier, not {@code null}
     * @throws DoesNotExistException if there is no such graph in the underlying storage
     */
    void removeGraph(String name) throws DoesNotExistException;

    /**
     * Returns true iff the factory has a graph with the given name
     *
     * @param name graph identifier, not {@code null}
     * @return {@code boolean}
     */
    boolean hasGraph(String name);

    /**
     * Answers a {@code Stream} where each element is the name of a graph in
     * the maker, and the complete sequence exhausts the set of names.
     * No particular order is expected from the list.
     *
     * @return {@code Stream} of graph's identifiers
     */
    Stream<String> names();

    /**
     * Closes the factory - no more requests will be executed, and any cleanup can be performed.
     */
    void close();

    /**
     * Returns the existing graph or null if it does not exist.
     *
     * @param name graph identifier, not {@code null}
     * @return {@link Graph}, or {@code null}
     */
    default Graph getGraphOrNull(String name) {
        try {
            return openGraph(name);
        } catch (DoesNotExistException e) {
            return null;
        }
    }

    /**
     * Lists all graphs.
     * @return {@code Stream} of {@link Graph}s
     */
    default Stream<Graph> graphs() {
        return names().map(this::openGraph);
    }
}
