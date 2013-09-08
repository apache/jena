/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.hp.hpl.jena.graph;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.node;
import static com.hp.hpl.jena.testing_framework.GraphTestUtils.triple;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runners.Suite;
import com.hp.hpl.jena.testing_framework.GraphEventManagerProducerInterface;
import com.hp.hpl.jena.testing_framework.GraphProducerInterface;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

@Suite.SuiteClasses({ AbstractGraphSuite.GraphTest.class,
		AbstractGraphSuite.ReifierTest.class,
		AbstractGraphSuite.FindLiterals.class,
		AbstractGraphSuite.ExtractTest.class,
		AbstractGraphSuite.EventManagerTest.class,
		AbstractGraphSuite.TransactionTest.class,
		AbstractGraphSuite.RegisterListenerTest.class,
		AbstractGraphSuite.ListenerTest.class,
		AbstractGraphSuite.PrefixMappingTest.class })
public abstract class AbstractGraphSuite {

	static protected GraphProducerInterface graphProducer;

	public static void setGraphProducer(GraphProducerInterface graphProducer) {
		AbstractGraphSuite.graphProducer = graphProducer;
	}

	public static class GraphTest extends AbstractGraphTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

	public static class ReifierTest extends AbstractReifierTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

	public static class FindLiterals extends AbstractFindLiteralsTest {
		public FindLiterals(String data, int size, String search,
				String results, boolean litReq) {
			super(data, size, search, results, litReq);
			AbstractFindLiteralsTest.graphProducer = AbstractGraphSuite.graphProducer;
		}
	}

	public static class ExtractTest extends AbstractGraphExtractTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

	public static class EventManagerTest extends AbstractEventManagerTest {
		@Override
		protected GraphEventManagerProducerInterface getEventManagerProducer() {
			return new GraphEventManagerProducerInterface() {

				@Override
				public GraphEventManager newEventManager() {
					return AbstractGraphSuite.graphProducer.newGraph()
							.getEventManager();
				}
			};
		}

		/**
		 * Utility: get a graph, register L with its manager, return the graph.
		 */
		protected Graph getAndRegister(GraphListener gl) {
			Graph g = AbstractGraphSuite.graphProducer.newGraph();
			g.getEventManager().register(gl);
			return g;
		}

		@Test
		public void testRemoveAllEvent() {
			Graph g = getAndRegister(L);
			g.clear();
			L.assertHas(new Object[] { "someEvent", g, GraphEvents.removeAll });
		}

		@Test
		public void testRemoveSomeEvent() {
			Graph g = getAndRegister(L);
			Node S = node("S"), P = node("??"), O = node("??");
			g.remove(S, P, O);
			Object event = GraphEvents.remove(S, P, O);
			L.assertHas(new Object[] { "someEvent", g, event });
		}

		/**
		 * Ensure that triples removed by calling .remove() on the iterator
		 * returned by a find() will generate deletion notifications.
		 */
		@Test
		public void testEventDeleteByFind() {
			Graph g = getAndRegister(L);
			if (g.getCapabilities().iteratorRemoveAllowed()) {
				Triple toRemove = triple("remove this triple");
				g.add(toRemove);
				ExtendedIterator<Triple> rtr = g.find(toRemove);
				assertTrue("ensure a(t least) one triple", rtr.hasNext());
				rtr.next();
				rtr.remove();
				rtr.close();
				L.assertHas(new Object[] { "add", g, toRemove, "delete", g,
						toRemove });
			}
		}
	}

	public static class TransactionTest extends AbstractTransactionHandlerTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

	public static class RegisterListenerTest extends
			AbstractRegisterGraphListenerTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

	public static class ListenerTest extends AbstractGraphListenerTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

	public static class PrefixMappingTest extends
			AbstractGraphPrefixMappingTest {
		@Override
		public GraphProducerInterface getGraphProducer() {
			return AbstractGraphSuite.graphProducer;
		}
	}

}
