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

package org.apache.jena.rdflink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.web.HttpSC;
import org.junit.Assume;
import org.junit.Test;

public abstract class AbstractTestRDFLink {
    // Testing data.
    static String DIR = "testing/RDFLink/";

    protected abstract RDFLink link();
    // Not all link types support abort.
    protected abstract boolean supportsAbort();

    // ---- Data
    static String dsgdata = StrUtils.strjoinNL
        ("(dataset"
        ,"  (graph (:s :p :o) (:s0 :p0 _:a))"
        ,"  (graph :g1 (:s :p :o) (:s1 :p1 :o1))"
        ,"  (graph :g2 (:s :p :o) (:s2 :p2 :o))"
        ,")"
        );

    static String dsgdata2 = StrUtils.strjoinNL
        ("(dataset"
        ,"  (graph (:x :y :z))"
        ,"  (graph :g9 (:s :p :o))"
        ,")"
        );


    static String graphData1 = StrUtils.strjoinNL
        ("(graph (:s :p :o) (:s1 :p1 :o))"
        );

    static String graphData2 = StrUtils.strjoinNL
        ("(graph (:s :p :o) (:s2 :p2 :o))"
        );

    static DatasetGraph dsg      = SSE.parseDatasetGraph(dsgdata);
    static DatasetGraph dsg2     = SSE.parseDatasetGraph(dsgdata2);

    static Node       graphName  = NodeFactory.createURI("http://test/graph");
    static Node       graphName2 = NodeFactory.createURI("http://test/graph2");
    static Graph      graph1     = SSE.parseGraph(graphData1);
    static Graph      graph2     = SSE.parseGraph(graphData2);
    // ---- Data

    @Test public void connect_01() {
        @SuppressWarnings("resource")
        RDFLink link = link();
        assertFalse(link.isClosed());
        link.close();
        assertTrue(link.isClosed());
        // Allow multiple close()
        link.close();
    }

    @Test public void dataset_load_1() {
        String testDataFile = DIR+"data.trig";
        try ( RDFLink link = link() ) {
            link.loadDataset(testDataFile);
            DatasetGraph ds0 = RDFDataMgr.loadDatasetGraph(testDataFile);
            DatasetGraph ds = link.getDataset();
            assertTrue("Datasets not isomorphic", isomorphic(ds0, ds));
        }
    }

    @Test public void dataset_put_1() {
        try ( RDFLink link = link() ) {
            link.putDataset(dsg);
            DatasetGraph dsg1 = link.getDataset();
            assertTrue("Datasets not isomorphic", isomorphic(dsg, dsg1));
        }
    }

    @Test public void dataset_put_2() {
        try ( RDFLink link = link() ) {
            link.putDataset(dsg);
            link.putDataset(dsg2);
            DatasetGraph dsg1 = link.getDataset();
            assertTrue("Datasets not isomorphic", isomorphic(dsg2, dsg1));
        }
    }

    @Test public void dataset_post_1() {
        try ( RDFLink link = link() ) {
            link.loadDataset(dsg);
            DatasetGraph dsg1 = link.getDataset();
            assertTrue("Datasets not isomorphic", isomorphic(dsg, dsg1));
        }
    }

    @Test public void dataset_post_2() {
        try ( RDFLink link = link() ) {
            link.loadDataset(dsg);
            link.loadDataset(dsg2);
            DatasetGraph dsg1 = link.getDataset();
            long x = Iter.count(dsg1.listGraphNodes());
            assertEquals("NG count", 3, x);
            assertFalse("Datasets are isomorphic", isomorphic(dsg, dsg1));
            assertFalse("Datasets are isomorphic", isomorphic(dsg2, dsg1));
        }
    }

    @Test public void dataset_clear_1() {
        try ( RDFLink link = link() ) {
            link.loadDataset(dsg);
            {
                DatasetGraph dsg1 = link.getDataset();
                assertFalse(dsg1.isEmpty());
            }
            link.clearDataset();
            {
                DatasetGraph dsg2 = link.getDataset();
                assertTrue(dsg2.isEmpty());
            }
        }
    }


