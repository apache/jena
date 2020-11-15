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

package org.apache.jena.shacl;

import static org.apache.jena.atlas.iterator.Iter.iter;
import static org.apache.jena.sparql.graph.NodeConst.nodeOwlImports;
import static org.apache.jena.sparql.graph.NodeConst.nodeOwlOntology;
import static org.apache.jena.sparql.graph.NodeConst.nodeRDFType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.sparql.graph.GraphFactory;

/**
 * Import processing.
 * <p>
 * Imports are triggered by a base (a single triple "? rdf:type owl:Ontology")
 * and imports (triples "base owl:Imports URI").
 * <p>
 * If there are other "? owl:imports ?" triples, they are ignored.
 */
public class Imports {
    private Imports() {}

    /**
     * Load a graph and process owl:imports to create a new, single graph.
     */
    public static Graph loadWithImports(String url) {
        url = IRIResolver.resolveString(url);
        Graph graph = RDFDataMgr.loadGraph(url);
        return withImportsWorker(url, graph);
    }

    /**
     * Process and return the owl:imports closure of a graph. The graph is included
     * in the results. Note that without knowing the URI, the start graph may be read
     * again if it is named as an import.
     */
    public static Graph withImports(Graph graph) {
        return withImportsWorker(null, graph);
    }

    /**
     * Process and return the owl:imports closure of a graph.
     * The graph is included in the results.
     */
    public static Graph withImports(String url, Graph graph) {
        url = IRIResolver.resolveString(url);
        return withImportsWorker(url, graph);
    }

    private static Graph withImportsWorker(String url, Graph graph) {
        // Partial check for any imports. Are there any imports triples?
        boolean hasImports = G.contains(graph, null, nodeOwlImports, null);
        if ( ! hasImports )
            return graph;
        // Probably some work to do.
        // This is "import self", and start the "visited".
        Graph acc = GraphFactory.createDefaultGraph();
        GraphUtil.addInto(acc, graph);
        Set<String> visited = new HashSet<>();
        if ( url != null )
            visited.add(url);
        processImports(visited, graph, acc);
        return acc;
    }

    /** Carefully traverse the imports, loading graphs. */
    private static void processImports(Set<String> visited, Graph graph, Graph acc) {
        List<Node> imports = imports(graph);
        for ( Node imported : imports ) {
            if ( ! imported.isURI() )
                // Ignore non-URIs.
                continue;
            String uri = imported.getURI();
            if ( ! visited.contains(uri) ) {
                visited.add(uri);
                // Read into a temporary graph to isolate errors.
                try {
                    Graph g2 = RDFDataMgr.loadGraph(uri);
                    GraphUtil.addInto(acc, g2);
                    processImports(visited, g2, acc);
                } catch (RuntimeException ex) {}
            }
        }
    }

    /** Return the imports for a graph */
    public static List<Node> imports(Graph graph) {
        Pair<Node,List<Node>> pair = baseAndImports(graph);
        return pair.getRight();
    }

    /**
     * Locate the base (a single triple ? rdf:type owl:Ontology)
     * and imports (triples "base owl:Imports URI").
     * Returns a Pair of (null,EmptyList) for no base.
     */
    public static Pair<Node,List<Node>> baseAndImports(Graph graph) {
        Node base = G.getZeroOrOnePO(graph, nodeRDFType, nodeOwlOntology);
        if ( base == null )
            return Pair.create(null, Collections.emptyList());
        List<Node> imports = allImports(base, graph);
        return Pair.create(base, imports);
    }

    /**
     * Locate the base (a single triple ? rdf:type owl:Ontology).
     * If none or more than one matching triple, then return null.
     */
    public static Node base(Graph graph) {
        // Filter for URI?
        return G.getZeroOrOnePO(graph, nodeRDFType, nodeOwlOntology);
    }

    /**
     * Locate any imports (triples "base owl:Imports URI").
     * Base may be a wildcard indicating "any owl:imports".
     */
    public static List<Node> allImports(Node base, Graph graph) {
        List<Node> imports = iter(G.listSP(graph, base, nodeOwlImports)).filter(Node::isURI).collect(Collectors.toList());
        return imports;
    }
}

