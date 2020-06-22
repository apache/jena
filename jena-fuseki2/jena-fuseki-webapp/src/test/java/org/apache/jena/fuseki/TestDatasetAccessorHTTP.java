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

import static org.apache.jena.fuseki.ServerCtl.datasetPath;
import static org.apache.jena.fuseki.ServerCtl.port;
import static org.apache.jena.fuseki.ServerCtl.serviceGSP;
import static org.apache.jena.fuseki.ServerCtl.urlDataset;
import static org.apache.jena.fuseki.ServerTest.gn1;
import static org.apache.jena.fuseki.ServerTest.gn2;
import static org.apache.jena.fuseki.ServerTest.gn99;
import static org.apache.jena.fuseki.ServerTest.model1;
import static org.apache.jena.fuseki.ServerTest.model2;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.test.FusekiTest;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class TestDatasetAccessorHTTP extends AbstractFusekiTest {
    // Model level testing.

    static final String datasetURI_not_1 = "http://localhost:" + port() + "/junk";
    static final String datasetURI_not_2 = serviceGSP() + "/not";
    static final String datasetURI_not_3 = "http://localhost:" + port() + datasetPath() + "/not/data";

    @Test
    public void test_ds_1() {
        // Can GET the dataset service.
        try {
            HttpOp.execHttpGet(serviceGSP());
        } catch (HttpException ex) {
            assertTrue(HttpSC.isClientError(ex.getStatusCode()));
            throw ex;
        }
    }

    @Test
    public void test_ds_2() {
        FusekiTest.expect404(() -> HttpOp.execHttpGet(datasetURI_not_1));
    }

    @Test
    public void test_ds_3() {
        FusekiTest.expect404(() -> HttpOp.execHttpGet(datasetURI_not_2));
    }

    @Test
    public void test_404_1() {
        // Not the right service.
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(datasetURI_not_1);
        Model graph = du.getModel(gn99);
        assertNull(graph);
    }

    @Test
    public void test_404_2() {
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(datasetURI_not_2);
        Model graph = du.getModel(gn99);
        assertNull(graph);
    }

    @Test
    public void test_404_3() {
        // Right service, wrong graph
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceGSP());
        Model graph = du.getModel(gn99);
        assertNull(graph);
    }

    @Test
    public void head_01() {
        DatasetAccessor du = connectToService();
        boolean b = du.containsModel(gn1);
        assertFalse("Blank remote dataset as a named graph", b);
    }

    @Test
    public void head_02() {
        DatasetAccessor du = connectToService();
        du.putModel(gn1, model1);
        boolean exists = du.containsModel(gn1);
        assertTrue(exists);
        exists = du.containsModel(gn2);
        assertFalse("Expected gn2 not to exist (1)", exists);

        exists = du.containsModel(gn2);
        assertFalse("Expected gn2 not to exist (2)", exists);
        // Clearup
        du.deleteModel(gn1);
    }

    @Test
    public void get_01() {
        DatasetAccessor du = connectToService();
        Model graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void get_02() {
        DatasetAccessor du = connectToService();
        Model graph = du.getModel(gn1);
        assertNull(graph);
    }

    // Dataset direct, not using the service endpoint.
    @Test
    public void get_03() {
        DatasetAccessor du = connectToDataset();
        Model graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void get_04() {
        DatasetAccessor du = connectToDataset();
        Model graph = du.getModel(gn1);
        assertNull(graph);
    }

    @Test
    public void delete_01() {
        DatasetAccessor du = connectToService();
        du.deleteDefault();
    }

    @Test
    public void delete_02() {
        DatasetAccessor du = connectToService();
        du.deleteModel(gn1);
        boolean exists = du.containsModel(gn1);
        assertFalse("Expected gn1 not to exist", exists);
    }

    @Test
    public void delete_03() {
        DatasetAccessor du = connectToDataset();
        du.deleteDefault();
    }

    @Test
    public void delete_04() {
        DatasetAccessor du = connectToDataset();
        du.deleteModel(gn1);
        boolean exists = du.containsModel(gn1);
        assertFalse("Expected gn1 not to exist", exists);
    }

    @Test
    public void put_01() {
        DatasetAccessor du = connectToService();
        du.putModel(model1);
        Model graph = du.getModel();
        assertTrue(graph.isIsomorphicWith(model1));
        // Empty it.
        du.deleteDefault();
        graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void put_02() {
        DatasetAccessor du = connectToService();
        du.putModel(gn1, model1);
        boolean exists = du.containsModel(gn1);
        assertTrue(exists);
        exists = du.containsModel(gn2);
        assertFalse("Expected gn2 not to exist", exists);

        Model graph = du.getModel();
        assertTrue(graph.isEmpty());
        graph = du.getModel(gn1);
        assertTrue(graph.isIsomorphicWith(model1));

        du.deleteModel(gn1);
        exists = du.containsModel(gn1);
        assertFalse("Expected gn1 not to exist", exists);

        graph = du.getModel(gn1);
        assertNull(graph);
    }

    @Test
    public void put_03() {
        DatasetAccessor du = connectToService();
        du.putModel(model1);
        du.putModel(model2);  // PUT overwrites
        Model graph = du.getModel();
        assertFalse(graph.isIsomorphicWith(model1));
        assertTrue(graph.isIsomorphicWith(model2));
        // Empty it.
        du.deleteDefault();
        graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void put_04() {
        DatasetAccessor du = connectToDataset();
        du.putModel(model1);
        Model graph = du.getModel();
        assertTrue(graph.isIsomorphicWith(model1));
        // Empty it.
        du.deleteDefault();
        graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void post_01() {
        DatasetAccessor du = connectToService();
        du.putModel(model1);
        du.add(model2);  // POST appends
        Model graph = du.getModel();

        Model graph3 = ModelFactory.createDefaultModel();
        graph3.add(model1);
        graph3.add(model2);

        assertFalse(graph.isIsomorphicWith(model1));
        assertFalse(graph.isIsomorphicWith(model2));
        assertTrue(graph.isIsomorphicWith(graph3));
        // Empty it.
        du.deleteDefault();
        graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void post_02() {
        DatasetAccessor du = connectToService();
        du.add(model1);
        du.add(model2);
        Model graph = du.getModel();

        Model graph3 = ModelFactory.createDefaultModel();
        graph3.add(model1);
        graph3.add(model2);

        assertFalse(graph.isIsomorphicWith(model1));
        assertFalse(graph.isIsomorphicWith(model2));
        assertTrue(graph.isIsomorphicWith(graph3));
        // Empty it.
        du.deleteDefault();
        graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void post_03() {
        DatasetAccessor du = connectToDataset();
        du.putModel(model1);
        du.add(model2);  // POST appends
        Model graph = du.getModel();

        Model graph3 = ModelFactory.createDefaultModel();
        graph3.add(model1);
        graph3.add(model2);

        assertFalse(graph.isIsomorphicWith(model1));
        assertFalse(graph.isIsomorphicWith(model2));
        assertTrue(graph.isIsomorphicWith(graph3));
        // Empty it.
        du.deleteDefault();
        graph = du.getModel();
        assertTrue(graph.isEmpty());
    }

    @Test
    public void clearup_1() {
        DatasetAccessor du = connectToService();
        du.deleteDefault();
        du.deleteModel(gn1);
        du.deleteModel(gn2);
        du.deleteModel(gn99);
    }

    static DatasetAccessor connectToService() {
        return DatasetAccessorFactory.createHTTP(serviceGSP());
    }

    static DatasetAccessor connectToDataset() {
        return DatasetAccessorFactory.createHTTP(urlDataset());
    }

}
