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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

import java.util.stream.Stream;

public class TestG_Quad {

	static { JenaSystem.init(); }

	private static final Node s = SSE.parseNode(":s");
	private static final Node p = SSE.parseNode(":p");
	private static final Node o = SSE.parseNode(":o");

	// DatasetGraph getOne (quads)
	@Test
	public void getOne_datasetgraph_single_ok() {
		DatasetGraph dsg = dataset(":g { :s :p :o } ");
		Node gname = SSE.parseNode(":g");
		Quad q = G.getOne(dsg, gname, s, p, o);
		assertEquals(Quad.create(gname, s, p, o), q);
	}

	@Test
	public void getOne_datasetgraph_none_throws() {
		DatasetGraph dsg = dataset("");
		Node gname = SSE.parseNode(":g");
		assertThrows(RDFDataException.class, ()->G.getOne(dsg, gname, s, p, o));
	}

	@Test
	public void getOne_datasetgraph_multiple_throws_with_wildcard() {
		DatasetGraph dsg = dataset(":g1 { :s :p :o } :g2 { :s :p :o } ");
		// Use graph wildcard to match multiple quad locations
		assertThrows(RDFDataException.class, ()->G.getOne(dsg, Node.ANY, s, p, o));
	}

	private static DatasetGraph dataset(String trigBody) {
		String setup = "PREFIX :     <http://example/>\n";
		return RDFParser.fromString(setup+trigBody, Lang.TRIG).toDatasetGraph();
	}

	@Test
	public void triples2quads() {
		var quad = SSE.parseQuad("(:g :s :p :o)");
		var q = G.triples2quads(quad.getGraph(), Stream.of(quad.asTriple())).findFirst().orElseThrow();
		assertEquals(quad, q);
	}

	@Test void triples2quadsDftGraph() {
		var triple = SSE.parseTriple("(:s :p :o)");
		var quad = G.triples2quadsDftGraph(Stream.of(triple)).findFirst().orElseThrow();
		assertEquals(Quad.create(Quad.defaultGraphIRI, triple), quad);
	}
}
