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

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.hp.hpl.jena.graph.Triple.Field;

public class TestTripleField {

	@Test
	public void testFieldsExistAndAreTyped() {
		assertInstanceOf(Triple.Field.class, Triple.Field.fieldSubject);
		assertInstanceOf(Triple.Field.class, Triple.Field.fieldObject);
		assertInstanceOf(Triple.Field.class, Triple.Field.fieldPredicate);
	}

	@Test
	public void testGetSubject() {
		assertEquals(node("s"), Field.fieldSubject.getField(triple("s p o")));
	}

	@Test
	public void testGetObject() {
		assertEquals(node("o"), Field.fieldObject.getField(triple("s p o")));
	}

	@Test
	public void testGetPredicate() {
		assertEquals(node("p"), Field.fieldPredicate.getField(triple("s p o")));
	}

	@Test
	public void testFilterSubject() {
		assertTrue(Field.fieldSubject.filterOn(node("a")).accept(
				triple("a P b")));
		assertFalse(Field.fieldSubject.filterOn(node("x")).accept(
				triple("a P b")));
	}

	@Test
	public void testFilterObject() {
		assertTrue(Field.fieldObject.filterOn(node("b"))
				.accept(triple("a P b")));
		assertFalse(Field.fieldObject.filterOn(node("c")).accept(
				triple("a P b")));
	}

	@Test
	public void testFilterPredicate() {
		assertTrue(Field.fieldPredicate.filterOn(node("P")).accept(
				triple("a P b")));
		assertFalse(Field.fieldPredicate.filterOn(node("Q")).accept(
				triple("a P b")));
	}

	@Test
	public void testFilterByTriple() {
		assertTrue(Field.fieldSubject.filterOn(triple("s P o")).accept(
				triple("s Q p")));
		assertFalse(Field.fieldSubject.filterOn(triple("s P o")).accept(
				triple("x Q p")));
	}

	@Test
	public void testWildcardFilterIsAny() {
		assertTrue(Field.fieldSubject.filterOn(triple("?x R s")).isAny());
		assertTrue(Field.fieldObject.filterOn(triple("x R ?s")).isAny());
		assertTrue(Field.fieldPredicate.filterOn(triple("x ?R s")).isAny());
	}
}
