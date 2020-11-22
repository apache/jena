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

package org.apache.jena.sparql.core.mem;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.GraphView;

/**
 * A {@link GraphView} specialization that relies on the {@link DatasetGraphInMemory} from which this graph is drawn to
 * manage prefixes.
 *
 */
public class GraphInMemory extends GraphView {

    private final DatasetGraphInMemory datasetGraph;

    GraphInMemory(final DatasetGraphInMemory dsg, final Node gn) {
        super(dsg, gn);
        this.datasetGraph = dsg;
    }

    @Override
    protected PrefixMapping createPrefixMapping() {
        return Prefixes.adapt(datasetGraph.prefixes());
    }

    private DatasetGraphInMemory datasetGraph() {
        return datasetGraph;
    }
}
