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

package org.apache.jena.ontapi.impl.repositories;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.GraphRepository;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shared.JenaException;

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Simple GraphRepository implementation with location mapping.
 */
public class DocumentGraphRepository implements GraphRepository {

    private final Supplier<Graph> factory;

    private final Map<String, Source> idToSource = new HashMap<>();
    private final Map<Source, Graph> sourceToGraph = new HashMap<>();

    public DocumentGraphRepository() {
        this(GraphMemFactory::createDefaultGraph);
    }

    public DocumentGraphRepository(Supplier<Graph> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    /**
     * Checks whether the specified string is a valid URI or a file path.
     *
     * @param uriOrFile to validate
     * @return the same string
     */
    public static String checkLocation(String uriOrFile) {
        Objects.requireNonNull(uriOrFile, "Null fileNameOrURI");
        RuntimeException ex = new RuntimeException("Wrong URI: <" + uriOrFile + ">");
        String file = null;
        if (uriOrFile.startsWith("file:")) {
            file = uriOrFile.replace("file:", "/");
        } else if (uriOrFile.startsWith("/")) {
            file = uriOrFile;
        }
        try {
            if (file != null) {
                Paths.get(file);
            }
            return uriOrFile;
        } catch (Exception e) {
            ex.addSuppressed(e);
        }
        try {
            new URI(uriOrFile);
            return uriOrFile;
        } catch (Exception e) {
            ex.addSuppressed(e);
        }
        throw ex;
    }

    private static Source parseLocation(String uriOrFile) {
        return new Source(checkLocation(uriOrFile), RDFLanguages.resourceNameToLang(uriOrFile, Lang.RDFXML));
    }

    private static Graph read(Source source, Graph target) {
        RDFParser.create().source(source.location).lang(source.lang).parse(target);
        return target;
    }

    /**
     * Adds mapping Graph's ID &lt;-&gt; source document location,
     * which can be an OS file path, class-resource path, or URI (ftp or http).
     * File URL should be of the form {@code file:///...}.
     * Class-resource path string should be without leading "/" symbol.
     * After successful load,
     * the graph will be available both by {@code id} and
     * by {@code fileNameOrUri} (via {@link #get(String)} method).
     * A graph can be associated with different identifiers but only with one source.
     *
     * @param id        Graph's id, arbitrary string
     * @param uriOrFile location of the Graph document (e.g. "file://ontology.ttl")
     * @return this instance
     * @see DocumentGraphRepository#get(String)
     */
    public DocumentGraphRepository addMapping(String id, String uriOrFile) {
        Objects.requireNonNull(id, "Null Graph Id");
        var source = parseLocation(Objects.requireNonNull(uriOrFile, "location (file or uri) is required"));
        idToSource.put(id, source);
        idToSource.put(uriOrFile, source);
        return this;
    }

    /**
     * Gets the graph by its ID, which can be ontology id, location (file or uri) or arbitrary identifier,
     * if there is a mapping for it.
     * The method attempts to load the graph if it is not yet in the repository.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph}
     * @see DocumentGraphRepository#addMapping(String, String)
     * @throws org.apache.jena.shared.JenaException if graph cannot be loaded
     */
    @Override
    public Graph get(String id) {
        try {
            var source = idToSource.computeIfAbsent(Objects.requireNonNull(id, "Null Graph Id"),
                    DocumentGraphRepository::parseLocation);
            return sourceToGraph.computeIfAbsent(source, it -> read(it, factory.get()));
        } catch (JenaException e) {
            idToSource.remove(id);
            throw e;
        }
    }

    /**
     * Lists all graph's identifiers.
     * Note that the number of identifiers may exceed the number of graphs if there are multiple id-source mappings.
     *
     * @return {@code Stream} of ids
     */
    @Override
    public Stream<String> ids() {
        return getIds().stream();
    }

    /**
     * Associates the graph with the specified id.
     * If there is no id-source mapping yet, the given id will be used as a document source.
     *
     * @param id    {@code String} Graph's identifier, which can be arbitrary string
     * @param graph {@link Graph}
     * @return {@link Graph} the previously associated with id or {@code null}
     */
    @Override
    public Graph put(String id, Graph graph) {
        Objects.requireNonNull(id, "Null Graph Id");
        Objects.requireNonNull(graph, "Null Graph");
        var source = idToSource.computeIfAbsent(id, it -> new Source(it, RDFLanguages.resourceNameToLang(id, Lang.RDFXML)));
        return sourceToGraph.put(source, graph);
    }

    /**
     * Removes and returns the graph identified by the specified {@code id}, along with all its associations.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph} or {@code null} if the graph is not found
     */
    @Override
    public Graph remove(String id) {
        var source = idToSource.remove(Objects.requireNonNull(id, "Null Graph Id"));
        if (source == null) {
            return null;
        }
        idToSource.entrySet().stream().toList().stream()
                .filter(it -> source.equals(it.getValue()))
                .forEach(it -> idToSource.remove(it.getKey()));
        return sourceToGraph.remove(source);
    }

    /**
     * Removes all graphs.
     */
    @Override
    public void clear() {
        sourceToGraph.clear();
        idToSource.clear();
    }

    /**
     * Returns the number of identifiers.
     * Note that it may exceed the number of graphs if there are multiple associations.
     *
     * @return {@code long}
     */
    @Override
    public long count() {
        return getIds().size();
    }

    /**
     * Returns all already loaded graphs.
     * Note that the number of returned graphs may not be equal to the number of mappings ({@link #count()}).
     *
     * @return distinct {@code Stream} of {@link Graph}s
     */
    @Override
    public Stream<Graph> graphs() {
        return sourceToGraph.values().stream();
    }

    /**
     * Returns {@code true} if a mapping for the specified identifier exists in the repository.
     *
     * @param id {@code String} Graph's identifier
     * @return boolean
     */
    @Override
    public boolean contains(String id) {
        return idToSource.containsKey(Objects.requireNonNull(id, "Null Graph Id"));
    }

    protected Set<String> getIds() {
        return idToSource.keySet();
    }

    private record Source(String location, Lang lang) {
    }

}
