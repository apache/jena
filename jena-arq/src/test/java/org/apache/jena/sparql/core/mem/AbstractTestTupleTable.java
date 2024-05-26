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
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertTrue ;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;

public abstract class AbstractTestTupleTable<TupleType, TupleTableType extends TupleTable<TupleType>> {

	protected abstract TupleType testTuple();

	protected abstract TupleTableType table();

	protected abstract Stream<TupleType> tuples();

	protected abstract Stream<Set<TupleSlot>> queryPatterns();

	protected static final Set<TupleSlot> allWildcardQuery = Set.of();

	protected long transactionalCount() {
	    table().begin(READ);
	    long x = rawCount() ;
        table().end() ;
        return x ;
	}

	protected long rawCount() {
	    return tuples().count() ;
	}

	@Test
	public void addAndRemoveSomeTuples() {
        assertEquals(0, transactionalCount()) ;

		// simple add-and-delete
	    table().begin(WRITE);
		table().add(testTuple());

		assertEquals(1, rawCount()) ;

		Set<TupleType> contents = tuples().collect(toSet());
		assertEquals(Set.of(testTuple()), contents);
		table().delete(testTuple());

        assertEquals(0, rawCount()) ;
        contents = tuples().collect(toSet());
		assertTrue(contents.isEmpty());
		table().end();

        assertEquals(0, transactionalCount()) ;

		// add, abort, then check to see that nothing was persisted
		table().begin(WRITE);
		table().add(testTuple());

		assertEquals(1, rawCount()) ;
		contents = tuples().collect(toSet());
		assertEquals(Set.of(testTuple()), contents);
		table().abort();

        assertEquals(0, transactionalCount()) ;

		// add, commit, and check to see that persistence occurred
		table().begin(WRITE);
		table().add(testTuple());
        assertEquals(1, rawCount()) ;
		contents = tuples().collect(toSet());
		assertEquals(Set.of(testTuple()), contents);
		table().commit();

		assertEquals(1, transactionalCount()) ;

		// remove the test tuple and check to see that it is gone
		table().begin(WRITE);
		assertEquals(1, rawCount()) ;
		table().clear();
		assertEquals(0, rawCount()) ;
		contents = tuples().collect(toSet());
		assertTrue(contents.isEmpty());
		table().commit();

		assertEquals(0, transactionalCount()) ;
	}
}
