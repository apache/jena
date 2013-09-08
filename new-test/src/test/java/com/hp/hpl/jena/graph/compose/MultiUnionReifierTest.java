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

package com.hp.hpl.jena.graph.compose;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.graph.AbstractReifierTest;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeCreateUtils;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.impl.ReifierStd;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.testing_framework.AbstractGraphProducer;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Test the reifier for multi-unions.
 */
public class MultiUnionReifierTest extends AbstractReifierTest {
	private GraphProducerInterface graphProducer = new AbstractGraphProducer() {
		@Override
		protected Graph createNewGraph() {
			Graph gBase = GraphFactory.createDefaultGraph();
			Graph g1 = GraphFactory.createDefaultGraph();
			Graph g2 = GraphFactory.createDefaultGraph();
			MultiUnion poly = new MultiUnion(new Graph[] { gBase, g1, g2 });
			poly.setBaseGraph(gBase);
			return poly;

		}

	};

	@Override
	public GraphProducerInterface getGraphProducer() {
		return graphProducer;
	}

	@Test
	public void testX() {
		List<Triple> triples = new ArrayList<Triple>();
		triples.add(new Triple(NodeFactory.createURI("eh:/a"), NodeFactory
				.createURI("eh:/P"), NodeFactory.createURI("eh:/b")));
		triples.add(new Triple(Node.ANY, RDF.object.asNode(), NodeFactory
				.createURI("eh:/d")));
		triples.add(new Triple(Node.ANY, RDF.predicate.asNode(), NodeFactory
				.createURI("eh:/R")));
		triples.add(new Triple(Node.ANY, RDF.subject.asNode(), NodeFactory
				.createURI("eh:/c")));
		triples.add(new Triple(Node.ANY, RDF.type.asNode(), RDF.Statement
				.asNode()));
		triples.add(new Triple(Node.ANY, RDF.object.asNode(), NodeFactory
				.createURI("eh:/c")));
		triples.add(new Triple(Node.ANY, RDF.predicate.asNode(), NodeFactory
				.createURI("eh:/Q")));
		triples.add(new Triple(Node.ANY, RDF.subject.asNode(), NodeFactory
				.createURI("eh:/b")));
		triples.add(new Triple(Node.ANY, RDF.type.asNode(), RDF.Statement
				.asNode()));
		triples.add(new Triple(NodeFactory.createURI("eh:/b"), NodeFactory
				.createURI("eh:/Q"), NodeFactory.createURI("eh:/c")));

		MultiUnion mu = multi("a P b; !b Q c; ~c R d", "");
		StringBuilder sb = new StringBuilder();
		List<Triple> results = GraphUtil.findAll(mu).toList();
		assertEquals(triples.size(), results.size());
		for (Triple t : results) {
			boolean found = false;
			for (Triple t2 : triples) {
				if (t2.matches(t)) {
					triples.remove(t);
					found = true;
					break;
				}
			}
			if (!found) {
				sb.append("did not find " + t.toString());
			}
		}
		if (sb.length() > 0) {
			fail(sb.toString());
		}

	}

	private MultiUnion multi(String a, String b) {
		Graph A = graph(a), B = graph(b);
		return new MultiUnion(new Graph[] { A, B });
	}

	static int count = 0;

	private Graph graph(String facts) {
		Graph result = Factory.createDefaultGraph();
		String[] factArray = facts.split(";");
		for (int i = 0; i < factArray.length; i += 1) {
			String fact = factArray[i].trim();
			if (fact.equals("")) {
			} else if (fact.charAt(0) == '!') {
				Triple t = NodeCreateUtils.createTriple(fact.substring(1));
				result.add(t);
				ReifierStd.reifyAs(result,
						NodeCreateUtils.create("_r" + ++count), t);
			} else if (fact.charAt(0) == '~') {
				Triple t = NodeCreateUtils.createTriple(fact.substring(1));
				ReifierStd.reifyAs(result,
						NodeCreateUtils.create("_r" + ++count), t);
			} else
				result.add(NodeCreateUtils.createTriple(fact));
		}
		return result;
	}

}
