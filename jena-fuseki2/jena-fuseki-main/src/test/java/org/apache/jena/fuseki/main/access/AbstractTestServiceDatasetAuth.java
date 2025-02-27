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

package org.apache.jena.fuseki.main.access;

import static org.apache.jena.fuseki.main.FusekiTestLib.expect403;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectOK;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery401;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery403;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.web.AuthSetup;

/** Tests for a dataset with SPARQL query and update, and no named endpoint services.
 * See {@link TestServiceDataAuthConfig} and {@link TestServiceDataAuthBuild}.
 */
public abstract class AbstractTestServiceDatasetAuth {

    // Need the port for server and for the AuthSetup.
    protected int port = WebLib.choosePort();
    private AuthSetup auth1 = new AuthSetup("localhost", port, "user1", "pw1", null);
    private AuthSetup auth2 = new AuthSetup("localhost", port, "user2", "pw2", null);
    private AuthSetup auth3 = new AuthSetup("localhost", port, "user3", "pw3", null);

    @Test public void no_auth() {
        // No user -> fails login
        expectQuery401(() -> {
            try ( RDFLink conn = RDFLink.connect(server().datasetURL("/db")) ) {
                //conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
                try ( QueryExec qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                    RowSetOps.consume(qExec.select());
                }
            }
        });
    }

    protected abstract FusekiServer server();

    @Test public void user1_update() {
        expectOK(()->{
            LibSec.withAuth(server().datasetURL("/db"), auth1, conn -> {
                conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            });
        });
    }

    @Test public void user2_query() {
        expectOK(()->{
            LibSec.withAuth(server().datasetURL("/db"), auth2, conn1 -> {
                try (RDFLink conn = conn1) {
                    try ( QueryExec qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                        RowSetOps.consume(qExec.select());
                    }
                }});
        });
    }

    @Test public void user2_update() {
        expect403(()->{
            LibSec.withAuth(server().datasetURL("/db"), auth2, conn -> {
                conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            });
        });
    }

    @Test public void user3_query() {
        expectQuery403(()->{
            LibSec.withAuth(server().datasetURL("/db"), auth3, conn1 -> {
                try (RDFLink conn = conn1) {
                    try ( QueryExec qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                        qExec.select().materialize();
                    }
                }});
        });
    }

    @Test public void user3_update() {
        expectOK(()->{
            LibSec.withAuth(server().datasetURL("/db"), auth3, conn -> {
                conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            });
        });
    }
}