    // Default graph

    @Test public void graph_load_1() {
        String testDataFile = DIR+"data.ttl";
        Graph g0 = RDFDataMgr.loadGraph(testDataFile);
        try ( RDFLink link = link() ) {
            link.load(testDataFile);
            Graph g1 = link.get();
            assertTrue("Graphs not isomorphic", isomorphic(g0, g1));
        }
    }

    @Test public void graph_put_1() {
        try ( RDFLink link = link() ) {
            link.put(graph1);
            DatasetGraph dsg1 = link.getDataset();
            Graph g0 = link.get();
            assertTrue("Graphs not isomorphic", isomorphic(graph1, dsg1.getDefaultGraph()));
            Graph g1 = link.get();
            assertTrue("Graphs not isomorphic", isomorphic(graph1, g1));
        }
    }

    @Test public void graph_put_2() {
        try ( RDFLink link = link() ) {
            link.put(graph1);
            link.put(graph2);
            Graph g = link.get();
            assertTrue("Graphs not isomorphic", isomorphic(g, graph2));
            assertFalse("Graphs not isomorphic", isomorphic(g, graph1));
        }
    }

    @Test public void graph_post_1() {
        try ( RDFLink link = link() ) {
            link.load(graph1);
            Graph g = link.get();
            assertTrue("Graphs not isomorphic", isomorphic(g, graph1));
        }
    }

    @Test public void graph_post_2() {
        try ( RDFLink link = link() ) {
            link.load(graph1);
            link.load(graph2);
            Graph g = link.get();
            Graph g0 = new Union( graph2, graph1);
            assertTrue("Graphs are not isomorphic", isomorphic(g0, g));
        }
    }

    @Test public void graph_delete_1() {
        String testDataFile = DIR+"data.ttl";
        Graph g0 = RDFDataMgr.loadGraph(testDataFile);
        try ( RDFLink link = link() ) {
            link.load(testDataFile);
            Graph g1 = link.get();
            assertFalse(g1.isEmpty());
            link.delete();
            Graph g2 = link.get();
            assertTrue(g2.isEmpty());
        }
    }

    // Named graphs

    @Test public void named_graph_load_1() {
        String testDataFile = DIR+"data.ttl";
        Graph g0 = RDFDataMgr.loadGraph(testDataFile);
        try ( RDFLink link = link() ) {
            link.load(graphName, testDataFile);
            Graph g = link.get(graphName);
            assertTrue("Graphs not isomorphic", isomorphic(g0, g));
            Graph gDft = link.get();
            assertTrue(gDft.isEmpty());
        }
    }

    @Test public void named_graph_put_1() {
        try ( RDFLink link = link() ) {
            link.put(graphName, graph1);
            DatasetGraph dsg1 = link.getDataset();
            Graph g0 = link.get(graphName);
            assertTrue("Graphs not isomorphic", isomorphic(graph1, dsg1.getGraph(graphName)));
            Graph g = link.get(graphName);
            assertTrue("Graphs not isomorphic", isomorphic(graph1, g));
        }
    }

    @Test public void named_graph_put_2() {
        try ( RDFLink link = link() ) {
            link.put(graphName, graph1);
            link.put(graphName, graph2);
            Graph g = link.get(graphName);
            assertTrue("Graphs not isomorphic", isomorphic(g, graph2));
            assertFalse("Graphs not isomorphic", isomorphic(g, graph1));
        }
    }

    @Test public void named_graph_put_2_different() {
        try ( RDFLink link = link() ) {
            link.put(graphName, graph1);
            link.put(graphName2, graph2);
            Graph g1 = link.get(graphName);
            Graph g2 = link.get(graphName2);
            assertTrue("Graphs not isomorphic", isomorphic(g1, graph1));
            assertTrue("Graphs not isomorphic", isomorphic(g2, graph2));
        }
    }

