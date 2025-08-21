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

package org.apache.jena.rdfconnection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateExecutionBuilder;
import org.apache.jena.update.UpdateRequest;

public abstract class AbstractTestRDFConnection {
    // Testing data.
    protected static String DIR = "testing/RDFConnection/";

    protected abstract RDFConnection connection();
    // Not all connection types support abort.
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


    static String graph1 = StrUtils.strjoinNL
        ("(graph (:s :p :o) (:s1 :p1 :o))"
        );

    static String graph2 = StrUtils.strjoinNL
        ("(graph (:s :p :o) (:s2 :p2 :o))"
        );

    static DatasetGraph dsgTest1       = SSE.parseDatasetGraph(dsgdata);
    static Dataset      datasetTest1   = DatasetFactory.wrap(dsgTest1);
    static DatasetGraph dsgTest2       = SSE.parseDatasetGraph(dsgdata2);
    static Dataset      datasetTest2   = DatasetFactory.wrap(dsgTest2);

    static String       graphName      = "http://test/graph";
    static String       graphName2     = "http://test/graph2";
    static Model        model1         = ModelFactory.createModelForGraph(SSE.parseGraph(graph1));
    static Model        model2         = ModelFactory.createModelForGraph(SSE.parseGraph(graph2));
    // ---- Data

    @Test public void connect_01() {
        @SuppressWarnings("resource")
        RDFConnection conn = connection();
        assertFalse(conn.isClosed());
        conn.close();
        assertTrue(conn.isClosed());
        // Allow multiple close()
        conn.close();
    }

    @Test public void dataset_load_1() {
        String testDataFile = DIR+"data.trig";
        try ( RDFConnection conn = connection() ) {
            conn.loadDataset(testDataFile);
            Dataset ds0 = RDFDataMgr.loadDataset(testDataFile);
            Dataset ds = conn.fetchDataset();
            assertTrue(isomorphic(ds0, ds), ()->"Datasets not isomorphic");
        }
    }

    @Test public void dataset_put_1() {
        try ( RDFConnection conn = connection() ) {
            conn.putDataset(datasetTest1);
            Dataset ds1 = conn.fetchDataset();
            assertTrue(isomorphic(datasetTest1, ds1), ()->"Datasets not isomorphic");
        }
    }

    @Test public void dataset_put_2() {
        try ( RDFConnection conn = connection() ) {
            conn.putDataset(datasetTest1);
            conn.putDataset(datasetTest2);
            Dataset ds1 = conn.fetchDataset();
            assertTrue(isomorphic(datasetTest2, ds1), ()->"Datasets not isomorphic");
        }
    }

    @Test public void dataset_post_1() {
        try ( RDFConnection conn = connection() ) {
            conn.loadDataset(datasetTest1);
            Dataset ds1 = conn.fetchDataset();
            assertTrue(isomorphic(datasetTest1, ds1), ()->"Datasets not isomorphic");
        }
    }

    @Test public void dataset_post_2() {
        try ( RDFConnection conn = connection() ) {
            conn.loadDataset(datasetTest1);
            conn.loadDataset(datasetTest2);
            Dataset ds1 = conn.fetchDataset();
            long x = Iter.count(ds1.listNames());
            assertEquals(3, x, ()->"NG count");
            assertFalse(isomorphic(datasetTest1, ds1), "Datasets are isomorphic");
            assertFalse(isomorphic(datasetTest2, ds1), "Datasets are isomorphic");
        }
    }

    // Default graph

    @Test public void graph_load_1() {
        String testDataFile = DIR+"data.ttl";
        Model m0 = RDFDataMgr.loadModel(testDataFile);
        try ( RDFConnection conn = connection() ) {
            conn.load(testDataFile);
            Model m = conn.fetch();
            assertTrue(isomorphic(m0, m), "Models not isomorphic");
        }
    }

    @Test public void graph_put_1() {
        try ( RDFConnection conn = connection() ) {
            conn.put(model1);
            Dataset ds1 = conn.fetchDataset();
            Model m0 = conn.fetch();
            assertTrue(isomorphic(model1, ds1.getDefaultModel()), "Models not isomorphic");
            Model m = conn.fetch();
            assertTrue(isomorphic(model1, m), "Models not isomorphic");
        }
    }

    @Test public void graph_put_2() {
        try ( RDFConnection conn = connection() ) {
            conn.put(model1);
            conn.put(model2);
            Model m = conn.fetch();
            assertTrue(isomorphic(m, model2), "Models not isomorphic");
            assertFalse(isomorphic(m, model1), "Models not isomorphic");
        }
    }

    @Test public void graph_post_1() {
        try ( RDFConnection conn = connection() ) {
            conn.load(model1);
            Model m = conn.fetch();
            assertTrue(isomorphic(m, model1), "Models not isomorphic");
        }
    }

    @Test public void graph_post_2() {
        try ( RDFConnection conn = connection() ) {
            conn.load(model1);
            conn.load(model2);
            Model m = conn.fetch();
            Model m0 = ModelFactory.createUnion(model2, model1);
            assertTrue(isomorphic(m0, m), "Models are not isomorphic");
        }
    }

