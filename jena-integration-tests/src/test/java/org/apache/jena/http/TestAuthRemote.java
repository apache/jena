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

import static org.junit.Assert.assertTrue;

import org.apache.jena.fuseki.test.FusekiTest;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.sys.HttpRequestModifier;
import org.apache.jena.http.sys.RegistryRequestModifier;
import org.apache.jena.query.ARQ;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkFactory;
import org.apache.jena.rdflink.RDFLinkRemote;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.*;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is more than just RDFLinkRemote - it covers the components
 */
public class TestAuthRemote {
    private static String user = "user";
    private static String password = "password";

    private static EnvTest env;
    @BeforeClass public static void beforeClass() {
        //FusekiLogging.setLogging();
        env = EnvTest.createAuth("/ds", DatasetGraphFactory.createTxnMem(), user, password);
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    // ---- QueryExecHTTP

    @Test
    public void auth_qe_no_auth() {
        FusekiTest.expect401(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    //.httpClient(hc)
                    .service(env.datasetURL())
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

    @Test
    public void auth_qe_good_auth() {
        try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                .httpClient(env.httpClientAuthGood())
                .service(env.datasetURL())
                .queryString("ASK{}")
                .build()) {
            qexec.ask();
        }
    }

    @Test
    public void auth_qe_bad_auth() {
        FusekiTest.expect401(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .httpClient(env.httpClientAuthBad())
                    .service(env.datasetURL())
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

    // Retries up to "java.net.httpclient.redirects.retrylimit" (3 by default).
    @Test
    public void auth_qe_bad_auth_retries() {
        FusekiTest.expect401(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    // retry blindly.
                    .httpClient(EnvTest.httpClient(env.authenticatorBadRetries()))
                    .service(env.datasetURL())
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

    // ---- UpdateExecHTTP

    @Test
    public void auth_update_no_auth() {
        FusekiTest.expect401(()->
            UpdateExecHTTP.newBuilder()
                .service(env.datasetURL())
                .updateString("INSERT DATA { <x:s> <x:p> <x:o> }")
                .build()
                .execute()
        );
    }

    @Test
    public void auth_update_good_auth() {
        UpdateExecHTTP.newBuilder()
            .httpClient(env.httpClientAuthGood())
            .service(env.datasetURL())
            .updateString("INSERT DATA { <x:s> <x:p> <x:o> }")
            .build()
            .execute();
    }

    @Test
    public void auth_update_bad_auth() {
        FusekiTest.expect401(()->
            UpdateExecHTTP.newBuilder()
                .httpClient(env.httpClientAuthBad())
                .service(env.datasetURL())
                .updateString("INSERT DATA { <x:s> <x:p> <x:o> }")
                .build()
                .execute()
        );
    }

    // ---- GSP

    @Test
    public void auth_gsp_no_auth() {
        FusekiTest.expect401(()->{
            GSP.request(env.datasetURL()).defaultGraph().GET();
        });
    }

    @Test
    public void auth_gsp_good_auth() {
        GSP.request(env.datasetURL()).httpClient(env.httpClientAuthGood()).defaultGraph().GET();
    }

    @Test
    public void auth_gsp_bad_auth() {
        // 401 because we didn't authenticate.
        FusekiTest.expect401(()->
            GSP.request(env.datasetURL()).httpClient(env.httpClientAuthBad()).defaultGraph().GET()
        );
    }

    // RDFLink

    @Test
    public void auth_link_no_auth_1() {
        FusekiTest.expect401(()->{
            try ( RDFLink link = RDFLinkFactory.connect(env.datasetURL()) ) {
                link.queryAsk("ASK{}");
            }
        });
    }

    @Test
    public void auth_link_no_auth_2() {
        FusekiTest.expect401(()->{
            try ( RDFLink link = RDFLinkFactory.connect(env.datasetURL()) ) {
                link.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            }
        });
    }

    @Test
    public void auth_link_no_auth_3() {
        FusekiTest.expect401(()->{
            try ( RDFLink link = RDFLinkFactory.connect(env.datasetURL()) ) {
                link.get();
            }
        });
    }

    @Test
    public void auth_link_good_auth() {
        try ( RDFLink link = RDFLinkRemote.newBuilder()
                    .destination(env.datasetURL())
                    .httpClient(env.httpClientAuthGood())
                    .build()) {
            link.queryAsk("ASK{}");
            link.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            link.get();
        }
    }

    @Test
    public void auth_link_bad_auth_1() {
        // 401 (not 403) because we didn't authenticate
        // 403 is recognized authenticate, not sufficient for ths resource.
        FusekiTest.expect401(()->{
            try ( RDFLink link = RDFLinkRemote.newBuilder()
                    .destination(env.datasetURL())
                    .httpClient(env.httpClientAuthBad())
                    .build()) {
            link.queryAsk("ASK{}");
            }
        });
    }

    @Test
    public void auth_link_bad_auth_2() {
        // 401 (not 403) because we didn't authenticate
        FusekiTest.expect401(()->{
            try ( RDFLink link = RDFLinkRemote.newBuilder()
                    .destination(env.datasetURL())
                    .httpClient(env.httpClientAuthBad())
                    .build()) {
            link.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            }
        });
    }

    @Test
    public void auth_link_bad_auth_3() {
        // 401 (not 403) because we didn't authenticate
        FusekiTest.expect401(()->{
            try ( RDFLink link = RDFLinkRemote.newBuilder()
                        .destination(env.datasetURL())
                        .httpClient(env.httpClientAuthBad())
                        .build()) {
                link.get();
            }
        });
    }

    // RegistryHttpClient

    private void exec_auth_registry_exact(String key) {
        RegistryHttpClient.get().add(key, env.httpClientAuthGood());
        try { exec_register_test(); }
        finally { RegistryHttpClient.get().clear(); }
    }

    private void exec_auth_registry_prefix(String key) {
        RegistryHttpClient.get().addPrefix(key, env.httpClientAuthGood());
        try { exec_register_test(); }
        finally { RegistryHttpClient.get().clear(); }
    }

    private void exec_register_test() {
        try (QueryExec qExec = QueryExecHTTP.newBuilder()
                .service(env.datasetURL())
                .queryString("ASK{  }")
                .build()) {
            boolean b = qExec.ask();
            assertTrue(b);
        }
    }

    @Test
    public void auth_registryHttpClient_exact_1() {
        exec_auth_registry_exact(env.datasetURL());
    }

    @Test
    public void auth_registryHttpClient_exact_2() {
        // Use prefix match for server-wide
        FusekiTest.expect401(()->{
            exec_auth_registry_exact(env.serverBaseURL());
        });
    }

    @Test
    public void auth_registryHttpClient_exact_401_1() {
        FusekiTest.expect401(()->{
            exec_auth_registry_exact("Junk");
        });
    }

    @Test
    public void auth_registryHttpClient_exact_401_2() {
        // Longer name, child
        String registerKey = env.serverPath(env.dsName()+"/XYZ");
        FusekiTest.expect401(()->{
            exec_auth_registry_exact(registerKey);
        });
    }

    @Test
    public void auth_registryHttpClient_exact_401_3() {
        // Longer name.
        // It does not match; prefixes are limited to ending in "/"
        String registerKey = env.serverPath(env.dsName()+"X");
        FusekiTest.expect401(()->{
            exec_auth_registry_exact(registerKey);
        });
    }

    @Test
    public void auth_registryHttpClient_exact_401_4() {
        // Different dataset
        String registerKey = env.serverPath("ABC");
        FusekiTest.expect401(()->{
            exec_auth_registry_exact(registerKey);
        });
    }

    @Test
    public void auth_registryHttpClient_exact_401_5() {
        // Shorter dataset name, prefix of actual one.
        // It does not match.
        String registerKey = env.serverPath("d");
        FusekiTest.expect401(()->{
            exec_auth_registry_exact(registerKey);
        });
    }

    @Test(expected=IllegalArgumentException.class)
    public void auth_registryHttpClient_prefix_1() {
        // Should use exact
        exec_auth_registry_prefix(env.datasetURL());
    }

    @Test
    public void auth_registryHttpClient_prefix_2() {
        // Everything at a server. Must end in "/"
        exec_auth_registry_prefix(env.serverBaseURL());
    }

    @Test
    public void auth_registryHttpClient_prefix_401_1() {
        FusekiTest.expect401(()->{
            exec_auth_registry_prefix("Junk/");
        });
    }

    @Test
    public void auth_registryHttpClient_prefix_401_2() {
        // Longer name.
        String registerKey = env.serverPath(env.dsName()+"/");
        FusekiTest.expect401(()->{
            exec_auth_registry_prefix(registerKey);
        });
    }

    @Test
    public void auth_registryHttpClient_prefix_401_3() {
        // Longer name.
        String registerKey = env.serverPath(env.dsName()+"/xyz/");
        FusekiTest.expect401(()->{
            exec_auth_registry_prefix(registerKey);
        });
    }

    @Test
    public void auth_registryHttpClient_prefix_401_4() {
        // Different dataset
        String registerKey = env.serverPath("A/");
        FusekiTest.expect401(()->{
            exec_auth_registry_prefix(registerKey);
        });
    }

    @Test(expected=IllegalArgumentException.class)
    public void auth_registryHttpClient_prefix_401_5() {
        // Not cause 401. Shorter dataset name, prefix of actual one.
        String registerKey = env.serverPath("d");
        exec_auth_registry_prefix(registerKey);
    }

    // Other ways of setting auth.
    // Using HttpClient is preferred but the basic operations can use
    // ServiceTuning or directly supply the headers.

    // ServiceTuning
    @Test
    public void auth_service_tuning_1_RegistryRequestModifier() {
        // using RegistryRequestModifier
        HttpRequestModifier mods = (params, headers) -> headers.put(HttpNames.hAuthorization, HttpLib.basicAuth(user, password));

        RegistryRequestModifier svcReg = new RegistryRequestModifier();
        svcReg.add(env.datasetURL(), mods);
        ARQ.getContext().put(ARQ.httpRegistryRequestModifer, svcReg);

        try {
            // Component level
            UpdateExecHTTP.newBuilder()
                .service(env.datasetURL())
                .updateString("INSERT DATA { <x:s> <x:p> <x:o> }")
                .build()
                .execute();
            try ( QueryExec qExec = QueryExecHTTP.newBuilder()
                    .service(env.datasetURL())
                    .queryString("ASK{ <x:s> <x:p> <x:o> }")
                    .build()){
                boolean b = qExec.ask();
                assertTrue(b);
            }
        } finally {
            // clear up
            ARQ.getContext().remove(ARQ.httpRegistryRequestModifer);
        }
    }

    @Test
    public void auth_service_tuning_2_HttpRequestModifer() {
        // As HttpRequestModifer
        HttpRequestModifier mods = (params, headers) -> headers.put(HttpNames.hAuthorization, HttpLib.basicAuth(user, password));
        ARQ.getContext().put(ARQ.httpRequestModifer, mods);
        try {
            try ( RDFLink link = RDFLinkRemote.newBuilder()
                    .destination(env.datasetURL())
                    .build()) {
                link.update("INSERT DATA { <x:s> <x:p> <x:z> }");
                boolean b = link.queryAsk("ASK{ <x:s> <x:p> <x:z> }");
                assertTrue(b);
            }
        } finally {
            // clear up
            ARQ.getContext().remove(ARQ.httpRequestModifer);
        }
    }

    @Test
    public void auth_header_1() {
        Triple triple = SSE.parseTriple("(<x:s> <x:p> <x:o>)");
        Graph graph = GraphFactory.createDefaultGraph();
        graph.add(triple);

        GSP.request(env.datasetURL())
            .httpHeader(HttpNames.hAuthorization, HttpLib.basicAuth(user, password))
            .defaultGraph()
            .POST(graph);
        // By query.
        try ( QueryExec qExec = QueryExecHTTP.newBuilder()
                .service(env.datasetURL())
                .queryString("ASK{ <x:s> <x:p> <x:o> }")
                .httpHeader(HttpNames.hAuthorization, HttpLib.basicAuth(user, password))
                .build()) {
            boolean b = qExec.ask();
            assertTrue(b);
        }
        // By GSP
        Graph graph2 =
            GSP.request(env.datasetURL())
                .httpHeader(HttpNames.hAuthorization, HttpLib.basicAuth(user, password))
                .defaultGraph()
                .GET();
        assertTrue(graph.isIsomorphicWith(graph2));
    }
}

