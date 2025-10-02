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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.web.HttpSC;

/**
 * A collection of convenience operations for HTTP level operations
 * for RDF related tasks which are performed asynchronously.
 *
 * See also {@link HttpRDF}.
 */
public class AsyncHttpRDF {
    // Add POST and PUT

    private static Consumer<HttpRequest.Builder> acceptHeaderGraph = HttpLib.setAcceptHeader(WebContent.defaultGraphAcceptHeader);
    private static Consumer<HttpRequest.Builder> acceptHeaderDatasetGraph = HttpLib.setAcceptHeader(WebContent.defaultDatasetAcceptHeader);

    /** Get a graph, asynchronously */
    public static CompletableFuture<Graph> asyncGetGraph(String url) {
        return asyncGetGraph(HttpEnv.getDftHttpClient(), url);
    }

    /** Get a graph, asynchronously */
    public static CompletableFuture<Graph> asyncGetGraph(HttpClient httpClient, String url) {
        Objects.requireNonNull(httpClient, "HttpClient");
        Objects.requireNonNull(url, "URL");
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        CompletableFuture<Void> cf = asyncGetToStream(httpClient, url, acceptHeaderGraph, dest, null);
        return cf.thenApply(x->graph);
    }

    /** Get a dataset, asynchronously */
    public static CompletableFuture<DatasetGraph> asyncGetDatasetGraph(String url) {
        return asyncGetDatasetGraph(HttpEnv.getDftHttpClient(), url);
    }

    /** Get a dataset, asynchronously */
    public static CompletableFuture<DatasetGraph> asyncGetDatasetGraph(HttpClient httpClient, String url) {
        Objects.requireNonNull(httpClient, "HttpClient");
        Objects.requireNonNull(url, "URL");
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        CompletableFuture<Void> cf = asyncGetToStream(httpClient, url, acceptHeaderDatasetGraph, dest, dsg);
        return cf.thenApply(x->dsg);
    }

    /**
     * Load a DatasetGraph asynchronously.
     * The dataset is updated inside a transaction.
     */
    public static CompletableFuture<Void> asyncLoadDatasetGraph(String url, DatasetGraph dsg) {
        return asyncLoadDatasetGraph(HttpEnv.getDftHttpClient(), url, dsg);
    }

    /**
     * Load a DatasetGraph asynchronously.
     * The dataset is updated inside a transaction.
     */
    public static CompletableFuture<Void> asyncLoadDatasetGraph(HttpClient httpClient, String url, DatasetGraph dsg) {
        Objects.requireNonNull(httpClient, "HttpClient");
        Objects.requireNonNull(url, "URL");
        Objects.requireNonNull(dsg, "dataset");
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        return asyncGetToStream(httpClient, url, acceptHeaderDatasetGraph, dest, dsg);
    }

    /**
     * Load a DatasetGraph asynchronously.
     * The dataset is updated inside a transaction.
     */
    public static CompletableFuture<Void> asyncLoadDatasetGraph(HttpClient httpClient, String url, Map<String, String> headers, DatasetGraph dsg) {
        Objects.requireNonNull(httpClient, "HttpClient");
        Objects.requireNonNull(url, "URL");
        Objects.requireNonNull(dsg, "dataset");
        StreamRDF dest = StreamRDFLib.dataset(dsg);
        return asyncGetToStream(httpClient, url, HttpLib.setHeaders(headers), dest, dsg);
    }

    /**
     * Execute an asynchronous GET and parse the result to a StreamRDF.
     * If "transactional" is not null, the object is used a write transaction around the parsing step.
     * <p>
     * Call {@link CompletableFuture#join} to await completion.<br/>
     * Call {@link #syncOrElseThrow(CompletableFuture)} to await completion,
     * with exceptions translated to the underlying {@code RuntimeException}.
     */
    public static CompletableFuture<Void> asyncGetToStream(HttpClient httpClient, String url, String acceptHeader, StreamRDF dest, Transactional transactional) {
        Objects.requireNonNull(httpClient, "HttpClient");
        Objects.requireNonNull(url, "URL");
        Objects.requireNonNull(dest, "StreamRDF");
        Consumer<HttpRequest.Builder> setAcceptHeader = HttpLib.setAcceptHeader(acceptHeader);
        return asyncGetToStream(httpClient, url, setAcceptHeader, dest, transactional);
    }

