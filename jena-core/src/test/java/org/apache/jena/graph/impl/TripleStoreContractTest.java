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

package org.apache.jena.graph.impl;

import org.junit.After;
import org.junit.Before;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;

import static org.junit.Assert.*;

import static org.apache.jena.testing_framework.GraphHelper.*;
import org.xenei.junit.contract.IProducer;

/**
 * AbstractTestTripleStore - post-hoc tests for TripleStores.
 */

@Contract(TripleStore.class)
public class TripleStoreContractTest<T extends TripleStore> {

	protected TripleStore store;
	
	private IProducer<T> producer;

	public TripleStoreContractTest() {
	}

	/**
	 * Subclasses must over-ride to return a new empty TripleStore.
	 */
	@Contract.Inject
	public final void setTripleStoreContractTestProducer(IProducer<T> producer) {
		this.producer = producer;
	}

	@Before
	public final void beforeAbstractTripleStoreTest() {
		store = producer.newInstance();
	}

	@After
	public final void afterAbstractTripleStoreTest() {
		producer.cleanUp();
	}

	@ContractTest
	public void testEmpty() {
		testEmpty(store);
	}

	@ContractTest
	public void testAddOne() {
		store.add(triple("x P y"));
		assertEquals(false, store.isEmpty());
		assertEquals(1, store.size());
		assertEquals(true, store.contains(triple("x P y")));
		assertEquals(nodeSet("x"), iteratorToSet(store.listSubjects()));
		assertEquals(nodeSet("y"), iteratorToSet(store.listObjects()));
		assertEquals(tripleSet("x P y"),
				iteratorToSet(store.find(triple("?? ?? ??"))));
	}

	@ContractTest
	public void testListSubjects() {
		someStatements(store);
		assertEquals(nodeSet("a x _z r q"), iteratorToSet(store.listSubjects()));
	}

	@ContractTest
	public void testListObjects() {
		someStatements(store);
		assertEquals(nodeSet("b y i _j _t 17"),
				iteratorToSet(store.listObjects()));
	}

	@ContractTest
	public void testContains() {
		someStatements(store);
		assertEquals(true, store.contains(triple("a P b")));
		assertEquals(true, store.contains(triple("x P y")));
		assertEquals(true, store.contains(triple("a P i")));
		assertEquals(true, store.contains(triple("_z Q _j")));
		assertEquals(true, store.contains(triple("x R y")));
		assertEquals(true, store.contains(triple("r S _t")));
		assertEquals(true, store.contains(triple("q R 17")));
		/* */
		assertEquals(false, store.contains(triple("a P x")));
		assertEquals(false, store.contains(triple("a P _j")));
		assertEquals(false, store.contains(triple("b Z r")));
		assertEquals(false, store.contains(triple("_a P x")));
	}

	@ContractTest
	public void testFind() {
		someStatements(store);
		assertEquals(tripleSet(""),
				iteratorToSet(store.find(triple("no such thing"))));
		assertEquals(tripleSet("a P b; a P i"),
				iteratorToSet(store.find(triple("a P ??"))));
		assertEquals(tripleSet("a P b; x P y; a P i"),
				iteratorToSet(store.find(triple("?? P ??"))));
		assertEquals(tripleSet("x P y; x R y"),
				iteratorToSet(store.find(triple("x ?? y"))));
		assertEquals(tripleSet("_z Q _j"),
				iteratorToSet(store.find(triple("?? ?? _j"))));
		assertEquals(tripleSet("q R 17"),
				iteratorToSet(store.find(triple("?? ?? 17"))));
	}

	@ContractTest
	public void testRemove() {
		store.add(triple("nothing before ace"));
		store.add(triple("ace before king"));
		store.add(triple("king before queen"));
		store.delete(triple("ace before king"));
		assertEquals(tripleSet("king before queen; nothing before ace"),
				iteratorToSet(store.find(triple("?? ?? ??"))));
		store.delete(triple("king before queen"));
		assertEquals(tripleSet("nothing before ace"),
				iteratorToSet(store.find(triple("?? ?? ??"))));
	}

	protected void someStatements(TripleStore ts) {
		ts.add(triple("a P b"));
		ts.add(triple("x P y"));
		ts.add(triple("a P i"));
		ts.add(triple("_z Q _j"));
		ts.add(triple("x R y"));
		ts.add(triple("r S _t"));
		ts.add(triple("q R 17"));
	}

	protected void testEmpty(TripleStore ts) {
		assertEquals(true, ts.isEmpty());
		assertEquals(0, ts.size());
		assertEquals(false, ts.find(triple("?? ?? ??")).hasNext());
		assertEquals(false, ts.listObjects().hasNext());
		assertEquals(false, ts.listSubjects().hasNext());
		assertFalse(ts.contains(triple("x P y")));
	}
}
