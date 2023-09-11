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

package org.apache.jena.sparql.exec.http;

import java.net.http.HttpClient;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.http.Push;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.graph.GraphFactory;

/**
 * Client for the
 * <a href="https://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store Protocol</a>.
 * <p>
 * This is extended to include operations GET, POST and PUT on datasets.
 * <p>
 * Examples:
 * <pre>
 *   // Get the default graph.
 *   Graph graph = GSP.service("http://example/dataset").defaultGraph().GET();
 * </pre>
 * <pre>
 *   // Get a named graph.
 *   Graph graph = GSP.service("http://example/dataset").namedGraph("http://my/graph").GET();
 * </pre>
 * <pre>
 *   // POST (add) to a named graph.
 *   Graph myData = ...;
 *   GSP.service("http://example/dataset").namedGraph("http://my/graph").POST(myData);
 * </pre>
 * <p>
 * See {@link DSP} for operations on datasets.
 */
public class GSP extends StoreProtocol<GSP> {

    // One, and only one of these two, must be set at the point the terminating operation is called.
    // 1 - Graph operation, GSP naming, default graph
    private boolean             defaultGraph    = false;
    // 2 - Graph operation, GSP naming, graph name.
    private String              graphName       = null;

    // Legacy, deprecated.
    // 3 - Dataset operation without ?default or ?graph=
    private boolean             datasetGraph    = false;

    /**
     * Create a request to the remote serviceURL (without a URL query string).
     * Call {@link #defaultGraph()} or {@link #graphName(String)} to select the target graph.
     * See {@link DSP} for dataset operations.
     * @param service
     */
    public static GSP service(String service) {
        return new GSP().endpoint(service);
    }

    /**
     * Create a request to the remote service (without GSP naming).
     * Call {@link #endpoint} to set the target.
     * Call {@link #defaultGraph()} or {@link #graphName(String)} to select the target graph.
     * See {@link DSP} for dataset operations.
     */
    public static GSP request() {
        return new GSP();
    }

    protected GSP() {}

    @Override
    protected GSP thisBuilder() { return this; }

    /** Send request for a named graph (that is, {@code ?graph=}) */
    public GSP graphName(String graphName) {
        Objects.requireNonNull(graphName);
        this.graphName = graphName;
        this.defaultGraph = false;
        return this;
    }

    /** Send request for a named graph (that is, {@code ?graph=}) */
    public GSP graphName(Node graphName) {
        Objects.requireNonNull(graphName);
        clearOperation();
        if ( ! graphName.isURI() && ! graphName.isBlank() )
            throw exception("Not an acceptable graph name: "+this.graphName);
        Node gn = RiotLib.blankNodeToIri(graphName);
        this.graphName = gn.getURI();
        this.defaultGraph = false;
        return this;
    }

    /** Send request for the default graph (that is, {@code ?default}) */
    public GSP defaultGraph() {
        clearOperation();
        this.defaultGraph = true;
        return this;
    }

    private void clearOperation() {
        this.defaultGraph = false;
        this.graphName = null;
    }

    final protected void validateGraphOperation() {
        Objects.requireNonNull("Service Endpoint", serviceEndpoint);
        if ( ! defaultGraph && graphName == null )
            throw exception("Need either default graph or a graph name");
    }

    // Synonyms mirror the dataset names, so getGraph/getDataset

    /** Get a graph */
    public Graph GET() {
        validateGraphOperation();
        ensureAcceptHeader(WebContent.defaultGraphAcceptHeader);
        String url = graphRequestURL();
        Graph graph = GraphFactory.createDefaultGraph();
        HttpClient hc = requestHttpClient(serviceEndpoint, url);
        HttpRDF.httpGetToStream(hc, url, httpHeaders, StreamRDFLib.graph(graph));
        return graph;
    }

//    /**
//     * Get a graph.
//     * <p>
//     * Synonym for {@link #GET()}.
//     */
//    public Graph getGraph() {
//        // Synonym
//        return GET();
//    }

    /**
     * POST the contents of a file using the filename extension to determine the
     * Content-Type to use if it is not already set.
     * <p>
     * This operation does not parse the file.
     * <p>
     * If the data may have quads (named graphs), use {@link DSP#POST(String)}.
     */
    public void POST(String file) {
        validateGraphOperation();
        String url = graphRequestURL();
        String fileExtContentType = contentTypeFromFilename(file);
        HttpClient hc = requestHttpClient(serviceEndpoint, url);
        uploadTriples(hc, url, file, fileExtContentType, httpHeaders, Push.POST);
    }

//    /**
//     * Load the contents of a file into the target graph using the filename extension to determine the
//     * Content-Type to use if it is not already set.
//     * <p>
//     * Synonym for {@link #POST(String)}.
//     * <p>
//     * This operation does not parse the file.
//     * <p>
//     * If the data may have quads (named graphs), use {@link #postDataset(String)}.
//     *
//     */
//    public void postGraph(String file) {
//        POST(file);
//    }

