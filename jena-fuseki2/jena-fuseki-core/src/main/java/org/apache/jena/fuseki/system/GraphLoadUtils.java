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


package org.apache.jena.fuseki.system;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

/** A packaging of code to do a controlled read of a graph or model */

public class GraphLoadUtils
{
    // ---- Model level

    public static Model readModel(String uri, int limit) {
        Graph g = GraphMemFactory.createGraphMem();
        readUtil(g, uri, limit);
        return ModelFactory.createModelForGraph(g);
    }

    public static void loadModel(Model model, String uri, int limit) {
        Graph g = model.getGraph();
        readUtil(g, uri, limit);
    }

    // ---- Graph level

    public static Graph readGraph(String uri, int limit) {
        Graph g = GraphMemFactory.createGraphMem();
        readUtil(g, uri, limit);
        return g;
    }

    public static void loadGraph(Graph g, String uri, int limit) {
        readUtil(g, uri, limit);
    }

    // ** Worker.
    private static void readUtil(Graph graph, String uri, int limit) {
        StreamRDF sink = StreamRDFLib.graph(graph);
        sink = new StreamRDFLimited(sink, limit);
        RDFParser.source(uri).streamManager(Fuseki.webStreamManager).parse(sink);
    }
}
