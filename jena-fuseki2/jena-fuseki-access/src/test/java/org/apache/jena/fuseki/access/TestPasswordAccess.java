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

package org.apache.jena.fuseki.access;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.web.HttpCaptureResponse;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpOp.CaptureInput;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for password access to a server. */
public class TestPasswordAccess {

    private static FusekiServer fusekiServer = null;
    private static int port = WebLib.choosePort();
    private static String serverURL = "http://localhost:"+port+"/";
    private static AuthSetup authSetup = new AuthSetup("localhost", port, "user1", "pw1", "TripleStore");
    
    @BeforeClass
    public static void beforeClass() {
        if ( false )
            // To watch the HTTP headers
            LogCtl.enable("org.apache.http.headers");
        
        UserStore userStore = JettyLib.makeUserStore(authSetup.user, authSetup.password);
        ConstraintSecurityHandler sh = JettyLib.makeSecurityHandler(authSetup.realm, userStore);
        
        // Secure these areas.
        JettyLib.addPathConstraint(sh, "/ds");
        JettyLib.addPathConstraint(sh, "/nowhere");
        
        fusekiServer =
            FusekiServer.create()
                .port(port)
                .add("/ds", DatasetFactory.createTxnMem())
                .add("/open", DatasetFactory.createTxnMem())
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
    
    @Test public void access_server() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL) ) {
            assertNotNull(in);
        } catch (HttpException ex) {
            // 404 is OK - no static file area. 
            if ( ex.getResponseCode() != HttpSC.NOT_FOUND_404 )
                throw ex;
        }
    }

    @Test public void access_open() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"open") ) {
            assertNotNull(in);
        }
    }

    // Should fail.
    @Test public void access_deny_ds() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ds") ) {
            assertNotNull(in);
        } catch (HttpException ex) {
            if ( ex.getResponseCode() != HttpSC.UNAUTHORIZED_401 )
                throw ex;
        }
    }
    
    // Should be 401, not be 404.
    @Test public void access_deny_nowhere() {
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"nowhere") ) {
            assertNotNull(in);
        } catch (HttpException ex) {
            if ( ex.getResponseCode() != HttpSC.UNAUTHORIZED_401 )
                throw ex;
        }
    }
    
    @Test public void access_allow_nowhere() {
        HttpClient hc = LibSec.httpClient(authSetup);
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"nowhere", null, hc, null) ) {
            // null for 404.
            assertNull(in);
        } catch (HttpException ex) {
            if ( ex.getResponseCode() != HttpSC.NOT_FOUND_404)
                throw ex;
        }
    }
    
    @Test public void access_allow_ds() {
        HttpClient hc = LibSec.httpClient(authSetup);
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        try( TypedInputStream in = HttpOp.execHttpGet(serverURL+"ds", null, hc, null) ) {
            assertNotNull(in);
        }
    }
}
