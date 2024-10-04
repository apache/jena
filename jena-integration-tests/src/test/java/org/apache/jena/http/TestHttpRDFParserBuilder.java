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

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.util.List;

import static org.junit.Assert.*;

public class TestHttpRDFParserBuilder {
    // The HttpRDF machinery (much of which is package visible) get tested by other
    // subsystems built on top of HttpRDF. This test suite is for the public API.
    private static EnvTest env;
    @BeforeClass public static void beforeClass() {
        env = EnvTest.create("/ds");
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    private String url(String path) { return env.datasetPath(path); }

    @Test public void RDFParser_using_default_http_environment() {
        Graph graph1 = SSE.parseGraph("(graph (:s :p 1) (:s :p 2))");
        HttpRDF.httpPutGraph(url("/ds?default"), graph1);
        var graph2 = GraphFactory.createGraphMem();
        var builder = RDFParserBuilder.create()
                .source(url("/ds?default"))
                .build();
        builder.parse(graph2);
        assertTrue(graph1.isIsomorphicWith(graph2));
    }

    @Test public void RDFParser_using_custom_http_environment() {
        Graph graph1 = SSE.parseGraph("(graph (:s :p 1) (:s :p 2))");
        HttpRDF.httpPutGraph(url("/ds?default"), graph1);
        final boolean[] proxyHasBeenCalled = {false};
        final var url = url("/ds?default");
        // create a custom httpClient and validate that it has been used
        var customHttpEnv = HttpClient.newBuilder()
                .proxy(new ProxySelector() {
                    @Override
                    public List<Proxy> select(URI uri) {
                        //validate the given url
                        assertEquals(url, uri.toString());
                        //memorize that the custom proxy selector has been called
                        proxyHasBeenCalled[0] = true;
                        return List.of();
                    }

                    @Override
                    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                        fail("can't connect to " + uri);
                    }

                })
                .build();
        var graph2 = GraphFactory.createGraphMem();
        var builder = RDFParserBuilder.create()
                .source(url)
                .httpClient(customHttpEnv)
                .build();
        builder.parse(graph2);
        assertTrue(graph1.isIsomorphicWith(graph2));
        assertTrue("ProxySelector in custom HttpClient has not been called.", proxyHasBeenCalled[0]);
    }
}
