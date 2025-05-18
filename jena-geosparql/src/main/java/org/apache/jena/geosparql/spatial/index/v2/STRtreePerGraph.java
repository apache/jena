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
package org.apache.jena.geosparql.spatial.index.v2;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class STRtreePerGraph {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Mapping of graph node to STRtree.
     *
     * @implNote
     *   The STRtree that corresponds to the default graph (referred to as 'default tree')
     *   uses the name {@link Quad#defaultGraphIRI}.
     *   The default tree is maintained as part of the treeMap (rather than as a separate field)
     *   to simplify merging of partial spatial indexes.
     */
    private Map<Node, STRtree> treeMap;

    /** Unmodifiable view of treeMap */
    private Map<Node, STRtree> treeMapView;

    private boolean isBuilt = false;

    public STRtreePerGraph() {
        this(new ConcurrentHashMap<>());
    }

    public STRtreePerGraph(STRtree defaultTree) {
        this(new ConcurrentHashMap<>());
        Objects.requireNonNull(defaultTree);
        setDefaultTree(defaultTree);
    }

    protected STRtreePerGraph(Map<Node, STRtree> treeMap) {
        super();
        this.treeMap = Objects.requireNonNull(treeMap);
        this.treeMapView = Collections.unmodifiableMap(this.treeMap);
    }

    public STRtree getDefaultTree() {
        return treeMap.get(Quad.defaultGraphIRI);
    }

    /** Returns an unmodifiable view of the tree map. */
    public Map<Node, STRtree> getTreeMap() {
        return treeMapView;
    }

    /** For serialization. */
    Map<Node, STRtree> getInternalTreeMap() {
        return treeMap;
    }

    /** Whether a tree with the given name exists. Handles default tree names. */
    public boolean contains(Node name) {
        return getTree(name) != null;
    }

    /** Whether a tree with the given name exists. Handles default tree names. */
    public STRtree getTree(Node name) {
        STRtree result = (name == null || Quad.isDefaultGraph(name))
            ? treeMap.get(Quad.defaultGraphIRI)
            : treeMap.get(name);
        return result;
    }

    /** Whether a tree with the given name exists. Handles default tree names. */
    public STRtree setTree(Node name, STRtree tree) {
        STRtree result;
        if (name == null || Quad.isDefaultGraph(name)) {
            result = setDefaultTree(tree);
        } else {
            result = setNamedTree(name, tree);
        }
        return result;
    }

    /** Returns the prior default tree. */
    public STRtree setDefaultTree(STRtree tree) {
        return setNamedTree(Quad.defaultGraphIRI, tree);
    }

    /** Returns the prior tree of graphNode. */
    protected STRtree setNamedTree(Node graphNode, STRtree tree) {
        Objects.requireNonNull(graphNode); // Default graph must name must be Quad.defaultGraphIRI.
        return treeMap.put(graphNode, tree);
    }

    /** Add all data of 'other' to this. Builds the added trees if {@link #isBuilt()} is true. */
    public void setTrees(Map<Node, STRtree> treeMap) {
        // If the index is already built then build all trees being added.
        if (isBuilt()) {
            treeMap.values().forEach(STRtree::build);
        }
        this.treeMap.putAll(treeMap);
    }

    public boolean removeTree(Node node) {
        if (node == null) {
            node = Quad.defaultGraphIRI;
        }
        return treeMap.remove(node) != null;
    }

    @SuppressWarnings("unchecked")
    public Collection<Node> queryOneGraph(Envelope searchEnvelope, Node graph) {
        Collection<Node> result;
        if (graph == null || Quad.isDefaultGraph(graph)) {
            // Handle default graph.
            STRtree defaultTree = getDefaultTree();
            result = defaultTree == null
                ? Set.of()
                : new LinkedHashSet<>(defaultTree.query(searchEnvelope));
        } else if (Quad.isUnionGraph(graph)) {
            // Handle union graph (avoid).
            LOGGER.warn("spatial index lookup on union graph");
            result = treeMap.entrySet().stream()
                .filter(e -> !Quad.isDefaultGraph(e.getKey())) // Exclude default graph.
                .map(Entry::getValue)
                .map(tree -> tree.query(searchEnvelope))
                .collect(LinkedHashSet::new,
                    Set::addAll,
                    Set::addAll);
        } else {
            // Handle specific named graph.
            STRtree tree = treeMap.get(graph);
            if (tree == null) {
                LOGGER.warn("graph not indexed: " + graph);
            }
            if (tree != null && !tree.isEmpty()) {
                result = new LinkedHashSet<>(tree.query(searchEnvelope));
            } else {
                result = new HashSet<>();
            }
        }
        return result;
    }

    public boolean isEmpty() {
        boolean result = treeMap.values().stream().allMatch(STRtree::isEmpty);
        return result;
    }

    public void build() {
        if (!isBuilt) {
            treeMap.values().forEach(STRtree::build);
            isBuilt = true;
        }
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public long size() {
        long result = treeMap.values().stream().mapToLong(STRtree::size).sum();
        return result;
    }
}
