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

import static org.junit.Assert.assertNull;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.AuthSetup;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for
 * <ul> 
 * <li>password access at the server-level
 * <li>assembler file 
 * </ul> 
 */
public class TestPasswordServer {

    // Per test.
    private FusekiServer fusekiServer = null;
    
    private static String REALM = "TripleStore";
    private AuthSetup authSetup1(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "user1", "pw1", REALM); }
    private AuthSetup authSetup2(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "user2", "pw2", REALM); }
    private AuthSetup authSetup3(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "user3", "pw3", REALM); }
    // Not in the user store.
    private AuthSetup authSetupX(FusekiServer server) { return new AuthSetup("localhost", server.getPort(), "userX", "pwX", REALM); }
    
    private static String serverURL(FusekiServer server) {
        return "http://localhost:"+server.getPort()+"/";
    }
    
    private FusekiServer fusekiServer(String configFile) {
        int port = WebLib.choosePort();
        // Read from assembler+passwd file.
        UserStore userStore = JettyLib.makeUserStore("testing/Access/passwd");
        ConstraintSecurityHandler sh = JettyLib.makeSecurityHandler(REALM, userStore);
        
        fusekiServer =
            FusekiServer.create()
                .port(port)
                .parseConfigFile(configFile)
                .securityHandler(sh)
                //.staticFileBase(".")
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
        if ( fusekiServer != null )
            fusekiServer.stop();
        HttpClient hc = HttpOp.createDefaultHttpClient();
        HttpOp.setDefaultHttpClient(hc);
    }
    
    private static void expectQuery403(Runnable action) {
        try{
            action.run();
            throw new HttpException("action completed");
        } catch (QueryExceptionHTTP ex) {
            // 404 is OK - no static file area. 
            if ( ex.getResponseCode() != HttpSC.FORBIDDEN_403 )
                throw ex;
        }
    }
    
    // Server authentication.

    @Test public void access_serverAny_user1() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-1.ttl");
        try { 
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try( TypedInputStream in = HttpOp.execHttpGet(serverURL(fusekiServer), null, hc, null) ) {
                assertNull(in);
            } catch (HttpException ex) {
                // 404 is OK - no static file area. 
                if ( ex.getResponseCode() != HttpSC.NOT_FOUND_404 )
                    throw ex;
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }
    
    @Test public void access_serverAny_db1() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-1.ttl");
        try { 
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(serverURL(fusekiServer)+"/database1").httpClient(hc).build() ) {
                conn.queryAsk("ASK{}");
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }

    @Test public void access_serverAny_db2() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-1.ttl");
        try { 
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(serverURL(fusekiServer)+"/database2").httpClient(hc).build() ) {
                conn.queryAsk("ASK{}");
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }
    
    @Test public void access_serverAny_db1_wrongUser() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-1.ttl");
        try { 
            // Must be logged in.
            HttpClient hc = LibSec.httpClient(authSetup2(fusekiServer)); // 2
            try ( RDFConnection conn = RDFConnectionRemote
                        .create().destination(serverURL(fusekiServer)+"/database1").httpClient(hc).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }

    // Specific server user.
    @Test public void access_serverUser_user1() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-2.ttl");
        try { 
            // Must be logged in as user1
            HttpClient hc = LibSec.httpClient(authSetup1(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(serverURL(fusekiServer)+"/database1").httpClient(hc).build() ) {
                conn.queryAsk("ASK{}");
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }
    
    // Specific server user.
    @Test public void access_serverUser_user2() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-2.ttl");
        try { 
            // user2 does not have service access 
            HttpClient hc = LibSec.httpClient(authSetup2(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(serverURL(fusekiServer)+"/database1").httpClient(hc).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }
    
    // Specific server user.
    @Test public void access_serverUser_user3() {
        FusekiServer fusekiServer = fusekiServer("testing/Access/config-server-2.ttl");
        try { 
            // user3 does not have server access 
            HttpClient hc = LibSec.httpClient(authSetup3(fusekiServer));
            try ( RDFConnection conn = RDFConnectionRemote
                    .create().destination(serverURL(fusekiServer)+"/database1").httpClient(hc).build() ) {
                expectQuery403(()->conn.queryAsk("ASK{}"));
            }
        } finally {
            fusekiServer.stop();
            fusekiServer = null;
        }
    }
}
