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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

import java.util.stream.Stream;

public class TestG_Triple {

	static { JenaSystem.init(); }

	private static final Node s = SSE.parseNode(":s");
	private static final Node p = SSE.parseNode(":p");
	private static final Node o = SSE.parseNode(":o");

	@Test
	public void getOne_single_ok() {
		Graph g = graph(":s :p :o .");
		Triple t = G.getOne(g, s, p, o);
		assertEquals(SSE.parseTriple("(:s :p :o)"), t);
	}

	@Test
	public void getOne_none_throws() {
		Graph g = graph(":s :q :o .");
		assertThrows(RDFDataException.class, ()->G.getOne(g, s, p, o));
	}

	@Test
	public void getOne_multiple_throws_with_wildcard() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o2 .");
		// Use wildcard for object to match multiple objects
		assertThrows(RDFDataException.class, ()->G.getOne(g, s, p, Node.ANY));
	}

	@Test
	public void getZeroOrOne_none_null() {
		Graph g = graph(":s :q :o .");
		Triple t = G.getZeroOrOne(g, s, p, o);
		assertNull(t);
	}

	@Test
	public void getZeroOrOne_one_ok() {
		Graph g = graph(":s :p :o .");
		Triple t = G.getZeroOrOne(g, s, p, o);
		assertEquals(SSE.parseTriple("(:s :p :o)"), t);
	}

	@Test
	public void getZeroOrOne_multiple_throws() {
		Graph g = graph(":s :p :o1 . :s :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.getZeroOrOne(g, s, p, Node.ANY));
	}

	@Test
	public void getOneOrNull_none_null() {
		Graph g = graph(":s :q :o .");
		Triple t = G.getOneOrNull(g, s, p, o);
		assertNull(t);
	}

	@Test
	public void getOneOrNull_multiple_null() {
		Graph g = graph(":s :p :o1 . :s :p :o2 .");
		Triple t = G.getOneOrNull(g, s, p, Node.ANY);
		assertNull(t);
	}
	private static Graph graph(String ttlBody) {
		String setup = "PREFIX :     <http://example/>\n";
		return RDFParser.fromString(setup+ttlBody, Lang.TURTLE).toGraph();
	}

	@Test
	public void quads2triples() {
		var quad = SSE.parseQuad("(:g :s :p :o)");
		var triple = G.quads2triples(Stream.of(quad)).findFirst().orElseThrow();
		assertEquals(SSE.parseTriple("(:s :p :o)"), triple);
	}
}
