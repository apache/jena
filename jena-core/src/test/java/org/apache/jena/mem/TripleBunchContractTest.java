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
package org.apache.jena.mem;

import static org.junit.Assert.*;
import static org.apache.jena.testing_framework.GraphHelper.*;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;

import org.apache.jena.graph.Triple;
import org.xenei.junit.contract.IProducer;
import org.apache.jena.testing_framework.NodeCreateUtils;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Test triple bunch implementations - NOT YET FINISHED
 */
@Contract(TripleBunch.class)
public class TripleBunchContractTest {
	private IProducer<? extends TripleBunch> producer;

	@Contract.Inject
	public final void setTripleBunchProducer(
			IProducer<? extends TripleBunch> producer) {
		this.producer = producer;
	}

	protected final IProducer<? extends TripleBunch> getTripleBunchProducer() {
		return producer;
	}

	public static final TripleBunch EMPTY_BUNCH = new ArrayBunch();

	protected static final Triple tripleSPO = triple("s P o");
	protected static final Triple tripleXQY = triple("x Q y");

	private TripleBunch testingBunch;

	@Before
	public final void beforeAbstractTripleBunchTest() {
		testingBunch = getTripleBunchProducer().newInstance();
	}

	@After
	public final void afterAbstractTripleBunchTest() {
		getTripleBunchProducer().cleanUp();
	}

	@ContractTest
	public void testEmptyBunch() {
		assertEquals(0, testingBunch.size());
		assertFalse(testingBunch.contains(tripleSPO));
		assertFalse(testingBunch.contains(tripleXQY));
		assertFalse(testingBunch.iterator().hasNext());
	}

	@ContractTest
	public void testAddElement() {
		testingBunch.add(tripleSPO);
		assertEquals(1, testingBunch.size());
		assertTrue(testingBunch.contains(tripleSPO));
		assertEquals(listOf(tripleSPO), iteratorToList(testingBunch.iterator()));
	}

	@ContractTest
	public void testAddElements() {
		testingBunch.add(tripleSPO);
		testingBunch.add(tripleXQY);
		assertEquals(2, testingBunch.size());
		assertTrue(testingBunch.contains(tripleSPO));
		assertTrue(testingBunch.contains(tripleXQY));
		assertEquals(setOf(tripleSPO, tripleXQY),
				iteratorToSet(testingBunch.iterator()));
	}

	@ContractTest
	public void testRemoveOnlyElement() {
		testingBunch.add(tripleSPO);
		testingBunch.remove(tripleSPO);
		assertEquals(0, testingBunch.size());
		assertFalse(testingBunch.contains(tripleSPO));
		assertFalse(testingBunch.iterator().hasNext());
	}

	@ContractTest
	public void testRemoveFirstOfTwo() {
		testingBunch.add(tripleSPO);
		testingBunch.add(tripleXQY);
		testingBunch.remove(tripleSPO);
		assertEquals(1, testingBunch.size());
		assertFalse(testingBunch.contains(tripleSPO));
		assertTrue(testingBunch.contains(tripleXQY));
		assertEquals(listOf(tripleXQY), iteratorToList(testingBunch.iterator()));
	}

	@ContractTest
	public void testTableGrows() {
		testingBunch.add(tripleSPO);
		testingBunch.add(tripleXQY);
		testingBunch.add(triple("a I b"));
		testingBunch.add(triple("c J d"));
	}

	@ContractTest
	public void testIterator() {
		testingBunch.add(triple("a P b"));
		testingBunch.add(triple("c Q d"));
		testingBunch.add(triple("e R f"));
		assertEquals(tripleSet("a P b; c Q d; e R f"), testingBunch.iterator()
				.toSet());
	}

	@ContractTest
	public void testIteratorRemoveOneItem() {
		testingBunch.add(triple("a P b"));
		testingBunch.add(triple("c Q d"));
		testingBunch.add(triple("e R f"));
		ExtendedIterator<Triple> it = testingBunch.iterator();
		while (it.hasNext())
			if (it.next().equals(triple("c Q d")))
				it.remove();
		assertEquals(tripleSet("a P b; e R f"), testingBunch.iterator().toSet());
	}

	@ContractTest
	public void testIteratorRemoveAlltems() {
		testingBunch.add(triple("a P b"));
		testingBunch.add(triple("c Q d"));
		testingBunch.add(triple("e R f"));
		ExtendedIterator<Triple> it = testingBunch.iterator();
		while (it.hasNext())
			it.removeNext();
		assertEquals(tripleSet(""), testingBunch.iterator().toSet());
	}

	protected List<Triple> listOf(Triple x) {
		List<Triple> result = new ArrayList<Triple>();
		result.add(x);
		return result;
	}

	protected Set<Triple> setOf(Triple x, Triple y) {
		Set<Triple> result = setOf(x);
		result.add(y);
		return result;
	}

	protected Set<Triple> setOf(Triple x) {
		Set<Triple> result = new HashSet<Triple>();
		result.add(x);
		return result;
	}

	public void testAddThenNextThrowsCME() {
		testingBunch.add(NodeCreateUtils.createTriple("a P b"));
		testingBunch.add(NodeCreateUtils.createTriple("c Q d"));
		ExtendedIterator<Triple> it = testingBunch.iterator();
		it.next();
		testingBunch.add(NodeCreateUtils.createTriple("change its state"));
		try {
			it.next();
			fail("should have thrown ConcurrentModificationException");
		} catch (ConcurrentModificationException e) {
			// expected
		}
	}

	public void testDeleteThenNextThrowsCME() {
		testingBunch.add(NodeCreateUtils.createTriple("a P b"));
		testingBunch.add(NodeCreateUtils.createTriple("c Q d"));
		ExtendedIterator<Triple> it = testingBunch.iterator();
		it.next();
		testingBunch.remove(NodeCreateUtils.createTriple("a P b"));
		try {
			it.next();
			fail("should have thrown ConcurrentModificationException");
		} catch (ConcurrentModificationException e) {
			// expected
		}
	}
}
