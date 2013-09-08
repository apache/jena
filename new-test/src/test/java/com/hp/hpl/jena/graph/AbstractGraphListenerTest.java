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

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.node;
import static com.hp.hpl.jena.testing_framework.GraphTestUtils.triple;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.testing_framework.AbstractGraphProducerUser;
import com.hp.hpl.jena.testing_framework.GraphTestUtils;

/**
 * Version of graph tests that set up a listener that copies all changes and
 * verifies that after every notification modified graph and original are
 * isomorphic.
 */
public abstract class AbstractGraphListenerTest extends
		AbstractGraphProducerUser {

	private Graph base;
	private CheckChanges checkChanges;

	@Before
	public void setupListener() {
		base = getGraphProducer().newGraph();
		checkChanges = new CheckChanges("", base, getGraphProducer().newGraph());
		base.getEventManager().register(checkChanges);
	}

	/**
	 * A listener to check that a graph is being tracked correctly by its
	 * events.
	 */
	protected class CheckChanges implements GraphListener {

		private Graph copy, original;
		private String desc;

		public CheckChanges(String description, Graph g, Graph c) {
			original = g;
			copy = c;
		}

		public void setDescription(String desc) {
			this.desc = desc;
		}

		protected void verify() {
			GraphTestUtils
					.assertIsomorphic(
							desc
									+ " has not been tracked correctly. [delegating,copy-from-listener]",
							original, copy);
		}

		@Override
		public void notifyAddIterator(Graph g, Iterator<Triple> it) {
			while (it.hasNext())
				copy.add(it.next());
			verify();
		}

		@Override
		public void notifyAddTriple(Graph g, Triple t) {
			copy.add(t);
			verify();
		}

		@Override
		public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
			while (it.hasNext())
				copy.delete(it.next());
			verify();
		}

		@Override
		public void notifyDeleteTriple(Graph g, Triple t) {
			copy.delete(t);
			verify();
		}

		@Override
		public void notifyEvent(Graph source, Object value) {
			if (value instanceof GraphEvents) {
				if (GraphEvents.removeAll.equals(value)) {
					notifyRemoveAll(source, Triple.ANY);
				} else {
					GraphEvents event = (GraphEvents) value;
					if ("remove".equals(event.getTitle())) {
						notifyRemoveAll(source, (Triple) event.getContent());
					}
				}
			}
			verify();
		}

		public void notifyRemoveAll(Graph source, Triple t) {
			SimpleBulkUpdateHandler.removeAll(copy, t.getSubject(),
					t.getPredicate(), t.getObject());
			verify();

		}

		@Override
		public void notifyAddList(Graph g, List<Triple> triples) {
			notifyAddIterator(g, triples.iterator());
		}

		@Override
		public void notifyDeleteArray(Graph g, Triple[] triples) {
			notifyDeleteIterator(g, Arrays.asList(triples).iterator());
		}

		@Override
		public void notifyAddArray(Graph g, Triple[] triples) {
			notifyAddIterator(g, Arrays.asList(triples).iterator());
		}

		@Override
		public void notifyAddGraph(Graph g, Graph added) {
			notifyAddIterator(g, added.find(Triple.ANY));
		}

		@Override
		public void notifyDeleteGraph(Graph g, Graph removed) {
			notifyDeleteIterator(g, removed.find(Triple.ANY));
		}

		@Override
		public void notifyDeleteList(Graph g, List<Triple> list) {
			notifyDeleteIterator(g, list.iterator());
		}

	}

	@Test
	public void testAddArray() {
		Triple[] triples = { triple("a b c"), triple("_a _b _c"),
				triple("1 2 3") };
		checkChanges.setDescription("Add array");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(triples);
	}

	@Test
	public void testAddGraph() {
		Graph arg = GraphFactory.createGraphMem();
		GraphTestUtils.graphAdd(arg, "a b c; _a _b_ c; 1 2 3 ");

		checkChanges.setDescription("Add graph");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(arg);
	}

	@Test
	public void testAddIterator() {
		Triple[] triples = { triple("a b c"), triple("_a _b _c"),
				triple("1 2 3") };

		checkChanges.setDescription("Add iterator");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(Arrays.asList(triples).iterator());
	}

	@Test
	public void testAddList() {
		Triple[] triples = { triple("a b c"), triple("_a _b _c"),
				triple("1 2 3") };

		checkChanges.setDescription("Add list");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(Arrays.asList(triples));
	}

	@Test
	public void testAddTriple() {
		checkChanges.setDescription("Add triple");
		base.add(triple("_a b 1"));
	}

	@Test
	public void deleteAddArray() {
		Triple[] add = { triple("a b c"), triple("_a _b _c"), triple("1 2 3"),
				triple("_a b 1") };
		Triple[] remove = { triple("a b c"), triple("_a _b _c"),
				triple("1 2 3") };
		checkChanges.setDescription("Delete array");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(add);
		buh.delete(remove);
	}

	@Test
	public void testRemoveGraph() {
		Graph add = GraphFactory.createGraphMem();
		GraphTestUtils.graphAdd(add, "a b c; _a _b_ c; 1 2 3; _a b 3");
		Graph remove = GraphFactory.createGraphMem();
		GraphTestUtils.graphAdd(remove, "a b c; _a _b_ c; 1 2 3 ");
		checkChanges.setDescription("Remove graph");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(add);
		buh.delete(remove);
	}

	@Test
	public void testRemoveIterator() {
		Triple[] add = { triple("a b c"), triple("_a _b _c"), triple("1 2 3"),
				triple("_a b 1") };
		Triple[] remove = { triple("a b c"), triple("_a _b _c"),
				triple("1 2 3") };

		checkChanges.setDescription("Remove iterator");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(add);
		buh.add(Arrays.asList(remove).iterator());
	}

	@Test
	public void testRemoveList() {

		Triple[] add = { triple("a b c"), triple("_a _b _c"), triple("1 2 3"),
				triple("_a b 1") };
		Triple[] remove = { triple("a b c"), triple("_a _b _c"),
				triple("1 2 3") };

		checkChanges.setDescription("Remove list");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(add);
		buh.add(Arrays.asList(remove));
	}

	@Test
	public void testRemoveTriple() {
		checkChanges.setDescription("Remove triple");
		base.add(triple("_a b 1"));
		base.delete(triple("_a b 1"));
	}

	@Test
	public void testRemoveTripleAsNodes() {
		checkChanges.setDescription("Remove triple");
		base.add(triple("_a b 1"));
		base.remove(node("_a"), node("b"), node("1"));
	}

	@Test
	public void testRemoveAll() {

		Triple[] add = { triple("a b c"), triple("_a _b _c"), triple("1 2 3"),
				triple("_a b 1") };

		checkChanges.setDescription("Remove list");
		BulkUpdateHandler buh = base.getBulkUpdateHandler();
		buh.add(add);
		base.clear();
	}

}