    // DELETE

    // Named graphs

    @Test public void named_graph_load_1() {
        String testDataFile = DIR+"data.ttl";
        Model m0 = RDFDataMgr.loadModel(testDataFile);
        try ( RDFConnection conn = connection() ) {
            conn.load(graphName, testDataFile);
            Model m = conn.fetch(graphName);
            assertTrue(isomorphic(m0, m), "Models not isomorphic");
            Model mDft = conn.fetch();
            assertTrue(mDft.isEmpty());
        }
    }

    @Test public void named_graph_put_1() {
        try ( RDFConnection conn = connection() ) {
            conn.put(graphName, model1);
            Dataset ds1 = conn.fetchDataset();
            Model m0 = conn.fetch(graphName);
            assertTrue(isomorphic(model1, ds1.getNamedModel(graphName)), "Models not isomorphic");
            Model m = conn.fetch(graphName);
            assertTrue(isomorphic(model1, m), "Models not isomorphic");
        }
    }

    @Test public void named_graph_put_2() {
        try ( RDFConnection conn = connection() ) {
            conn.put(graphName, model1);
            conn.put(graphName, model2);
            Model m = conn.fetch(graphName);
            assertTrue(isomorphic(m, model2), "Models not isomorphic");
            assertFalse(isomorphic(m, model1), "Models not isomorphic");
        }
    }

    @Test public void named_graph_put_2_different() {
        try ( RDFConnection conn = connection() ) {
            conn.put(graphName, model1);
            conn.put(graphName2, model2);
            Model m1 = conn.fetch(graphName);
            Model m2 = conn.fetch(graphName2);
            assertTrue(isomorphic(m1, model1), "Models not isomorphic");
            assertTrue(isomorphic(m2, model2), "Models not isomorphic");
        }
    }

    @Test public void named_graph_post_1() {
        try ( RDFConnection conn = connection() ) {
            conn.load(graphName, model1);
            Model m = conn.fetch(graphName);
            assertTrue(isomorphic(m, model1), "Models not isomorphic");
        }
    }

    @Test public void named_graph_post_2() {
        try ( RDFConnection conn = connection() ) {
            conn.load(graphName, model1);
            conn.load(graphName, model2);
            Model m = conn.fetch(graphName);
            Model m0 = ModelFactory.createUnion(model2, model1);
            assertTrue(isomorphic(m0, m), "Models are not isomorphic");
        }
    }

    // DELETE

    // Remote connections don't support transactions fully.
    //@Test public void transaction_01()

    private static boolean isomorphic(Dataset ds1, Dataset ds2) {
        return IsoMatcher.isomorphic(ds1.asDatasetGraph(), ds2.asDatasetGraph());
    }

    private static boolean isomorphic(Model model1, Model model2) {
        return model1.isIsomorphicWith(model2);
    }

