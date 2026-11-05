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

package org.apache.jena.sparql.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.SetUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphFilteredView;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;

/**
 * Tests for {@code OpDatasetNames} evaluation via {@link QueryEngineMainQuad}.
 */
public class TestOpDatasetNames {

    private static final String NS = "http://example/";
    private static final Node G1 = NodeFactory.createURI(NS + "g1");
    private static final Node G2 = NodeFactory.createURI(NS + "g2");
    private static final Quad Q_DFT_G1 = SSE.parseQuad("(_ <http://example/s> <http://example/p> <http://example/g1>)");
    private static final Quad Q_DFT_NONE = SSE.parseQuad("(_ <http://example/s> <http://example/p> <http://example/nonexistent>)");
    private static final Quad Q_G1 = SSE.parseQuad("(<http://example/g1> <http://example/s1> <http://example/p> 1)");
    private static final Quad Q_G2 = SSE.parseQuad("(<http://example/g2> <http://example/s2> <http://example/p> 2)");

    private static final String QUERY_UNBOUND = "SELECT ?g { GRAPH ?g {} }";
    private static final String QUERY_BOUND_G1 = "SELECT ?g { VALUES ?g { <http://example/g1> } GRAPH ?g {} }";
    private static final String QUERY_BOUND_NONEXISTENT = "SELECT ?g { VALUES ?g { <http://example/nonexistent> } GRAPH ?g {} }";
    private static final String QUERY_BOUND_LITERAL = "SELECT ?g { VALUES ?g { 'not-a-graph' } GRAPH ?g {} }";
    private static final String QUERY_SEQUENCE_BOUND = "SELECT ?g { <http://example/s> <http://example/p> ?g . GRAPH ?g {} }";
    private static final String QUERY_SEQUENCE_INPUT = "SELECT ?s ?g { <http://example/s> <http://example/p> ?s . GRAPH ?g {} }";
    private static final String ASK_G1 = "ASK { GRAPH <http://example/g1> {} }";
    private static final String ASK_NONEXISTENT = "ASK { GRAPH <http://example/nonexistent> {} }";
    private static final String ASK_DEFAULT_GRAPH = "ASK { GRAPH <urn:x-arq:DefaultGraph> {} }";
    private static final String ASK_UNION_GRAPH = "ASK { GRAPH <urn:x-arq:UnionGraph> {} }";

    @Test
    public void opDatasetNames_emptyDataset() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Txn.executeRead(dsg, () -> {
            Set<Node> graphs = selectGraphsQuadEngine(dsg, QUERY_UNBOUND);
            assertTrue(graphs.isEmpty());
            assertFalse(askQuadEngine(dsg, ASK_G1));
        });
    }

    @Test
    public void opDatasetNames_unboundVar() {
        DatasetGraph dsg = createPopulatedTxnMem();
        Txn.executeRead(dsg, () -> {
            Set<Node> graphs = selectGraphsQuadEngine(dsg, QUERY_UNBOUND);
            assertEquals(Set.of(G1, G2), graphs);
        });
    }

    @Test
    public void opDatasetNames_preBoundMatchingVar() {
        DatasetGraph dsg = createPopulatedTxnMem();
        Txn.executeRead(dsg, () -> {
            Set<Node> graphs = selectGraphsQuadEngine(dsg, QUERY_BOUND_G1);
            assertEquals(Set.of(G1), graphs);
        });
    }

    @Test
    public void opDatasetNames_preBoundNonMatchingVar() {
        DatasetGraph dsg = createPopulatedTxnMem();
        Txn.executeRead(dsg, () -> {
            assertTrue(selectGraphsQuadEngine(dsg, QUERY_BOUND_NONEXISTENT).isEmpty());
            assertTrue(selectGraphsQuadEngine(dsg, QUERY_BOUND_LITERAL).isEmpty());
        });
    }

    @Test
    public void opDatasetNames_sequenceBoundVar() {
        DatasetGraph dsg = createPopulatedTxnMem();
        Txn.executeRead(dsg, () -> {
            Set<Node> graphs = selectGraphsQuadEngine(dsg, QUERY_SEQUENCE_BOUND);
            assertEquals(Set.of(G1), graphs);
        });
    }

    @Test
    public void opDatasetNames_concreteGraphName() {
        DatasetGraph dsg = createPopulatedTxnMem();
        Txn.executeRead(dsg, () -> {
            assertTrue(askQuadEngine(dsg, ASK_G1));
            assertFalse(askQuadEngine(dsg, ASK_NONEXISTENT));
            assertTrue(askQuadEngine(dsg, ASK_DEFAULT_GRAPH));
            assertTrue(askQuadEngine(dsg, ASK_UNION_GRAPH));
        });
    }

    @Test
    public void opDatasetNames_wrappedAndFilteredView() {
        DatasetGraph base = createPopulatedTxnMem();
        Txn.executeRead(base, () -> {
            DatasetGraph wrapped = new DatasetGraphWrapper(base);
            assertEquals(Set.of(G1, G2), selectGraphsQuadEngine(wrapped, QUERY_UNBOUND));
            assertEquals(Set.of(G1), selectGraphsQuadEngine(wrapped, QUERY_BOUND_G1));
            assertTrue(selectGraphsQuadEngine(wrapped, QUERY_BOUND_NONEXISTENT).isEmpty());
            assertTrue(askQuadEngine(wrapped, ASK_G1));
            assertFalse(askQuadEngine(wrapped, ASK_NONEXISTENT));

            Predicate<Quad> filter = q -> q.getGraph().equals(G1);
            DatasetGraph filtered = new DatasetGraphFilteredView(base, filter, Set.of(G1));
            assertEquals(Set.of(G1), selectGraphsQuadEngine(filtered, QUERY_UNBOUND));
            assertEquals(Set.of(G1), selectGraphsQuadEngine(filtered, QUERY_BOUND_G1));
            assertTrue(selectGraphsQuadEngine(filtered, QUERY_BOUND_NONEXISTENT).isEmpty());
            assertTrue(askQuadEngine(filtered, ASK_G1));
            assertFalse(askQuadEngine(filtered, ASK_NONEXISTENT));
        });
    }

    @Test
    public void opDatasetNames_preservesInputBindings() {
        DatasetGraph dsg = createPopulatedTxnMem();
        Txn.executeRead(dsg, () -> {
            List<Binding> rows = selectBindingsQuadEngine(dsg, QUERY_SEQUENCE_INPUT);
            assertEquals(4, rows.size());
            Set<String> pairs = SetUtils.toSet(rows.stream().map(b -> b.get("s").getURI() + " " + b.get("g").getURI()));
            assertEquals(Set.of(NS + "g1 " + NS + "g1",
                                NS + "g1 " + NS + "g2",
                                NS + "nonexistent " + NS + "g1",
                                NS + "nonexistent " + NS + "g2"), pairs);
        });
    }

    private DatasetGraph createPopulatedTxnMem() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Txn.executeWrite(dsg, () -> {
            dsg.add(Q_DFT_G1);
            dsg.add(Q_DFT_NONE);
            dsg.add(Q_G1);
            dsg.add(Q_G2);
        });
        return dsg;
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
}
