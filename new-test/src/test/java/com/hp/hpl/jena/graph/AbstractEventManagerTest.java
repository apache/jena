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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.testing_framework.GraphEventManagerProducerInterface;
import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;

/**
 * An abstract test that tests EventManager implementations to ensure they
 * comply with the EventManager contract.
 * 
 * Subclasses of this class must implement the getEventManagerProducer() method
 * to create a new instance of the EventManager for testing.
 */

public abstract class AbstractEventManagerTest {

	protected abstract GraphEventManagerProducerInterface getEventManagerProducer();

	protected static final Triple[] tripleArray = tripleArray("S P O; A R B; X Q Y");

	// the grah that is used as the source of the events.
	private Graph mockGraph;
	// The event manager we are working with.
	private GraphEventManager gem;

	@Before
	public void startup() {
		mockGraph = Mockito.mock(Graph.class);
		gem = getEventManagerProducer().newEventManager();
		L = new RecordingListener();
	}

	/**
	 * Test that when a listener is registered the same EventManager is
	 * returned.
	 */
	@Test
	public void testEventRegister() {
		assertSame(gem, gem.register(new RecordingListener()));
	}

	/**
	 * Test that we can safely unregister a listener that isn't registered.
	 */
	@Test
	public void testEventUnregister() {
		gem.unregister(L);
	}

	/**
	 * Handy triple for test purposes.
	 */
	protected Triple SPO = NodeCreateUtils.createTriple("S P O");

	/**
	 * The RecordingListener that reports all the events sent from the
	 * EventManager.
	 */
	protected RecordingListener L;

	/**
	 * Test that adding a triple is reported.
	 */
	@Test
	public void testAddTriple() {
		gem.register(L);
		gem.notifyAddTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "add", mockGraph, SPO });
	}

	/**
	 * Test that deleting a triple is reported.
	 */
	@Test
	public void testDeleteTriple() {
		gem.register(L);
		gem.notifyDeleteTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "delete", mockGraph, SPO });
	}

	/**
	 * Test that when 2 listeners are added both receive events.
	 */
	@Test
	public void testTwoListeners() {
		RecordingListener L1 = new RecordingListener();
		RecordingListener L2 = new RecordingListener();
		gem.register(L1).register(L2);
		gem.notifyAddTriple(mockGraph, SPO);
		L2.assertHas(new Object[] { "add", mockGraph, SPO });
		L1.assertHas(new Object[] { "add", mockGraph, SPO });
	}

	/**
	 * Test that unregistering a listener after registering it results in it not
	 * receiving messages.
	 */
	@Test
	public void testUnregisterWorks() {
		gem.register(L).unregister(L);
		gem.notifyAddTriple(mockGraph, SPO);
		L.assertHas(new Object[] {});
	}

	/**
	 * Test that registering a listener twice results in the listener receiving
	 * the events twice.
	 */
	@Test
	public void testRegisterTwice() {
		gem.register(L).register(L);
		gem.notifyAddTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "add", mockGraph, SPO, "add", mockGraph, SPO });
	}

	/**
	 * Test that registering a listener twice and unregistering it once will
	 * result in the listener receiving each event one time.
	 */
	@Test
	public void testUnregisterOnce() {
		gem.register(L).register(L).unregister(L);
		gem.notifyDeleteTriple(mockGraph, SPO);
		L.assertHas(new Object[] { "delete", mockGraph, SPO });
	}

	/**
	 * Test that adding an array is reported as adding an array.
	 */
	@Test
	public void testAddArray() {
		gem.register(L);
		gem.notifyAddArray(mockGraph, tripleArray);
		L.assertHas(new Object[] { "add[]", mockGraph, tripleArray });
	}

	/**
	 * Test that adding a list is reported as adding a list
	 */
	@Test
	public void testAddList() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyAddList(mockGraph, elems);
		L.assertHas(new Object[] { "addList", mockGraph, elems });
	}

	/**
	 * Test that deleting an array is reported as deleting an array.
	 */
	@Test
	public void testDeleteArray() {
		gem.register(L);
		gem.notifyDeleteArray(mockGraph, tripleArray);
		L.assertHas(new Object[] { "delete[]", mockGraph, tripleArray });
	}

	/**
	 * Test that deleting a list is reported as deleting a list.
	 */
	@Test
	public void testDeleteList() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyDeleteList(mockGraph, elems);
		L.assertHas(new Object[] { "deleteList", mockGraph, elems });
	}

	/**
	 * Test that adding a list as an iterator is reported as an add iterator.
	 */
	@Test
	public void testAddListAsIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyAddIterator(mockGraph, elems);
		L.assertHas(new Object[] { "addIterator", mockGraph, elems });
	}

	/**
	 * Test that adding an iterator is reported as adding an iterator.
	 */
	@Test
	public void testAddIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyAddIterator(mockGraph, elems.iterator());
		L.assertHas(new Object[] { "addIterator", mockGraph, elems });
	}

	/**
	 * Test that deleting an iterator is reported as a deleting an iterator.
	 */
	@Test
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
	@Test
	public void testDeleteListAsIterator() {
		gem.register(L);
		List<Triple> elems = Arrays.asList(tripleArray);
		gem.notifyDeleteIterator(mockGraph, elems);
		L.assertHas(new Object[] { "deleteIterator", mockGraph, elems });
	}

	/**
	 * Test that adding a graph is reported as adding a graph.
	 */
	@Test
	public void testAddGraph() {
		gem.register(L);
		Graph other = Mockito.mock(Graph.class);
		gem.notifyAddGraph(mockGraph, other);
		L.assertHas(new Object[] { "addGraph", mockGraph, other });
	}

	/**
	 * Test that deleting a graph is reported as deleting a graph.
	 */
	@Test
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
	@Test
	public void testGeneralEvent() {
		gem.register(L);
		Object value = new int[] {};
		gem.notifyEvent(mockGraph, value);
		L.assertHas(new Object[] { "someEvent", mockGraph, value });
	}

}
