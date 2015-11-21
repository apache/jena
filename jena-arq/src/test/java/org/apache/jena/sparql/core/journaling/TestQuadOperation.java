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

package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.core.DatasetGraphFactory.createTxnMem;

import org.apache.jena.graph.Node;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadAddition;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadDeletion;
import org.junit.Assert;
import org.junit.Test;

public class TestQuadOperation extends Assert {

	private static final Node graphName = createURI("info:graph");
	private static final Quad q = new Quad(graphName, createBlankNode(), createURI("info:test"), createBlankNode());
	private static final QuadAddition quadAddition = new QuadAddition(q);
	private static final QuadDeletion quadDeletion = new QuadDeletion(q);

	@Test
	public void testEquals() {
		assertEquals(new QuadAddition(q), new QuadAddition(q));
		assertEquals(new QuadDeletion(q), new QuadDeletion(q));
		assertNotEquals(new QuadAddition(q), new QuadDeletion(q));
		assertNotEquals(new QuadDeletion(q), new QuadAddition(q));
		assertNotEquals(new QuadAddition(q), q);
		assertNotEquals(new QuadDeletion(q), q);
	}

	@Test
	public void testActOn() {
		final DatasetGraph testDsg = createTxnMem();
		quadAddition.actOn(testDsg);
		assertTrue(testDsg.contains(q));
		quadDeletion.actOn(testDsg);
		assertFalse(testDsg.contains(q));
	}

	@Test
	public void testInversion() {
		final DatasetGraph testDsg = createTxnMem();
		quadDeletion.inverse().actOn(testDsg);
		assertTrue(testDsg.contains(q));
		quadAddition.inverse().actOn(testDsg);
		assertFalse(testDsg.contains(q));
	}

	@Test
	public void testInversionInSequence() {
		final DatasetGraph testDsg = createTxnMem();
		quadAddition.actOn(testDsg);
		assertTrue(testDsg.contains(q));
		quadAddition.inverse().actOn(testDsg);
		assertFalse(testDsg.contains(q));
	}

	@Test
	public void testWithActualGraphStore() {
		final DatasetGraph realDsg = DatasetGraphFactory.createMem();
		realDsg.addGraph(graphName, new GraphMem());
		quadAddition.actOn(realDsg);
		assertTrue(realDsg.contains(q));
		quadDeletion.actOn(realDsg);
		assertFalse(realDsg.contains(q));
		quadAddition.actOn(realDsg);
		assertTrue(realDsg.contains(q));
		quadAddition.inverse().actOn(realDsg);
		assertFalse(realDsg.contains(q));
	}
}