    @Test public void named_graph_post_1() {
        try ( RDFLink link = link() ) {
            link.load(graphName, graph1);
            Graph g = link.get(graphName);
            assertTrue("Graphs not isomorphic", isomorphic(g, graph1));
        }
    }

    @Test public void named_graph_post_2() {
        try ( RDFLink link = link() ) {
            link.load(graphName, graph1);
            link.load(graphName, graph2);
            Graph g = link.get(graphName);
            Graph g0 = new Union(graph2, graph1);
            assertTrue("Graphs are not isomorphic", isomorphic(g0, g));
        }
    }

    // DELETE

    // Remote connections don't support transactions fully.
    //@Test public void transaction_01()

    @Test public void named_graph_delete_1() {
        String testDataFile = DIR+"data.ttl";
        Graph g0 = RDFDataMgr.loadGraph(testDataFile);
        try ( RDFLink link = link() ) {
            link.load(graphName, testDataFile);
            Graph g1 = link.get(graphName);
            assertFalse(g1.isEmpty());
            link.delete(graphName);
            // either isEmpty (local-all graph "exist"), null (general dataset,
            // or fixed set of graphs) or 404 (remote);
            try {
                Graph g2 = link.get(graphName);
                if ( g2 != null )
                    assertTrue(g2.isEmpty());
            } catch (HttpException ex) {
                assertEquals(HttpSC.NOT_FOUND_404, ex.getStatusCode());
            }
        }
    }
    private static boolean isomorphic(DatasetGraph ds1, DatasetGraph ds2) {
        return IsoMatcher.isomorphic(ds1, ds2);
    }

    private static boolean isomorphic(Graph graph1, Graph graph2) {
        return graph1.isIsomorphicWith(graph2);
    }

    @Test public void query_01() {
        try ( RDFLink link = link() ) {
            Txn.executeRead(link, ()->{
                try ( QueryExec qExec = link.query("SELECT ?x {}") ) {
                    RowSet rs = qExec.select();
                    assertNotNull(rs);
                }
            });
        }
    }

    @Test public void query_02() {
        try ( RDFLink link = link() ) {
            Txn.executeRead(link, ()->{
                try ( QueryExec qExec = link.query("ASK{}") ) {
                    boolean b = qExec.ask();
                    assertTrue(b);
                }
            });
        }
    }

    @Test public void query_03() {
        try ( RDFLink link = link() ) {
            Txn.executeRead(link, ()->{
                try ( QueryExec qExec = link.query("CONSTRUCT WHERE{}") ) {
                    Graph g = qExec.construct();
                    assertNotNull(g);
                }
            });
        }
    }

    @Test public void query_ask_01() {
        try ( RDFLink link = link() ) {
            Txn.executeRead(link, ()->{
                boolean b = link.queryAsk("ASK{}");
                assertTrue(b);
            });
        }
    }

    @Test public void query_ask_02() {
        try ( RDFLink link = link() ) {
            boolean b = link.queryAsk("ASK{}");
            assertTrue(b);
        }
    }

    @Test public void query_select_01() {
        AtomicInteger counter = new AtomicInteger(0);
        try ( RDFLink link = link() ) {
            Txn.executeWrite(link, ()->link.loadDataset(DIR+"data.trig"));
            Txn.executeRead(link, ()->
                link.querySelect("SELECT * { ?s ?p ?o }" , (r)->counter.incrementAndGet()));
            assertEquals(2, counter.get());
        }
    }

    @Test public void query_select_02() {
        AtomicInteger counter = new AtomicInteger(0);
        try ( RDFLink link = link() ) {
            link.loadDataset(DIR+"data.trig");
            link.querySelect("SELECT * { ?s ?p ?o}" , (r)->counter.incrementAndGet());
            assertEquals(2, counter.get());
        }
    }

