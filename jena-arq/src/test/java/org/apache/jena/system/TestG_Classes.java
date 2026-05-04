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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

public class TestG_Classes {

	static { JenaSystem.init(); }

	@Test
	public void listSubClasses_includes_transitive_subclasses_and_self() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:A rdfs:subClassOf :B .
				:C rdfs:subClassOf :A .
				:D rdfs:subClassOf :B .
				:E rdfs:subClassOf :C .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();

		Node B = SSE.parseNode(":B");
		List<Node> subs = G.listSubClasses(g, B);
		Set<Node> set = new HashSet<>(subs);
		assertEquals(5, set.size());
		assertTrue(set.contains(SSE.parseNode(":B")));
		assertTrue(set.contains(SSE.parseNode(":A")));
		assertTrue(set.contains(SSE.parseNode(":C")));
		assertTrue(set.contains(SSE.parseNode(":D")));
		assertTrue(set.contains(SSE.parseNode(":E")));
	}

	@Test
	public void listSuperClasses_includes_transitive_superclasses_and_self() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:A rdfs:subClassOf :B .
				:C rdfs:subClassOf :A .
				:E rdfs:subClassOf :C .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();

		Node E = SSE.parseNode(":E");
		List<Node> supers = G.listSuperClasses(g, E);
		Set<Node> set = new HashSet<>(supers);
		assertEquals(4, set.size());
		assertTrue(set.contains(SSE.parseNode(":E")));
		assertTrue(set.contains(SSE.parseNode(":C")));
		assertTrue(set.contains(SSE.parseNode(":A")));
		assertTrue(set.contains(SSE.parseNode(":B")));
	}

	@Test
	public void subClasses_return_set_equivalent_to_list() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:A rdfs:subClassOf :B .
				:C rdfs:subClassOf :A .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();

		Node B = SSE.parseNode(":B");
		List<Node> subsList = G.listSubClasses(g, B);
		Set<Node> subsSet = G.subClasses(g, B);
		assertEquals(new HashSet<>(subsList), subsSet);
	}

	@Test
	public void superClasses_return_set_equivalent_to_list() {
		String body = "PREFIX : <http://example/>\n" +
					  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
					  ":A rdfs:subClassOf :B . :C rdfs:subClassOf :A .";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();

		Node C = SSE.parseNode(":C");
		List<Node> supersList = G.listSuperClasses(g, C);
		Set<Node> supersSet = G.superClasses(g, C);
		assertEquals(new HashSet<>(supersList), supersSet);
	}

	@Test
	public void listSubClasses_no_relations_contains_self_only() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				:Z a :U .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();
		Node Z = SSE.parseNode(":Z");
		List<Node> subs = G.listSubClasses(g, Z);
		assertEquals(1, subs.size());
		assertEquals(Z, subs.get(0));
	}

	@Test
	public void listTypesOfNodeRDFS_includes_direct_and_superclasses() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:A rdfs:subClassOf :B .
				:x rdf:type :A .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();
		Node x = SSE.parseNode(":x");
		List<Node> types = G.listTypesOfNodeRDFS(g, x);
		Set<Node> set = new HashSet<>(types);
		assertTrue(set.contains(SSE.parseNode(":A")));
		assertTrue(set.contains(SSE.parseNode(":B")));
	}

	@Test
	public void listNodesOfTypeRDFS_includes_nodes_of_subclasses() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:C rdfs:subClassOf :A .
				:x rdf:type :A .
				:y rdf:type :C .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();
		Node A = SSE.parseNode(":A");
		List<Node> nodes = G.listNodesOfTypeRDFS(g, A);
		Set<Node> set = new HashSet<>(nodes);
		assertTrue(set.contains(SSE.parseNode(":x")));
		assertTrue(set.contains(SSE.parseNode(":y")));
	}

	@Test
	public void allTypesOfNodeRDFS_returns_set_of_types_including_superclasses() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:A rdfs:subClassOf :B .
				:x rdf:type :A .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();
		Node x = SSE.parseNode(":x");
		Set<Node> types = G.allTypesOfNodeRDFS(g, x);
		assertTrue(types.contains(SSE.parseNode(":A")));
		assertTrue(types.contains(SSE.parseNode(":B")));
	}

	@Test
	public void allNodesOfTypeRDFS_returns_set_of_nodes_including_subclass_instances() {
		String body = """
				PREFIX : <http://example/>
				PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
				PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
				:C rdfs:subClassOf :A .
				:x rdf:type :A .
				:y rdf:type :C .
				""";
		Graph g = RDFParser.fromString(body, Lang.TURTLE).toGraph();
		Node A = SSE.parseNode(":A");
		Set<Node> nodes = G.allNodesOfTypeRDFS(g, A);
		assertTrue(nodes.contains(SSE.parseNode(":x")));
		assertTrue(nodes.contains(SSE.parseNode(":y")));
	}
}
