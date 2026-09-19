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

package org.apache.jena.sparql.exec.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;

public class TestServiceCancellation {
    static { org.apache.jena.sys.JenaSystem.init(); }

    private static final String ENDPOINT = "http://example.invalid/sparql";
    private static final String HEADER = "{\"head\":{\"vars\":[\"x\"]},\"results\":{\"bindings\":[";
    private static final String ROW = "{\"x\":{\"type\":\"literal\",\"value\":\"value\"}}";
    private static final String RESULTS = HEADER + (ROW + ",").repeat(999) + ROW + "]}}";

    @Test
    public void cancelledBeforeRequest() {
        Context context = context();
        AtomicBoolean signal = Context.getOrSetCancelSignal(context);
        signal.set(true);
        ResponseBody body = new ResponseBody(RESULTS, signal, Integer.MAX_VALUE);
        ResponseClient client = new ResponseClient(body);
        context.set(ARQ.httpQueryClient, client);

        assertThrows(QueryCancelledException.class, () -> Service.exec(op(), context));
        assertEquals(0, client.requests);
        assertEquals(0, body.position);
    }

    @Test
    public void cancelledDuringMaterialization() {
        Context context = context();
        AtomicBoolean signal = Context.getOrSetCancelSignal(context);
        // The reader has returned several rows before the response sets the signal.
        ResponseBody body = new ResponseBody(RESULTS, signal, HEADER.length() + 10 * (ROW.length() + 1));
        context.set(ARQ.httpQueryClient, new ResponseClient(body));

        assertThrows(QueryCancelledException.class, () -> Service.exec(op(), context));
        assertTrue(body.closed);
        assertTrue(body.position < RESULTS.length() / 2, "Cancellation must not drain the remaining rows");
    }

