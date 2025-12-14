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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.irix.IRIs;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.streammgr.LocationMapper;
import org.apache.jena.riot.system.streammgr.StreamManager;
import org.apache.jena.shacl.sys.ShaclSystem;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.system.G;
import org.apache.jena.system.RDFDataException;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Import processing.
 * <p>
 * Imports are triggered by a base (a single triple "? rdf:type owl:Ontology") and
 * imports (triples "base owl:Imports URI").
 * <p>
 * If there are other "? owl:imports ?" triples, they are ignored.
 */
public class Imports {

    public static Logger importsLogger = ShaclSystem.shaclSystemLogger;

    private Imports() {}

    /**
     * Load a graph and process owl:imports to create a new, single graph.
     */
    public static Graph loadWithImports(String url) {
        url = IRIs.resolve(url);
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
     * Process and return the owl:imports closure of a graph. The graph is included
     * in the results.
     */
    public static Graph withImports(String url, Graph graph) {
        url = IRIs.resolve(url);
        return withImportsWorker(url, graph);
    }

    private static Graph withImportsWorker(String url, Graph graph) {
        // Partial check for any imports. Are there any imports triples?
        boolean hasImports = G.contains(graph, null, nodeOwlImports, null);
        if ( !hasImports )
            return graph;
        if ( importsLogger.isDebugEnabled() ) {
            importsLogger.debug("Imports");
        }
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
            if ( !imported.isURI() )
                // Ignore non-URIs.
                continue;
            String uri = imported.getURI();
            if ( importsLogger.isDebugEnabled() )
                importsLogger.debug("Import: " + uri);

            // FmtLog.info(Imports.class, "Import: %s", uri);
            if ( visited.contains(uri) ) {
                if ( importsLogger.isDebugEnabled() )
                    importsLogger.debug("Skipped: " + uri);
                continue;
            }
            visited.add(uri);
            // Read into a temporary graph to isolate errors.
            Graph g2 = loadOneGraph(uri);
            GraphUtil.addInto(acc, g2);
            processImports(visited, g2, acc);
        }
    }

    private static final LocationMapper mapSHACL = new LocationMapper();
    static {
        // Inclusion in this list does not imply the shapes file is parseable or actually works!
        mapSHACL.addAltEntry("http://topbraid.org/tosh",    "http://topbraid.org/tosh.ttl");
        mapSHACL.addAltEntry("http://datashapes.org/dash",  "http://datashapes.org/dash.ttl");
        mapSHACL.addAltEntry("https://topbraid.org/tosh",   "https://topbraid.org/tosh.ttl");
        mapSHACL.addAltEntry("https://datashapes.org/dash", "https://datashapes.org/dash.ttl");
        //mapSHACL.addAltEntry("http://www.w3.org/ns/shacl",  "https://www.w3.org/ns/shacl");
    }

    public static StreamManager shaclImportsStreamManager = StreamManager.get().clone();
    static {
        if ( ! mapSHACL.isEmpty() )
            shaclImportsStreamManager.locationMapper(mapSHACL.clone());
    }

    private static Graph loadOneGraph(String uriOrFile) {
        try {
            return RDFParser.source(uriOrFile)
                    .streamManager(shaclImportsStreamManager)
                    .toGraph();
        } catch (HttpException ex) {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                FmtLog.error(importsLogger, "Not found: %s", uriOrFile);
            else
                FmtLog.error(importsLogger, "HTTP exception: " + ex.getMessage());
            throw ex;
        } catch (RiotParseException ex) {
            FmtLog.error(importsLogger, "Parse error reading '%s': %s", uriOrFile, ex.getMessage());
            throw ex;
        }
    }

    /** Return the imports for a graph */
    public static List<Node> imports(Graph graph) {
        Pair<Node, List<Node>> pair = baseAndImports(graph);
        return pair.getRight();
    }

    /**
     * Locate the base (a single triple ? rdf:type owl:Ontology) and imports (triples
     * "base owl:Imports URI"). May return null for the base in which case all
     * imports are returned.
     */
    public static Pair<Node, List<Node>> baseAndImports(Graph graph) {
        Node base = null;
        if ( G.containsOne(graph, null, nodeRDFType, nodeOwlOntology) ) {
            base = G.getOnePO(graph, nodeRDFType, nodeOwlOntology);
        }
        List<Node> imports = allImports(base, graph);
        return Pair.create(base, imports);
    }

    /**
     * Locate the base (a single triple ? rdf:type owl:Ontology). If none or more
     * than one matching triple, then return null.
     */
    public static Node base(Graph graph) {
        // Filter for URI?
        try {
            return G.getZeroOrOnePO(graph, nodeRDFType, nodeOwlOntology);
        } catch (RDFDataException ex) {
            return null;
        }
    }

    /**
     * Locate any imports (triples "base owl:Imports URI"). Base may be a wildcard
     * indicating "any owl:imports".
     */
    public static List<Node> allImports(Node base, Graph graph) {
        List<Node> imports = iter(G.listSP(graph, base, nodeOwlImports)).filter(Node::isURI).toList();
        return imports;
    }
}
