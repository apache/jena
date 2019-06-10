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

import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.web.HttpOp;
import org.junit.Test;

/**
 * Additional tests for the SPARQL Graph Store protocol, mainly for HTTP file upload.
 * See {@linkplain TestDatasetAccessorHTTP} and {@linkplain TestHttpOp} for tests
 * that exercise direct GSp functionality
 *
 * @see TestDatasetAccessorHTTP
 * @see TestHttpOp
 */
public class TestFileUpload extends AbstractFusekiTest {
    @Test
    public void upload_gsp_01() {
        FileSender x = new FileSender(ServerCtl.serviceGSP() + "?default");
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle");
        x.send("POST");

        Model m = ModelFactory.createDefaultModel();
        TypedInputStream in = HttpOp.execHttpGet(ServerCtl.serviceGSP(), "text/turtle");
        RDFDataMgr.read(m, in, RDFLanguages.contentTypeToLang(in.getMediaType()));
        // which is is effectively :
// DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceREST);
// Model m = du.getModel();
        assertEquals(1, m.size());
    }

    @Test
    public void upload_gsp_02() {
        FileSender x = new FileSender(ServerCtl.serviceGSP() + "?default");
        x.add("D.ttl", "<http://example/s> <http://example/p> 123 .", "text/turtle");
        x.add("D.nt", "<http://example/s> <http://example/p> <http://example/o-456> .", "application/n-triples");
        x.send("PUT");

        // BUG
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP());
        Model m = du.getModel();
        assertEquals(2, m.size());
    }

    // Extension of GSP - no graph selector => dataset
    @Test
    public void upload_gsp_03() {
        FileSender x = new FileSender(ServerCtl.serviceGSP());
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle");
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> <http://example/o> }", "text/trig");
        x.send("POST");

        DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP());
        Model m = du.getModel();
        assertEquals(1, m.size());
    }

    @Test
    public void upload_gsp_04() {
        {
            DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP());
            Model m = du.getModel();
            assertEquals(0, m.size());
        }
        FileSender x = new FileSender(ServerCtl.urlDataset());
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/plain");
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> 123,456 }", "text/plain");
        x.send("POST");

        DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP());
        Model m = du.getModel();
        assertEquals(1, m.size());
        m = du.getModel("http://example/g");
        assertEquals(2, m.size());
    }

    // Via DatasetAccessor

    @Test
    public void dataset_accessor_01() {
        FileSender x = new FileSender(ServerCtl.urlDataset());
        x.add("D.nq", "", "application/-n-quads");
        x.send("PUT");

        DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP());
        Model m = du.getModel();
        assertEquals(0, m.size());
    }

    @Test
    public void dataset_accessor_02() {
        FileSender x = new FileSender(ServerCtl.urlDataset());
        x.add("D.nq", "<http://example/s> <http://example/p> <http://example/o-456> <http://example/g> .", "application/n-quads");
        x.send("PUT");

        DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP());
        Model m = du.getModel("http://example/g");
        assertEquals(1, m.size());
        m = du.getModel();
        assertEquals(0, m.size());
    }

}
