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

package org.apache.jena.tdb2.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.Test;

public class TestGraphView_Prefixes {

    public static Graph graphX(DatasetGraph dataset, Node graphName) {
        DatasetGraphSwitchable dsg = TDBInternal.getDatabaseContainer(dataset);
        return new GraphViewSwitchable_Prefixes(dsg, graphName);
    }

    public static Graph graphX(DatasetGraph dataset) {
        DatasetGraphSwitchable dsg = TDBInternal.getDatabaseContainer(dataset);
        return new GraphViewSwitchable_Prefixes(dsg);
    }

    @Test public void tdb_local_prefixes_1() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Node gn1 = NodeFactory.createURI("http://example/gn1");
        Graph g1 = graphX(dsg, gn1);
        dsg.execute(()->{
            g1.getPrefixMapping().setNsPrefix("ns1", "http://host/uri1");
        });
        dsg.execute(()->{
            String x = g1.getPrefixMapping().getNsPrefixURI("ns1");
            assertEquals("http://host/uri1", x);
            assertFalse(dsg.prefixes().containsPrefix("ns1"));
            assertNull(dsg.prefixes().get("ns1"));
        });
    }

    @Test public void tdb_local_prefixes_2() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();

        Graph gDft = graphX(dsg);
        Node gn1 = NodeFactory.createURI("http://example/gn1");
        Graph g1 = graphX(dsg, gn1);

        dsg.execute(()->{
            gDft.getPrefixMapping().setNsPrefix("ns2", "http://host/uri2");
            g1.getPrefixMapping().setNsPrefix("ns1", "http://host/uri1");
        });
        dsg.execute(()->{
            String x = gDft.getPrefixMapping().getNsPrefixURI("ns2");
            assertEquals("http://host/uri2", x);
            assertFalse(dsg.prefixes().containsPrefix("ns2"));
            assertNull(dsg.prefixes().get("ns2"));
        });

        dsg.execute(()->{
            String x = g1.getPrefixMapping().getNsPrefixURI("ns2");
            assertNull(x);
        });
    }

    @Test public void tdb_local_prefixes_3() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Node gn1 = NodeFactory.createURI("http://example/gn1");
        Graph g1 = graphX(dsg, gn1);
        dsg.executeWrite(()-> g1.getPrefixMapping().setNsPrefix("foo0", "http://host/bar0") );
        dsg.executeRead(()-> assertEquals("http://host/bar0", g1.getPrefixMapping().getNsPrefixURI("foo0")) );
    }

    @Test public void tdb_local_prefixes_model_1() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Node gn1 = NodeFactory.createURI("http://example/gn1");
        Graph g1 = graphX(dsg, gn1);
        Model m1 = ModelFactory.createModelForGraph(g1);
        Dataset ds = DatasetFactory.create(m1);

        // No "promote" on wrapper datasets.
        ds.executeWrite(()->m1.setNsPrefix("ex", "http://example/"));
        ds.executeRead(()-> assertEquals("http://example/", m1.getNsPrefixURI("ex")));
    }
}
