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

package org.apache.jena.ontapi.impl.repositories;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.GraphRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.LocationMappingVocab;

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * {@link GraphRepository} implementation that loads RDF documents on demand and
 * caches loaded graphs by their resolved document source.
 * <p>
 * The repository supports two kinds of location mapping:
 * <ul>
 *     <li>exact mappings, added with {@link #addMapping(String, String)}, bind one graph ID to one document location;</li>
 *     <li>prefix mappings, added with {@link #addPrefixMapping(String, String)}
 *     and removed with {@link #removePrefixMapping(String)}, rewrite matching ID prefixes lazily.</li>
 * </ul>
 * Exact mappings take precedence over prefix mappings. Prefix mappings are not
 * expanded eagerly, so IDs covered only by a prefix mapping are not listed by
 * {@link #ids()} until {@link #get(String)} successfully materializes a concrete
 * ID-to-source binding. {@link #contains(String)} may return {@code true} for
 * such an ID before it is materialized; this means that a mapping rule exists,
 * not that the target document is guaranteed to load successfully. Several IDs
 * can resolve to the same document source and therefore share the same cached
 * graph instance.
 */
public class DocumentGraphRepository implements GraphRepository {

    private final Supplier<Graph> factory;

    // Exact and materialized id-to-document bindings. Prefix mappings are added here only after get(id) resolves them.
    private final Map<String, Source> idToSource = new HashMap<>();
    // Prefix mappings are lazy: they can make contains(id) true but create an id entry only after get(id) resolves it.
    private final Map<String, String> idPrefixToLocationPrefix = new HashMap<>();
    // Removed concrete ids must not be reintroduced by prefix fallback while sibling ids under the same prefix still resolve.
    private final Set<String> excludedFromPrefixMapping = new HashSet<>();
    // Cache of loaded graphs by document source; several ids can point to the same cached graph.
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
        bind(id, source);
        bind(uriOrFile, source);
        excludedFromPrefixMapping.remove(id);
        excludedFromPrefixMapping.remove(uriOrFile);
        return this;
    }

    /**
     * Adds mapping from a graph ID prefix to a source document location prefix.
     * If no exact mapping is found for an ID, the ID prefix is replaced by the
     * location prefix and the remaining ID suffix is preserved.
     * If several prefix mappings match the same ID, the mapping with the longest
     * location prefix is used, matching the historical Jena {@code LocationMapper}
     * behavior.
     * Prefix mappings are lazy: they are used by {@link #contains(String)} and
     * {@link #get(String)}, but do not appear in {@link #ids()} until a concrete
     * ID is resolved by {@code get}.
     * <p>
     * Example:
     * <pre>{@code
     * DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
     *         .addPrefixMapping("vocab/", "builtins-");
     *
     * repository.contains("vocab/rdfs.rdf"); // true
     * repository.get("vocab/rdfs.rdf");      // loads "builtins-rdfs.rdf"
     * }</pre>
     * Equivalent Jena location mapping model:
     * <pre>{@code
     * PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
     *
     * [] lm:mapping [
     *      lm:prefix "vocab/" ;
     *      lm:altPrefix "builtins-"
     * ] .
     * }</pre>
     *
     * @param idPrefix        Graph's ID prefix
     * @param uriOrFilePrefix location prefix
     * @return this instance
     * @see DocumentGraphRepository#get(String)
     */
    public DocumentGraphRepository addPrefixMapping(String idPrefix, String uriOrFilePrefix) {
        idPrefixToLocationPrefix.put(
                Objects.requireNonNull(idPrefix, "Null Graph Id prefix"),
                Objects.requireNonNull(uriOrFilePrefix, "location prefix is required")
        );
        excludedFromPrefixMapping.removeIf(id -> id.startsWith(idPrefix));
        return this;
    }

    /**
     * Removes a graph ID prefix mapping.
     * Already materialized graph IDs and loaded graphs are not removed by this method.
     * To remove a concrete graph association, use {@link #remove(String)}.
     *
     * @param idPrefix Graph's ID prefix
     * @return removed location prefix or {@code null} if no prefix mapping existed
     */
    public String removePrefixMapping(String idPrefix) {
        return idPrefixToLocationPrefix.remove(Objects.requireNonNull(idPrefix, "Null Graph Id prefix"));
    }

    /**
     * Adds location mappings from a Jena location mapping RDF model.
     * Entries with {@code lm:name} and {@code lm:altName} are processed as exact mappings.
     * Entries with {@code lm:prefix} and {@code lm:altPrefix} are processed as prefix mappings.
     * <p>
     * Example Jena location mapping model:
     * <pre>{@code
     * PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
     *
     * [] lm:mapping [
     *      lm:name "http://example.com/ontology" ;
     *      lm:altName "file:ontologies/ontology.ttl"
     * ] .
     * }</pre>
     *
     * @param model RDF model with {@code lm:mapping} entries
     * @return this instance
     * @see LocationMappingVocab
     * @see DocumentGraphRepository#addMapping(String, String)
     */
    public DocumentGraphRepository addMappings(Model model) {
        Objects.requireNonNull(model, "Null location mapping model");
        StmtIterator mappings = model.listStatements(null, LocationMappingVocab.mapping, (RDFNode) null);
        try {
            while (mappings.hasNext()) {
                Statement s = mappings.nextStatement();
                Resource mapping = s.getResource();
                if (mapping.hasProperty(LocationMappingVocab.name) && mapping.hasProperty(LocationMappingVocab.altName)) {
                    String name = mapping.getRequiredProperty(LocationMappingVocab.name).getString();
                    String altName = mapping.getRequiredProperty(LocationMappingVocab.altName).getString();
                    addMapping(name, altName);
                }
                if (mapping.hasProperty(LocationMappingVocab.prefix) && mapping.hasProperty(LocationMappingVocab.altPrefix)) {
                    String prefix = mapping.getRequiredProperty(LocationMappingVocab.prefix).getString();
                    String altPrefix = mapping.getRequiredProperty(LocationMappingVocab.altPrefix).getString();
                    addPrefixMapping(prefix, altPrefix);
                }
            }
        } finally {
            mappings.close();
        }
        return this;
    }

    /**
     * Gets the graph by its ID, which can be ontology id, location (file or uri) or arbitrary identifier,
     * if there is a mapping for it.
     * The method attempts to load the graph if it is not yet in the repository.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph}
     * @throws org.apache.jena.shared.JenaException if graph cannot be loaded
     * @see DocumentGraphRepository#addMapping(String, String)
     */
    @Override
    public Graph get(String id) {
        Objects.requireNonNull(id, "Null Graph Id");
        try {
            var source = idToSource.computeIfAbsent(id, this::toSource);
            return sourceToGraph.computeIfAbsent(source, it -> read(it, factory.get()));
        } catch (JenaException e) {
            idToSource.remove(id);
            throw e;
        }
    }

    /**
     * Lists all graph's identifiers.
     * The result contains exact mappings and prefix-derived IDs already materialized by {@link #get(String)}.
     * It does not enumerate possible IDs covered only by lazy prefix mappings.
     * Note that the number of identifiers may exceed the number of loaded graphs if several IDs map to one source.
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
        excludedFromPrefixMapping.remove(id);
        return sourceToGraph.put(source, graph);
    }

    /**
     * Removes and returns the graph identified by the specified {@code id}, along with all its associations.
     * Prefix mappings themselves are not removed by this method. If the removed ID
     * matches a registered prefix mapping, only that concrete ID is excluded from
     * prefix fallback; other IDs under the same prefix remain resolvable. The removed
     * ID becomes resolvable again after it is re-added explicitly or by adding a
     * matching prefix mapping again.
     * Use {@link #removePrefixMapping(String)} to remove a prefix mapping rule.
     *
     * @param id {@code String} Graph's identifier
     * @return {@link Graph} or {@code null} if the graph is not found
     */
    @Override
    public Graph remove(String id) {
        Objects.requireNonNull(id, "Null Graph Id");
        var source = idToSource.remove(id);
        if (source == null) {
            return null;
        }
        // Do not let a removed exact id reappear immediately through a still-active prefix mapping.
        excludedFromPrefixMapping.add(id);
        idToSource.entrySet().stream().toList().stream()
                .filter(it -> source.equals(it.getValue()))
                .forEach(it -> {
                    idToSource.remove(it.getKey());
                    // Apply the same removal semantics to all aliases of the same document source.
                    excludedFromPrefixMapping.add(it.getKey());
                });
        return sourceToGraph.remove(source);
    }

    /**
     * Removes all graphs.
     */
    @Override
    public void clear() {
        sourceToGraph.clear();
        idToSource.clear();
        idPrefixToLocationPrefix.clear();
        excludedFromPrefixMapping.clear();
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
    public Stream<Graph> loadedGraphs() {
        return sourceToGraph.values().stream().distinct();
    }

    /**
     * Returns {@code true} if a mapping for the specified identifier exists in the repository.
     *
     * @param id {@code String} Graph's identifier
     * @return boolean
     */
    @Override
    public boolean contains(String id) {
        Objects.requireNonNull(id, "Null Graph Id");
        return idToSource.containsKey(id) || !excludedFromPrefixMapping.contains(id) && findPrefixMapping(id) != null;
    }

    protected Set<String> getIds() {
        return idToSource.keySet();
    }

    private void bind(String id, Source source) {
        Source old = idToSource.put(id, source);
        removeGraphIfOrphaned(old);
    }

    private void removeGraphIfOrphaned(Source source) {
        if (source != null && !idToSource.containsValue(source)) {
            sourceToGraph.remove(source);
        }
    }

    private Source toSource(String id) {
        if (excludedFromPrefixMapping.contains(id)) {
            // The id was removed explicitly; keep get(id)'s direct-location fallback, but skip prefix remapping.
            return parseLocation(id);
        }
        String prefix = findPrefixMapping(id);
        if (prefix == null) {
            return parseLocation(id);
        }
        String location = idPrefixToLocationPrefix.get(prefix) + id.substring(prefix.length());
        return parseLocation(location);
    }

    private String findPrefixMapping(String id) {
        String res = null;
        String locationPrefix = null;
        for (String prefix : idPrefixToLocationPrefix.keySet()) {
            if (id.startsWith(prefix)) {
                String candidate = idPrefixToLocationPrefix.get(prefix);
                if (locationPrefix == null || locationPrefix.length() < candidate.length()) {
                    res = prefix;
                    locationPrefix = candidate;
                }
            }
        }
        return res;
    }

    private record Source(String location, Lang lang) {
    }

}
