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

package org.apache.jena.rdfs;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.rdfs.engine.GraphIncRDFS;
import org.apache.jena.sparql.graph.GraphFactory;

/** Test a GraphRDFS */
public class TestGraphIncRDFS_Combined extends AbstractTestGraphRDFS {

    private Graph testGraph = null;

    public TestGraphIncRDFS_Combined(){
        Graph g = GraphFactory.createDefaultGraph();
        GraphUtil.addInto(g, data);
        GraphUtil.addInto(g, vocab);
        SetupRDFS setup = RDFSFactory.setupRDFS(g);
        testGraph = new GraphIncRDFS(data, setup);
    }

    @Override
    protected boolean removeVocabFromReferenceResults() { return false; }

    @Override
    protected Graph getTestGraph() {
        return testGraph;
    }

    @Override
    protected String getTestLabel() {
        return "Combined data, vocab";
    }

}
