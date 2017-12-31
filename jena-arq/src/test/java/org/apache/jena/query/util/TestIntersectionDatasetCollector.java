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

package org.apache.jena.query.util;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import java.util.stream.Stream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class TestIntersectionDatasetCollector extends TestDatasetCollector {

    @Override
    public DatasetCollector testInstance() {
        return DatasetLib.collectors().intersect();
    }

    @Test
    public void testIntersection() {
        final Model m1 = createModelForGraph(parseGraph("(graph (triple <s1> <p1> <o1> ))"));
        final Dataset ds1 = DatasetFactory.create(m1);
        final String graphName1 = createBlankNode().toString();
        ds1.addNamedModel(graphName1, m1);
        final Model m2 = createModelForGraph(parseGraph("(graph (triple <s2> <p2> <o2> ))"));
        final Dataset ds2 = DatasetFactory.create(m2);
        final String graphName2 = createBlankNode().toString();
        ds2.addNamedModel(graphName2, m2);
        final Model m3 = createModelForGraph(parseGraph("(graph (triple <s3> <p3> <o3> ))"));
        final String graphName3 = createBlankNode().toString();
        ds1.addNamedModel(graphName3, m3);
        ds2.addNamedModel(graphName3, m3);
        
        Dataset ds = Stream.<Dataset>builder().add(ds1).add(ds2).build().collect(testInstance());
        
        assertTrue(ds.getDefaultModel().isEmpty());
        assertTrue(ds.getNamedModel(graphName1).isEmpty());
        assertTrue(ds.getNamedModel(graphName2).isEmpty());
        assertTrue(m3.isIsomorphicWith(ds.getNamedModel(graphName3)));
    }

}
