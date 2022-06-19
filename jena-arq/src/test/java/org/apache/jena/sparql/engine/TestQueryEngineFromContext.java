/**
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

package org.apache.jena.sparql.engine;

import java.util.Collections;
import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.engine.main.solver.OpExecutorQuads;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert;
import org.junit.Test;

/**
 * The default query engine evaluates GRAPH ?g { ?s ?p ?o } with
 * a call to listGraphNodes() which suppresses calls to find(g, s, p, o).
 * Custom query engines may be able to use indices within find() to
 * efficiently determine candidate graphs.
 *
 * This class tests registration of a custom query engine factory.
 *
 */
public class TestQueryEngineFromContext {

    private class DatasetGraphForTesting
        extends DatasetGraphMap
    {
        @Override
        public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
            Node x = NodeFactory.createURI("urn:x");
            return Collections.singleton(Quad.create(x, x, x, x)).iterator();
        }

        @Override
        public Iterator<Node> listGraphNodes() {
            throw new UnsupportedOperationException("Evaluation of 'GRAPH ?g { ?s ?p ?o }' using QueryEngineMainQuad"
                    + "with OpExecutorQuads must not result in listGraphNodes to be called");
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWithDefaultQueryEngine() {
        DatasetGraph testSetup = new DatasetGraphForTesting();
        Dataset ds = DatasetFactory.wrap(testSetup);

        try (QueryExecution qe = QueryExecutionFactory.create("SELECT * { GRAPH ?g { ?s ?p ?o } }", ds)) {
            // Expected to fail with default query engine
            ResultSetFormatter.consume(qe.execSelect());
        }
    }

    @Test
    public void testWithCustomQueryEngine() {

        DatasetGraph testSetup = new DatasetGraphForTesting();
        Dataset ds = DatasetFactory.wrap(testSetup);

        try (QueryExecution qe = QueryExecutionFactory.create("SELECT * { GRAPH ?g { ?s ?p ?o } }", ds)) {
            Context cxt = qe.getContext();
            QC.setFactory(cxt, OpExecutorQuads::new);

            QueryEngineRegistry reg = new QueryEngineRegistry();
            reg.add(QueryEngineMainQuad.getFactory());
            QueryEngineRegistry.set(cxt, reg);


            int rows = ResultSetFormatter.consume(qe.execSelect());
            Assert.assertEquals(1, rows);
        }
    }

}
