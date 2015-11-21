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

package org.apache.jena.sparql.core.mem;

import static java.util.stream.Collectors.toSet;
import static org.apache.jena.ext.com.google.common.collect.ImmutableSet.of;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTestTupleTable<TupleType, TupleTableType extends TupleTable<TupleType>> extends Assert {

	protected abstract TupleType testTuple();

	protected abstract TupleTableType table();

	protected abstract Stream<TupleType> tuples();

	protected abstract Stream<Set<TupleSlot>> queryPatterns();

	protected static final Set<TupleSlot> allWildcardQuery = of();

	@Test
	public void addAndRemoveSomeTuples() {

		// simple add-and-delete
		table().begin(WRITE);
		assertTrue(table().isInTransaction());
		table().add(testTuple());
		Set<TupleType> contents = tuples().collect(toSet());
		assertEquals(ImmutableSet.of(testTuple()), contents);
		table().delete(testTuple());
		contents = tuples().collect(toSet());
		assertTrue(contents.isEmpty());
		table().end();
		assertFalse(table().isInTransaction());

		// add, abort, then check to see that nothing was persisted
		table().begin(WRITE);
		assertTrue(table().isInTransaction());
		table().add(testTuple());
		contents = tuples().collect(toSet());
		assertEquals(ImmutableSet.of(testTuple()), contents);
		table().abort();
		assertFalse(table().isInTransaction());
		table().begin(READ);
		assertTrue(table().isInTransaction());
		try {
			contents = tuples().collect(toSet());
			assertTrue(contents.isEmpty());
		} finally {
			table().end();
			assertFalse(table().isInTransaction());
		}

		// add, commit, and check to see that persistence occurred
		table().begin(WRITE);
		assertTrue(table().isInTransaction());
		table().add(testTuple());
		contents = tuples().collect(toSet());
		assertEquals(ImmutableSet.of(testTuple()), contents);
		table().commit();
		assertFalse(table().isInTransaction());
		table().begin(READ);
		assertTrue(table().isInTransaction());
		try {
			contents = tuples().collect(toSet());
			assertEquals(ImmutableSet.of(testTuple()), contents);
		} finally {
			table().end();
			assertFalse(table().isInTransaction());
		}
		// remove the test tuple and check to see that it is gone
		table().begin(WRITE);
		assertTrue(table().isInTransaction());
		table().clear();
		contents = tuples().collect(toSet());
		assertTrue(contents.isEmpty());
		table().commit();
		table().begin(READ);
		assertTrue(table().isInTransaction());
		try {
			contents = tuples().collect(toSet());
			assertTrue(contents.isEmpty());
		} finally {
			table().end();
			assertFalse(table().isInTransaction());
		}
	}
}
