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
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.impl.repositories.PersistentGraphRepository;
import org.apache.jena.ontapi.model.OntID;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Graph repository.
 * Each {@link Graph} is associated with ID.
 * For OWL Ontology Graphs, Graph ID can be {@link OntID#getImportsIRI()}.
 */
public interface GraphRepository {

    /**
     * A factory method to create {@link GraphRepository} instance
     * that loads graphs on demand from the given location.
     * The location is specified by the method {@link DocumentGraphRepository#addMapping(String, String)}.
     * If there is no mapping specified,
     * graph id will be used as a source.
     *
     * @return {@link DocumentGraphRepository}
     */
    static DocumentGraphRepository createGraphDocumentRepositoryMem() {
        return createGraphDocumentRepository(GraphMemFactory::createDefaultGraph);
    }

    /**
     * A factory method to creates {@link GraphRepository} instance
     * that loads graphs on demand from the given location.
     * The location is specified by the method {@link DocumentGraphRepository#addMapping(String, String)}.
     * If there is no mapping specified,
     * graph id will be used as a source URL or file path.
     *
     * @param factory {@link Supplier} to produce new {@link Graph}, not {@code null}
     * @return {@link DocumentGraphRepository}
     */
    static DocumentGraphRepository createGraphDocumentRepository(Supplier<Graph> factory) {
        return new DocumentGraphRepository(Objects.requireNonNull(factory, "Null graph factory"));
    }

    /**
     * A factory method for creating persistent {@link GraphRepository}; persistence is ensured by the {@code maker}.
     * @param maker {@link GraphMaker} a factory to create/fetch/remove {@link Graph}s
     * @return {@link PersistentGraphRepository}
     */
    static PersistentGraphRepository createPersistentGraphRepository(GraphMaker maker) {
        return new PersistentGraphRepository(Objects.requireNonNull(maker));
    }

    /**
     * Gets Graph by ID.
     *
     * @param id {@code String} Graph's identifier, usually it is ontology ID or source URL or file path
     * @return {@link Graph}
     */
    Graph get(String id);

    /**
     * @return {@code Stream} of Graph's identifiers
     */
    Stream<String> ids();

    /**
     * Associates the specified graph with the specified ID,
     * returning the previous association or {@code null} if there was no association.
     *
     * @param id    {@code String} Graph's identifier
     * @param graph {@link Graph}
     * @return {@link Graph} or {@code null}
     */
    Graph put(String id, Graph graph);

    /**
     * Removes graph.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph} associated with the id, or null if there was no graph for the given id
     */
    Graph remove(String id);

    /**
     * Removes all graphs.
     */
    void clear();

    /**
     * @return number of graphs
     */
    default long count() {
        return ids().count();
    }

    /**
     * Lists all graphs.
     *
     * @return {@code Stream} of {@link Graph}s
     */
    default Stream<Graph> graphs() {
        return ids().map(this::get).filter(Objects::nonNull);
    }

    /**
     * @param id {@code String} Graph's identifier
     * @return boolean
     */
    default boolean contains(String id) {
        Objects.requireNonNull(id);
        return ids().anyMatch(id::equals);
    }

}
