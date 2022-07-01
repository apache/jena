/**
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

package org.apache.jena.commonsrdf.examples;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.commonsrdf.JenaRDF;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;

/** Adapt a Jena Graph after parsing data into it */
public class Ex_JenaGraphToCommonsRDFGraph {
    static { LogCtl.setLogging(); }

    public static void main(String ...a) {
        org.apache.jena.graph.Graph jGraph = GraphFactory.createGraphMem();
        RDFDataMgr.read(jGraph, "D.ttl");

        // "graph" is a CommonsRDF graph
        Graph graph = JenaCommonsRDF.fromJena(jGraph);

        // Add to CommonsRDF Graph
        RDF rft = new JenaRDF();
        graph.add(rft.createIRI("http://example/s2"),
                  rft.createIRI("http://example/p2"),
                  rft.createLiteral("foo"));
        System.out.println("==== Write CommonsRDF graph\n");
        graph.stream().forEach(System.out::println);

        System.out.println("\n==== Write Jena graph directly\n");
        // And its in the Jena graph
        RDFDataMgr.write(System.out, jGraph, Lang.TTL);
    }
}