    @Test
    public void cancellationObservedAfterBlockedReadReturns() throws Exception {
        Context context = context();
        AtomicBoolean signal = Context.getOrSetCancelSignal(context);
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ResponseBody body = new ResponseBody(RESULTS, signal, Integer.MAX_VALUE) {
            @Override
            public int read() {
                if (reading.getCount() != 0) {
                    reading.countDown();
                    try {
                        assertTrue(release.await(10, TimeUnit.SECONDS));
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(ex);
                    }
                }
                return super.read();
            }
        };
        context.set(ARQ.httpQueryClient, new ResponseClient(body));
        var executor = Executors.newSingleThreadExecutor();
        try {
            var result = executor.submit(() -> {
                assertThrows(QueryCancelledException.class, () -> Service.exec(op(), context));
            });
            assertTrue(reading.await(5, TimeUnit.SECONDS));
            signal.set(true);
            release.countDown();
            result.get(5, TimeUnit.SECONDS);
            assertTrue(body.closed);
            assertTrue(body.position < RESULTS.length() / 2);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    public void cancelledAtEndOfResults() {
        Context context = context();
        AtomicBoolean signal = Context.getOrSetCancelSignal(context);
        String results = HEADER + ROW + "]}}";
        // Raise cancellation as the parser reaches the closing bindings array.
        ResponseBody body = new ResponseBody(results, signal, results.length() - 3);
        context.set(ARQ.httpQueryClient, new ResponseClient(body));

        assertThrows(QueryCancelledException.class, () -> Service.exec(op(), context));
        assertTrue(signal.get());
        assertTrue(body.closed);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void successfulResultsAreDetached(boolean withCancelSignal) {
        Context context = context();
        if (withCancelSignal)
            Context.getOrSetCancelSignal(context);
        ResponseBody body = new ResponseBody(RESULTS, new AtomicBoolean(), Integer.MAX_VALUE);
        context.set(ARQ.httpQueryClient, new ResponseClient(body));

        QueryIterator iter = Service.exec(op(), context);
        assertTrue(body.closed);
        try {
            int count = 0;
            while (iter.hasNext()) {
                assertEquals("value", iter.next().get("x").getLiteralLexicalForm());
                count++;
            }
            assertEquals(1000, count);
        } finally {
            iter.close();
        }
    }

    @Test
    public void abortedCloseDoesNotReadResponse() {
        ResponseBody body = new ResponseBody(RESULTS, new AtomicBoolean(), Integer.MAX_VALUE);
        try (QueryExecHTTP exec = QueryExecHTTP.newBuilder().endpoint(ENDPOINT)
                .query("SELECT * { ?s ?p ?o }").httpClient(new ResponseClient(body)).build()) {
            exec.select();
            int beforeClose = body.position;
            exec.abort();
            exec.abort();
            exec.close();
            exec.close();
            assertTrue(body.closed);
            assertEquals(beforeClose, body.position, "Closing an aborted query must not read the body");
        }
    }

    @Test
    public void cancelledSilentServiceDoesNotConsumeRemainingResponse() {
        Context context = context();
        AtomicBoolean signal = Context.getOrSetCancelSignal(context);
        ResponseBody body = new ResponseBody(RESULTS, signal, HEADER.length() + 10 * (ROW.length() + 1));
        context.set(ARQ.httpQueryClient, new ResponseClient(body));
        try (QueryExec exec = QueryExec.dataset(DatasetGraphZero.create()).context(context)
                .query("SELECT * { SERVICE SILENT <" + ENDPOINT + "> { ?s ?p ?x } }").build()) {
            assertThrows(QueryCancelledException.class, () -> exec.select().materialize());
            assertTrue(body.closed);
            assertTrue(body.position < RESULTS.length() / 2);
        }
    }

    @Test
    public void silentServiceDiscardsPartialResultsOnError() {
        Context context = context();
        ResponseBody body = new ResponseBody(HEADER + ROW + ",broken", new AtomicBoolean(), Integer.MAX_VALUE);
        context.set(ARQ.httpQueryClient, new ResponseClient(body));
        try (QueryExec exec = QueryExec.dataset(DatasetGraphZero.create()).context(context)
                .query("SELECT * { VALUES ?outer { 1 } SERVICE SILENT <" + ENDPOINT + "> { ?s ?p ?x } }").build()) {
            var rows = exec.select().materialize();
            assertTrue(rows.hasNext());
            var row = rows.next();
            assertNotNull(row.get("outer"));
            assertNull(row.get("x"));
            assertFalse(rows.hasNext());
            assertTrue(body.closed);
        }
    }

    @Test
    public void remapsScopedVariables() {
        Context context = context();
        ResponseBody body = new ResponseBody(HEADER + ROW + "]}}", new AtomicBoolean(), Integer.MAX_VALUE);
        context.set(ARQ.httpQueryClient, new ResponseClient(body));
        OpService op = new OpService(NodeFactory.createURI(ENDPOINT), SSE.parseOp("(bgp (?s ?p ?/x))"), false);
        QueryIterator iter = Service.exec(op, context);
        try {
            var row = iter.next();
            assertNull(row.get("x"));
            assertEquals("value", row.get("/x").getLiteralLexicalForm());
            assertFalse(iter.hasNext());
            assertTrue(body.closed);
        } finally {
            iter.close();
        }
    }

    @Test
    public void httpTimeoutIsPreserved() {
        Context context = context().set(ARQ.httpQueryTimeout, 1234);
        ResponseClient client = new ResponseClient(new ResponseBody(HEADER + "]}}", new AtomicBoolean(), Integer.MAX_VALUE));
        context.set(ARQ.httpQueryClient, client);
        QueryIterator iter = Service.exec(op(), context);
        iter.close();
        assertEquals(Duration.ofMillis(1234), client.lastRequest.timeout().orElseThrow());
    }

    private static Context context() {
        return Context.create().set(ARQ.httpServiceAllowed, true);
    }

    private static OpService op() {
        return new OpService(NodeFactory.createURI(ENDPOINT), SSE.parseOp("(bgp (?s ?p ?x))"), false);
    }

    private static class ResponseBody extends InputStream {
        private final byte[] bytes;
        private final AtomicBoolean signal;
        private final int cancelAt;
        private int position;
        private boolean closed;

        ResponseBody(String text, AtomicBoolean signal, int cancelAt) {
            bytes = text.getBytes(StandardCharsets.UTF_8);
            this.signal = signal;
            this.cancelAt = cancelAt;
        }

        @Override
        public int read() {
            if (position >= cancelAt)
                signal.set(true);
            return position < bytes.length ? bytes[position++] & 0xff : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (len == 0)
                return 0;
            // One byte per read prevents parser buffering from hiding excessive consumption.
            int value = read();
            if (value == -1)
                return -1;
            b[off] = (byte)value;
            return 1;
        }

        @Override
        public void close() { closed = true; }
    }

    /** Supplies a controlled response body without a network or HTTP server thread pool. */
    private static class ResponseClient extends HttpClient {
        private final InputStream body;
        private int requests;
        private HttpRequest lastRequest;

        ResponseClient(InputStream body) { this.body = body; }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> handler) {
            requests++;
            lastRequest = request;
            // QueryExecHTTP requests an InputStream body.
            @SuppressWarnings("unchecked")
            T responseBody = (T)body;
            HttpResponse<T> response = new HttpResponse<>() {
                @Override public int statusCode() { return 200; }
                @Override public HttpRequest request() { return request; }
                @Override public Optional<HttpResponse<T>> previousResponse() { return Optional.empty(); }
                @Override public HttpHeaders headers() {
                    return HttpHeaders.of(Map.of("Content-Type", List.of("application/sparql-results+json")), (k, v) -> true);
                }
                @Override public T body() { return responseBody; }
                @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
                @Override public URI uri() { return request.uri(); }
                @Override public Version version() { return Version.HTTP_1_1; }
            };
            return CompletableFuture.completedFuture(response);
        }

        @Override public <T> HttpResponse<T> send(HttpRequest r, HttpResponse.BodyHandler<T> h) { throw new UnsupportedOperationException(); }
        @Override public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest r, HttpResponse.BodyHandler<T> h, HttpResponse.PushPromiseHandler<T> p) { throw new UnsupportedOperationException(); }
        @Override public Optional<CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<ProxySelector> proxy() { return Optional.empty(); }
        @Override public SSLContext sslContext() { throw new UnsupportedOperationException(); }
        @Override public SSLParameters sslParameters() { return new SSLParameters(); }
        @Override public Optional<Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_1_1; }
        @Override public Optional<Executor> executor() { return Optional.empty(); }
    }
}
