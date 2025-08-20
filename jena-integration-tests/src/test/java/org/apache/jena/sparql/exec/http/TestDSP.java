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

package org.apache.jena.sparql.exec.http;

import static org.apache.jena.fuseki.test.HttpTest.expect404;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.http.HttpOp;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.IsoMatcher;

public class TestDSP {

    static String DIR = "testing/RDFLink/";

    private FusekiServer server = null;
    private final String dsName = "/data";
    private final boolean verbose = false;


    @BeforeEach public void makeServer() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        server = FusekiServer.create()
                .verbose(verbose)
                .enablePing(true)
                //.addServlet(data, holder)
                .add(dsName, dsg)
                .build()
                .start();
    }

    @AfterEach public void releaseServer() {
        if ( server != null )
            server.stop();
    }


    private String url(String path) {
        return server.datasetURL(path);
    }

    // GSP endpoint
    private String dspServiceURL() {
        return url(dsName);
    }

    static DatasetGraph dataset = makeDatasetGraph();
    static DatasetGraph makeDatasetGraph() {
        DatasetGraph dataset = DatasetGraphFactory.createTxnMem();
        RDFDataMgr.read(dataset, new StringReader("PREFIX : <http://example/> :s :p :o . :g { :sg :pg :og }"), null, Lang.TRIG);
        return dataset;
    }

    // ----------------------------------------

    @Test public void dsp_put_get_01() {
        DSP.service(dspServiceURL()).PUT(dataset);
        DatasetGraph dsg = DSP.service(dspServiceURL()).GET();
        assertNotNull(dsg);
        assertTrue(IsoMatcher.isomorphic(dataset, dsg));
    }

    @Test public void dsp_post_get_02() {
        DSP.service(dspServiceURL()).POST(dataset);
        DatasetGraph dsg = DSP.service(dspServiceURL()).GET();
        assertNotNull(dsg);
        assertTrue(IsoMatcher.isomorphic(dataset, dsg));
    }

    @Test
    public void dsp_clear_01() {
        // DELETE on the GSP endpoint would be the default graph.
        // DELETE on the dataset endpoint is not supported by Fuseki - this does "CLER ALL"
        DSP.service(dspServiceURL()).clear();
    }

    @Test
    public void dsp_clear_02() {
        DSP.service(dspServiceURL()).POST(dataset);
        DSP.service(dspServiceURL()).clear();
        DatasetGraph dsg = DSP.service(dspServiceURL()).GET();
        assertFalse(dsg.find().hasNext());
    }

    @Test public void dsp_put_delete_01() {
        DSP.service(dspServiceURL()).PUT(dataset);
        DSP.service(dspServiceURL()).clear();
        DatasetGraph dsg = DSP.service(dspServiceURL()).GET();
        assertTrue(dsg.isEmpty());
    }

    @Test public void dspHead_dataset_1() {
        // Base dspServiceURL(), default content type => N-Quads (dump format)
        String h = HttpOp.httpHead(dspServiceURL(), null);
        assertNotNull(h);
        assertEquals(Lang.NQUADS.getHeaderString(), h);
    }

    @Test public void dspHead_dataset_2() {
        String ct = Lang.TRIG.getHeaderString();
        String h = HttpOp.httpHead(dspServiceURL(), ct);
        assertNotNull(h);
        assertEquals(ct, h);
    }

    @Test public void dspHead_graph_1() {
        String target = dspServiceURL()+"?default";
        String h = HttpOp.httpHead(target, null);
        assertNotNull(h);
        // "Traditional default".
        assertEquals(Lang.RDFXML.getHeaderString(), h);
    }

    @Test public void dspHead_graph_2() {
        String target = dspServiceURL()+"?default";
        String ct = Lang.TTL.getHeaderString();
        String h = HttpOp.httpHead(target, ct);
        assertNotNull(h);
        assertEquals(ct, h);
    }



    // 404

    @Test public void dsp_404_dataset() {
        expect404(
            ()->DSP.service(dspServiceURL()+"junk").GET()
        );
    }
}
