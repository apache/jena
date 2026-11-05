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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFilteredView;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.update.*;

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

    private static final Node G1 = NodeFactory.createURI("http://example/g1");
    private static final Node G2 = NodeFactory.createURI("http://example/g2");
    private static final Quad Q_DFT_G1 = SSE.parseQuad("(_ <http://example/s> <http://example/p> <http://example/g1>)");
    private static final Quad Q_DFT_NONE = SSE.parseQuad("(_ <http://example/s> <http://example/p> <http://example/nonexistent>)");
    private static final Quad Q_G1 = SSE.parseQuad("(<http://example/g1> <http://example/s1> <http://example/p> 1)");
    private static final Quad Q_G2 = SSE.parseQuad("(<http://example/g2> <http://example/s2> <http://example/p> 2)");

    private static final String QUERY_UNBOUND = "SELECT ?g { GRAPH ?g {} }";
    private static final String QUERY_BOUND_G1 = "SELECT ?g { VALUES ?g { <http://example/g1> } GRAPH ?g {} }";
    private static final String QUERY_BOUND_G2 = "SELECT ?g { VALUES ?g { <http://example/g2> } GRAPH ?g {} }";
    private static final String QUERY_BOUND_NONEXISTENT = "SELECT ?g { VALUES ?g { <http://example/nonexistent> } GRAPH ?g {} }";
    private static final String QUERY_SEQUENCE_BOUND = "SELECT ?g { <http://example/s> <http://example/p> ?g . GRAPH ?g {} }";
    private static final String QUERY_SEQUENCE_INPUT = "SELECT ?s ?g { <http://example/s> <http://example/p> ?s . GRAPH ?g {} }";
    private static final String ASK_G1 = "ASK { GRAPH <http://example/g1> {} }";
    private static final String ASK_NONEXISTENT = "ASK { GRAPH <http://example/nonexistent> {} }";

    @Test
    public void opDatasetNames_switchable_quadEngine() {
        DatasetGraph dsg = createPopulatedDatasetGraph();
        Txn.executeRead(dsg, () -> {
            assertEquals(Set.of(G1, G2), selectGraphsQuadEngine(dsg, QUERY_UNBOUND));
            assertEquals(Set.of(G1), selectGraphsQuadEngine(dsg, QUERY_BOUND_G1));
            assertTrue(selectGraphsQuadEngine(dsg, QUERY_BOUND_NONEXISTENT).isEmpty());
            assertEquals(Set.of(G1), selectGraphsQuadEngine(dsg, QUERY_SEQUENCE_BOUND));
            assertInputBindingsPreserved(selectBindingsQuadEngine(dsg, QUERY_SEQUENCE_INPUT));
            assertTrue(askQuadEngine(dsg, ASK_G1));
            assertFalse(askQuadEngine(dsg, ASK_NONEXISTENT));
        });
    }

    @Test
    public void opDatasetNames_tdbEngine_preservesInputBindings() {
        DatasetGraph dsg = createPopulatedDatasetGraph();
        Txn.executeRead(dsg, () -> {
            try ( QueryExec qExec = QueryExec.dataset(dsg).query(QUERY_SEQUENCE_INPUT).build() ) {
                assertInputBindingsPreserved(Iter.toList(qExec.select()));
            }
        });
    }

    @Test
    public void opDatasetNames_wrapped_quadEngine() {
        DatasetGraph base = createPopulatedDatasetGraph();
        DatasetGraph wrapped = new DatasetGraphWrapper(base);
        Txn.executeRead(wrapped, () -> {
            assertEquals(Set.of(G1, G2), selectGraphsQuadEngine(wrapped, QUERY_UNBOUND));
            assertEquals(Set.of(G1), selectGraphsQuadEngine(wrapped, QUERY_BOUND_G1));
            assertTrue(selectGraphsQuadEngine(wrapped, QUERY_BOUND_NONEXISTENT).isEmpty());
            assertEquals(Set.of(G1), selectGraphsQuadEngine(wrapped, QUERY_SEQUENCE_BOUND));
            assertTrue(askQuadEngine(wrapped, ASK_G1));
            assertFalse(askQuadEngine(wrapped, ASK_NONEXISTENT));
        });
    }

    @Test
    public void opDatasetNames_filteredView_quadEngine() {
        DatasetGraph base = createPopulatedDatasetGraph();
        Predicate<Quad> filter = q -> q.getGraph().equals(G1);
        DatasetGraph filtered = new DatasetGraphFilteredView(base, filter, Set.of(G1));
        Txn.executeRead(filtered, () -> {
            assertEquals(Set.of(G1), selectGraphsQuadEngine(filtered, QUERY_UNBOUND));
            assertEquals(Set.of(G1), selectGraphsQuadEngine(filtered, QUERY_BOUND_G1));
            assertTrue(selectGraphsQuadEngine(filtered, QUERY_BOUND_G2).isEmpty());
            assertTrue(selectGraphsQuadEngine(filtered, QUERY_BOUND_NONEXISTENT).isEmpty());
            assertTrue(askQuadEngine(filtered, ASK_G1));
            assertFalse(askQuadEngine(filtered, ASK_NONEXISTENT));
        });
    }

    private DatasetGraph createPopulatedDatasetGraph() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph();
        Txn.executeWrite(dsg, () -> {
            dsg.add(Q_DFT_G1);
            dsg.add(Q_DFT_NONE);
            dsg.add(Q_G1);
            dsg.add(Q_G2);
        });
        return dsg;
    }

    private void assertInputBindingsPreserved(List<Binding> rows) {
        assertEquals(4, rows.size());
        Set<String> pairs = SetUtils.toSet(rows.stream().map(b -> b.get("s").getURI() + " " + b.get("g").getURI()));
        assertEquals(Set.of(G1.getURI() + " " + G1.getURI(),
                            G1.getURI() + " " + G2.getURI(),
                            "http://example/nonexistent " + G1.getURI(),
                            "http://example/nonexistent " + G2.getURI()), pairs);
    }

    private Set<Node> selectGraphsQuadEngine(DatasetGraph dsg, String queryString) {
        List<Binding> rows = selectBindingsQuadEngine(dsg, queryString);
        return SetUtils.toSet(rows.stream().map(b -> b.get("g")));
    }

    private List<Binding> selectBindingsQuadEngine(DatasetGraph dsg, String queryString) {
        Context cxt = quadEngineContext(dsg);
        try ( QueryExec qExec = QueryExec.dataset(dsg).query(queryString).context(cxt).build() ) {
            return Iter.toList(qExec.select());
        }
    }

    private boolean askQuadEngine(DatasetGraph dsg, String queryString) {
        Context cxt = quadEngineContext(dsg);
        try ( QueryExec qExec = QueryExec.dataset(dsg).query(queryString).context(cxt).build() ) {
            return qExec.ask();
        }
    }

    private Context quadEngineContext(DatasetGraph dsg) {
        Context cxt = Context.setupContextForDataset(null, dsg);
        QueryEngineRegistry reg = new QueryEngineRegistry();
        reg.add(QueryEngineMainQuad.getFactory());
        QueryEngineRegistry.set(cxt, reg);
        return cxt;
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
