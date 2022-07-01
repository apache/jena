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
import org.apache.commons.rdf.simple.SimpleRDF;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.commonsrdf.JenaCommonsRDF;
import org.apache.jena.commonsrdf.JenaRDF;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

/** Use jena to parse data into an CommonsRDF graph */
public class Ex_ParseIntoCommonsRDFGraph {
    static { LogCtl.setLogging(); }

    public static void main(String ...a) {
        System.out.println("== RDFTermFactoryJena");
        RDF rft1 = new JenaRDF(); // This can be a non-Jena backed factory
        parse(rft1);
        System.out.println();
        System.out.println("== SimpleRDFTermFactory");
        RDF rft2 = new SimpleRDF();
        parse(rft2);
    }

    public static void parse(RDF rft) {
        Graph crdf_graph = rft.createGraph();
        StreamRDF dest = JenaCommonsRDF.streamJenaToCommonsRDF(rft, crdf_graph);
        RDFDataMgr.parse(dest, "D.ttl");
        crdf_graph.stream().forEach(System.out::println);
    }

}

