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

package org.apache.jena.graph;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;

import static org.junit.Assert.*;

import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.testing_framework.GraphHelper;
import org.xenei.junit.contract.IProducer;
import org.apache.jena.testing_framework.NodeCreateUtils;

import static org.apache.jena.testing_framework.GraphHelper.*;

/**
 * An abstract test that tests EventManager implementations to ensure they
 * comply with the EventManager contract.
 * 
 * Subclasses of this class must implement the getEventManagerProducer() method
 * to create a new instance of the EventManager for testing.
 */

@Contract(GraphEventManager.class)
public class GraphEventManagerContractTest<T extends GraphEventManager> {

	protected static final Triple[] tripleArray = tripleArray("S P O; Foo R B; X Q Y");

	private IProducer<T> producer;
	
	// the graph that is used as the source of the events.
	private Graph mockGraph;
	// The event manager we are working with.
	private GraphEventManager gem;

	public GraphEventManagerContractTest() {
	}

	@Contract.Inject
	public final void setGraphEventManagerContractTestProducer(
			IProducer<T> producer) {
		this.producer= producer;
	}

	@After
	public final void afterGraphEventManagerContractTest() {
		producer.cleanUp();
	}

	@Before
	public final void beforeGraphEventManagerContractTest() {
		mockGraph = Mockito.mock(Graph.class);
		gem = producer.newInstance();
		L = new RecordingGraphListener();
	}

	/**
	 * Test that when a listener is registered the same EventManager is
	 * returned.
	 */
	@ContractTest
	public void testEventRegister() {
		assertSame(gem, gem.register(new RecordingGraphListener()));
	}

	/**
	 * Test that we can safely unregister a listener that isn't registered.
	 */
	@ContractTest
	public void testEventUnregister() {
		assertSame(gem, gem.unregister(L));
	}

	/**
	 * Handy triple for test purposes.
	 */
	protected Triple SPO = NodeCreateUtils.createTriple("S P O");

	/**
	 * The RecordingListener that reports all the events sent from the
	 * EventManager.
	 */
	protected RecordingGraphListener L;

