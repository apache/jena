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

package org.apache.jena.fuseki.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.assembler.NamedDatasetAssembler;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Testing configurations involving shared datasets
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestFusekiDatasetSharing {
    static {
        JenaSystem.init();
    }

    private  static String DIR = "testing/Config/";

    private static FusekiServer server = null;
    private static String URL_ds_named;
    // Shared named storage with ds-named.
    private static String URL_ds_rdfs_base_named;
    // Shared named storage with ds-named.
    private static String URL_ds_view_base_named;

    private static String URL_ds_unnamed_1;
    private static String URL_ds_unnamed_2;

    private static String URL_ds_view_unnamed_1;
    private static String URL_ds_view_unnamed_2;

    @BeforeClass public static void beforeClass() {
        NamedDatasetAssembler.sharedDatasetPool.clear();
        Graph g = RDFParser.source(DIR+"ds-sharing.ttl").lang(Lang.TTL).toGraph();

        server = FusekiServer.create().port(0).parseConfig(g).start();
        //FusekiMainInfo.logServer(Fuseki.serverLog, server, true);

        // fuseki:name "ds-named"
        // fuseki:name "ds-rdfs-base-named"
        // fuseki:name "ds-view-base-named"

        // fuseki:name "ds-unnamed-1"
        // fuseki:name "ds-unnamed-2"

        // fuseki:name "ds-view-base-unnamed-1"
        // fuseki:name "ds-view-base-unnamed-2"

        URL_ds_named = server.datasetURL("/ds-named");
        // Shared named storage with ds-named.
        URL_ds_rdfs_base_named = server.datasetURL("/ds-rdfs-base-named");
        // Shared named storage with ds-named.
        URL_ds_view_base_named = server.datasetURL("/ds-view-base-named");

        URL_ds_unnamed_1 = server.datasetURL("/ds-unnamed-1");
        URL_ds_unnamed_2 = server.datasetURL("/ds-unnamed-2");

        URL_ds_view_unnamed_1 = server.datasetURL("/ds-view-base-unnamed-1");
        URL_ds_view_unnamed_2 = server.datasetURL("/ds-view-base-unnamed-2");

        List<String> all1 = List.of(URL_ds_named,
                                    URL_ds_rdfs_base_named,
                                    URL_ds_view_base_named,
                                    URL_ds_unnamed_1,
                                    URL_ds_unnamed_2,
                                    URL_ds_view_unnamed_1,
                                    URL_ds_view_unnamed_2);
        Set<String> all2 = new HashSet<>(all1);
        assertEquals(all1.size(), all2.size());
    }

    @AfterClass public static void afterClass() {
        if ( server != null )
            server.stop();
        NamedDatasetAssembler.sharedDatasetPool.clear();
    }
        // Tests:
        /*
         * Add to URL_ds_named, see in URL_ds_rdfs_base_named
         * Add to URL_ds_named, do not see in URL_ds_unnamed
         * Add to URL_ds_unnamed_1, do see in URL_ds_unnamed2 (Fuseki sharing).
         * Add to ds-view-base-unnamed, do not see in URL_ds_unnamed2
         */

    @Test public void ds_1_sharing_named() {
        test(URL_ds_named, URL_ds_rdfs_base_named, true);
    }

    @Test public void ds_2_sharing_named() {
        test(URL_ds_named, URL_ds_view_base_named, true);
    }

    @Test public void ds_3_sharing_named_unnamed() {
        test(URL_ds_named, URL_ds_unnamed_1, false);
    }

    @Test public void ds_4_sharing_unnamed_unnamed() {
        test(URL_ds_unnamed_1, URL_ds_unnamed_2, true); // Fuseki sharing.
    }

    // Both base datasets are one step down -> no Fuseki sharing.
    @Test public void ds_5_sharing_view_unnamed_view_unnamed() {
        test(URL_ds_view_unnamed_1, URL_ds_view_unnamed_2, false);
    }
    // One dataset is one step down -> no Fuseki sharing despite same resource ds-unnamed-1
    @Test public void ds_6_sharing_view_unnamed_unnamed() {
        test(URL_ds_view_unnamed_1, URL_ds_unnamed_1, false);
    }

    // One dataset is one step down -> no Fuseki sharing despite same resource ds-unnamed-1
    @Test public void ds_7_sharing_unnamed_view_unnamed() {
        test(URL_ds_unnamed_1, URL_ds_view_unnamed_1, false);
    }

    private static void test(String URL1, String URL2, boolean canSee) {
        String msg = String.format("URL1 = %s :: URL2 = %s :; expected = %s\n", URL1, URL2, canSee);
        Graph dataOneTriple = SSE.parseGraph("(graph (:s :p1 :o))");
        Graph dataZeroTriples = SSE.parseGraph("(graph)");

        // Clear targets.
        GSP.service(URL1).defaultGraph().PUT(dataZeroTriples);
        GSP.service(URL2).defaultGraph().PUT(dataZeroTriples);

        // Add data to first one.
        GSP.service(URL1).defaultGraph().PUT(dataOneTriple);

        // Fetch from the second one.
        Graph data2 = GSP.service(URL2).defaultGraph().GET();

        if ( canSee )
            assertFalse(msg, data2.isEmpty());
        else
            assertTrue(msg,data2.isEmpty());
    }

}
