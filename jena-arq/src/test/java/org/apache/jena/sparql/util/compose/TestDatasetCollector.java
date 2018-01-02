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

import java.util.stream.Stream;

import org.apache.jena.graph.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.util.compose.DatasetCollector;
import org.junit.Assert;
import org.junit.Test;

public abstract class TestDatasetCollector extends Assert {

    public abstract DatasetCollector testInstance();

    @Test
    public void collectionOfEmptyStreamShouldBeEmpty() {
        final Dataset collected = Stream.<Dataset>empty().collect(testInstance());
        assertTrue(collected.isEmpty());
    }

    @Test
    public void collectionOfStreamOfEmptyDatasetsShouldBeEmpty() {
        Stream<Dataset> stream = Stream.<Dataset>builder()
                .add(DatasetFactory.create())
                .add(DatasetFactory.create())
                .add(DatasetFactory.create()).build();
        final Dataset collected = stream.collect(testInstance());
        assertTrue(collected.isEmpty());
    }

    @Test(expected=NullPointerException.class)
    public void noNullDatasetsAllowed() {
        Stream.<Dataset>builder().add(null).build().collect(testInstance());
    }
    
    @Test
    public void collectingOneDatasetGivesThatDataset() {
        Graph graph = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        Model model = createModelForGraph(graph);
        Dataset dataset = DatasetFactory.create(model);
        Node graphName = NodeFactory.createBlankNode();
        dataset.addNamedModel(graphName.toString(), model);
        Dataset collection = Stream.<Dataset>builder().add(dataset).build().collect(testInstance());
        assertDatasetsAreIsomorphicPerGraph(dataset, collection);
    }

    protected static void assertDatasetsAreIsomorphicPerGraph(Dataset dataset1, Dataset dataset2) {
        assertGraphsAreIsomorphic(dataset1.getDefaultModel(), dataset2.getDefaultModel());
        dataset1.listNames().forEachRemaining(graphName ->
            assertGraphsAreIsomorphic(dataset1.getNamedModel(graphName), dataset2.getNamedModel(graphName)));
    }
    
    protected static void assertGraphsAreIsomorphic(Model graph1, Model graph2) {
        assertTrue(graph1.isIsomorphicWith(graph2));
    }
}
