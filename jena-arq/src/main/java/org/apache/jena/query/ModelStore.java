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

package org.apache.jena.query;

import java.net.http.HttpClient;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.http.GSP;

/**
 * Client for the
 * <a href="https://www.w3.org/TR/sparql11-http-rdf-update/"
 * >SPARQL 1.1 Graph Store Protocol</a>
 * working at the Model/Resource API level.
 * <p>
 * This is extended to include operations GET, POST and PUT on RDF Datasets.
 * <p>
 * Examples:
 * <pre>
 *   // Get the default graph.
 *   Model model = ModelStore.service("http://example/dataset").defaultModel().GET();
 * </pre>
 * <pre>
 *   // Get a named graph.
 *   Model model = ModelStore.service("http://example/dataset").namedGraph("http://my/graph").GET();
 * </pre>
 * <pre>
 *   // POST (add) to a named graph.
 *   Model myData = ...;
 *   ModelStore.request("http://example/dataset").namedGraph("http://my/graph").POST(myData);
 * </pre>
 *
 * @see GSP
 */
public class ModelStore {

    /**
     * Create a request to the remote service (without Graph Store Protocol naming).
     * Call {@link #defaultModel()} or {@link #namedGraph(String)} to select the target graph.
     * @param service
     */
    public static ModelStore service(String service) {
        return new ModelStore().endpoint(service);
    }

    private final GSP gsp = GSP.request();
    protected final GSP gsp() { return gsp; }

    protected ModelStore() {}

    /**
     * Set the URL of the query endpoint. This replaces any value set in the
     * {@link #service(String)} call.
     */
    public ModelStore endpoint(String serviceURL) {
        gsp().endpoint(serviceURL);
        return this;
    }

    public ModelStore httpClient(HttpClient httpClient) {
        gsp().httpClient(httpClient);
        return this;
    }

    /**
     * Set an HTTP header that is added to the request.
     * See {@link #accept}, {@link #acceptHeader} and {@link #contentType(RDFFormat)}.
     * for specific handling of {@code Accept:} and {@code Content-Type}.
     */
    public ModelStore httpHeader(String headerName, String headerValue) {
        gsp().httpHeader(headerName, headerValue);
        return this;
    }

    /** Send request for a named graph (that is, {@code ?graph=}) */
    public ModelStore namedGraph(String graphName) {
        gsp().graphName(graphName);
        return this;
    }

    /** Send request for the default graph (that is, {@code ?default}) */
    public ModelStore defaultModel() {
        gsp().defaultGraph();
        return this;
    }

    /** Send request for the dataset. This is "no GSP naming". */
    public ModelStore dataset() {
        gsp().dataset();
        return this;
    }

    /** Set the accept header on GET requests. Optional; if not set, a system default is used. */
    public ModelStore acceptHeader(String acceptHeader) {
        gsp().acceptHeader(acceptHeader);
        return this;
    }

    /** Set the accept header on GET requests. Optional; if not set, a system default is used. */
    public ModelStore accept(Lang lang) {
        gsp().accept(lang);
        return this;
    }

    /**
     * Set the Content-type for a POST, PUT request of a file
     * or serialization of a graph opf dataset is necessary.
     * Optional; if not set, the file extension is used or the
     * system default RDF syntax encoding.
     */
    public ModelStore contentTypeHeader(String contentType) {
        gsp().contentTypeHeader(contentType);
        return this;
    }

    /**
     * Set the Content-type for a POST, PUT request of a file
     * or serialization of a graph opf dataset is necessary.
     * Optional; if not set, the file extension is used or the
     * system default RDF syntax encoding.
     */
    public ModelStore contentType(RDFFormat rdfFormat) {
        gsp().contentType(rdfFormat);
        return this;
    }

    /** Get a graph */
    public Model GET() {
        Graph graph = gsp().GET();
        Model model = ModelFactory.createModelForGraph(graph);
        return model;
    }

//    /**
//     * Fetch a model.
//     * <p>
//     * Synonym for {@link #GET()}.
//     */
//    public Model getModel() {
//        // Synonym
//        return GET();
//    }

    /**
     * POST the contents of a file using the filename extension to determine the
     * Content-Type to use if it is not already set.
     * <p>
     * This operation does not parse the file.
     */
    public void POST(String file) {
        gsp().POST(file);
    }

//    /**
//     * Add the contents of a file into the target graph using the filename extension to determine the
//     * Content-Type to use if it is not already set.
//     * <p>
//     * Synonym for {@link #POST(String)}.
//     * <p>
//     * This operation does not parse the file.
//     * <p>
//     * If the data may have quads (named graphs), use {@link #postDataset(String)}.
//     *
//     */
//    public void postModel(String file) {
//        POST(file);
//    }

    /** POST a model. */
    public void POST(Model model) {
        gsp().POST(model.getGraph());
    }

//    /**
//     * POST a model.
//     * <p>
//     * Synonym for {@link #POST(Model)}.
//     */
//    public void postModel(Model model) {
//        // Synonym
//        POST(model);
//    }

    /**
     * PUT the contents of a file using the filename extension to determine the
     * Content-Type to use if it is not already set.
     * <p>
     * This operation does not parse the file.
     * <p>
     * If the data may have quads (named graphs), use {@link #putDataset(String)}.
     */
    public void PUT(String file) {
        gsp().PUT(file);
    }

//    /**
//     * Replace the remote model with the contents of a file using the filename extension to determine the
//     * Content-Type to use if it is not already set.
//     * <p>
//     * Synonym for {@link #PUT(String)}.
//     * <p>
//     * This operation does not parse the file.
//     * <p>
//     * If the data may have quads (named graphs), use {@link #putDataset(String)}.
//     */
//    public void putModel(String file) {
//        // Synonym
//        PUT(file);
//    }

    /**
     * PUT a graph.
     */
    public void PUT(Model model) {
        gsp().PUT(model.getGraph());
    }

//    /**
//     * Put a model - replace the previous contents.
//     * <p>
//     * Synonym for {@link #PUT(Model)}.
//     */
//    public void putModel(Model model) {
//        // Synonym
//        PUT(graph);
//    }

    /** Delete a graph. */
    public void DELETE() {
        gsp().DELETE();
    }


    /**
     * GET dataset.
     * <p>
     * If the remote end is a graph, the result is a dataset with that
     * graph data in the default graph of the dataset.
     */
    public Dataset getDataset() {
        DatasetGraph dsg = gsp().getDataset();
        return DatasetFactory.wrap(dsg);
    }

    /**
     * POST the contents of a file using the filename extension to determine the
     * Content-Type to use if not already set.
     * <p>
     * This operation does not parse the file.
     */
    public void postDataset(String file) {
        gsp().postDataset(file);
    }

    /** POST a dataset */
    public void postDataset(Dataset dataset) {
        gsp().postDataset(dataset.asDatasetGraph());
    }

    /**
     * PUT the contents of a file using the filename extension to determine the
     * Content-Type to use if not already set.
     * <p>
     * This operation does not parse the file.
     */
    public void putDataset(String file) {
        gsp().putDataset(file);
    }

    /** PUT a dataset */
    public void putDataset(Dataset dataset) {
        gsp().putDataset(dataset.asDatasetGraph());
    }

    /** Clear - delete named graphs, empty the default graph - SPARQL "CLEAR ALL" */
    public void clearDataset() {
        gsp().clearDataset();
    }
}
