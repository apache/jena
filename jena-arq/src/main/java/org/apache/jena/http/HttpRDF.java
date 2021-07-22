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

package org.apache.jena.http;

import static org.apache.jena.http.HttpLib.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.graph.GraphFactory;

/**
 * A collection of convenience operations for  HTTP level operations
 * for RDF related tasks. This does not include GSP naming
 * which is in {@link GSP}.
 *
 * See also {@link AsyncHttpRDF}.
 */
public class HttpRDF {

    // ---- GET
    /**
     * GET a graph from a URL
     *
     * @throws HttpException
     */
    public static Graph httpGetGraph(String url) {
        return httpGetGraph(HttpEnv.getDftHttpClient(), url);
    }

    /**
     * GET a graph from a URL using the provided "Accept" header.
     *
     * @throws HttpException
     */
    public static Graph httpGetGraph(String url, String acceptHeader) {
        return httpGetGraph(HttpEnv.getDftHttpClient(), url, acceptHeader);
    }

    /**
     * GET a graph from a URL using the {@link HttpClient} provided.
     *
     * @throws HttpException
     */
    public static Graph httpGetGraph(HttpClient httpClient, String url) {
        Graph graph = GraphFactory.createDefaultGraph();
        httpGetToStream(httpClient, url,  WebContent.defaultGraphAcceptHeader, StreamRDFLib.graph(graph));
        return graph;
    }

    /**
     * GET a graph from a URL using the {@link HttpClient} provided
     * and the "Accept" header.
     *
     * @throws HttpException
     */
    public static Graph httpGetGraph(HttpClient httpClient, String url, String acceptHeader) {
        Graph graph = GraphFactory.createDefaultGraph();
        httpGetToStream(httpClient, url, acceptHeader, StreamRDFLib.graph(graph));
        return graph;
    }

    /**
     * Send the RDF data from the resource at the URL to the StreamRDF.
     * Beware of parse errors!
     * @throws HttpException
     */
    public static void httpGetToStream(String url, String acceptHeader, StreamRDF dest) {
        httpGetToStream(HttpEnv.getDftHttpClient(), url, acceptHeader, dest);
    }

    /**
     * Read the RDF data from the resource at the URL and send to the StreamRDF.
     * <p>
     * Beware of parse errors!
     * @throws HttpException
     * @throws RiotException
     */
    public static void httpGetToStream(HttpClient client, String url, String acceptHeader, StreamRDF dest) {
        if ( acceptHeader == null )
            acceptHeader = "*/*";
        httpGetToStream(client, url, HttpLib.setAcceptHeader(acceptHeader), dest);
    }

    /**
     * Read the RDF data from the resource at the URL and send to the StreamRDF.
     * <p>
     * Beware of parse errors!
     * @throws HttpException
     * @throws RiotException
     */
    public static void httpGetToStream(HttpClient client, String url, Map<String, String> headers, StreamRDF dest) {
        httpGetToStream(client, url, HttpLib.setHeaders(headers), dest);
    }

    // Worker
    private static void httpGetToStream(HttpClient client, String url, Consumer<HttpRequest.Builder> modifier, StreamRDF dest) {
        HttpResponse<InputStream> response = execGetToInput(client, url, modifier);
        httpResponseToStreamRDF(url, response, dest);
    }

    /*package*/ static void httpResponseToStreamRDF(String url, HttpResponse<InputStream> response, StreamRDF dest) {
        InputStream in = handleResponseInputStream(response);
        String base = determineBaseURI(url, response);
        Lang lang = determineSyntax(response, Lang.RDFXML);
        try {
            RDFParser.create()
                .base(base)
                .source(in)
                .lang(lang)
                .parse(dest);
        } catch (RiotParseException ex) {
            // We only read part of the input stream.
            throw ex;
        } finally {
            // Even if parsing finished, it is possible we only read part of the input stream (e.g. RDF/XML).
            finish(in);
        }
    }

