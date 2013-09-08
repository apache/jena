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

package com.hp.hpl.jena.graph;

import java.util.Arrays;
import java.util.Set;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;
import com.hp.hpl.jena.util.iterator.Map1;

@RunWith(Parameterized.class)
abstract public class AbstractFindLiteralsTest {
	private final String data;
	private final int size;
	private final String search;
	private final String results;
	private final boolean reqLitType;

	protected static GraphProducerInterface graphProducer;

	public AbstractFindLiteralsTest(final String data, final int size,
			final String search, final String results, boolean reqLitType) {

		this.data = data;
		this.size = size;
		this.search = search;
		this.results = results;
		this.reqLitType = reqLitType;

	}

	static final Map1<Triple, Node> getObject = new Map1<Triple, Node>() {
		@Override
		public Node map1(Triple o) {
			return o.getObject();
		}
	};

	@Test
	public void testFind() {
		Graph g = graphProducer.newGraph();

		if (!reqLitType || g.getCapabilities().handlesLiteralTyping()) {
			graphWith(g, data);

			Node literal = NodeCreateUtils.create(search);
			//
			assertEquals("graph has wrong size", size, g.size());
			Set<Node> got = g.find(Node.ANY, Node.ANY, literal)
					.mapWith(getObject).toSet();
			assertEquals(nodeSet(results), got);
		}
	}

	@After
	public void closeGraphs() {
		graphProducer.closeGraphs();
	}

	@Parameters(name = "TestFindLiterals: graph '{'{0}'}' size {1} search {2} expecting '{'{3}'}'")
	public static Iterable<Object[]> data() {
		Object[][] result = {
				{ "a P 'simple'", 1, "'simple'", "'simple'", false },

				{ "a P 'simple'xsd:string", 1, "'simple'",
						"'simple'xsd:string", true },
				{ "a P 'simple'", 1, "'simple'xsd:string", "'simple'", true },
				{ "a P 'simple'xsd:string", 1, "'simple'xsd:string",
						"'simple'xsd:string", false },
				//
				{ "a P 'simple'; a P 'simple'xsd:string", 2, "'simple'",
						"'simple' 'simple'xsd:string", true },
				{ "a P 'simple'; a P 'simple'xsd:string", 2,
						"'simple'xsd:string", "'simple' 'simple'xsd:string",
						true },
				//
				{ "a P 1", 1, "1", "1", false },
				{ "a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float", false },
				{ "a P '1'xsd:double", 1, "'1'xsd:double", "'1'xsd:double",
						false },
				{ "a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float", false },
				{ "a P '1.1'xsd:float", 1, "'1'xsd:float", "", false },
				{ "a P '1'xsd:double", 1, "'1'xsd:int", "", false },
				//
				{ "a P 'abc'rdf:XMLLiteral", 1, "'abc'", "", false },
				{ "a P 'abc'", 1, "'abc'rdf:XMLLiteral", "", false },
				//
				// floats & doubles are not compatible
				//
				{ "a P '1'xsd:float", 1, "'1'xsd:double", "", false },
				{ "a P '1'xsd:double", 1, "'1'xsd:float", "", false },
				//
				{ "a P 1", 1, "'1'", "", false },
				{ "a P 1", 1, "'1'xsd:integer", "'1'xsd:integer", false },
				{ "a P 1", 1, "'1'", "", false },
				{ "a P '1'xsd:short", 1, "'1'xsd:integer", "'1'xsd:short", true },
				{ "a P '1'xsd:int", 1, "'1'xsd:integer", "'1'xsd:int", true }, };
		return Arrays.asList(result);
	}
}
