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

import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.http.Push;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphZero;

/**
 * Client for dataset operations over HTTP.
 * <p> for graph operations, see {@link GSP} which is the client-side of
 * <a href="https://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store Protocol</a>.
 * <p>
 * This class provided GET, POST, PUT and clear() on datasets.
 * DELETE is not supported. HTTP DELETE means "remove resource", not "clear resource".
 * <p>
 * Examples:
 * <pre>
 *   // Get the dataset.
 *   DatasetGraph graph = DSP.service("http://example/dataset").GET();
 * </pre>
 * <pre>
 *   // POST (add) to a dataset
 *   DatasetGraph myData = ...;
 *   GSP.service("http://example/dataset").POST(myData);
 * </pre>
 */
public class DSP extends StoreProtocol<DSP>{
    /**
     * Create a request to the remote service.
     */
    public static DSP service(String service) {
        return new DSP().endpoint(service);
    }

    /**
     * Create a request to the remote service (without GSP naming).
     * Call {@link #endpoint} to set the target.
     */
    public static DSP request() {
        return new DSP();
    }

    protected DSP() {}

    @Override
    protected DSP thisBuilder() { return this; }

    /**
     * GET dataset.
     * <p>
     * If the remote end is a graph, the result is a dataset with that
     * graph data in the default graph of the dataset.
     */
    public DatasetGraph GET() {
        ensureAcceptHeader(WebContent.defaultRDFAcceptHeader);
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
        HttpRDF.httpGetToStream(hc, serviceEndpoint, httpHeaders, StreamRDFLib.dataset(dsg));
        return dsg;
    }

    /**
     * POST the contents of a file using the filename extension to determine the
     * Content-Type to use if not already set.
     * <p>
     * This operation does not parse the file.
     */
    public void POST(String file) {
        String fileExtContentType = contentTypeFromFilename(file);
        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
        uploadQuads(hc, serviceEndpoint, file, fileExtContentType, httpHeaders, Push.POST);
    }

    /** POST a dataset */
    public void POST(DatasetGraph dataset) {
        RDFFormat requestFmt = rdfFormat(HttpEnv.defaultQuadsFormat);
        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
        HttpRDF.httpPostDataset(hc, serviceEndpoint, dataset, requestFmt, httpHeaders);
    }

    /**
     * PUT the contents of a file using the filename extension to determine the
     * Content-Type to use if not already set.
     * <p>
     * This operation does not parse the file.
     */
    public void PUT(String file) {
        String fileExtContentType = contentTypeFromFilename(file);
        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
        uploadQuads(hc, serviceEndpoint, file, fileExtContentType, httpHeaders, Push.PUT);
    }

    /** PUT a dataset */
    public void PUT(DatasetGraph dataset) {
        RDFFormat requestFmt = rdfFormat(HttpEnv.defaultQuadsFormat);
        HttpClient hc = requestHttpClient(serviceEndpoint, serviceEndpoint);
        HttpRDF.httpPutDataset(hc, serviceEndpoint, dataset, requestFmt, httpHeaders);
    }

    private static DatasetGraph emptyDSG = DatasetGraphZero.create();
    /** Clear - delete named graphs, empty the default graph - similar to SPARQL "CLEAR ALL" */
    public void clear() {
        // Without relying on SPARQL Update.
        PUT(emptyDSG);
    }

    /**
     * Send a file of quads to a URL. The Content-Type is inferred from the file
     * extension.
     */
    private static void uploadQuads(HttpClient httpClient, String endpoint, String file, String fileExtContentType, Map<String, String> headers, Push mode) {
        Lang lang = RDFLanguages.contentTypeToLang(fileExtContentType);
        if ( !RDFLanguages.isQuads(lang) && !RDFLanguages.isTriples(lang) )
            throw new ARQException("Not an RDF format: " + file + " (lang=" + lang + ")");
        pushFile(httpClient, endpoint, file, fileExtContentType, headers, mode);
    }
}
