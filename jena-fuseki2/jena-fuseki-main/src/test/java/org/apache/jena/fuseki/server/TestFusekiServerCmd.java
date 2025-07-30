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

package org.apache.jena.fuseki.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.mgt.FusekiServerCtl;
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.exec.http.Params;

/**
 * Tests Fuseki Server run with all features (FMods)from the command line.
 */
public class TestFusekiServerCmd {

    private static String FUSEKI_BASE = FusekiServerCtl.envFusekiBase;

    private static String basearea = "target/run";

    private static void deleteFusekiDir() throws IOException {
        File file = new File(basearea);
        if ( file.exists() )
            FileUtils.deleteDirectory(file);
    }

    @BeforeEach void setup() throws IOException {
        clearUp();
    }

    @AfterAll static void afterAll() throws IOException {
        clearUp();
    }

    static void clearUp() throws IOException {
        deleteFusekiDir();
        FusekiServerCtl.clearUpSystemState();
        // Put back the FUSEKI_BASE setting.
        Lib.setenv(FUSEKI_BASE, basearea);
    }

    @Test public void plainStart() {
        FusekiServer server = FusekiServerRunner.construct();
        server.start();
        server.stop();
    }

    @Test public void persistentConfigurationBlockCommandLine() {
        // Create a persistent configuration.

        String dbName = "/ds93" ;
        FusekiServer server0 = FusekiServerRunner.construct();
        server0.start();
        addDataset(server0, dbName);
        server0.stop();

        // Check run area?

        // Name clash.
        assertThrows(FusekiConfigException.class, ()-> start_stop_server("--mem", dbName) );

        // And no args is fine.
        start_stop_server();

        // Different database
        start_stop_server("--mem", "/ds99");
    }


    private void addDataset(FusekiServer server, String dbName) {
        String serverURL = server.serverURL();
        assertNotNull(serverURL);
        String actionDataset = serverURL+"$/datasets";
        String datasetURL = server.datasetURL(dbName);
        Params params = Params.create().add("dbName", dbName).add("dbType", "mem");
        // Use the template form of adding a dataset.
        HttpOp.httpPostForm(actionDataset, params);
    }

    private void start_stop_server(String... args) {
        FusekiServer server = FusekiServerRunner.construct(args);
        server.start();
        server.stop();
    }
}
