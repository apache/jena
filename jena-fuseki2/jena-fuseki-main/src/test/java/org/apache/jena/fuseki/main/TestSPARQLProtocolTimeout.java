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

package org.apache.jena.fuseki.main;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.Convert;
import org.apache.jena.update.UpdateExecution;

public class TestSPARQLProtocolTimeout extends AbstractFusekiTest
{
    @BeforeEach
    public void before() {
        Graph graph = createTestGraph();
        GSP.service(serviceGSP()).defaultGraph().PUT(graph);
    }

    /** Create a model with 1000 triples. Same method as in {@link org.apache.jena.sparql.api.TestQueryExecutionCancel}. */
    static Graph createTestGraph() {
        Graph graph = GraphFactory.createDefaultGraph();
        IntStream.range(0, 1000)
            .mapToObj(i -> NodeFactory.createURI("http://www.example.org/r" + i))
            .forEach(node -> graph.add(node, node, node));
        return graph;
    }

    static String query(String base, String queryString) {
        return base + "?query=" + Convert.encWWWForm(queryString);
    }

    /** If the HTTP client reaches its timeout and disconnects from the server then it is up
     *  to the server whether it will cancel or complete the started SPARQL update execution. */
    @Test
    public void update_timeout_01() {
        assertThrows(HttpException.class, ()->
            UpdateExecution.service(serviceUpdate())
                .update("INSERT { } WHERE { ?a ?b ?c . ?d ?e ?f . ?g ?h ?i . }")
                .timeout(500, TimeUnit.MILLISECONDS)
                .execute()
                );
    }
}
