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

package org.apache.jena.fuseki.mod.shiro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.net.Host;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.cmds.FusekiMain;
import org.apache.jena.fuseki.main.sys.FusekiModule;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpOp;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;

public class TestModShiro {
    static final String unlocal = Host.getHostAddress();
    static final String localRE = Pattern.quote("localhost");

    static {
        FusekiLogging.setLogging();
        // Incase it is finding file:log4j.properties first.
//        LogCtl.disable(Fuseki.serverLog);
//        LogCtl.disable(Fuseki.actionLog);
//        LogCtl.disable(FMod_Shiro.shiroConfigLog);
    }

    @BeforeEach void before() {
        System.getProperties().remove(FusekiServerCtl.envFusekiShiro);
        AuthEnv.get().clearAuthEnv();
    }

    @AfterEach void after() {
        AuthEnv.get().clearAuthEnv();
    }

    @AfterAll static void afterAll() {
        System.getProperties().remove(FusekiServerCtl.envFusekiShiro);
    }

    private String unlocalhost(FusekiServer server, String dataset) {
        String local  = server.datasetURL(dataset);
        if ( unlocal != null )
            local = local.replaceFirst(localRE, unlocal);
        return local;
    }

    /** Builder for a server with Shiro */
    private FusekiServer.Builder serverBuilderWithShiro(String filename) {
        System.getProperties().setProperty(FusekiServerCtl.envFusekiShiro, filename);
        FusekiModules modules = FusekiModules.create(FMod_Shiro.create());
        return FusekiServer.create()
                .port(0)
                .fusekiModules(modules);
    }

    @Test public void access_localhost() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiModules modules = FusekiModules.create(FMod_Shiro.create());
        FusekiServer server = serverBuilderWithShiro("testing/Shiro/shiro_localhost.ini")
                .add("/local/ds", dsg)
                .add("/public/ds", dsg)
                .build();
        server.start();
        String dsPublic = "/public/ds";
        String dsLocal = "/local/ds";
        try {
            attemptByAddr(server, dsPublic);
            HttpException httpEx = assertThrows(HttpException.class, ()->attemptByAddr(server, dsLocal));
            assertEquals(403, httpEx.getStatusCode(), "Expected HTTP 403");

            attemptByLocalhost(server, dsLocal);
        } finally {
            server.stop();
            AuthEnv.get().clearAuthEnv();
        }
    }

    @Test public void access_userPassword() {
        String dsname = "/ds";
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = serverBuilderWithShiro("testing/Shiro/shiro_userpassword.ini")
                .add(dsname, dsg)
                .enablePing(true)
                .build();
        server.start();

        String URL = server.datasetURL(dsname);

        try {
            // No user-password
            {
                HttpException httpEx = assertThrows(HttpException.class, ()->attemptByLocalhost(server, dsname));
                assertEquals(401, httpEx.getStatusCode(), "Expected HTTP 401");
            }

            // user-password via authenticator: localhost
            {
                Authenticator authenticator = AuthLib.authenticator("user1", "passwd1");
                HttpClient httpClient = HttpEnv.httpClientBuilder().authenticator(authenticator).build();
                attemptByLocalhost(server, httpClient, dsname);
                // and a SPARQL query
                QueryExecHTTP.service(URL).httpClient(httpClient).query("ASK{}").ask();
            }

            // user-password via registration
            {
                AuthEnv.get().registerUsernamePassword(server.serverURL(), "user1", "passwd1");
                attemptByLocalhost(server, dsname);
                AuthEnv.get().unregisterUsernamePassword(server.serverURL());
            }

            // try the ping (proxy for admin operations)
            {
                Authenticator authenticator = AuthLib.authenticator("admin", "pw");
                HttpClient httpClient = HttpEnv.httpClientBuilder().authenticator(authenticator).build();
                HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping");
                AuthEnv.get().unregisterUsernamePassword(server.serverURL());
            }

            {
                // Bad password
                AuthEnv.get().registerUsernamePassword(server.serverURL(), "user1", "passwd2");
                HttpException httpEx = assertThrows(HttpException.class, ()->attemptByLocalhost(server, dsname));
                assertEquals(401, httpEx.getStatusCode(), "Expected HTTP 401");
                AuthEnv.get().unregisterUsernamePassword(server.serverURL());
            }

        } finally {
            server.stop();
            AuthEnv.get().clearAuthEnv();
        }
    }

    @Test public void shiroByCommandLine() {
        String dsname = "/ds";
        FusekiModule fmod = FMod_Shiro.create();
        FusekiMain.addCustomiser(fmod);

        // And also a module!
        FusekiServer server = FusekiMain.builder("--port=0", "--shiro=testing/Shiro/shiro_userpassword.ini", "--mem", dsname)
                // Must be same instance.
                .fusekiModules(FusekiModules.create(fmod))
                .build();
        server.start();
        try {
         // No user-password
            HttpException httpEx = assertThrows(HttpException.class, ()->attemptByLocalhost(server, dsname));
            assertEquals(401, httpEx.getStatusCode(), "Expected HTTP 401");
        } finally { server.stop(); }
        FusekiMain.resetCustomisers();
    }

    // ----------------------------

    // TEST **** command line

    private void attemptByAddr(FusekiServer server, String dsname) {
        attemptByAddr(server, null, dsname);
    }

    private void attemptByAddr(FusekiServer server, HttpClient httpClient, String dsname) {
        String URL = server.datasetURL(dsname);
        String URLip = unlocalhost(server, dsname);
        attempt(URLip, httpClient);
    }

    private void attemptByLocalhost(FusekiServer server, String dsname) {
        attemptByLocalhost(server, null, dsname);
    }

    private void attemptByLocalhost(FusekiServer server, HttpClient httpClient, String dsname) {
        String URL = server.datasetURL(dsname);
        attempt(URL, httpClient);
    }

    private void attempt(String URL, HttpClient httpClient) {
        GSP gsp = GSP.service(URL).defaultGraph();
        if ( httpClient != null )
            gsp.httpClient(httpClient);
        Graph g = gsp.GET();
        assertNotNull(g);
    }
}
