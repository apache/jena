/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.graph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Triple.*;
import org.apache.jena.test.JenaTestLib;

public class TestTripleField {

    @Test
    public void testFieldsExistAndAreTyped() {
        JenaTestLib.assertInstanceOf(Triple.Field.class, Triple.Field.fieldSubject);
        JenaTestLib.assertInstanceOf(Triple.Field.class, Triple.Field.fieldObject);
        JenaTestLib.assertInstanceOf(Triple.Field.class, Triple.Field.fieldPredicate);
    }

    @Test
    public void testGetSubject() {
        assertEquals(GraphTestLib.node("s"), Field.fieldSubject.getField(GraphTestLib.triple("s p o")));
    }

    @Test
    public void testGetObject() {
        assertEquals(GraphTestLib.node("o"), Field.fieldObject.getField(GraphTestLib.triple("s p o")));
    }

    @Test
    public void testGetPredicate() {
        assertEquals(GraphTestLib.node("p"), Field.fieldPredicate.getField(GraphTestLib.triple("s p o")));
    }

    @Test
    public void testFilterSubject() {
        assertTrue(Field.fieldSubject.filterOn(GraphTestLib.node("a")).test(GraphTestLib.triple("a P b")));
        assertFalse(Field.fieldSubject.filterOn(GraphTestLib.node("x")).test(GraphTestLib.triple("a P b")));
    }

    @Test
    public void testFilterObject() {
        assertTrue(Field.fieldObject.filterOn(GraphTestLib.node("b")).test(GraphTestLib.triple("a P b")));
        assertFalse(Field.fieldObject.filterOn(GraphTestLib.node("c")).test(GraphTestLib.triple("a P b")));
    }

    @Test
    public void testFilterPredicate() {
        assertTrue(Field.fieldPredicate.filterOn(GraphTestLib.node("P")).test(GraphTestLib.triple("a P b")));
        assertFalse(Field.fieldPredicate.filterOn(GraphTestLib.node("Q")).test(GraphTestLib.triple("a P b")));
    }

    @Test
    public void testFilterOnConcreteSubject() {
        assertTrue(Field.fieldSubject.filterOnConcrete(GraphTestLib.node("a")).test(GraphTestLib.triple("a P b")));
        assertFalse(Field.fieldSubject.filterOnConcrete(GraphTestLib.node("x")).test(GraphTestLib.triple("a P b")));
    }

    @Test
    public void testFilterOnConcreteObject() {
        assertTrue(Field.fieldObject.filterOnConcrete(GraphTestLib.node("b")).test(GraphTestLib.triple("a P b")));
        assertFalse(Field.fieldObject.filterOnConcrete(GraphTestLib.node("c")).test(GraphTestLib.triple("a P b")));
    }

    @Test
    public void testFilterOnConcretePredicate() {
        assertTrue(Field.fieldPredicate.filterOnConcrete(GraphTestLib.node("P")).test(GraphTestLib.triple("a P b")));
        assertFalse(Field.fieldPredicate.filterOnConcrete(GraphTestLib.node("Q")).test(GraphTestLib.triple("a P b")));
    }

    @Test
    public void testFilterByTriple() {
        assertTrue(Field.fieldSubject.filterOn(GraphTestLib.triple("s P o")).test(GraphTestLib.triple("s Q p")));
        assertFalse(Field.fieldSubject.filterOn(GraphTestLib.triple("s P o")).test(GraphTestLib.triple("x Q p")));
    }
}