    @Test public void query_construct_01() {
        try ( RDFLink link = link() ) {
            Txn.executeWrite(link, ()->link.loadDataset(DIR+"data.trig"));
            Txn.executeRead(link, ()-> {
                Graph g = link.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
                assertEquals(2, g.size());
            });
        }
    }

    @Test public void query_construct_02() {
        try ( RDFLink link = link() ) {
            link.loadDataset(DIR+"data.trig");
            Graph g = link.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
            assertEquals(2, g.size());
        }
    }

    @Test public void query_build_01() {
        try ( RDFLink link = link() ) {
            Txn.executeRead(link, ()->{
                RowSet rs = link.newQuery().query("SELECT * { ?s ?p ?o}").select();
                assertNotNull(rs);
            });
        }
    }

    @Test public void query_build_02() {
        try ( RDFLink link = link() ) {
            Txn.executeRead(link, ()->{
                Binding binding = SSE.parseBinding("(binding (?X 123))");
                QueryExec qExec = link.newQuery().query("SELECT ?X { }")
                        .substitution(binding)
                        .build();
                RowSet rs = qExec.select();
                Node x = rs.next().get(Var.alloc("X"));
                assertNotNull(x);
            });
        }
    }

    @Test public void update_01() {
        try ( RDFLink link = link() ) {
            link.update("INSERT DATA { <urn:x:s> <urn:x:p> <urn:x:o>}");
        }
    }

    @Test public void update_02() {
        try ( RDFLink link = link() ) {
            Txn.executeWrite(link, ()->link.update("INSERT DATA { <urn:x:s> <urn:x:p> <urn:x:o>}"));
        }
    }

    @Test public void update_03() {
    	UpdateRequest update = new UpdateRequest();
    	update.add("INSERT DATA { <urn:x:s> <urn:x:p> <urn:x:o>}");
        try ( RDFLink link = link() ) {
            link.update(update);
        }
    }

    @Test public void update_04() {
    	UpdateRequest update = new UpdateRequest();
    	update.add("INSERT DATA { <urn:x:s> <urn:x:p> <urn:x:o>}");
        try ( RDFLink link = link() ) {
            Txn.executeWrite(link, ()->link.update(update));
        }
    }

    @Test public void update_05() {
        UpdateRequest update = new UpdateRequest();
        update.add("INSERT DATA { <urn:ex:s> <urn:ex:p> <urn:ex:o>}");
        try ( RDFLink link = link() ) {
            UpdateExecBuilder updateBuilder = link.newUpdate();
            UpdateExec uExec = updateBuilder.update(update).build();
            Txn.executeWrite(link, ()->uExec.execute());
        }
    }

    // Not all Transactional support abort.
    @Test public void transaction_commit_read_01() {
        String testDataFile = DIR+"data.trig";
        try ( RDFLink link = link() ) {

            link.begin(ReadWrite.WRITE);
            link.loadDataset(dsg);
            link.commit();
            link.end();

            link.begin(ReadWrite.READ);
            Graph g = link.get();
            assertTrue(isomorphic(g, dsg.getDefaultGraph()));
            link.end();
        }
    }

    // Not all RDFLinks support abort.
    @Test public void transaction_abort_read02() {
        Assume.assumeTrue(supportsAbort());

        String testDataFile = DIR+"data.trig";
        try ( RDFLink link = link() ) {
            link.begin(ReadWrite.WRITE);
            link.loadDataset(testDataFile);
            link.abort();
            link.end();

            link.begin(ReadWrite.READ);
            Graph g = link.get();
            assertTrue(g.isEmpty());
            link.end();
        }
    }

    @Test(expected=JenaTransactionException.class)
    public void transaction_bad_01() {
        try ( RDFLink link = link() ) {
            link.begin(ReadWrite.WRITE);
            // Should have link.commit();
            link.end();
        }
    }

    @Test public void setTimeout() {
        try ( RDFLink link = link() ) {
            link.newQuery()
                .query("ASK{}")
                .timeout(1000, TimeUnit.MILLISECONDS)
                .ask();
        }
    }
}

