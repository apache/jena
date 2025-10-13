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

package org.apache.jena.tdb2.match;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.engine.DatasetGraphWithGraphTransform;
import org.apache.jena.rdfs.engine.GraphMatch;
import org.apache.jena.rdfs.engine.MapperX;
import org.apache.jena.rdfs.engine.MatchRDFSWrapper;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.AutoTxn;
import org.apache.jena.system.G;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.store.NodeId;
import org.junit.jupiter.api.Test;

public class TestMatchTDB2 {
    @Test
    public void testRdfsOnNodeIdLevel() {
        Graph schema = SSE.parseGraph("(graph (rdf:type rdf:type rdf:Property) (:p rdfs:domain :C) )");

        Dataset baseDs = TDB2Factory.createDataset();
        DatasetGraph baseDsg = baseDs.asDatasetGraph();

        MapperX<NodeId, Tuple3<NodeId>> mapper = MapperXTDB.create(baseDsg);

        try (AutoTxn txn = Txn.autoTxn(baseDsg, ReadWrite.WRITE)) {
            // !!! The schema must be added first, otherwise we won't have NodeIds !!!
            // !!! Also, all terms (especially rdf:type) must have corresponding NodeIds !!!
            G.addInto(baseDsg.getDefaultGraph(), schema);
            ConfigRDFS<NodeId> configRDFS = RDFSFactory.setupRDFS(schema, mapper);

            // Add wrapping on NodeId level.
            DatasetGraph rdfsDsg = new DatasetGraphWithGraphTransform(baseDsg,
                g -> GraphMatch.adapt(g, new MatchRDFSWrapper<>(configRDFS, MatchTDB.wrap(g))));

            // Add data.
            Graph data = SSE.parseGraph("(graph (:s :p :o) )");
            G.addInto(rdfsDsg.getDefaultGraph(), data);

            // Execute queries and compare.
            Graph expectedGraph = SSE.parseGraph("(graph (:s rdf:type :C) )");
            Graph actualGraph = QueryExec.dataset(rdfsDsg).query("CONSTRUCT WHERE { <http://example/s> a ?o }").construct();

            Set<Triple> expected = Iter.toSet(expectedGraph.find());
            Set<Triple> actual = Iter.toSet(actualGraph.find());

            assertEquals(expected, actual);
            txn.commit();
        }
    }
}
