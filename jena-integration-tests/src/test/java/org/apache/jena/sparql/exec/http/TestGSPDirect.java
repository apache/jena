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

package org.apache.jena.sparql.exec.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.http.HttpOp;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.web.HttpSC;

/** Tests specifically for GSP direct naming. */
@EnabledIf(value="isEnabled_GSPDirectNaming", disabledReason ="GSP_DIRECT_NAMING not enabled")
public class TestGSPDirect {

    static boolean isEnabled_GSPDirectNaming() {
        return Fuseki.GSP_DIRECT_NAMING;
    }

    private FusekiServer server = null;
    private final boolean verbose = false;

    private final String dsName = "/graphs";
    private final String endpoint = "graphs";

    private static String configNamedService = """
            PREFIX :        <http://test/config#>
            PREFIX fuseki:  <http://jena.apache.org/fuseki#>
            PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>

            :service rdf:type fuseki:Service ;
                fuseki:name "/gsp-direct" ;
                fuseki:endpoint [ fuseki:operation fuseki:gsp-direct-rw ; ] ;
                fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
                fuseki:dataset [ rdf:type ja:MemoryDataset; ]
            .
            """;
    private static String configTwoServices = """
            PREFIX :        <http://test/config#>
            PREFIX fuseki:  <http://jena.apache.org/fuseki#>
            PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>

            ## Load data with indirect GSP
            :service1 rdf:type fuseki:Service ;
                fuseki:name "/gsp-rw" ;
                fuseki:endpoint [ fuseki:operation fuseki:gsp-rw ; ] ;
                fuseki:dataset :dataset ;
                .
            ## Access with direct
            :service2 rdf:type fuseki:Service ;
                fuseki:name "/gsp-direct-r" ;
                fuseki:endpoint [ fuseki:operation fuseki:gsp-direct-r ; ] ;
                fuseki:dataset :dataset ;
                .

            :dataset rdf:type ja:MemoryDataset .
            """;
    //At the root!
    private static String configRootService = """
            PREFIX :        <http://test/config#>
            PREFIX fuseki:  <http://jena.apache.org/fuseki#>
            PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>

            :service rdf:type fuseki:Service ;
                fuseki:name "/" ;
                fuseki:endpoint [ fuseki:operation fuseki:gsp-direct-rw ; ] ;
                fuseki:endpoint [ fuseki:operation fuseki:query ; ] ;
                fuseki:dataset [ rdf:type ja:MemoryDataset; ]
            .
            """;


    // Not @BeforeEach - some servers are different.
    public void makeServer() {
        server = makeServer(dsName, endpoint, verbose);
    }

    private static FusekiServer makeServer(String dsName, String gspEndpoint, boolean verbose) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        DataService dSrv = DataService.newBuilder(dsg)
                // Direct
                .addEndpoint(Operation.GSP_Direct_RW)
                // Indirect (i.e. ?default, ?graph=)
                .addEndpoint(Operation.GSP_RW)
                .build();
        FusekiServer.create().add(dsName, dSrv).build();

