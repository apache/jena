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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
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

    private STRtree defaultTree;
    private Map<Node, STRtree> namedTreeMap;

    // XXX Make srsInfo part of this class?
    // this.srsInfo = SRSRegistry.getSRSInfo(SRS_URI.DEFAULT_WKT_CRS84);

    private boolean isBuilt = false;

    public STRtreePerGraph() {
        this(new STRtree(STRtreeUtils.MINIMUM_CAPACITY), new ConcurrentHashMap<>());
    }

    public STRtreePerGraph(STRtree defaultTree) {
        this(defaultTree, new ConcurrentHashMap<>());
    }

    public STRtreePerGraph(STRtree defaultTree, Map<Node, STRtree> namedTreeMap) {
        super();
        this.defaultTree = Objects.requireNonNull(defaultTree);
        this.namedTreeMap = Objects.requireNonNull(namedTreeMap);
    }

    public STRtree getDefaultTree() {
        return defaultTree;
    }

    public Map<Node, STRtree> getNamedTreeMap() {
        return namedTreeMap;
    }

    /** Whether a tree with the given name exists. Handles default tree names. */
    public boolean contains(Node name) {
        return getTree(name) != null;
    }

    /** Whether a tree with the given name exists. Handles default tree names. */
    public STRtree getTree(Node name) {
        STRtree result = (name == null || Quad.isDefaultGraph(name))
            ? defaultTree
            : namedTreeMap.get(name);
        return result;
    }

    /** Whether a tree with the given name exists. Handles default tree names. */
    public STRtree setTree(Node name, STRtree tree) {
        STRtree result;
        if (name == null || Quad.isDefaultGraph(name)) {
            result = this.defaultTree;
            this.defaultTree = tree;
        } else {
            result = setNamedTree(name, tree);
        }
        return result;
    }

    /** Returns the prior default tree. */
    public STRtree setDefaultTree(STRtree tree) {
        STRtree result = defaultTree;
        this.defaultTree = tree;
        return result;
    }

    /** Returns the prior tree of graphNode. */
    public STRtree setNamedTree(Node graphNode, STRtree tree) {
        return namedTreeMap.put(graphNode, tree);
    }

    @SuppressWarnings("unchecked")
    public Collection<Node> queryOneGraph(Envelope searchEnvelope, Node graph) {
        // LOGGER.debug("spatial index lookup on graph: " + graph);

        Collection<Node> result;

        // handle union graph
        if (graph == null || Quad.isDefaultGraph(graph)) {
            result = new LinkedHashSet<>(defaultTree.query(searchEnvelope));
        } else if (Quad.isUnionGraph(graph)) {
            LOGGER.warn("spatial index lookup on union graph");
            result = namedTreeMap.values().stream()
                .map(tree -> tree.query(searchEnvelope))
                .collect(LinkedHashSet::new,
                    Set::addAll,
                    Set::addAll);
        } else {
            STRtree tree = namedTreeMap.get(graph);
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
        boolean result = defaultTree.isEmpty()
            || namedTreeMap.values().stream().allMatch(STRtree::isEmpty);
        return result;
    }

    public void build() {
        if (!isBuilt) {
            defaultTree.build();
            namedTreeMap.values().forEach(STRtree::build);
            isBuilt = true;
        }
    }

    public long size() {
        long result = defaultTree.size()
            + namedTreeMap.values().stream().mapToLong(STRtree::size).sum();
        return result;
    }
}
