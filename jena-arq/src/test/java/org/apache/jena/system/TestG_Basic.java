/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

public class TestG_Basic {

	static { JenaSystem.init(); }

	@Test
	public void contains_true_and_false() {
		Graph g = graph(":s :p :o .");
		Node s = SSE.parseNode(":s");
		Node p = SSE.parseNode(":p");
		Node o = SSE.parseNode(":o");

		assertTrue(G.contains(g, s, p, o));
		// different predicate -> false
		assertFalse(G.contains(g, s, SSE.parseNode(":q"), o));
	}

	@Test
	public void containsNode_detects_nodes() {
		Graph g = graph(":s :p :o . _:b :p :o .");
		Node s = SSE.parseNode(":s");
		Node o = SSE.parseNode(":o");
		Node missing = SSE.parseNode(":x");

		assertTrue(G.containsNode(g, s));
		assertTrue(G.containsNode(g, o));
		assertFalse(G.containsNode(g, missing));
	}

	@Test
	public void hasProperty_true_false() {
		Graph g = graph(":s :p :o .");
		Node s = SSE.parseNode(":s");
		Node p = SSE.parseNode(":p");
		Node q = SSE.parseNode(":q");

		assertTrue(G.hasProperty(g, s, p));
		assertFalse(G.hasProperty(g, s, q));
	}

	@Test
	public void containsOne_single_none_multiple() {
		Node s = SSE.parseNode(":s");
		Node p = SSE.parseNode(":p");
		Node o = SSE.parseNode(":o");

		Graph gSingle = graph(":s :p :o .");
		assertTrue(G.containsOne(gSingle, s, p, o));

		Graph gNone = graph(":s :q :o .");
		assertFalse(G.containsOne(gNone, s, p, o));

		Graph gMulti = graph(":s :p :o1 . :s :p :o2 .");
		assertFalse(G.containsOne(gMulti, s, p, Node.ANY));
	}

	@Test
	public void hasType_direct() {
		Graph g = graph(":x rdf:type :A .");
		Node x = SSE.parseNode(":x");
		Node A = SSE.parseNode(":A");
		assertTrue(G.hasType(g, x, A));
		assertFalse(G.hasType(g, x, SSE.parseNode(":B")));
	}

	@Test
	public void isOfType_with_subclass() {
		String body = """
		        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		        PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		        PREFIX : <http://example/>

				:A rdfs:subClassOf :B .
				:x rdf:type :A .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();
		Node x = SSE.parseNode(":x");
		Node B = SSE.parseNode(":B");
		Node C = SSE.parseNode(":C");
		assertTrue(G.isOfType(g, x, B));
		assertFalse(G.isOfType(g, x, C));
	}

	private static Graph graph(String ttlBody) {
		String setup = "PREFIX :     <http://example/>\n" +
					   "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
		return RDFParser.fromString(setup+ttlBody, Lang.TURTLE).toGraph();
	}
}
