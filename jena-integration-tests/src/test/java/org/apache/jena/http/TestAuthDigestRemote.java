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
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Digest authentication.
 * Digest auth is not provided by java.net.http.
 * Jena has to implement it itself (in AuthLib).
 */
public class TestAuthDigestRemote {
    private static String user = "user";
    private static String password = "password";

    private static FusekiServer server = null;
    private static String dsEndpoint;
    private static URI dsEndpointURI;

    @BeforeClass public static void beforeClass() {
        server = server("/ds", DatasetGraphFactory.createTxnMem(), user, password);
    }

    private static FusekiServer server(String dsName, DatasetGraph dsg, String user, String password) {
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);
        FusekiServer.Builder builder = FusekiServer.create()
            .port(0)
            .enablePing(true)
            .auth(AuthScheme.DIGEST)
            .add(dsName, dsg);
        if ( user != null ) {
            UserStore userStore = JettyLib.makeUserStore(user, password);
            SecurityHandler sh = JettyLib.makeSecurityHandler("TripleStore",  userStore, AuthScheme.DIGEST);
            builder.securityHandler(sh)
                   .serverAuthPolicy(Auth.policyAllowSpecific(user));
        }
        FusekiServer server = builder.build();
        server.start();
        dsEndpoint = "http://localhost:"+server.getHttpPort()+"/ds";
        dsEndpointURI = URI.create(dsEndpoint);
        return server;
    }

    //@Before public void before() {}
    @After public void after() {
        AuthEnv.get().unregisterUsernamePassword(dsEndpointURI);
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
            Log.warn(TestAuthDigestRemote.class, "Exception in test suite shutdown", th);
        }
    }

    // ---- QueryExecHTTP

    @Test
    public void auth_disgest_qe_no_auth() {
        expect401(()->{
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .endpoint(dsEndpoint)
                    .queryString("ASK{}")
                    .build()) {
                qexec.ask();
            }
        });
    }

//    @Test
//    public void auth_disgest_qe_good_auth() {
//        // Digest auth does not work with java.net.http.
//        // Jena has to implement it itself (in AuthLib).
//        Authenticator authenticator = AuthLib.authenticator(user, password);
//        HttpClient hc = HttpClient.newBuilder().authenticator(authenticator).build();
//
//        expect401(()->{
//            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
//                    .httpClient(hc)
//                    .endpoint(dsEndpoint)
//                    .queryString("ASK{}")
//                    .build()) {
//                qexec.ask();
//            }
//        });
//    }

    @Test
    public void auth_disgest_qe_good_registered() {
        // Digest only work via AuthEnv.
        AuthEnv.get().registerUsernamePassword(dsEndpointURI, user, password);

        try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                //.httpClient(hc)
                .endpoint(dsEndpoint)
                .queryString("ASK{}")
                .build()) {
            qexec.ask();
        }
    }

    @Test
    public void auth_disgest_qe_bad_registered() {
        expect401(()->{
            AuthEnv.get().registerUsernamePassword(dsEndpointURI, "wrong-user", password);
            try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                    .endpoint(dsEndpoint)
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
            GSP.service(dsEndpoint).defaultGraph().GET();
        });
    }

    @Test
    public void auth_gsp_good_registered() {
        AuthEnv.get().registerUsernamePassword(dsEndpointURI, user, password);
        Graph graph = GSP.service(dsEndpoint).defaultGraph().GET();
        assertNotNull(graph);
    }

    @Test
    public void auth_gsp_bad_registered() {
        AuthEnv.get().registerUsernamePassword(dsEndpointURI, "wrong-user", password);
        expect401(()->{
            GSP.service(dsEndpoint).defaultGraph().GET();
        });
    }
}
