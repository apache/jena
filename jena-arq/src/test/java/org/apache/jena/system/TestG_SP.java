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

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.util.iterator.ExtendedIterator;

public class TestG_SP {

	static { JenaSystem.init(); }

	private static final Node s = SSE.parseNode(":s");
	private static final Node x = SSE.parseNode(":x");
	private static final Node p = SSE.parseNode(":p");
	private static final Node o1 = SSE.parseNode(":o1");
	private static final Node o2 = SSE.parseNode(":o2");

	@Test
	public void getSP_many_returns_one_of_objects() {
		Graph g = graph(":s :p :o1 . :s :p :o2 .");
		Node got = G.getSP(g, s, p);
		assertTrue(o1.sameTermAs(got) || o2.sameTermAs(got), "getSP should return one of the objects when multiple exist");
	}

	@Test
	public void getOneSP_single_ok() {
		Graph g = graph(":s :p :o1 .");
		Node got = G.getOneSP(g, s, p);
		assertEquals(o1, got);
	}

	@Test
	public void getOneSP_none_throws() {
		Graph g = graph(":s :q :o1 .");
		assertThrows(RDFDataException.class, ()->G.getOneSP(g, s, p));
	}

	@Test
	public void getOneSP_multiple_throws() {
		Graph g = graph(":s :p :o1 . :s :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.getOneSP(g, s, p));
	}

	@Test
	public void getZeroOrOneSP_none_null() {
		Graph g = graph(":s :q :o1 .");
		Node got = G.getZeroOrOneSP(g, s, p);
		assertNull(got);
	}

	@Test
	public void getZeroOrOneSP_one_ok() {
		Graph g = graph(":s :p :o1 .");
		Node got = G.getZeroOrOneSP(g, s, p);
		assertEquals(o1, got);
	}

	@Test
	public void getZeroOrOneSP_multiple_throws() {
		Graph g = graph(":s :p :o1 . :s :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.getZeroOrOneSP(g, s, p));
	}

	@Test
	public void hasOneSP_none_false() {
		Graph g = graph(":s :q :o1 .");
		assertFalse(G.hasOneSP(g, s, p));
	}

	@Test
	public void hasOneSP_one_true() {
		Graph g = graph(":s :p :o1 .");
		assertTrue(G.hasOneSP(g, s, p));
	}

	@Test
	public void hasOneSP_multiple_throws() {
		Graph g = graph(":s :p :o1 . :s :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.hasOneSP(g, s, p));
	}

	private static Graph graph(String ttlBody) {
		String setup = "PREFIX :     <http://example/>\n";
		return RDFParser.fromString(setup+ttlBody, Lang.TURTLE).toGraph();
	}

	// --- Wildcard (Node.ANY) handling tests

	@Test
	public void getSP_subjectAny_returns_one_of_objects() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o2 .");
		Node got = G.getSP(g, Node.ANY, p);
		assertTrue(o1.sameTermAs(got) || o2.sameTermAs(got));
	}

	@Test
	public void getOneSP_subjectAny_single_ok() {
		Graph g = graph(":s1 :p :o1 .");
		Node got = G.getOneSP(g, Node.ANY, p);
		assertEquals(o1, got);
	}

	@Test
	public void getOneSP_subjectAny_multiple_throws() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.getOneSP(g, Node.ANY, p));
	}

	@Test
	public void getZeroOrOneSP_predicateAny_single_ok() {
		Graph g = graph(":s :p1 :o1 .");
		Node got = G.getZeroOrOneSP(g, s, Node.ANY);
		assertEquals(o1, got);
	}

	@Test
	public void getZeroOrOneSP_predicateAny_multiple_throws() {
		Graph g = graph(":s :p1 :o1 . :s :p2 :o2 .");
		assertThrows(RDFDataException.class, ()->G.getZeroOrOneSP(g, s, Node.ANY));
	}

	@Test
	public void hasOneSP_predicateAny_true_false_and_throws() {
		// none -> false
		Graph g0 = graph(":s :q :o1 .");
		assertFalse(G.hasOneSP(g0, x, Node.ANY));

		// single -> true
		Graph g1 = graph(":s :p1 :o1 .");
		assertTrue(G.hasOneSP(g1, s, Node.ANY));

		// multiple -> throws
		Graph g2 = graph(":s :p1 :o1 . :s :p2 :o2 .");
		assertThrows(RDFDataException.class, ()->G.hasOneSP(g2, s, Node.ANY));
	}

	@Test
	public void iterSP_returns_objects() {
		Graph g = graph(":s :p :o1 . :s :p :o2 . :s2 :p :o2 .");
		ExtendedIterator<Node> iter = G.iterSP(g, s, p);
		try {
			List<Node> seen = iter.toList();
			assertEquals(2, seen.size());
			assertTrue(seen.contains(o1) && seen.contains(o2));
		} finally { iter.close(); }
	}

	@Test
	public void listSP_returns_list_of_objects() {
		Graph g = graph(":s :p :o1 . :s :p :o2 . :s2 :p :o2 .");
		List<Node> list = G.listSP(g, s, p);
		assertEquals(2, list.size());
		assertTrue(list.contains(o1) && list.contains(o2));
	}

	@Test
	public void countSP_counts_matches() {
		Graph g = graph(":s :p :o1 . :s :p :o2 . :s2 :p :o2 .");
		long count = G.countSP(g, s, p);
		assertEquals(2, count);
	}

	@Test
	public void allSP_returns_set_of_objects() {
		Graph g = graph(":s :p :o1 . :s :p :o2 . :s2 :p :o2 .");
		Set<Node> all = G.allSP(g, s, p);
		assertEquals(2, all.size());
		assertTrue(all.contains(o1) && all.contains(o2));
	}
}