    /**
     * MUST consume or close the input stream
     * @see HttpLib#finish(HttpResponse)
     */
    private static HttpResponse<InputStream> execGetToInput(HttpClient client, String url, Consumer<HttpRequest.Builder> modifier) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(url);
        HttpRequest requestData = HttpLib.newGetRequest(url, modifier);
        HttpResponse<InputStream> response = HttpLib.execute(client, requestData);
        handleHttpStatusCode(response);
        return response;
    }

    public static void httpPostGraph(String url, Graph graph) {
        httpPostGraph(HttpEnv.getDftHttpClient(), url, graph, HttpEnv.dftTriplesFormat);
    }

    public static void httpPostGraph(HttpClient httpClient, String url, Graph graph, RDFFormat format) {
        httpPostGraph(httpClient, url, graph, format, null);
    }

    public static void httpPostGraph(HttpClient httpClient, String url, Graph graph,
                                     RDFFormat format, Map<String, String> httpHeaders) {
        BodyPublisher bodyPublisher = graphToHttpBody(graph, format);
        pushBody(httpClient, url, Push.POST, bodyPublisher, format, httpHeaders);
    }

    /** Post a graph and expect an RDF graph back as the result. */
    public static Graph httpPostGraphRtn(String url, Graph graph) {
        return httpPostGraphRtn(HttpEnv.getDftHttpClient(), url, graph,  HttpEnv.dftTriplesFormat, null);
    }

    /** Post a graph and expect an RDF graph back as the result. */
    public static Graph httpPostGraphRtn(HttpClient httpClient, String url, Graph graph, RDFFormat format, Map<String, String> httpHeaders) {
        BodyPublisher bodyPublisher = graphToHttpBody(graph, HttpEnv.dftTriplesFormat);
        HttpResponse<InputStream> httpResponse = pushWithResponse(httpClient, url, Push.POST, bodyPublisher, format, httpHeaders);
        Graph graphResponse = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graphResponse);
        httpResponseToStreamRDF(url, httpResponse, dest);
        return graphResponse;
    }

    public static void httpPostDataset(HttpClient httpClient, String url, DatasetGraph dataset, RDFFormat format) {
        httpPostDataset(httpClient, url, dataset, format, null);
    }

    public static void httpPostDataset(HttpClient httpClient, String url, DatasetGraph dataset,
                                       RDFFormat format, Map<String, String> httpHeaders) {
        BodyPublisher bodyPublisher = datasetToHttpBody(dataset, format);
        pushBody(httpClient, url, Push.POST, bodyPublisher, format, httpHeaders);
    }

    public static void httpPutGraph(String url, Graph graph) {
        httpPutGraph(HttpEnv.getDftHttpClient(), url, graph, HttpEnv.dftTriplesFormat);
    }

    public static void httpPutGraph(HttpClient httpClient, String url, Graph graph, RDFFormat fmt) {
        httpPutGraph(httpClient, url, graph, fmt, null);
    }

    public static void httpPutGraph(HttpClient httpClient, String url, Graph graph,
                                    RDFFormat format, Map<String, String> httpHeaders) {
        BodyPublisher bodyPublisher = graphToHttpBody(graph, format);
        pushBody(httpClient, url, Push.PUT, bodyPublisher, format, httpHeaders);
    }

    public static void httpPutDataset(HttpClient httpClient, String url, DatasetGraph dataset, RDFFormat format) {
        httpPutDataset(httpClient, url, dataset, format, null);
    }

    public static void httpPutDataset(HttpClient httpClient, String url, DatasetGraph dataset,
                                      RDFFormat format, Map<String, String> httpHeaders) {
        BodyPublisher bodyPublisher = datasetToHttpBody(dataset, format);
        pushBody(httpClient, url, Push.PUT, bodyPublisher, format, httpHeaders);
    }

    // Shared between push* and put*
    private static void pushBody(HttpClient httpClient, String url, Push style, BodyPublisher bodyPublisher,
                                 RDFFormat format, Map<String, String> httpHeaders) {
        String contentType = format.getLang().getHeaderString();
        if ( httpHeaders == null )
            httpHeaders = Collections.singletonMap(HttpNames.hContentType, contentType);
        else
            httpHeaders.put(HttpNames.hContentType, contentType);
        HttpLib.httpPushData(httpClient, style, url, HttpLib.setHeaders(httpHeaders), bodyPublisher);
    }

    private static HttpResponse<InputStream> pushWithResponse(HttpClient httpClient, String url, Push style, BodyPublisher bodyPublisher,
                                                              RDFFormat format, Map<String, String> httpHeaders) {
        String contentType = format.getLang().getHeaderString();
        if ( httpHeaders == null )
            httpHeaders = Collections.singletonMap(HttpNames.hContentType, contentType);
        else
            httpHeaders.put(HttpNames.hContentType, contentType);
        return HttpLib.httpPushWithResponse(httpClient, style, url, HttpLib.setHeaders(httpHeaders), bodyPublisher);
    }

    public static void httpDeleteGraph(String url) {
        httpDeleteGraph(HttpEnv.getDftHttpClient(), url);
    }

    public static void httpDeleteGraph(HttpClient httpClient, String url) {
        URI uri = toRequestURI(url);
        HttpRequest requestData = HttpLib.requestBuilderFor(url)
            .DELETE()
            .uri(uri)
            .build();
        HttpResponse<InputStream> response = execute(httpClient, requestData);
        handleResponseNoBody(response);
    }

    /** RDF {@link Lang}. */
    /*package*/ static <T> Lang determineSyntax(HttpResponse<T> response, Lang dftSyntax) {
        String ctStr = responseHeader(response, HttpNames.hContentType);
        if ( ctStr != null ) {
            int i = ctStr.indexOf(';');
            if ( i >= 0 )
                ctStr = ctStr.substring(0, i);
        }
        Lang lang = RDFLanguages.contentTypeToLang(ctStr);
        return dft(lang, dftSyntax);
    }

    /*package*/ static <T> String determineBaseURI(String url, HttpResponse<T> response) {
        // RFC 7231: 3.1.4.2. and Appendix B: Content-Location does not affect base URI.
        // With help from Stuart Williams.
        URI uri = response.uri();
        return uri.toString();
    }

    // This sets Content-Length but requires the entire graph being serialized
    // to get the serialization size.
    //
    // An alternative is to stream the output but then the HTTP connection can't
    // be reused (don't know when the request finishes, only closing the connection
    // indicates that).

    /*package*/ static BodyPublisher graphToHttpBody(Graph graph, RDFFormat syntax) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(128*1024);
        RDFDataMgr.write(out, graph, syntax);
        byte[] bytes = out.toByteArray();
        IO.close(out);
        return BodyPublishers.ofByteArray(bytes);
    }

    /*package*/ static BodyPublisher datasetToHttpBody(DatasetGraph dataset, RDFFormat syntax) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(128*1024);
        RDFDataMgr.write(out, dataset, syntax);
        byte[] bytes = out.toByteArray();
        IO.close(out);
        return BodyPublishers.ofByteArray(bytes);
    }
}