    /** POST a graph. */
    public void POST(Graph graph) {
        validateGraphOperation();
        RDFFormat requestFmt = rdfFormat(HttpEnv.defaultTriplesFormat);
        String url = graphRequestURL();
        HttpClient hc = requestHttpClient(serviceEndpoint, url);
        HttpRDF.httpPostGraph(hc, url, graph, requestFmt, httpHeaders);
    }

//    /**
//     * POST a graph.
//     * <p>
//     * Synonym for {@link #POST(Graph)}.
//     */
//    public void postGraph(Graph graph) {
//        // Synonym
//        POST(graph);
//    }

    /**
     * PUT the contents of a file using the filename extension to determine the
     * Content-Type to use if it is not already set.
     * <p>
     * This operation does not parse the file.
     * <p>
     * If the data may have quads (named graphs), use {@link DSP#PUT(String)}.
     */
    public void PUT(String file) {
        validateGraphOperation();
        String url = graphRequestURL();
        String fileExtContentType = contentTypeFromFilename(file);
        HttpClient hc = requestHttpClient(serviceEndpoint, url);
        uploadTriples(hc, url, file, fileExtContentType, httpHeaders, Push.PUT);
    }

//    /**
//     * PUT the contents of a file using the filename extension to determine the
//     * Content-Type to use if it is not already set.
//     * <p>
//     * Synonym for {@link #PUT(String)}.
//     * <p>
//     * This operation does not parse the file.
//     * <p>
//     * If the data may have quads (named graphs), use {@link #putDataset(String)}.
//     */
//    public void putGraph(String file) {
//        // Synonym
//        PUT(file);
//    }

    /**
     * PUT a graph.
     */
    public void PUT(Graph graph) {
        validateGraphOperation();
        RDFFormat requestFmt = rdfFormat(HttpEnv.defaultTriplesFormat);
        String url = graphRequestURL();
        HttpClient hc = requestHttpClient(serviceEndpoint, url);
        HttpRDF.httpPutGraph(hc, url, graph, requestFmt, httpHeaders);
    }

//    /**
//     * Put a graph - replace the previous contents.
//     * <p>
//     * Synonym for {@link #PUT(Graph)}.
//     */
//    public void putGraph(Graph graph) {
//        // Synonym
//        PUT(graph);
//    }

    /** Delete a graph. */
    public void DELETE() {
        validateGraphOperation();
        String url = graphRequestURL();
        HttpClient hc = requestHttpClient(serviceEndpoint, url);
        HttpRDF.httpDeleteGraph(hc, url);
    }

//    /**
//     * Delete a graph.
//     * <p>
//     * Synonym for {@link #DELETE()}.
//     */
//    public void deleteGraph() {
//        // Synonym
//        DELETE();
//    }

    /**
     * Return the query string for a graph using the
     * <a href="https://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store Protocol</a>.
     * The {@code graphName} be a valid, absolute URI (i.e. includes the scheme)
     * or the word "default", or null, for the default graph of the store
     * or the word "union" for the union graph of the store (this is a Jena extension).
     * or the word "none" for graph operation but no naming. (this is a Jena extension).
     * @param graphStore
     * @param graphName
     * @return String without the "?"
     */

    protected static String queryStringForGraph(String graphName) {
        if ( graphName == null )
            return HttpNames.paramGraphDefault;
        switch (graphName) {
            case HttpNames.graphTargetDefault:
                return HttpNames.paramGraphDefault;
            case HttpNames.graphTargetUnion:
                return HttpNames.paramGraph+"=union";
            default:
                return HttpNames.paramGraph+"="+HttpLib.urlEncodeQueryString(graphName);
        }
    }

    // Expose access for subclasses. "final" to ensure that this class controls constraints and expectations.
    // Only valid when the request has been correctly setup.

    final protected String graphName()           { return graphName; }
    final protected boolean isDefaultGraph()     { return graphName == null; }
    final protected boolean isGraphOperation()   { return defaultGraph || graphName != null; }

    private String graphRequestURL() {
        return HttpLib.requestURL(serviceEndpoint, queryStringForGraph(graphName));
    }

    final protected void internalDataset() {
        // Set as dataset request.
        // Checking is done by validateDatasetOperation.
        // The dataset operations have "Dataset" in the name, so less point having
        // required dataset(). We can't use GET() because the return type
        // would be "Graph or DatasetGraph"
        // Reconsider if graph synonyms provided.
        this.datasetGraph = true;
    }

    final protected void validateDatasetOperation() {
        Objects.requireNonNull("Service Endpoint", serviceEndpoint);
        if ( defaultGraph )
            throw exception("Default graph specified for dataset operation");
        if ( graphName != null )
            throw exception("A graph name specified for dataset operation");
        if ( ! datasetGraph )
            throw exception("Dataset request not specified for dataset operation");
    }

    /** Send a file of triples to a URL. */
    private static void uploadTriples(HttpClient httpClient, String gspUrl, String file, String fileExtContentType,
                                      Map<String, String> headers, Push mode) {
        Lang lang = RDFLanguages.contentTypeToLang(fileExtContentType);
        if ( lang == null )
            throw new ARQException("Not a recognized as an RDF format: "+fileExtContentType);
        if ( RDFLanguages.isQuads(lang) && ! RDFLanguages.isTriples(lang) )
            throw new ARQException("Can't load quads into a graph");
        if ( ! RDFLanguages.isTriples(lang) )
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")");
        pushFile(httpClient, gspUrl, file, fileExtContentType, headers, mode);
    }
}