    @Test public void query_01() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeRead(conn, ()->{
                try ( QueryExecution qExec = conn.query("SELECT ?x {}") ) {
                    ResultSet rs = qExec.execSelect();
                    ResultSetFormatter.consume(rs);
                }
            });
        }
    }

    @Test public void query_02() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeRead(conn, ()->{
                try ( QueryExecution qExec = conn.query("ASK{}") ) {
                    boolean b = qExec.execAsk();
                    assertTrue(b);
                }
            });
        }
    }

    @Test public void query_ask_01() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeRead(conn, ()->{
                boolean b = conn.queryAsk("ASK{}");
                assertTrue(b);
            });
        }
    }

    @Test public void query_ask_02() {
        try ( RDFConnection conn = connection() ) {
            boolean b = conn.queryAsk("ASK{}");
            assertTrue(b);
        }
    }

    @Test public void query_select_01() {
        AtomicInteger counter = new AtomicInteger(0);
        try ( RDFConnection conn = connection() ) {
            Txn.executeWrite(conn, ()->conn.loadDataset(DIR+"data.trig"));
            Txn.executeRead(conn, ()->
                conn.querySelect("SELECT * { ?s ?p ?o }" , (r)->counter.incrementAndGet()));
            assertEquals(2, counter.get());
        }
    }

    @Test public void query_select_02() {
        AtomicInteger counter = new AtomicInteger(0);
        try ( RDFConnection conn = connection() ) {
            conn.loadDataset(DIR+"data.trig");
            conn.querySelect("SELECT * { ?s ?p ?o}" , (r)->counter.incrementAndGet());
            assertEquals(2, counter.get());
        }
    }

    @Test public void query_construct_01() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeWrite(conn, ()->conn.loadDataset(DIR+"data.trig"));
            Txn.executeRead(conn, ()-> {
                Model m = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
                assertEquals(2, m.size());
            });
        }
    }

    @Test public void query_construct_02() {
        try ( RDFConnection conn = connection() ) {
            conn.loadDataset(DIR+"data.trig");
            Model m = conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }");
            assertEquals(2, m.size());
        }
    }

    @Test public void query_build_01() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeRead(conn, ()->{
                ResultSet rs = conn.newQuery().query("SELECT * { ?s ?p ?o}").select();
                assertNotNull(rs);
            });
        }
    }

    @Test public void query_build_02() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeRead(conn, ()->{
                QuerySolutionMap qsm = new QuerySolutionMap();
                qsm.add("X", ResourceFactory.createTypedLiteral("123", XSDDatatype.XSDinteger));
                QueryExecution qExec = conn.newQuery().query("SELECT ?X { }")
                        .substitution(qsm)
                        .build();
                String s = qExec.getQueryString();
                assertTrue(s.contains("123"));
                ResultSet rs = qExec.execSelect();
                RDFNode x = rs.next().get("X");
                assertNotNull(x);
            });
        }
    }

    @Test public void update_01() {
        try ( RDFConnection conn = connection() ) {
            conn.update("INSERT DATA { <urn:ex:s> <urn:ex:p> <urn:ex:o>}");
        }
    }

    @Test public void update_02() {
        try ( RDFConnection conn = connection() ) {
            Txn.executeWrite(conn, ()->conn.update("INSERT DATA { <urn:ex:s> <urn:ex:p> <urn:ex:o>}"));
        }
    }

    @Test public void update_03() {
        UpdateRequest update = new UpdateRequest();
        update.add("INSERT DATA { <urn:ex:s> <urn:ex:p> <urn:ex:o>}");
        try ( RDFConnection conn = connection() ) {
            conn.update(update);
        }
    }

    @Test public void update_04() {
        UpdateRequest update = new UpdateRequest();
        update.add("INSERT DATA { <urn:ex:s> <urn:ex:p> <urn:ex:o>}");
        try ( RDFConnection conn = connection() ) {
            Txn.executeWrite(conn, ()->conn.update(update));
        }
    }

    @Test public void update_05() {
        UpdateRequest update = new UpdateRequest();
        update.add("INSERT DATA { <urn:ex:s> <urn:ex:p> <urn:ex:o>}");
        try ( RDFConnection conn = connection() ) {
            UpdateExecutionBuilder updateBuilder = conn.newUpdate();
            UpdateExecution uExec = updateBuilder.update(update).build();
            Txn.executeWrite(conn, ()->uExec.execute());
        }
    }

    // Not all Transactional support abort.
    @Test public void transaction_commit_read_01() {
        try ( RDFConnection conn = connection() ) {

            conn.begin(ReadWrite.WRITE);
            conn.loadDataset(datasetTest1);
            conn.commit();
            conn.end();

            conn.begin(ReadWrite.READ);
            Model m = conn.fetch();
            assertTrue(isomorphic(m, datasetTest1.getDefaultModel()));
            conn.end();
        }
    }

    // Not all RDFConnections support abort.
    @Test public void transaction_abort_read02() {
        assumeTrue(supportsAbort());

        String testDataFile = DIR+"data.trig";
        try ( RDFConnection conn = connection() ) {
            conn.begin(ReadWrite.WRITE);
            conn.loadDataset(testDataFile);
            conn.abort();
            conn.end();

            conn.begin(ReadWrite.READ);
            Model m = conn.fetch();
            assertTrue(m.isEmpty());
            conn.end();
        }
    }

    @Test
    public void transaction_bad_01() {
        try ( RDFConnection conn = connection() ) {
            conn.begin(ReadWrite.WRITE);
            // Should have conn.commit();
            assertThrows(JenaTransactionException.class, ()->conn.end());
        }
    }

    /** Non-standard query syntax on local connection is expected to fail (regardless of syntax checking hint) */
    @Test
    public void non_standard_syntax_query_local_1a() {
        assertThrows(QueryParseException.class, ()->{
            try ( RDFConnection conn = RDFConnection.connect(DatasetFactory.empty()) ) {
                try (QueryExecution qe = conn.newQuery().parseCheck(false).query("FOOBAR").build()) { }
            }});
    }

    /** Non-standard query syntax on local connection is expected to fail (regardless of syntax checking hint) */
    public void non_standard_syntax_query_local_1b() {
        assertThrows(QueryParseException.class, ()->{
            try ( RDFConnection conn = RDFConnection.connect(DatasetFactory.empty()) ) {
                try (QueryExecution qe = conn.newQuery().parseCheck(true).query("FOOBAR").build()) { }
            }});
    }

    /** Non-standard update syntax on local connection is expected to fail (regardless of syntax checking hint) */
    @Test
    public void non_standard_syntax_update_local_1a() {
        assertThrows(QueryParseException.class, ()->{
            try ( RDFConnection conn = RDFConnection.connect(DatasetFactory.empty()) ) {
                conn.newUpdate().parseCheck(false).update("FOOBAR").build();
            }});
    }

    /** Non-standard update syntax on local connection is expected to fail (regardless of syntax checking hint) */
    public void non_standard_syntax_update_local_1b() {
        assertThrows(QueryParseException.class, ()->{
            try ( RDFConnection conn = RDFConnection.connect(DatasetFactory.empty()) ) {
                conn.newUpdate().parseCheck(true).update("FOOBAR").build();
            }});
    }
}