	/**
	 * Test that adding a triple is reported.
	 */
	@ContractTest
	public void testAddTriple() {
		gem.register(L);
		gem.notifyAddTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "add", mockGraph, SPO });
	}

	/**
	 * Test that deleting a triple is reported.
	 */
	@ContractTest
	public void testDeleteTriple() {
		gem.register(L);
		gem.notifyDeleteTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "delete", mockGraph, SPO });
	}

	/**
	 * Test that when 2 listeners are added both receive events.
	 */
	@ContractTest
	public void testTwoListeners() {
		RecordingGraphListener L1 = new RecordingGraphListener();
		RecordingGraphListener L2 = new RecordingGraphListener();
		gem.register(L1).register(L2);
		gem.notifyAddTriple(mockGraph, SPO);
		L2.assertHas(new Object[] { "add", mockGraph, SPO });
		L1.assertHas(new Object[] { "add", mockGraph, SPO });
	}

	/**
	 * Test that unregistering a listener after registering it results in it not
	 * receiving messages.
	 */
	@ContractTest
	public void testUnregisterWorks() {
		gem.register(L).unregister(L);
		gem.notifyAddTriple(mockGraph, SPO);
		L.assertHas(new Object[] {});
	}

	/**
	 * Test that registering a listener twice results in the listener receiving
	 * the events twice.
	 */
	@ContractTest
	public void testRegisterTwice() {
		gem.register(L).register(L);
		gem.notifyAddTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "add", mockGraph, SPO, "add", mockGraph, SPO });
	}

	/**
	 * Test that registering a listener twice and unregistering it once will
	 * result in the listener receiving each event one time.
	 */
	@ContractTest
	public void testUnregisterOnce() {
		gem.register(L).register(L).unregister(L);
		gem.notifyDeleteTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "delete", mockGraph, SPO });
	}

	/**
	 * Test that adding an array is reported as adding an array.
	 */
	@ContractTest
	public void testAddArray() {
		gem.register(L);
		gem.notifyAddArray(mockGraph, tripleArray);
		L.assertHas(new Object[] { "add[]", mockGraph, tripleArray });
	}

	/**
	 * Test that adding a list is reported as adding a list
	 */
	@ContractTest
	public void testAddList() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyAddList(mockGraph, elems);
		L.assertHas(new Object[] { "addList", mockGraph, elems });
	}

	/**
	 * Test that deleting an array is reported as deleting an array.
	 */
	@ContractTest
	public void testDeleteArray() {
		gem.register(L);
		gem.notifyDeleteArray(mockGraph, tripleArray);
		L.assertHas(new Object[] { "delete[]", mockGraph, tripleArray });
	}

	/**
	 * Test that deleting a list is reported as deleting a list.
	 */
	@ContractTest
	public void testDeleteList() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyDeleteList(mockGraph, elems);
		L.assertHas(new Object[] { "deleteList", mockGraph, elems });
	}

	/**
	 * Test that adding a list as an iterator is reported as an add iterator.
	 */
	@ContractTest
	public void testAddListAsIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyAddIterator(mockGraph, elems);
		L.assertHas(new Object[] { "addIterator", mockGraph, elems });
	}

	/**
	 * Test that adding an iterator is reported as adding an iterator.
	 */
	@ContractTest
	public void testAddIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyAddIterator(mockGraph, elems.iterator());
		L.assertHas(new Object[] { "addIterator", mockGraph, elems });
	}

	/**
	 * Test that deleting an iterator is reported as a deleting an iterator.
	 */
	@ContractTest
	public void testDeleteIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyDeleteIterator(mockGraph, elems.iterator());
		L.assertHas(new Object[] { "deleteIterator", mockGraph, elems });
	}

	/**
	 * Test that deleting a list as an iterator is reported as deleting an
	 * iterator.
	 */
	@ContractTest
	public void testDeleteListAsIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyDeleteIterator(mockGraph, elems);
		L.assertHas(new Object[] { "deleteIterator", mockGraph, elems });
	}

	/**
	 * Test that adding a graph is reported as adding a graph.
	 */
	@ContractTest
	public void testAddGraph() {
		gem.register(L);
		Graph other = Mockito.mock(Graph.class);
		gem.notifyAddGraph(mockGraph, other);
		L.assertHas(new Object[] { "addGraph", mockGraph, other });
	}

	/**
	 * Test that deleting a graph is reported as deleting a graph.
	 */
	@ContractTest
	public void testDeleteGraph() {
		gem.register(L);
		Graph other = Mockito.mock(Graph.class);
		gem.notifyDeleteGraph(mockGraph, other);
		L.assertHas(new Object[] { "deleteGraph", mockGraph, other });
	}

	/**
	 * Test that sending a general event is reported as an event and the value
	 * is saved.
	 */
	@ContractTest
	public void testGeneralEvent() {
		gem.register(L);
		Object value = new int[] {};
		gem.notifyEvent(mockGraph, value);
		L.assertHas(new Object[] { "someEvent", mockGraph, value });
	}

	@ContractTest
	public void testListening() {
		assertFalse("Should not be listening", gem.listening());
		gem.register(L);
		assertTrue("Should be listening", gem.listening());
		gem.unregister(L);
		assertFalse("Should not be listening", gem.listening());
	}

	//
	// Foo series of tests to check modifying the manger in mid notification
	//

	private ComeAndGoListener all[];

	abstract private static class ComeAndGoListener implements GraphListener {

		// Was I registered when start() was called, and have not been
		// unregistered.
		boolean inPlay = false;
		// currently registered or not.
		boolean registered = false;
		boolean notified = false;

		void register(GraphEventManager gem) {
			registered = true;
			gem.register(this);
		}

		void unregister(GraphEventManager gem) {
			registered = false;
			inPlay = false;
			gem.unregister(this);
		}

		void start() {
			if (registered)
				inPlay = true;
		}

		void check() {
			if (inPlay && !notified)
				fail("listener that was in-play was not notified of adding triple.");
		}

		@Override
		final public void notifyAddTriple(Graph g, Triple t) {
			notified = true;
			doSomeDamage();
		}

		abstract void doSomeDamage();

		@Override
		public void notifyAddArray(Graph g, Triple[] triples) {
		}

		@Override
		public void notifyAddGraph(Graph g, Graph added) {
		}

		@Override
		public void notifyAddIterator(Graph g, Iterator<Triple> it) {
		}

		@Override
		public void notifyAddList(Graph g, List<Triple> triples) {
		}

		@Override
		public void notifyDeleteArray(Graph g, Triple[] triples) {
		}

		@Override
		public void notifyDeleteGraph(Graph g, Graph removed) {
		}

		@Override
		public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
		}

		@Override
		public void notifyDeleteList(Graph g, List<Triple> L) {
		}

		@Override
		public void notifyDeleteTriple(Graph g, Triple t) {
		}

		@Override
		public void notifyEvent(Graph source, Object value) {
		}

	}

	/**
	 * ComeAndGoListener implementation that does no damage
	 * 
	 */
	private static final class SimpleListener extends ComeAndGoListener {
		@Override
		void doSomeDamage() {
		}
	}

	/**
	 * Test adding a triple to trigger event.
	 * 
	 * @param registerTo
	 *            Number of listeners to register.
	 * @param allx
	 */
	private void testAddingTriple(int registerTo, ComeAndGoListener... allx) {
		all = allx;
		// register addMe
		for (int i = 0; i < registerTo; i++) {
			all[i].register(gem);
		}

		// start them all
		for (ComeAndGoListener l : all) {
			l.start();
		}
		// send the notification.
		gem.notifyAddTriple(mockGraph, GraphHelper.triple("make a change"));
		// check them
		for (ComeAndGoListener l : all) {
			l.check();
		}
	}

	/**
	 * Test that a listener added during event processing does not receive the
	 * event.
	 */
	@ContractTest
	public void testAddOne() {
		testAddingTriple(2, new ComeAndGoListener() {
			@Override
			void doSomeDamage() {
				all[2].register(gem);
			}
		}, new SimpleListener(), new SimpleListener());
	}

	/**
	 * Test that when a listener that has not yet been handled is removed during
	 * event processing it receive the event.
	 */
	@ContractTest
	public void testDelete2nd() {
		testAddingTriple(3, new ComeAndGoListener() {
			@Override
			void doSomeDamage() {
				all[1].unregister(gem);
			}
		}, new SimpleListener(), new SimpleListener());
	}

	/**
	 * Test that when a listener that has been handled is removed during event
	 * processing it receives the event.
	 */
	@ContractTest
	public void testDelete1st() {
		testAddingTriple(3, new SimpleListener(), new ComeAndGoListener() {
			@Override
			void doSomeDamage() {
				all[0].unregister(gem);
			}
		}, new SimpleListener());
	}

	/**
	 * Test that when a listener that removes itself during event processing it
	 * receives the event.
	 */
	@ContractTest
	public void testDeleteSelf() {
		testAddingTriple(3, new ComeAndGoListener() {
			@Override
			void doSomeDamage() {
				unregister(gem);
			}
		}, new SimpleListener(), new SimpleListener());
	}

	/**
	 * Test that when a listener that removes and adds itself during event
	 * processing it receives the event.
	 */
	@ContractTest
	public void testDeleteAndAddSelf() {
		testAddingTriple(3, new ComeAndGoListener() {
			@Override
			void doSomeDamage() {
				unregister(gem);
				register(gem);
			}
		}, new SimpleListener(), new SimpleListener());
	}

}
