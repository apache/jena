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

package org.apache.jena.sparql.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.adapter.DatasetGraphSPARQL;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;
import org.junit.jupiter.api.Test;

public class TestDatasetGraphSPARQL extends AbstractDatasetGraphTests {

    @Override
    protected DatasetGraph emptyDataset() {
        DatasetGraph backend = DatasetGraphFactory.create();

        DatasetGraph frontend = new DatasetGraphSPARQL() {
            @Override
            protected UpdateExec update(UpdateRequest update) {
                return UpdateExec.dataset(backend).update(update).build();
            }

            @Override
            protected QueryExec query(Query query) {
                return QueryExec.dataset(backend).query(query).build();
            }
        };

        return frontend;
    }

    @Test
    public void deleteDefaultGraph() {
        DatasetGraph dsg = testDataset();
        dsg.deleteAny(Quad.defaultGraphIRI, Node.ANY, Node.ANY, Node.ANY);
        assertFalse(dsg.isEmpty());
        assertTrue(dsg.getDefaultGraph().isEmpty());
    }

    @Test
    public void deleteNamedGraph() {
        DatasetGraph dsg = testDataset();
        dsg.deleteAny(NodeFactory.createURI("http://www.example.org/g"), Node.ANY, Node.ANY, Node.ANY);
        assertEquals(0, dsg.size());
        assertFalse(dsg.isEmpty());
    }

    @Test
    public void deleteAllNamedGraphs() {
        DatasetGraph dsg = testDataset();
        dsg.deleteAny(Quad.unionGraph, Node.ANY, Node.ANY, Node.ANY);
        assertEquals(0, dsg.size());
        assertFalse(dsg.isEmpty());
    }

    @Test
    public void deleteAllGraphs() {
        DatasetGraph dsg = testDataset();
        dsg.deleteAny(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
        assertTrue(dsg.isEmpty());
    }

    private DatasetGraph testDataset() {
        DatasetGraph dsg = emptyDataset();
        RDFParser.fromString("""
            PREFIX eg: <http://www.example.org/>
            eg:s1 eg:p eg:o .
            eg:g {
                eg:s2 eg:p eg:o
            }
            """, Lang.TRIG).parse(dsg);
        assertFalse(dsg.getDefaultGraph().isEmpty());
        assertEquals(1, dsg.size());
        return dsg;
    }
}
