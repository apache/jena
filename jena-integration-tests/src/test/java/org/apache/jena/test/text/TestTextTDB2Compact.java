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

package org.apache.jena.test.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.jena.atlas.io.IOX;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.http.HttpOp;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sys.JenaSystem;
import org.junit.Test;

/** A test of running TDB2 compaction on the storage of a text dataset. */
public class TestTextTDB2Compact {
    static {
        JenaSystem.init();
    }

    @Test
    public void tdb2CompactText() {
        LogCtl.withLevel(Fuseki.compactLog, "WARN", ()->execTdb2CompactText());
    }

    private static void execTdb2CompactText() {
        // GH-1432
        // Input files.
        String DIR = "testing/TextIndex";
        // Target
        String DB = "target/text_tdb2";

        // Ensure a clean setup.
        IOX.deleteAll(DB);
        FileOps.ensureDir(DB);

        FusekiServer server =
                FusekiServer.create()
                    .port(0)
                    .parseConfigFile(DIR+   "/conf-tdb2-text.ttl")
                    .enableCompact(true)
                    .enableTasks(true)
                    .start();
        try {
            String serverURL = "http://localhost:"+server.getHttpPort();
            GSP.service(serverURL+"/fedora").defaultGraph().POST(DIR+"/D1.ttl");
            assertEquals(1, count(serverURL));

            String str0 = HttpOp.httpPostRtnString(serverURL+"/$/compact/fedora");
            JsonObject obj0 = JSON.parse(str0);
            String taskId = obj0.get("taskId").getAsString().value();

            int x = 0;
            int attempts = 50;
            int interval = 50;

            for( ; x < attempts; x++) {
                String str1 = HttpOp.httpGetString(serverURL+"/$/tasks/"+taskId);
                JsonObject obj1 = JSON.parse(str1);
                if ( obj1.hasKey("success") )
                    break;
                if ( x == 10 )
                    interval = 100;
                if ( x == 20 )
                    interval = 200;
                Lib.sleep(interval);
            }
            if ( x >= attempts ) {
                fail("Compact did not finish in time.");
                return;
            }

            // Load data after compact
            GSP.service(serverURL+"/fedora").defaultGraph().POST(DIR+"/D2.ttl");
            assertEquals(2, count(serverURL));
        } finally {
            server.stop();
        }
    }

    private static long count(String serverURL) {
        try ( QueryExec qExec = QueryExec.service(serverURL+"/fedora")
                .query("PREFIX text:    <http://jena.apache.org/text#> SELECT * { ?x text:query 'Title' }")
                .build() ) {
            RowSet rs = qExec.select();
            long count = RowSetOps.count(rs);
            return count;
        }
    }
}
