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

package org.apache.jena.commonsrdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.apache.commons.rdf.simple.SimpleRDF;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.junit.Test;

/** Tests of working to/from another ComonsRDF implementation, in thsi case, commonsrdf-simple. */ 
public class TestJenaSimple {
    @Test public void jenaParserToAlienRDF() {
        RDF rft = new SimpleRDF();
        parse(rft);
    }
    

    @Test public void jenaWruteFromAlienRDF() {
        RDF rft = new SimpleRDF();
        Graph crdf_graph = parse(rft);
        
        org.apache.jena.graph.Graph jenaGraph = JenaCommonsRDF.toJena(crdf_graph);
        
        String str = org.apache.jena.riot.RDFWriter.create().source(jenaGraph).lang(Lang.TTL).asString();
        assertTrue(str.contains("123"));
    }

    public static Graph parse(RDF rft) {
        Graph crdf_graph = rft.createGraph();
        StreamRDF dest = JenaCommonsRDF.streamJenaToCommonsRDF(rft, crdf_graph);
        StringReader in = new StringReader("PREFIX : <http://example/> :s :p 123");
        
        org.apache.jena.riot.RDFParser.create().source(in).lang(Lang.TTL).parse(dest);
        long x = crdf_graph.stream().count();
        assertEquals(1, x);
        return crdf_graph;
    }
}
