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

import org.apache.jena.ontapi.GraphRepository;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shared.JenaException;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple GraphRepository implementation with location mapping.
 */
public class DocumentGraphRepository implements GraphRepository {

    private final Supplier<Graph> factory;
    private final Map<String, Source> mappings = new HashMap<>();
    private final Map<String, Graph> graphs = new HashMap<>();

    public DocumentGraphRepository() {
        this(GraphMemFactory::createDefaultGraph);
    }

    public DocumentGraphRepository(Supplier<Graph> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    /**
     * Validates the specified String is a valid URI or file path.
     *
     * @param fileNameOrURI to validate
     * @return the same string
     */
    public static String check(String fileNameOrURI) {
        Objects.requireNonNull(fileNameOrURI, "Null fileNameOrURI");
        RuntimeException ex = new RuntimeException("Wrong URI: <" + fileNameOrURI + ">");
        String file = null;
        if (fileNameOrURI.startsWith("file:")) {
            file = fileNameOrURI.replace("file:", "/");
        } else if (fileNameOrURI.startsWith("/")) {
            file = fileNameOrURI;
        }
        try {
            if (file != null) {
                Paths.get(file);
            }
            return fileNameOrURI;
        } catch (Exception e) {
            ex.addSuppressed(e);
        }
        try {
            new URI(fileNameOrURI);
            return fileNameOrURI;
        } catch (Exception e) {
            ex.addSuppressed(e);
        }
        throw ex;
    }

    private static Source parseLocation(String fileNameOrUri) {
        return new Source(check(fileNameOrUri), RDFLanguages.resourceNameToLang(fileNameOrUri, Lang.RDFXML));
    }

    private static Graph read(Source source, Graph target) {
        RDFParser.create().source(source.location).lang(source.lang).parse(target);
        return target;
    }

    /**
     * Adds mapping Graph's ID &lt;-&gt; source document location,
     * which can be a file path, class-resource path, or URI (ftp or http).
     * Note that class-resource path string should be without leading "/" symbol.
     *
     * @param id            Graph's id
     * @param fileNameOrUri location of the Graph document
     * @return this instance
     */
    public DocumentGraphRepository addMapping(String id, String fileNameOrUri) {
        mappings.put(Objects.requireNonNull(id, "Null Graph Id"), parseLocation(fileNameOrUri));
        return this;
    }

    private Source getMapping(String id) {
        return mappings.computeIfAbsent(id, DocumentGraphRepository::parseLocation);
    }

    /**
     * Gets Graph by ID.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph}
     * @throws JenaException if graph can't be found
     */
    @Override
    public Graph get(String id) {
        return graphs.computeIfAbsent(Objects.requireNonNull(id, "Null Graph Id"), s -> read(getMapping(s), factory.get()));
    }

    @Override
    public Stream<String> ids() {
        return getIds().stream();
    }

    @Override
    public Graph put(String id, Graph graph) {
        return graphs.put(Objects.requireNonNull(id, "Null Graph Id"), Objects.requireNonNull(graph, "Null Graph"));
    }

    @Override
    public Graph remove(String id) {
        mappings.remove(id);
        return graphs.remove(Objects.requireNonNull(id, "Null Graph Id"));
    }

    @Override
    public void clear() {
        mappings.clear();
        graphs.clear();
    }

    @Override
    public long count() {
        return getIds().size();
    }

    @Override
    public boolean contains(String id) {
        return graphs.containsKey(id) || mappings.containsKey(id);
    }

    public Set<String> getIds() {
        return Stream.of(graphs.keySet(), mappings.keySet()).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

    private record Source(String location, Lang lang) {
    }

}
