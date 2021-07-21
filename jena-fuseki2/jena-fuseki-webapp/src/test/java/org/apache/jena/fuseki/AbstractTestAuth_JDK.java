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

package org.apache.jena.fuseki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Authenticator;
import java.net.http.HttpClient;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.fuseki.cmd.JettyFusekiWebapp;
import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.fuseki.webapp.FusekiEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class AbstractTestAuth_JDK {

    @SuppressWarnings("deprecation")
    protected static final int authPort             = FusekiNetLib.choosePort();
    protected static final String authUrlRoot       = "http://localhost:"+authPort+"/";
    protected static final String authDatasetPath   = "/dataset";
    protected static final String authServiceUpdate = "http://localhost:"+authPort+authDatasetPath+"/update";
    protected static final String authServiceQuery  = "http://localhost:"+authPort+authDatasetPath+"/query";
    protected static final String authServiceREST   = "http://localhost:"+authPort+authDatasetPath+"/data";
    private static File realmFile;

    private static final String FusekiTestHome = "target/FusekiHome";
    private static final String FusekiTestBase = FusekiTestHome+"/run";

    // False when in TS_FusekiWebapp
    public static boolean RunDependently = true;

    @BeforeClass public static void setupFusekiServer() throws IOException {
        ensureEnvironment();

        realmFile = File.createTempFile("realm", ".properties");
        // user: password, realm
        try ( FileWriter writer = new FileWriter(realmFile); ) {
            writer.write("allowed: password, fuseki\n");
            writer.write("forbidden: password, other");
        }

        ServerCtl.setupServer(authPort, realmFile.getAbsolutePath(), authDatasetPath, true);
    }

    private static void ensureEnvironment() {
       if ( RunDependently ) {
           FileOps.ensureDir(FusekiTestHome);
           FileOps.clearAll(FusekiTestHome);
           System.setProperty("FUSEKI_HOME", FusekiTestHome);
           System.setProperty("FUSEKI_BASE", FusekiTestBase);
           FusekiEnv.setEnvironment();
           FusekiLogging.setLogging();
           //LogCtl.enable(Fuseki.actionLog);
       }
    }

    @AfterClass public static void teardownServer() {
        JettyFusekiWebapp.instance.stop();
    }

    public static QueryExecutionHTTP withAuthJDK(QueryExecutionHTTPBuilder builder, String user, String passwd) {
        Authenticator authenticator = AuthLib.authenticator(user, passwd);
        HttpClient hc = HttpClient.newBuilder().authenticator(authenticator).build();
        return builder.httpClient(hc).build();
    }
    public static UpdateExecutionHTTP withAuthJDK(UpdateExecutionHTTPBuilder builder, String user, String passwd) {
        Authenticator authenticator = AuthLib.authenticator(user, passwd);
        HttpClient hc = HttpClient.newBuilder().authenticator(authenticator).build();
        return builder.httpClient(hc).build();
    }
}
