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

import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import org.apache.jena.graph.Graph;
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
        Graph graph = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        Model model = createModelForGraph(graph);
        Dataset dataset = DatasetFactory.create(model);
    }

}
