/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.fuseki.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.http.HttpRequest.BodyPublishers;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.graph.Node;
import org.apache.jena.http.HttpOp;
import org.apache.jena.rdfpatch.RDFPatch;
import org.apache.jena.rdfpatch.RDFPatchOps;
import org.apache.jena.rdfpatch.changes.RDFChangesCollector;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestPatchFuseki {

    private static Pair<FusekiServer, DatasetGraph> create(String ...patchEndpoints) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String dsName = "/ds";

        DataService.Builder builder = DataService.newBuilder(dsg);
        for ( String ep : patchEndpoints )
            builder.addEndpoint(Operation.Patch, ep);
        DataService dataSrv = builder.build();

        FusekiServer server = FusekiServer.create()
                //.verbose(true)
                .port(0)
                .add(dsName, dataSrv)
                .build();
        return Pair.create(server, dsg);
    }

    private static RDFPatch patch1() {
        RDFChangesCollector changes = new RDFChangesCollector();
        changes.add(node(":g"), node(":s"), node(":p"), node(":o"));
        return changes.getRDFPatch();
    }

    private static RDFPatch patch2() {
        RDFChangesCollector changes = new RDFChangesCollector();
        changes.delete(node(":g"), node(":s"), node(":p"), node(":o"));
        return changes.getRDFPatch();
    }

    private static void applyPatch(String dest, RDFPatch patch) {
        String body = RDFPatchOps.str(patch);
        // Undo at Jena 4.3.0
        //HttpOp.httpPost(dest, DeltaFuseki.patchContentType, body);
        HttpOp.httpPost(dest, WebContent.contentTypePatch, BodyPublishers.ofString(body));
    }

    private static Node node(String string) {
        return SSE.parseNode(string);
    }

    private static void runTest(BiConsumer<DatasetGraph, String> action, String...epNames) {
        Pair<FusekiServer, DatasetGraph> p = create(epNames);
        FusekiServer server = p.getLeft();
        DatasetGraph dsg = p.getRight();
        server.start();
        String url = "http://localhost:"+server.getPort();
        try {
            action.accept(dsg, url);
        } finally { server.stop(); }
    }

    @Test
    public void apply_patch_1() {
        BiConsumer<DatasetGraph, String> action = (dsg, url) -> {
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            // Service name
            applyPatch(url+"/ds/patch", patch1());
            assertTrue(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
        };
        runTest(action, "patch");
    }

    @Test
    public void apply_patch_2() {
        BiConsumer<DatasetGraph, String> action = (dsg, url) -> {
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            // Content type.
            applyPatch(url+"/ds", patch1());
            assertTrue(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            applyPatch(url+"/ds", patch2());
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
        };
        runTest(action, "");
    }

    @Test
    public void apply_patch_3() {
        BiConsumer<DatasetGraph, String> action = (dsg, url) -> {
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            applyPatch(url+"/ds/patch", patch1());
            assertTrue(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            applyPatch(url+"/ds", patch2());
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
        };
        runTest(action, "", "patch");
    }

    @Test
    public void patch_standard_setup_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        String dsName = "/ds";
        FusekiServer server = FusekiServer.create()
                //.verbose(true)
                .port(0)
                .add(dsName, dsg)
                .build();
        server.start();
        String url = "http://localhost:"+server.getPort();
        try {
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            applyPatch(url+"/ds/patch", patch1());
            assertTrue(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
            applyPatch(url+"/ds", patch2());
            assertFalse(dsg.contains(node(":g"), node(":s"), node(":p"), node(":o")));
        } finally { server.stop(); }
    }

    @Test
    public void patch_config_1() {
        FusekiServer server = FusekiServer.create()
                //.verbose(true)
                .port(0)
                .parseConfigFile("testing/Config/server-patch.ttl")
                .build();
        server.start();
        String url = "http://localhost:"+server.getPort();
        try {
            applyPatch(url+"/ds", patch1());
            RowSet rowSet = QueryExecHTTP.service(server.datasetURL("/ds")).query("SELECT * { GRAPH ?g { ?s ?p ?o } }").select();
            long x = RowSetOps.count(rowSet);
            assertEquals(1,x);
        } finally { server.stop(); }
    }
}
