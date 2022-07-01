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
import org.apache.jena.riot.RDFDataMgr;

/** Test graphs  */
public abstract class AbstractTestGraphRDFS extends AbstractTestRDFS {
    private static final String DIR = "testing/RDFS";
    private static final String DATA_FILE = DIR+"/rdfs-data.ttl";
    private static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl";

    // Should be the same outcomes.
    // Backward rules.
    private static final String RULES_FILE_BWD = DIR+"/rdfs-min-backwards.rules";
    // Forward rules.
    private static final String RULES_FILE_FWD = DIR+"/rdfs-min.rules";

    private static final String RULES_FILE = RULES_FILE_FWD;

    private static Graph referenceGraph;
    protected static Graph vocab;
    protected static Graph data;
    static {
        vocab = RDFDataMgr.loadGraph(VOCAB_FILE);
        data = RDFDataMgr.loadGraph(DATA_FILE);
        referenceGraph = LibTestRDFS.createRulesGraph(data, vocab, RULES_FILE);
    }

    /** The graph with the right answers (via jena-core Rules) */
    @Override
    final
    protected Graph getReferenceGraph() {
        return referenceGraph;
    }

    @Override
    protected String getReferenceLabel() {
        return "InfGraph";
    }


}
