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
import static org.junit.Assert.fail;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.riot.web.HttpCaptureResponse;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpOp.CaptureInput;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.web.AuthSetup;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for access to services using programmatic setup.
 * <p>
 * See {@link TestSecurityConfig} for tests with server-level access control and also
 * access control by assembler
 * <p>
 * See {@link TestSecurityFilterFuseki} for graph-level access control.
 */
public class TestSecurityBuilderSetup {

    private static FusekiServer fusekiServer = null;
    private static int port = WebLib.choosePort();
    private static String serverURL = "http://localhost:"+port+"/";
    private static AuthSetup authSetup1 = new AuthSetup("localhost", port, "user1", "pw1", "TripleStore");
    private static AuthSetup authSetup2 = new AuthSetup("localhost", port, "user2", "pw2", "TripleStore");
    // Not in the user store.
    private static AuthSetup authSetupX = new AuthSetup("localhost", port, "userX", "pwX", "TripleStore");

    @BeforeClass
    public static void beforeClass() {
        if ( false )
            // To watch the HTTP headers
            LogCtl.enable("org.apache.http.headers");

        // Two authorized users.
        UserStore userStore = new UserStore();
        JettyLib.addUser(userStore, authSetup1.user, authSetup1.password);
        JettyLib.addUser(userStore, authSetup2.user, authSetup2.password);
        try { userStore.start(); }
        catch (Exception ex) { throw new RuntimeException("UserStore", ex); }

        ConstraintSecurityHandler sh = JettyLib.makeSecurityHandler(authSetup1.realm, userStore);

        // Secure these areas.
        // User needs to be logged in.
        JettyLib.addPathConstraint(sh, "/ds");
        // Allow auth control even through there isn't anything there
        JettyLib.addPathConstraint(sh, "/nowhere");
        // user1 only.
        JettyLib.addPathConstraint(sh, "/ctl");

        // Not controlled: "/open"

        DataService dSrv = new DataService(DatasetGraphFactory.createTxnMem());
        FusekiConfig.populateStdServices(dSrv, false);
        AuthPolicy reqAuth = Auth.policyAllowSpecific("user1");
        dSrv.setAuthPolicy(reqAuth);

        fusekiServer =
            FusekiServer.create()
                .port(port)
                .add("/ds", DatasetFactory.createTxnMem())
                .add("/open", DatasetFactory.createTxnMem())
                .add("/ctl", dSrv)
                .securityHandler(sh)
                //.staticFileBase(".")
                .build();
        fusekiServer.start();
    }

    @Before
    public void before() {
        // Reset before every test and after the suite.
        HttpClient hc = HttpOp.createDefaultHttpClient();
        HttpOp.setDefaultHttpClient(hc);
    }

    @AfterClass
    public static void afterClass() {
        fusekiServer.stop();
        HttpClient hc = HttpOp.createDefaultHttpClient();
        HttpOp.setDefaultHttpClient(hc);
    }

    // Server authentication.

    @Test public void access_server() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL) ) {
            assertNotNull(in);
            fail("Didn't expect to succeed");
        } catch (HttpException ex) {
            // 404 is OK - no static file area.
            if ( ex.getStatusCode() != HttpSC.NOT_FOUND_404 )
                throw ex;
        }
    }

    @Test public void access_open() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"open") ) {
            assertNotNull(in);
        }
    }

    @Test public void access_open_user1() {
        // OK.
        LibSec.withAuth(serverURL+"open", authSetup1, (conn)->{
            conn.queryAsk("ASK{}");
        });
    }

    @Test public void access_open_userX() {
        // OK.
        LibSec.withAuth(serverURL+"open", authSetupX, (conn)->{
            conn.queryAsk("ASK{}");
        });
    }


    // Should fail.
    @Test public void access_deny_ds() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ds") ) {
            fail("Didn't expect to succeed");
        } catch (HttpException ex) {
            if ( ex.getStatusCode() != HttpSC.UNAUTHORIZED_401 )
                throw ex;
        }
    }

    // Should be 401, not be 404.
    @Test public void access_deny_nowhere() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"nowhere") ) {
            fail("Didn't expect to succeed");
        } catch (HttpException ex) {
            if ( ex.getStatusCode() != HttpSC.UNAUTHORIZED_401 )
                throw ex;
        }
    }

    @Test public void access_allow_nowhere() {
        HttpClient hc = LibSec.httpClient(authSetup1);
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"nowhere", null, hc, null) ) {
            // null for 404.
            assertNull(in);
        } catch (HttpException ex) {
            if ( ex.getStatusCode() != HttpSC.NOT_FOUND_404)
                throw ex;
        }
    }

    @Test public void access_allow_ds() {
        HttpClient hc = LibSec.httpClient(authSetup1);
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ds", null, hc, null) ) {
            assertNotNull(in);
        }
    }

    // Service level : ctl.
    @Test public void access_service_ctl_user1() {
        // user1 -- allowed.
        HttpClient hc = LibSec.httpClient(authSetup1);
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ctl", null, hc, null) ) {
            assertNotNull(in);
        }
    }

    @Test public void access_service_ctl_user2() {
        // user2 -- can login, not allowed.
        HttpClient hc = LibSec.httpClient(authSetup2);
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ctl", null, hc, null) ) {
            fail("Didn't expect to succeed");
        } catch (HttpException ex) {
            if ( ex.getStatusCode() != HttpSC.FORBIDDEN_403)
                throw ex;
        }
    }

    @Test public void access_service_ctl_userX() {
        // userX -- can't login, not allowed.
        HttpClient hc = LibSec.httpClient(authSetupX);
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ctl", null, hc, null) ) {
            fail("Didn't expect to succeed");
        } catch (HttpException ex) {
            if ( ex.getStatusCode() != HttpSC.UNAUTHORIZED_401)
                throw ex;
        }
    }
}
