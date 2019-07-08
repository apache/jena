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

import static org.apache.jena.fuseki.main.FusekiTestLib.*;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery401;
import static org.apache.jena.fuseki.main.FusekiTestLib.expectQuery403;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.web.AuthSetup;
import org.junit.AfterClass;
import org.junit.Test;

/** Tests for a dataset with SPARQL query and update, and no named endpoint services.
 * See {@link TestServiceDataAuthConfig} and {@link TestServiceDataAuthBuild}.
 */  
public abstract class AbstractTestServiceDatasetAuth {

    protected static FusekiServer server; 
    protected static int port;
    
    private static AuthSetup auth1 = new AuthSetup("localhost", port, "user1", "pw1", null);
    private static AuthSetup auth2 = new AuthSetup("localhost", port, "user2", "pw2", null);
    private static AuthSetup auth3 = new AuthSetup("localhost", port, "user3", "pw3", null);

    // @BeforeClass : subclass must set "server".
    // Setup : user1 and user2 can query, user1 and user3 can update.
    
    @AfterClass public static void afterClass () {
        server.stop();
    }

    @Test public void no_auth() {
        // No user -> fails login
        expectQuery401(() -> {
            try ( RDFConnection conn = RDFConnectionFactory.connect("http://localhost:" + port + "/db") ) {
                //conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
                try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                    ResultSetFormatter.consume(qExec.execSelect());
                }
            }
        });
    }

    @Test public void user1_update() {
        expectOK(()->{
            LibSec.withAuth("http://localhost:"+port+"/db", auth1, conn -> {
                conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            });
        });
    }
    
    @Test public void user2_query() {
        expectOK(()->{
            LibSec.withAuth("http://localhost:"+port+"/db", auth2, conn1 -> {
                try (RDFConnection conn = conn1) {
                    try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                        ResultSetFormatter.consume(qExec.execSelect());
                    }
                }});
        });
    }
    
    @Test public void user2_update() {
        expect403(()->{
            LibSec.withAuth("http://localhost:"+port+"/db", auth2, conn -> {
                conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            });
        });
    }

    @Test public void user3_query() {
        expectQuery403(()->{
            LibSec.withAuth("http://localhost:"+port+"/db", auth3, conn1 -> {
                try (RDFConnection conn = conn1) {
                    try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                        QueryExecUtils.executeQuery(qExec);
                    }
                }});
        });
    }
    
    @Test public void user3_update() {
        expectOK(()->{
            LibSec.withAuth("http://localhost:"+port+"/db", auth3, conn -> {
                conn.update("INSERT DATA { <x:s> <x:p> <x:o> }");
            });
        });
    }
}
