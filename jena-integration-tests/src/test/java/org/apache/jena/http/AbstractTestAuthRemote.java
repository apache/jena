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

import org.apache.jena.graph.Graph;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.junit.Test;

/**
 * Covers the AuthEnv/Lib challenge response code
 * for basic and digest authentication.
 */
public abstract class AbstractTestAuthRemote {

    protected abstract String endpoint() ;
    protected abstract URI endpointURI();
    protected abstract String user();
    protected abstract String password();

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
    public void auth_qe_good_registered() {
        // Digest auth is not provided by java.net.http.
        // Digest only works via AuthEnv.
        AuthEnv.get().registerUsernamePassword(endpointURI(), user(), password());

        try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                //.httpClient(hc)
                .endpoint(endpoint())
                .queryString("ASK{}")
                .build()) {
            qexec.ask();
        }
    }

    @Test
    public void auth_qe_good_registered_query() {
        // Digest only works via AuthEnv.
        AuthEnv.get().registerUsernamePassword(endpointURI(), user(), password());
        // This has a query string with newlines.
        // Issue: https://github.com/apache/jena/issues/1318
        Query query = QueryFactory.create("ASK{}");
        try ( QueryExec qexec = QueryExecHTTP.newBuilder()
                //.httpClient(hc)
                .endpoint(endpoint())
                .query(query)
                .build()) {
            qexec.ask();
        }
    }

    @Test
    public void auth_qe_bad_registered() {
        expect401(()->{
            AuthEnv.get().registerUsernamePassword(endpointURI(), "wrong-user", password());
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
        AuthEnv.get().registerUsernamePassword(endpointURI(), user(), password());
        Graph graph = GSP.service(endpoint()).defaultGraph().GET();
        assertNotNull(graph);
    }

    @Test
    public void auth_gsp_bad_registered() {
        AuthEnv.get().registerUsernamePassword(endpointURI(), "wrong-user", password());
        expect401(()->{
            GSP.service(endpoint()).defaultGraph().GET();
        });
    }

}
