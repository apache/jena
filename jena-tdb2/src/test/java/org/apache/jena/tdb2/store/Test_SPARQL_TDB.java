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

package org.apache.jena.tdb2.store;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.jupiter.api.Test;

/**
 * Test SPARQL
 */
public class Test_SPARQL_TDB {
    private static Dataset create() {
        return TDB2Factory.createDataset();
    }

    private static Dataset create(Location location) {
        return TDB2Factory.connectDataset(location);
    }

    private static String graphName = "http://example/";
    private static Triple triple = SSE.parseTriple("(<x> <y> 123)");

    // Standalone graph.
    @Test
    public void sparql1() {
        // Test OpExecutor.execute(OpBGP) for a named graph used as a standalone
        // model
        Dataset ds = create();
        add(ds, graphName, triple);
        Txn.executeRead(ds, () -> {
            Model m = ds.getNamedModel(graphName);
            String qs = "SELECT * { ?s ?p ?o . }";
            Query query = QueryFactory.create(qs);
            QueryExecution qexec = QueryExecutionFactory.create(query, m);
            ResultSet rs = qexec.execSelect();
            ResultSetFormatter.consume(rs);
        });
    }

    // Standalone graph.
    @Test
    public void sparql2() {
        // Test OpExecutor.execute(OpFilter)for a named graph used as a standalone
        // model
        Dataset ds = create();
        add(ds, graphName, triple);

        Txn.executeRead(ds, () -> {
            Model m = ds.getNamedModel(graphName);
            String qs = "SELECT * { ?s ?p ?o . FILTER ( ?o < 456 ) }";
            Query query = QueryFactory.create(qs);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
                ResultSet rs = qexec.execSelect();
                ResultSetFormatter.consume(rs);
            }
        });
    }

    // Requires OpDatasetNames
    @Test
    public void sparql3() {
        Dataset dataset = create();
        // No triple added
        Txn.executeRead(dataset, () -> {
            Query query = QueryFactory.create("SELECT ?g { GRAPH ?g {} }");
            QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
            ResultSet rs = qExec.execSelect();
            int n = ResultSetFormatter.consume(rs);
            assertEquals(0, n);
        });
    }

    @Test
    public void sparql4() {
        Dataset dataset = create();
        add(dataset, graphName, triple);
        Txn.executeRead(dataset, () -> {
            Query query = QueryFactory.create("SELECT ?g { GRAPH ?g {} }");
            QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
            ResultSet rs = qExec.execSelect();
            int n = ResultSetFormatter.consume(rs);
            assertEquals(1, n);
        });
    }

    @Test
    public void sparql5() {
        Dataset dataset = create();
        add(dataset, graphName, triple);
        Txn.executeRead(dataset, () -> {
            Query query = QueryFactory.create("ASK { GRAPH <" + graphName + "> {} }");
            boolean b = QueryExecutionFactory.create(query, dataset).execAsk();
            assertEquals(true, b);
        });
    }

    @Test
    public void sparql6() {
        Dataset dataset = create();
        add(dataset, graphName, triple);
        Txn.executeRead(dataset, () -> {
            Query query = QueryFactory.create("ASK { GRAPH <http://example/x> {} }");
            boolean b = QueryExecutionFactory.create(query, dataset).execAsk();
            assertEquals(false, b);
        });
    }

    @Test
    public void sparql7() {
        // https://github.com/apache/jena/issues/3751
        String qs = """
                PREFIX : <http://example/>
                SELECT * { ?x :property+ ?y . ?z :q1 123 . ?z :q2 456 . }
                """;
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        dsg.executeRead(()->{
            RowSet rs = QueryExec.dataset(dsg).query(qs).select();
            RowSetOps.consume(rs);
        });
    }

    // Optimized index access - distinct with single tuple query.
    @Test
    public void sparql8() {
        DatasetGraph tdbDsg = create().asDatasetGraph();
        DatasetGraph refDsg = DatasetGraphFactory.createTxnMem();
        try (AutoTxn refTxn = Txn.autoTxn(refDsg, ReadWrite.WRITE);
             AutoTxn tdbTxn = Txn.autoTxn(tdbDsg, ReadWrite.WRITE)) {
            for (int v = 0; v < 3; ++v) {
                Node g = v == 0 ? Quad.defaultGraphIRI : NodeFactory.createURI("http://www.example.org/g" + v);
                for (int y = 0; y < 5; ++y) {
                    Node p = NodeFactory.createURI("http://www.example.org/p" + y);
                    for (int x = 0; x < (y+1)*100; ++x) {
                        Node s = NodeFactory.createURI("http://www.example.org/s" + x);
                        for (int z = 0; z < 5; ++z) {
                            Node o1 = NodeFactory.createURI("http://www.example.org/s" + z);
                            Node o2 = NodeFactory.createURI("http://www.example.org/o" + z);
                            refDsg.add(g, s, p, o1);
                            refDsg.add(g, s, p, o2);
                        }
                    }
                }
            }
            tdbDsg.addAll(refDsg);
            refTxn.commit();
            tdbTxn.commit();
        }

        if (false) {
            Txn.executeRead(refDsg, () -> {
                RDFDataMgr.write(System.out, refDsg, RDFFormat.TRIG_PRETTY);
            });
        }

        // Model m = ds.getNamedModel(graphName);
        // String qs = "SELECT DISTINCT ?p { GRAPH ?g { ?s ?p ?o } }";
        // String qs = "SELECT DISTINCT ?g ?p { GRAPH ?g { ?s ?p ?o } }";
        // String qs = "SELECT DISTINCT ?g ?p { GRAPH <urn:x-arq:DefaultGraph> { ?s ?p ?s } }";
        // String qs = "SELECT (COUNT(DISTINCT ?p) AS ?c) { GRAPH <urn:x-arq:DefaultGraph> { ?s ?p ?s } }";
        String queryStr = """
            SELECT ?g (COUNT(DISTINCT ?p) AS ?c) {
              GRAPH ?g { ?s ?p <http://www.example.org/s1> }
            }
            GROUP BY ?g
            ORDER BY ?g ?c
        """;
//        String queryStr = """
//            SELECT DISTINCT ?g ?s ?p {
//              GRAPH ?g { ?s ?p <http://www.example.org/s1> }
//            }
//            ORDER BY ?g ?s ?p
//        """;
        // queryStr = "SELECT * { GRAPH ?g { ?s ?p ?o } } ORDER BY ?g ?s ?p ?o";
        Query query = QueryFactory.create(queryStr);
        Table expected = Txn.calculateRead(refDsg, () -> QueryExec.dataset(refDsg).query(query).table());
        Table actual = Txn.calculateRead(tdbDsg, () -> QueryExec.dataset(tdbDsg).query(query).table());

        if (!expected.equals(actual)) {
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(expected.toRowSet())));
            System.out.println(ResultSetFormatter.asText(ResultSet.adapt(actual.toRowSet())));

            System.out.println("Exected size: " + expected.size());
            System.out.println("Actual size: " + actual.size());
        }

        assertEquals(expected, actual);
    }

    private static void add(Dataset dataset, String graphName, Triple triple) {
        Txn.executeWrite(dataset, () -> {
            Graph g2 = dataset.asDatasetGraph().getGraph(NodeFactory.createURI(graphName));
            g2.add(triple);
        });
    }

    // Test transactions effective.

    @Test
    public void sparql_txn_1() {
        Dataset dataset = create();
        Txn.executeWrite(dataset, () -> {
            update(dataset, "INSERT DATA { <x:s> <x:p> <x:o> }");
        });
        // Explicit trasnaction steps.
        dataset.begin(ReadWrite.READ);
        try {
            int n = count(dataset);
            assertEquals(1, n);
            n = count(dataset, "SELECT * { <x:s> <x:p> <x:o>}");
            assertEquals(1, n);
        } finally {
            dataset.end();
        }
    }

    @Test
    public void sparql_txn_2() {
        Dataset dataset1 = create(Location.mem("foo"));
        Dataset dataset2 = create(Location.mem("foo"));

        Txn.executeWrite(dataset1, () -> {
            update(dataset1, "INSERT DATA { <x:s> <x:p> <x:o> }");
        });

        Txn.executeRead(dataset1, () -> {
            assertEquals(1, count(dataset1));
        });

        // Same location.
        Txn.executeRead(dataset2, () -> {
            assertEquals(1, count(dataset2));
        });
    }

    @Test
    public void sparql_update_unionGraph() {
        Dataset ds = TDB2Factory.createDataset();
        // Update concrete default graph
        Txn.executeWrite(ds, () -> {
            ds.asDatasetGraph().add(SSE.parseQuad("(<g> <s> <p> 123)"));
        });
        ds.getContext().setTrue(TDB2.symUnionDefaultGraph);

        Txn.executeWrite(ds, () -> {
            // Update by looking in union graph
            String us = StrUtils.strjoinNL("INSERT { GRAPH <http://example/g2> { ?s ?p 'NEW' } }", "WHERE { ", "?s ?p 123", " }");
            UpdateRequest req = UpdateFactory.create(us);
            UpdateAction.execute(req, ds);
        });

        Txn.executeRead(ds, () -> {
            Model m = ds.getNamedModel("http://example/g2");
            assertEquals(1, m.size(), () -> "Did not find 1 statement in named graph");
        });
    }

    private int count(Dataset dataset) {
        return count(dataset, "SELECT * { ?s ?p ?o }");
    }

    private int count(Dataset dataset, String queryString)

    {
        Query query = QueryFactory.create(queryString);
        QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
        ResultSet rs = qExec.execSelect();
        return ResultSetFormatter.consume(rs);
    }

    private void update(Dataset dataset, String string) {
        UpdateRequest req = UpdateFactory.create(string);
        UpdateExecution proc = UpdateExecutionFactory.create(req, dataset);
        proc.execute();
    }
}
