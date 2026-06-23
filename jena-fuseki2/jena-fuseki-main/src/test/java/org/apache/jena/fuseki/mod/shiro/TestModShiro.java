/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.mod.shiro;

import java.net.Authenticator;
import java.net.http.HttpClient;

import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.net.Host;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiMain;
import org.apache.jena.fuseki.main.FusekiServer;
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

import static org.junit.jupiter.api.Assertions.*;

public class TestModShiro {
    static final String unlocal = determineUnlocal();
    static final String localRE = "(localhost)|(127.0.0.1)|(::1)";

    static {
        FusekiLogging.setLogging();
        // In case it is finding file:log4j.properties first.
//        LogCtl.disable(Fuseki.serverLog);
//        LogCtl.disable(Fuseki.actionLog);
//        LogCtl.disable(FMod_Shiro.shiroConfigLog);
    }

    @BeforeEach void before() {
        FusekiServerCtl.clearUpSystemState();
        AuthEnv.get().clearAuthEnv();
    }

    @AfterEach void after() {
        AuthEnv.get().clearAuthEnv();
        FusekiServerCtl.clearUpSystemState();
    }

    private static String determineUnlocal() {
        // Get a string for the host in a URL that names this machine but isn't localhost.
        return Host.getHostAddressForIRI();
    }

    private String unlocalhost(FusekiServer server, String dataset) {
        String local  = server.datasetURL(dataset);
        if ( unlocal != null ) {
            local = local.replaceFirst(localRE, unlocal);
        }
        return local;
    }

    /** Builder for a server with Shiro */
    private FusekiServer.Builder serverBuilderWithShiro(String filename) {
        Lib.setenv(FusekiServerCtl.envFusekiShiro, filename);
        FusekiModules modules = FusekiModules.create(FMod_Shiro.create());
        return FusekiServer.create().port(0).fusekiModules(modules);
    }

    // Shiro resource naming is "file:<unencoded path>"
    @Test public void access_shiro_file_1() {
        String dsname = "/ds";
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = serverBuilderWithShiro("testing/Shiro/shiro withSpaceInName.ini")
                .add(dsname, dsg)
                .build();
        try { server.start(); }
        finally { server.stop(); }
    }

    @Test public void access_shiro_file_2() {
        String dsname = "/ds";
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer.Builder builder = serverBuilderWithShiro("testing/Shiro/shiro-no-such-file.ini");
        // Even if no datasets, it's an error - Shiro protects the whole server.
        assertThrows(FusekiConfigException.class, ()->builder.build());
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

            // try the ping (proxy for admin operations as admin user)
            Pattern startOfDateTimePattern  = Pattern.compile("[0-9]{4}-.*");
            {
                Authenticator authenticator = AuthLib.authenticator("admin", "pw");
                HttpClient httpClient = HttpEnv.httpClientBuilder().authenticator(authenticator).build();
                String pingResultString = HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping");
                assertTrue(startOfDateTimePattern.matcher(pingResultString).find(),
                        "admin user should be able to ping: " + pingResultString);
                AuthEnv.get().unregisterUsernamePassword(server.serverURL());
            }
            // try the ping (proxy for  operations as user1 user without access)
            {
                // check that PingResult is still unauthorized
                HttpClient httpClientAnon = HttpEnv.httpClientBuilder().build();
                HttpException httpEx = assertThrows(HttpException.class, ()->HttpOp.httpGetString(httpClientAnon, server.serverURL()+"$/ping"));
                assertEquals(401, httpEx.getStatusCode(), "Expect HTTP 401 if not logged in");
                Authenticator authenticator = AuthLib.authenticator("user1", "passwd1");
                HttpClient httpClient = HttpEnv.httpClientBuilder().authenticator(authenticator).build();
                String pingResultString = HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping");
                assertFalse(startOfDateTimePattern.matcher(pingResultString).find(),
                        "user1 user should not be able to ping: " + pingResultString);
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
        }
    }

    @Test public void access_userPassword_group() {
        String dsname = "/ds";
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = serverBuilderWithShiro("testing/Shiro/shiro_user_group_password.ini")
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

            // try the ping (proxy for admin operations as admin user)
            Pattern startOfDateTimePattern  = Pattern.compile("[0-9]{4}-.*");
            {
                Authenticator authenticator = AuthLib.authenticator("admin", "pw");
                HttpClient httpClient = HttpEnv.httpClientBuilder().authenticator(authenticator).build();
                String pingResultString = HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping");
                assertTrue(startOfDateTimePattern.matcher(pingResultString).find(),
                    "admin user should be able to ping: " + pingResultString);
                AuthEnv.get().unregisterUsernamePassword(server.serverURL());
            }
            // try the ping (proxy for  operations as user1 user without access)
            {
                // check that PingResult is still unauthorized
                HttpClient httpClientAnon = HttpEnv.httpClientBuilder().build();
                HttpException httpEx = assertThrows(HttpException.class, ()->HttpOp.httpGetString(httpClientAnon, server.serverURL()+"$/ping"));
                assertEquals(401, httpEx.getStatusCode(), "Expect HTTP 401 if not logged in");
                Authenticator authenticator = AuthLib.authenticator("user1", "passwd1");
                HttpClient httpClient = HttpEnv.httpClientBuilder().authenticator(authenticator).build();
                //  check again that the user is logged in and can access ds
                HttpOp.httpGetString(httpClient, server.datasetURL(dsname));
                System.out.println(server.serverURL()+ "$/ping");
                //String pingResultString = HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping");
                //assertEquals(pingResultString, "OK", "PingResult: " + pingResultString);

               // String pingResultString = HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping");
               // assertEquals(pingResultString, "OK", "PingResult: " + pingResultString);
                HttpException httpEx2 = assertThrows(HttpException.class, ()->HttpOp.httpGetString(httpClient, server.serverURL()+"$/ping"));
                assertEquals(403, httpEx2.getStatusCode(), "Expect HTTP 403 if not logged in" + httpEx2.getResponse() + httpEx2.getMessage() + httpEx2.getStatusLine() + httpEx2.getStatusCode());

           //     assertFalse(startOfDateTimePattern.matcher(pingResultString).find(),
               //    "user1 user should not be able to ping: " + pingResultString);
            }
            {
                // Bad password
                AuthEnv.get().registerUsernamePassword(server.serverURL(), "user1", "passwd2");
             //   attemptByLocalhost(server, dsname);
                HttpException httpEx = assertThrows(HttpException.class, ()->attemptByLocalhost(server, dsname));
                assertEquals(401, httpEx.getStatusCode(), "Expected HTTP 401");
                AuthEnv.get().unregisterUsernamePassword(server.serverURL());
            }

        } finally {
            server.stop();
        }
    }


    @Test public void shiroByCommandLine() {
        String dsname = "/ds";
        FusekiModule fmod = FMod_Shiro.create();
        FusekiModules x = FusekiModules.create(fmod);

        // And also a module!
        FusekiServer server = FusekiMain.construct(x, "--port=0", "--shiro=testing/Shiro/shiro_userpassword.ini", "--mem", dsname);
        server.start();
        try {
         // No user-password
            HttpException httpEx = assertThrows(HttpException.class, ()->attemptByLocalhost(server, dsname));
            assertEquals(401, httpEx.getStatusCode(), "Expected HTTP 401");
        } finally { server.stop(); }
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