        FusekiServer server = FusekiServer.create()
                .port(0)
                .verbose(verbose)
                .enablePing(true)
                .add(gspEndpoint, dSrv)
                .build()
                .start();
        return server;
    }

    @AfterEach
    public void releaseServer() {
        if ( server != null )
            server.stop();
    }

    @Test
    public void gsp_direct_POST_GET () {
        makeServer();
        String serviceURL = server.serverURL()+endpoint;
        String URL1 = serviceURL+"/dir/file";
        String URL2 = serviceURL+"/dir/noSuchData";

        Graph data1 = GraphFactory.createDefaultGraph();
        data1.add(SSE.parseTriple("(:s :p 123)"));

        GSP.service(serviceURL).directGraphName(URL1).POST(data1);

        Graph data2 = GraphFactory.createDefaultGraph();
        data2.add(SSE.parseTriple("(:s :p 456)"));
        GSP.service(serviceURL).directGraphName(URL1).POST(data2);

        Graph g1 = GSP.service(serviceURL).directGraphName(URL1).GET();
        assertEquals(2, g1.size());
    }

    @Test
    public void gsp_direct_PUT_GET () {
        makeServer();
        String serviceURL = server.serverURL()+endpoint;
        String URL1 = serviceURL+"/dir/file";
        String URL2 = serviceURL+"/dir/noSuchData";

        Graph data1 = GraphFactory.createDefaultGraph();
        data1.add(SSE.parseTriple("(:s :p 123)"));

        GSP.service(serviceURL).directGraphName(URL1).PUT(data1);

        Graph data2 = GraphFactory.createDefaultGraph();
        data2.add(SSE.parseTriple("(:s :p 456)"));
        GSP.service(serviceURL).directGraphName(URL1).PUT(data2);

        Graph fetch = GSP.service(serviceURL).directGraphName(URL1).GET();
        assertEquals(1, fetch.size());
    }

    @Test
    public void gsp_direct_PUT_DELETE_GET () {
        makeServer();
        String serviceURL = server.serverURL()+endpoint;
        String URL1 = serviceURL+"/dir/file";
        String URL2 = serviceURL+"/dir/noSuchData";

        Graph data1 = GraphFactory.createDefaultGraph();
        data1.add(SSE.parseTriple("(:s :p 123)"));
        HttpException ex = assertThrows(HttpException.class, ()->GSP.service(serviceURL).directGraphName(URL1).DELETE());
        assertEquals(HttpSC.NOT_FOUND_404, ex.getStatusCode());
    }


    @Test
    public void gsp_direct_POST_GET_different () {
        makeServer();
        String serviceURL = server.serverURL()+endpoint;
        String URL1 = serviceURL+"/dir/file";
        String URL2 = serviceURL+"/dir/noSuchData";

        Graph data = GraphFactory.createDefaultGraph();
        data.add(SSE.parseTriple("(:s :p :o)"));
        GSP.service(serviceURL).directGraphName(URL1).POST(data);
        FusekiTestLib.expect404(()-> GSP.service(serviceURL).directGraphName(URL2).GET() );
    }

    @Test
    public void gsp_direct_POST_GET_byQuads () {
        makeServer();
        String serviceURL = server.serverURL()+endpoint;
        String URL1 = serviceURL+"/dir/file1";

        Graph data = GraphFactory.createDefaultGraph();
        data.add(SSE.parseTriple("(:s :p :o)"));

        GSP.service(serviceURL).directGraphName(URL1).POST(data);

        DatasetGraph dsg = DSP.service(serviceURL).GET();
        assertTrue(dsg.getDefaultGraph().isEmpty(), "Dataset default graph is not empty");

        Node gn = NodeFactory.createURI(URL1);
        assertEquals(1, dsg.getGraph(gn).size());
    }

    @Test
    public void gsp_direct_POST_GET_usingDSP () {
        makeServer();
        // Fake using DSP.
        String URL = server.serverURL()+endpoint+"/dir/file";
        String service = server.serverURL()+endpoint;

        Graph data = GraphFactory.createDefaultGraph();
        data.add(SSE.parseTriple("(:s :p :o)"));

        GSP.service(service).graphName(URL).POST(data);
        // Retrieve

        Graph g  = DSP.service(URL).GET().getDefaultGraph();
        assertEquals(1, g.size());
    }

    @Test
    public void gsp_direct_bad_URL() {
        makeServer();
        String serviceURL = server.serverURL()+endpoint;
        String URL1 = "http://somewhere.test/dir/file1";
        HttpException ex = assertThrows(HttpException.class, ()->GSP.service(serviceURL).directGraphName(URL1));
        // Local error.
        assertEquals(-1, ex.getStatusCode());
        assertTrue(StrUtils.containsIgnoreCase(ex.getMessage(),"direct"));
    }

    @Test
    public void gsp_direct_config_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Graph configGraph = RDFParser.fromString(configNamedService, Lang.TTL).toGraph();
        server = FusekiServer.create()
                .port(0)
                .verbose(verbose)
                .enablePing(true)
                .parseConfig(configGraph)
                .build()
                .start();
        String pingStr = HttpOp.httpGetString(server.serverURL()+"$/ping");
        assertNotNull(pingStr);

        String serviceURL = server.datasetURL("/gsp-direct");
        String URL1 = serviceURL+"/dir/file";
        String URL2 = serviceURL+"/dir/noSuchData";
        Graph data = GraphFactory.createDefaultGraph();
        data.add(SSE.parseTriple("(:s :p :o)"));
        GSP.service(serviceURL).directGraphName(URL1).POST(data);
        FusekiTestLib.expect404(()-> GSP.service(serviceURL).directGraphName(URL2).GET() );
    }

    @Test
    public void gsp_direct_config_2() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Graph configGraph = RDFParser.fromString(configRootService, Lang.TTL).toGraph();
        server = FusekiServer.create()
                .port(0)
                .verbose(verbose)
                .enablePing(true)
                .parseConfig(configGraph)
                .build()
                .start();
        String serviceURL = server.datasetURL("/");
        String URL1 = serviceURL+"dir/file";
        String URL2 = serviceURL+"dir/noSuchData";
        Graph data = GraphFactory.createDefaultGraph();
        data.add(SSE.parseTriple("(:s :p :o)"));
        GSP.service(serviceURL).directGraphName(URL1).POST(data);
        FusekiTestLib.expect404(()-> GSP.service(serviceURL).directGraphName(URL2).GET() );

        String pingStr = HttpOp.httpGetString(server.serverURL()+"$/ping");
        assertNotNull(pingStr);
    }

    @Test
    public void gsp_direct_config_3() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Graph configGraph = RDFParser.fromString(configTwoServices, Lang.TTL).toGraph();
        server = FusekiServer.create()
                .port(0)
                .verbose(verbose)
                .parseConfig(configGraph)
                .build()
                .start();
        String serviceURL1 = server.datasetURL("/gsp-rw");
        String serviceURL2 = server.datasetURL("/gsp-direct-r");
        String URL = serviceURL2+"/dir/file";

        Graph data = GraphFactory.createDefaultGraph();
        data.add(SSE.parseTriple("(:s :p 'abc')"));

        // Write using GS indirect naming - graph name is use naming scheme of the read service.
        GSP.service(serviceURL1).graphName(URL).POST(data);

        // access
        Graph read = GSP.service(serviceURL2).directGraphName(URL).GET();
        assertEquals(1,read.size());

        // attempt to delete
        int sc = FusekiTestLib.expectFail(()-> GSP.service(serviceURL2).directGraphName(URL).DELETE() );
        assertEquals(sc, HttpSC.METHOD_NOT_ALLOWED_405);
    }


}
