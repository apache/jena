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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.CollectorStreamTriples;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/**
 * Stream machinery tests.
 * Tests of stream results are mainly in TestMaterialized*
 */
public class TestStreamRDFS {

    static final String DIR = "testing/RDFS";
    static final String DATA_FILE = DIR+"/rdfs-data.ttl";
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl";
    protected static Graph vocab;
    protected static Graph data;
    protected static ConfigRDFS<Node> setup;

    static {
        vocab = RDFDataMgr.loadGraph(VOCAB_FILE);
        data = RDFDataMgr.loadGraph(DATA_FILE);
    }

    @Test public void basic_0() {
        List<Triple> results = infOutput(stream->StreamRDFOps.sendGraphToStream(data, stream));
        Set<Triple> resultSet = new HashSet<>(results);
        assertEquals(5, data.size());
        assertEquals(13, resultSet.size());
        assertEquals(13, results.size());
    }

    private static Node node_c  = SSE.parseNode(":c");
    private static Node node_X  = SSE.parseNode(":X");
    private static Node node_p  = SSE.parseNode(":p");
    private static Node node_Q  = SSE.parseNode(":Q");
    private static Node node_Q2 = SSE.parseNode(":Q2");
    private static Node rdfType = NodeConst.nodeRDFType;

    @Test public void infer_1() {
        Triple t = SSE.parseTriple("(:c :p :x)");
        List<Triple> results = infOutput(x->x.triple(t));

        assertEquals(4, results.size());
        assertTrue(listContains(results, "(:c :p :x)"));
        assertTrue(listContains(results, "(:c :pTop :x)"));
        assertTrue(listContains(results, "(:c rdf:type :Q)"));
        assertTrue(listContains(results, "(:c rdf:type :Q2)"));
    }

    @Test public void infer_2() {
        Triple t = SSE.parseTriple("(:X rdf:type :T)");
        List<Triple> results = infOutput(x->x.triple(t));

        // Types :T :T2 :T3 and :U.
        assertEquals(4, results.size());
        assertTrue(listContains(results, "(:X rdf:type :T)"));
        assertTrue(listContains(results, "(:X rdf:type :T2)"));
        assertTrue(listContains(results, "(:X rdf:type :T3)"));
        assertTrue(listContains(results, "(:X rdf:type :U)"));
    }

    private static boolean listContains(List<Triple> list, String strTriple) {
        Triple triple = SSE.parseTriple(strTriple);
        return list.stream().anyMatch(t->triple.equals(t));
    }

    private static boolean match(Node node, Node slot) {
        return node == null || node == Node.ANY || node.equals(slot);
    }

    // Inference to a list
    private static List<Triple> infOutput(Consumer<StreamRDF> action) {
        CollectorStreamTriples dest = new CollectorStreamTriples();
        StreamRDF stream = RDFSFactory.streamRDFS(dest, vocab);
        exec(stream, action);
        return dest.getCollected();
    }

    private static void exec(StreamRDF stream, Consumer<StreamRDF> action) {
        stream.start();
        try {
            action.accept(stream);
        } finally { stream.finish(); }
    }
}
