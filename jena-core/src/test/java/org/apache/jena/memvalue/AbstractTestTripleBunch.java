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

package org.apache.jena.memvalue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

public abstract class AbstractTestTripleBunch {
    protected static final Triple tripleSPO = GraphTestLib.triple("s P o");
    protected static final Triple tripleXQY = GraphTestLib.triple("x Q y");

    protected static final TripleBunch emptyBunch = new ArrayBunch();

    protected abstract TripleBunch getBunch();

    @Test
    public void testEmptyBunch() {
        TripleBunch b = getBunch();
        assertEquals(0, b.size());
        assertFalse(b.contains(tripleSPO));
        assertFalse(b.contains(tripleXQY));
        assertFalse(b.iterator().hasNext());
    }

    @Test
    public void testAddElement() {
        TripleBunch b = getBunch();
        b.add(tripleSPO);
        assertEquals(1, b.size());
        assertTrue(b.contains(tripleSPO));
        assertEquals(listOf(tripleSPO), Iter.toList(b.iterator()));
    }

    @Test
    public void testAddElements() {
        TripleBunch b = getBunch();
        b.add(tripleSPO);
        b.add(tripleXQY);
        assertEquals(2, b.size());
        assertTrue(b.contains(tripleSPO));
        assertTrue(b.contains(tripleXQY));
        assertEquals(setOf(tripleSPO, tripleXQY), Iter.toSet(b.iterator()));
    }

    @Test
    public void testRemoveOnlyElement() {
        TripleBunch b = getBunch();
        b.add(tripleSPO);
        b.remove(tripleSPO);
        assertEquals(0, b.size());
        assertFalse(b.contains(tripleSPO));
        assertFalse(b.iterator().hasNext());
    }

    @Test
    public void testRemoveFirstOfTwo() {
        TripleBunch b = getBunch();
        b.add(tripleSPO);
        b.add(tripleXQY);
        b.remove(tripleSPO);
        assertEquals(1, b.size());
        assertFalse(b.contains(tripleSPO));
        assertTrue(b.contains(tripleXQY));
        assertEquals(listOf(tripleXQY), Iter.toList(b.iterator()));
    }

    @Test
    public void testTableGrows() {
        TripleBunch b = getBunch();
        b.add(tripleSPO);
        b.add(tripleXQY);
        b.add(GraphTestLib.triple("a I b"));
        b.add(GraphTestLib.triple("c J d"));
    }

    @Test
    public void testIterator() {
        TripleBunch b = getBunch();
        b.add(GraphTestLib.triple("a P b"));
        b.add(GraphTestLib.triple("c Q d"));
        b.add(GraphTestLib.triple("e R f"));
        assertEquals(GraphTestLib.tripleSet("a P b; c Q d; e R f"), b.iterator().toSet());
    }

    @Test
    public void testIteratorRemoveOneItem() {
        TripleBunch b = getBunch();
        b.add(GraphTestLib.triple("a P b"));
        b.add(GraphTestLib.triple("c Q d"));
        b.add(GraphTestLib.triple("e R f"));
        ExtendedIterator<Triple> it = b.iterator();
        while (it.hasNext()) if (it.next().equals(GraphTestLib.triple("c Q d"))) it.remove();
        assertEquals(GraphTestLib.tripleSet("a P b; e R f"), b.iterator().toSet());
    }

    @Test
    public void testIteratorRemoveAlltems() {
        TripleBunch b = getBunch();
        b.add(GraphTestLib.triple("a P b"));
        b.add(GraphTestLib.triple("c Q d"));
        b.add(GraphTestLib.triple("e R f"));
        ExtendedIterator<Triple> it = b.iterator();
        while (it.hasNext()) it.removeNext();
        assertEquals(GraphTestLib.tripleSet(""), b.iterator().toSet());
    }

    protected List<Triple> listOf(Triple x) {
        List<Triple> result = new ArrayList<>();
        result.add(x);
        return result;
    }

    protected Set<Triple> setOf(Triple x, Triple y) {
        Set<Triple> result = setOf(x);
        result.add(y);
        return result;
    }

    protected Set<Triple> setOf(Triple x) {
        Set<Triple> result = new HashSet<>();
        result.add(x);
        return result;
    }
}
