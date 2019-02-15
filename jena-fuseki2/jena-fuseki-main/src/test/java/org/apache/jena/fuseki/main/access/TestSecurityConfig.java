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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.function.Consumer;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.AuthSetup;
import org.apache.jena.web.HttpSC;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for security of the server, services and endpoints using configuration file setup.
 */
public class TestSecurityConfig {

    private static String REALM = "TripleStore";
    private AuthSetup authSetup1(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "user1", "pw1", REALM); }
    private AuthSetup authSetup2(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "user2", "pw2", REALM); }
    private AuthSetup authSetup3(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "user3", "pw3", REALM); }
    // Not in the user store.
    private AuthSetup authSetupX(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "userX", "pwX", REALM); }

    private static String serverURL(FusekiServer server) {
        return "http://localhost:"+server.getPort()+"/";
    }

    private static String datasetURL(FusekiServer server, String dsName) {
        if ( dsName.startsWith("/") )
            dsName = dsName.substring(1);
        return "http://localhost:"+server.getPort()+"/"+dsName;
    }

    private static FusekiServer fusekiServer(String configFile) {
        int port = WebLib.choosePort();
        FusekiServer fusekiServer =
            FusekiServer.create()
                .port(port)
                .parseConfigFile(configFile)
                .passwordFile("testing/Access/passwd")
                .build();
        fusekiServer.start();
        return fusekiServer;
    }

    @BeforeClass
    public static void beforeClass() {
        // Reset before every test and after the suite.
        HttpClient hc = HttpOp.createDefaultHttpClient();
        HttpOp.setDefaultHttpClient(hc);
    }

    @After
    public void after() {
        HttpClient hc = HttpOp.createDefaultHttpClient();
        HttpOp.setDefaultHttpClient(hc);
    }

    private static void expectQuery403(Runnable action) {
        expectQuery(action, HttpSC.FORBIDDEN_403);
    }

    private static void expectQuery401(Runnable action) {
        expectQuery(action, HttpSC.UNAUTHORIZED_401);
    }

    private static void expectQuery(Runnable action, int expected) {
        try {
            action.run();
            throw new HttpException("action completed");
        } catch (QueryExceptionHTTP ex) {
            if ( ex.getResponseCode() != expected )
                throw ex;
        }
    }

    private static void test(String configFile, Consumer<FusekiServer> action) {
        FusekiServer fusekiServer = fusekiServer(configFile);
        try {
            action.accept(fusekiServer);
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }

    // config-server-0.ttl : service level ACL
    @Test public void access_serverNone() {
        test("testing/Access/config-server-0.ttl", fusekiServer -> {
            // Server access.
            try (TypedInputStream in = HttpOp.execHttpGet(serverURL(fusekiServer))) {
                assertNotNull(in);
            } catch (HttpException ex) {
                // 404 is OK - no static file area.
                if ( ex.getResponseCode() != HttpSC.NOT_FOUND_404 )
                    throw ex;
            }
        });
    }    

    @Test public void access_serverNone_db1() {
        test("testing/Access/config-server-0.ttl", (fusekiServer)->{
            // db1 - secured - try no user
            try ( RDFConnection conn = RDFConnectionRemote.create().destination(datasetURL(fusekiServer, "database1"))
                    .build() ) {
                expectQuery401(()->conn.queryAsk("ASK{}"));
            }
            // db1 - secured - try wrong user
            HttpClient hcUser2 = LibSec.httpClient(authSetup2(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote.create().destination(datasetURL(fusekiServer, "database1"))
                    .httpClient(hcUser2)
                    .build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
            
            // db1 - secured - with user
            HttpClient hcUser1 = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote.create().destination(datasetURL(fusekiServer, "database1"))
                    .httpClient(hcUser1)
                    .build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    @Test public void access_serverNone_db2() {
        test("testing/Access/config-server-0.ttl", (fusekiServer)->{
            try ( RDFConnection conn = RDFConnectionRemote.create()
                    .destination(datasetURL(fusekiServer, "database2"))
                    // No HttpClient.
                    .build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    // config-server-1.ttl : server=*; service level ACL
    @Test public void access_serverAny_user1() {
        test("testing/Access/config-server-1.ttl", fusekiServer->{
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try( TypedInputStream in = HttpOp.execHttpGet(serverURL(fusekiServer), null, hc, null) ) {
                assertNull(in);
            } catch (HttpException ex) {
                // 404 is OK - no static file area. 
                if ( ex.getResponseCode() != HttpSC.NOT_FOUND_404 )
                    throw ex;
            }
        });
    }

    @Test public void access_dataset_db1() {
        test("testing/Access/config-server-1.ttl", fusekiServer->{
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "database1")).httpClient(hc).build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    @Test public void access_dataset_db2() {
        test("testing/Access/config-server-1.ttl", fusekiServer->{
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "database2")).httpClient(hc).build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    @Test public void access_dataset_db1_wrongUser() {
        test("testing/Access/config-server-1.ttl", fusekiServer->{
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup2(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                        .create().destination(datasetURL(fusekiServer, "database1")).httpClient(hc).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    // config-server-2.ttl : server=user1,user2; service level ACL
    @Test public void access_dataset_user1() {
        test("testing/Access/config-server-2.ttl", fusekiServer->{
            // Must be logged in as user1
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "database1")).httpClient(hc).build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    // Specific server user.
    @Test public void access_dataset_user2() {
        test("testing/Access/config-server-2.ttl", fusekiServer->{ 
            // user2 does not have service access 
            HttpClient hc = LibSec.httpClient(authSetup2(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "database1")).httpClient(hc).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    // Specific server user.
    @Test public void access_dataset_user3() {
        test("testing/Access/config-server-2.ttl", fusekiServer->{
            // user3 does not have server access 
            HttpClient hc = LibSec.httpClient(authSetup3(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "database1")).httpClient(hc).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    // config-server-3.ttl : service and endpoint
    @Test public void serviceAndEndpoint_anon() {
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            try ( RDFConnection conn = RDFConnectionRemote
                .create().destination(datasetURL(fusekiServer, "db")).build() ) {
                expectQuery401(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    @Test public void serviceAndEndpoint_unknownUser() {
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            HttpClient hc = LibSec.httpClient(authSetupX(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                .create().destination(datasetURL(fusekiServer, "db")).httpClient(hc).build() ) {
                // Fails authentication.
                expectQuery401(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    // Deny - not in every endpoint
    @Test public void serviceAndEndpoint_user1() {
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            HttpClient hc1 = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db"))
                    .httpClient(hc1).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }
    
    // Go to endpoint.
    @Test public void serviceAndEndpointDirect_user1() {
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            HttpClient hc1 = LibSec.httpClient(authSetup1(fusekiServer));
            
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db"))
                    .queryEndpoint(datasetURL(fusekiServer, "db")+"/query1")
                    .httpClient(hc1).build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    @Test public void serviceAndEndpoint_user2() {
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            HttpClient hc2 = LibSec.httpClient(authSetup2(fusekiServer));
            // -- Dataset query. User2 is not in dataset.
            try ( RDFConnection conn = RDFConnectionRemote
                .create().destination(datasetURL(fusekiServer, "db")).httpClient(hc2).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }
    
    // Still "no" - dataset excludes.
    @Test public void serviceAndEndpointDirect_user2() {
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            HttpClient hc2 = LibSec.httpClient(authSetup2(fusekiServer));
            // -- Dataset query. User2 is not in dataset.
            try ( RDFConnection conn = RDFConnectionRemote
                .create().destination(datasetURL(fusekiServer, "db"))
                .queryEndpoint(datasetURL(fusekiServer, "db")+"/query2")
                .httpClient(hc2).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    @Test public void serviceAndEndpoint_user3() {
        // Not user3 - it is not in every endpoint.
        test("testing/Access/config-server-3.ttl", fusekiServer->{
            HttpClient hc3 = LibSec.httpClient(authSetup3(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                .create().destination(datasetURL(fusekiServer, "db")).httpClient(hc3).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }
    
    // config-server-4.ttl : endpoint only.
    // Deny - not in every endpoint
    @Test public void endpoint_user1() {
        test("testing/Access/config-server-4.ttl", fusekiServer->{
            HttpClient hc1 = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db2"))
                    .httpClient(hc1).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }
    

    // Go to endpoint.
    @Test public void endpointDirect_user1() {
        test("testing/Access/config-server-4.ttl", fusekiServer->{
            HttpClient hc1 = LibSec.httpClient(authSetup1(fusekiServer));
            
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db2"))
                    .queryEndpoint(datasetURL(fusekiServer, "db2")+"/query1")
                    .httpClient(hc1).build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }

    @Test public void endpoint_user2() {
        test("testing/Access/config-server-4.ttl", fusekiServer->{
            HttpClient hc2 = LibSec.httpClient(authSetup2(fusekiServer));
            // -- Dataset query. User2 is not in dataset.
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db2")).httpClient(hc2).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }
    
    // Yes by endpoint only.
    @Test public void endpointDirect_user2() {
        test("testing/Access/config-server-4.ttl", fusekiServer->{
            HttpClient hc2 = LibSec.httpClient(authSetup2(fusekiServer));
            // -- Dataset query. User2 is onthis specific endpoint.
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db2"))
                    .queryEndpoint(datasetURL(fusekiServer, "db2")+"/query2")
                    .httpClient(hc2).build() ) {
                conn.queryAsk("ASK{}");
            }
        });
    }
    
    // No - not at this endpoint.
    @Test public void endpointDirect_user2a() {
        test("testing/Access/config-server-4.ttl", fusekiServer->{
            HttpClient hc2 = LibSec.httpClient(authSetup2(fusekiServer));
            // -- Dataset query. User2 is onthis specific endpoint.
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db2"))
                    .queryEndpoint(datasetURL(fusekiServer, "db2")+"/query3")
                    .httpClient(hc2).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }

    @Test public void endpoint_user3() {
        // Not user3 - it is not in every endpoint.
        test("testing/Access/config-server-4.ttl", fusekiServer->{
            HttpClient hc3 = LibSec.httpClient(authSetup3(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(datasetURL(fusekiServer, "db2")).httpClient(hc3).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        });
    }
}
