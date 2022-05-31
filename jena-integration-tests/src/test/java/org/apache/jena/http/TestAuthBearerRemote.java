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

import static org.apache.jena.fuseki.test.HttpTest.expect401;
import static org.apache.jena.fuseki.test.HttpTest.expect403;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.auth.AuthBearerFilter;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.auth.AuthChallenge;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Bearer authentication is different - it is a framework.
 * <p>
 * We test here with some client-side functionality that checks the JWT "sub" locally.
 */
public class TestAuthBearerRemote {

    private static String user = "user";

    private static FusekiServer server = null;
    private static String dsEndpoint;
    private static URI dsEndpointURI;

    protected String endpoint() {
        return dsEndpoint;
    }

    protected URI endpointURI() {
        return dsEndpointURI;
    }

    @BeforeClass public static void beforeClass() {
        server = server("/ds", DatasetGraphFactory.createTxnMem());
    }

    // Client-side challenge callback.
    private void setBearerAuthProvider(String username) {
        BiFunction<String, AuthChallenge, String> testTokenSupplier = (uri, authHeader) -> AuthBearerTestLib.generateTestToken(username);
        AuthEnv.get().setBearerTokenProvider(testTokenSupplier);
    }

    // Client-side provide token ahead of time.
    private void addBearerAuthToken(String requestTarget, String username) {
        String token = AuthBearerTestLib.generateTestToken(username);
        AuthEnv.get().setBearerToken(requestTarget, token);
    }

    private static FusekiServer server(String dsName, DatasetGraph dsg) {
        // Server verified user function.
        Function<String, String> verifiedUser = token -> {
            String u = AuthBearerTestLib.subjectFromEncodedJWT(token);
            if ( u == null )
                return null;
            if ( user.equals(u) )
                return user;
            return null;
        };

        FusekiServer server = FusekiServer.create()
            .port(0)
            .enablePing(true)
            //.auth(AuthScheme.BEARER)
            .addFilter("/*", new AuthBearerFilter(verifiedUser))
            .add(dsName, dsg)
            .build();
        server.start();
        dsEndpoint = "http://localhost:"+server.getHttpPort()+"/ds";
        dsEndpointURI = URI.create(dsEndpoint);
        return server;
    }

    @AfterClass public static void afterClass() {
        dsEndpoint = null;
        dsEndpointURI = null;
        if ( server == null )
            return;
        try {
            server.stop();
            server = null;
        } catch (Throwable th) {
            Log.warn(TestAuthBearerRemote.class, "Exception in test suite shutdown", th);
        }
    }

    @After public void afterTest() {
        // Clear bearer auth.
        AuthEnv.get().setBearerTokenProvider(null);
        AuthEnv.get().clearActiveAuthentication();
    }


    // ---- QueryExecHTTP

    @Test
    public void auth_qe_no_auth() {
        expect401(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .endpoint(endpoint())
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

    @Test
    public void auth_qe_good_challenge_handler() {
        // 401 challenge handler.
        setBearerAuthProvider(user);
        try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                .endpoint(endpoint())
                .queryString("ASK{}")
                .build()) {
            qexec.ask();
        }
    }

    @Test
    public void auth_qe_good_bearer_request() {
        // Register token to use.
        addBearerAuthToken(endpoint(), user);
        try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                .endpoint(endpoint())
                .queryString("ASK{}")
                .build()) {
            qexec.ask();
        }
    }

    @Test
    public void auth_qe_bad_bearer_request() {
        addBearerAuthToken(endpoint(), "wrong-user");

        expect403(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .endpoint(endpoint())
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

    @Test
    public void auth_qe_bad_registered() {
        setBearerAuthProvider("wrong-user");
        expect403(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .endpoint(endpoint())
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

    // ---- GSP

    @Test
    public void auth_gsp_no_auth() {
        expect401(()->{
            GSP.service(endpoint()).defaultGraph().GET();
        });
    }

    @Test
    public void auth_gsp_good_registered() {
        setBearerAuthProvider("user");
        Graph graph = GSP.service(endpoint()).defaultGraph().GET();
        assertNotNull(graph);
    }

    @Test
    public void auth_gsp_bad_registered() {
        expect401(()->{
            GSP.service(endpoint()).defaultGraph().GET();
        });
    }
}
