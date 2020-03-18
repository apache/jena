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

package org.apache.jena.sparql.util.compose;

import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.Assert;
import org.junit.Test;

public class TestUnionDatasetCollector extends TestDatasetCollector {

    @Override
    public DatasetCollector testInstance() {
        return DatasetLib.collectors().union();
    }

    @Test
    public void testUnion() {
        final Graph g1 = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        final Model m1 = createModelForGraph(g1);
        final Dataset dsg1 = DatasetFactory.create(m1);
        final String graphName1 = NodeFactory.createBlankNode().toString();
        dsg1.addNamedModel(graphName1, m1);
        final Graph g2 = parseGraph("(graph (triple <s2> <p2> <o2> ))");
        final Dataset dsg2 = DatasetFactory.create(createModelForGraph(g2));
        final Model m2 = createModelForGraph(g2);
        final String graphName2 = NodeFactory.createBlankNode().toString();
        dsg2.addNamedModel(graphName2, m2);
        final Stream<Dataset> stream = Stream.<Dataset>builder().add(dsg1).add(dsg2).build();
        Dataset dataset = stream.collect(testInstance());

        assertEquals(2, Iter.count(dataset.listNames()));
        assertTrue(m1.isIsomorphicWith(dataset.getNamedModel(graphName1)));
        assertTrue(m2.isIsomorphicWith(dataset.getNamedModel(graphName2)));
        // all statements in any input should be present in the union
        m1.listStatements().mapWith(dataset.getDefaultModel()::contains).forEachRemaining(Assert::assertTrue);
        m2.listStatements().mapWith(dataset.getDefaultModel()::contains).forEachRemaining(Assert::assertTrue);
        // all statements in the union should be present in an input
        List<Statement> leftovers = dataset.getDefaultModel().listStatements()
                .filterDrop(m1::contains)
                .filterDrop(m2::contains).toList();
        assertTrue(leftovers.isEmpty());
    }
}
