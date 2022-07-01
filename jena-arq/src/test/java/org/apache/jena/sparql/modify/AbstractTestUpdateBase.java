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

package org.apache.jena.sparql.modify;

import org.apache.jena.arq.ARQTestSuite;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.update.UpdateAction;

public abstract class AbstractTestUpdateBase
{
    protected abstract DatasetGraph getEmptyDatasetGraph();

    protected void defaultGraphData(DatasetGraph gStore, Graph data) {
        Graph g = gStore.getDefaultGraph();
        g.clear();
        GraphUtil.addInto(g, data);
    }

    protected void namedGraphData(DatasetGraph gStore, Node uri, Graph data) {
        Graph g = gStore.getGraph(uri);
        if ( g == null ) {
            gStore.addGraph(uri, GraphFactory.createJenaDefaultGraph());
            g = gStore.getGraph(uri);
        } else
            g.clear();
        GraphUtil.addInto(g, data);
    }

    protected static final String FileBase = ARQTestSuite.testDirUpdate;

    protected static void script(DatasetGraph gStore, String filename) {
        UpdateAction.readExecute(FileBase + "/" + filename, gStore);
    }

    protected static boolean graphEmpty(Graph graph) {
        return graph.isEmpty();
    }

    protected static boolean graphContains(Graph graph, Triple triple) {
        return graph.contains(triple);
    }
}
