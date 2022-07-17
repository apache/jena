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

package org.apache.jena.geosparql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.base.Sys;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.geosparql.assembler.VocabGeoSPARQL;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class TestGeoAssembler {
    private static String DIR="src/test/files/GeoAssembler/";

    @BeforeClass public static void setup() {
        try {
            //Just jul-to-slf4j is not enough
            // Is this an initialization order thing?
            // But this wipes out messages sent on the preconfigured loggers.
            Class.forName("org.slf4j.bridge.SLF4JBridgeHandler");
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        } catch (ClassNotFoundException ex) {}


        FileOps.ensureDir("target/GeoSPARQL/");
        FileOps.ensureDir("target/GeoSPARQL/DB");
        FileOps.clearDirectory("target/GeoSPARQL/DB");
        FileOps.delete("target/GeoSPARQL/DB/spatial.index");
        Dataset ds = TDB2Factory.connectDataset("target/GeoSPARQL/DB");
        ds.executeWrite(()->{
            RDFDataMgr.read(ds, DIR+"geosparql_test.rdf");
        });
    }

    @Test public void geoAssemblerTest() {
        Dataset dataset = (Dataset)AssemblerUtils.build(DIR+"geo-assembler.ttl", VocabGeoSPARQL.tGeoDataset);
        assertNotNull(dataset);
    }

    @Test public void testBasicFusekiGeoAssembler() {
        testBuildDataQuery("geo-config.ttl", "ds1");
    }

    @Test public void testExampleFusekiGeoAssembler() {
        testBuildDataQuery("geo-config-ex.ttl", "ds2");
    }

    @Test public void testMemFusekiGeoAssembler() {
        testBuildDataQuery("geo-config-mem.ttl", "ds3");
    }

    @Test public void testFusekiTextGeoTDB2() {
        assumeFalse(Sys.isWindows);
        testBuildPing("fuseki-text-geo-tdb2.ttl", "ds");
    }

    @Test public void testFusekiGeoTextTDB2() {
        assumeFalse(Sys.isWindows);
        testBuildPing("fuseki-geo-text-tdb2.ttl", "ds");
    }

    // Test for the configurations with data loaded in the assembler
    private void testBuildDataQuery(String filename, String dbName) {
        FusekiServer server = FusekiServer.create().port(0)
            .parseConfigFile("file:"+DIR+filename)
            .build();

        try {
            server.start();
            int port = server.getPort();
            String URL = "http://localhost:"+port+"/"+dbName;

            String queryStr = "PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n"
                        + "\n"
                        + "SELECT *\n"
                        + "WHERE{\n"
                        //  "{ <http://example.org/Geometry#PolygonH> ?P ?O } UNION \n"
                        + " {  <http://example.org/Geometry#PolygonH> geo:sfContains ?obj }\n"
                        //+ "    <http://example.org/Geometry#PolygonH> ?P ?obj .\n"
                        + "} ORDER by ?obj";

                RowSet rs = QueryExec.service(URL).query(queryStr).select();
                long x = RowSetOps.count(rs);
                assertEquals(8, x);
        } finally {
            server.stop();
        }
    }

    // Test for valid configurations.
    private void testBuildPing(String config, String dbName) {
        FusekiServer server = FusekiServer.create().port(0)
                .parseConfigFile("file:"+DIR+config)
                .build();
        try {
            server.start();
            int port = server.getPort();
            String URL = "http://localhost:"+port+"/"+dbName;
            boolean ask = QueryExec.service(URL).query("ASK{}").ask();
            assertTrue(ask);
        } finally {
            server.stop();
        }
    }
}
