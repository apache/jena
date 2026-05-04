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

public class TestG_PO {

	static { JenaSystem.init(); }

	private static final Node s1 = SSE.parseNode(":s1");
	private static final Node s2 = SSE.parseNode(":s2");
	private static final Node x = SSE.parseNode(":x");
	private static final Node p = SSE.parseNode(":p");
	private static final Node o1 = SSE.parseNode(":o1");
	private static final Node o2 = SSE.parseNode(":o2");

	@Test
	public void getPO_many_returns_one_of_subjects() {
		Graph g = graph(":s1 :p :o . :s2 :p :o .");
		Node got = G.getPO(g, p, SSE.parseNode(":o"));
		assertTrue(s1.sameTermAs(got) || s2.sameTermAs(got));
	}

	@Test
	public void getOnePO_single_ok() {
		Graph g = graph(":s :p :o1 .");
		Node got = G.getOnePO(g, p, o1);
		assertEquals(SSE.parseNode(":s"), got);
	}

	@Test
	public void getOnePO_none_throws() {
		Graph g = graph(":s :q :o1 .");
		assertThrows(RDFDataException.class, ()->G.getOnePO(g, p, o1));
	}

	@Test
	public void getOnePO_multiple_throws() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o1 .");
		assertThrows(RDFDataException.class, ()->G.getOnePO(g, p, o1));
	}

	@Test
	public void getZeroOrOnePO_none_null() {
		Graph g = graph(":s :q :o1 .");
		Node got = G.getZeroOrOnePO(g, p, o1);
		assertNull(got);
	}

	@Test
	public void getZeroOrOnePO_one_ok() {
		Graph g = graph(":s :p :o1 .");
		Node got = G.getZeroOrOnePO(g, p, o1);
		assertEquals(SSE.parseNode(":s"), got);
	}

	@Test
	public void getZeroOrOnePO_multiple_throws() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o1 .");
		assertThrows(RDFDataException.class, ()->G.getZeroOrOnePO(g, p, o1));
	}

	@Test
	public void hasOnePO_none_false() {
		Graph g = graph(":s :q :o1 .");
		assertFalse(G.hasOnePO(g, p, o1));
	}

	@Test
	public void hasOnePO_one_true() {
		Graph g = graph(":s :p :o1 .");
		assertTrue(G.hasOnePO(g, p, o1));
	}

	@Test
	public void hasOnePO_multiple_throws() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o1 .");
		assertThrows(RDFDataException.class, ()->G.hasOnePO(g, p, o1));
	}

	// Wildcard tests similar to TestG_SP
	@Test
	public void getPO_objectAny_returns_one_of_subjects() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o2 .");
		Node got = G.getPO(g, p, Node.ANY);
		assertTrue(SSE.parseNode(":s1").sameTermAs(got) || SSE.parseNode(":s2").sameTermAs(got));
	}

	@Test
	public void getOnePO_objectAny_single_ok() {
		Graph g = graph(":s1 :p :o1 .");
		Node got = G.getOnePO(g, p, Node.ANY);
		assertEquals(SSE.parseNode(":s1"), got);
	}

	@Test
	public void getOnePO_objectAny_multiple_throws() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.getOnePO(g, p, Node.ANY));
	}

	@Test
	public void getZeroOrOnePO_objectAny_single_ok() {
		Graph g = graph(":s1 :p :o1 .");
		Node got = G.getZeroOrOnePO(g, p, Node.ANY);
		assertEquals(SSE.parseNode(":s1"), got);
	}

	@Test
	public void getZeroOrOnePO_objectAny_multiple_throws() {
		Graph g = graph(":s1 :p :o1 . :s2 :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.getZeroOrOnePO(g, p, Node.ANY));
	}

	@Test
	public void hasOnePO_objectAny_true_false_and_throws() {
		// none -> false
		Graph g0 = graph(":s :q :o1 .");
		assertFalse(G.hasOnePO(g0, p, Node.ANY));

		// single -> true
		Graph g1 = graph(":s1 :p :o1 .");
		assertTrue(G.hasOnePO(g1, p, Node.ANY));

		// multiple -> throws
		Graph g2 = graph(":s1 :p :o1 . :s2 :p :o2 .");
		assertThrows(RDFDataException.class, ()->G.hasOnePO(g2, p, Node.ANY));
	}

	@Test
	public void iterPO_returns_subjects() {
		Graph g = graph(":s1 :p :o . :s2 :p :o . :s3 :q :o .");
		ExtendedIterator<Node> iter = G.iterPO(g, p, SSE.parseNode(":o"));
		try {
			List<Node> seen = iter.toList();
			assertEquals(2, seen.size());
			assertTrue(seen.contains(SSE.parseNode(":s1")) && seen.contains(SSE.parseNode(":s2")));
		} finally { iter.close(); }
	}

	@Test
	public void listPO_returns_list_of_subjects() {
		Graph g = graph(":s1 :p :o . :s2 :p :o . :s3 :q :o .");
		List<Node> list = G.listPO(g, p, SSE.parseNode(":o"));
		assertEquals(2, list.size());
		assertTrue(list.contains(SSE.parseNode(":s1")) && list.contains(SSE.parseNode(":s2")));
	}

	@Test
	public void countPO_counts_matches() {
		Graph g = graph(":s1 :p :o . :s2 :p :o . :s3 :q :o .");
		long count = G.countPO(g, p, SSE.parseNode(":o"));
		assertEquals(2, count);
	}

	@Test
	public void allPO_returns_set_of_subjects() {
		Graph g = graph(":s1 :p :o . :s2 :p :o . :s3 :q :o .");
		Set<Node> all = G.allPO(g, p, SSE.parseNode(":o"));
		assertEquals(2, all.size());
		assertTrue(all.contains(SSE.parseNode(":s1")) && all.contains(SSE.parseNode(":s2")));
	}

	private static Graph graph(String ttlBody) {
		String setup = "PREFIX :     <http://example/>\n";
		return RDFParser.fromString(setup+ttlBody, Lang.TURTLE).toGraph();
	}
}