    private static CompletableFuture<Void> asyncGetToStream(HttpClient httpClient, String url, Consumer<HttpRequest.Builder> modifier, StreamRDF dest, Transactional _transactional) {
        CompletableFuture<HttpResponse<InputStream>> cf = asyncGetToInput(httpClient, url, modifier);
        Transactional transact = ( _transactional == null ) ? TransactionalNull.create() : _transactional;
        return cf.thenApply(httpResponse->{
            transact.executeWrite(()->{
                try {
                    HttpRDF.httpResponseToStreamRDF(url, httpResponse, dest);
                } finally {
                    HttpLib.finishResponse(httpResponse);
                }
            });
            return null;
        });
    }

    /**
     * Wait for the {@code CompletableFuture} or throw a runtime exception.
     * This operation extracts RuntimeException from the {@code CompletableFuture}.
     */
    public static void syncOrElseThrow(CompletableFuture<Void> cf) {
        getOrElseThrow(cf);
    }

    /**
     * Wait for the {@code CompletableFuture} then return the result or throw a runtime exception.
     * This operation extracts RuntimeException from the {@code CompletableFuture}.
     */
    public static <T> T getOrElseThrow(CompletableFuture<T> cf) {
        return getOrElseThrow(cf, null);
    }

    /**
     * Get the value of a {@link CompletableFuture} that executes of an HTTP request.
     * In case on any error, an {@link HttpException} is thrown.
     *
     * @param <T> The type of the value being computed.
     * @param cf The completable future.
     * @param httpRequest An optional HttpRequest for improving feedback in case of exceptions.
     * @return The value computed by the completable future.
     */
    public static <T> T getOrElseThrow(CompletableFuture<T> cf, HttpRequest httpRequest) {
        Objects.requireNonNull(cf);
        try {
            return cf.join();
        //} catch (CancellationException ex1) { // Let this pass out.
        } catch (CompletionException ex) {
            Throwable cause = ex.getCause();
            if ( cause != null ) {

                // Pass on our own HttpException instances such as 401 Unauthorized.
                if ( cause instanceof HttpException httpEx ) {
                    throw new HttpException(httpEx.getStatusCode(), httpEx.getStatusLine(), httpEx.getResponse(), cause);
                }

                final String msg = cause.getMessage();

                if ( cause instanceof IOException ) {
                    // Rather than an HTTP exception, bad authentication becomes IOException("too many authentication attempts");
                    if ( msg != null &&
                            ( msg.contains("too many authentication attempts") ||
                              msg.contains("No credentials provided") ) ) {
                        throw new HttpException(401, HttpSC.getMessage(401), null, cause);
                    }
                    if (httpRequest != null) {
                        throw new HttpException(httpRequest.method()+" "+httpRequest.uri().toString(), cause);
                    }
                }

                throw new HttpException(msg, cause);
            }
            // Note: CompletionException without cause should never happen.
            throw new HttpException(ex);
        }
    }

    /**
     * MUST consume or close the input stream
     * @see HttpLib#finish(HttpResponse)
     */
    private static CompletableFuture<HttpResponse<InputStream>> asyncGetToInput(HttpClient httpClient, String url, Consumer<HttpRequest.Builder> modifier) {
        Objects.requireNonNull(httpClient);
        Objects.requireNonNull(url);
        HttpRequest requestData = HttpLib.newGetRequest(url, modifier);
        return HttpLib.asyncExecute(httpClient, requestData);
    }
}
