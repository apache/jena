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

package org.apache.jena.sparql.service.enhancer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.service.enhancer.claimingcache.PredicateRangeSet;
import org.apache.jena.sparql.service.enhancer.claimingcache.PredicateTrue;
import org.apache.jena.sparql.service.enhancer.util.LinkedList;

public class TestLinkedList {
    @Test
    public void testEmptyList() {
        List<Integer> list = new LinkedList<>(List.of());
        ListIterator<Integer> it = list.listIterator(0);
        assertFalse(it.hasNext());
        assertFalse(it.hasPrevious());
    }

    @Test
    public void testSingletonList() {
        List<Integer> list = new LinkedList<>(List.of(0));
        ListIterator<Integer> it = list.listIterator(0);
        int v;

        // Test alternation of next/prev.
        for (int i = 0; i < 1; ++i) {
            assertTrue(it.hasNext());
            assertFalse(it.hasPrevious());
            v = it.next();
            assertEquals(0, v);

            assertFalse(it.hasNext());
            assertTrue(it.hasPrevious());
            v = it.previous();
            assertEquals(0, v);
        }
    }

    @Test
    public void reverseTraversal() {
        List<Integer> list = new LinkedList<>(List.of(0, 1, 2));
        ListIterator<Integer> it = list.listIterator(3);
        for (int expected = 2; expected >= 0; --expected) {
            int actual = it.previous();
            assertEquals(expected, actual);
        }
    }

    @Test
    public void positionNearEnd() {
        List<Integer> list = new LinkedList<>(List.of(0, 1, 2, 3, 4));
        ListIterator<Integer> it = list.listIterator(3);

        List<Integer> actualTail = new ArrayList<>();
        it.forEachRemaining(actualTail::add);

        List<Integer> expectedTail = List.of(3, 4);
        assertEquals(expectedTail, actualTail);
    }

    @Test
    public void positionNearStart() {
        List<Integer> list = new LinkedList<>(List.of(0, 1, 2, 3, 4));
        ListIterator<Integer> it = list.listIterator(2);

        List<Integer> actualHead = new ArrayList<>();
        while (it.hasPrevious()) {
            actualHead.add(it.previous());
        }

        List<Integer> expectedHead = List.of(1, 0);
        assertEquals(expectedHead, actualHead);
    }

    @Test
    public void testInsert() {
        List<Integer> actual = new LinkedList<>(List.of(0, 1, 2));
        ListIterator<Integer> it = actual.listIterator(1);
        it.add(3);
        it.add(4);

        List<Integer> expected = List.of(0, 4, 3, 1, 2);
        assertEquals(expected, actual);
    }

    @Test
    public void testRanges() {
        RangeSet<Long> rs = TreeRangeSet.create();
        rs.add(Range.closedOpen(0l, 11l));

        LinkedList<Predicate<Long>> actual = new LinkedList<>();
        actual.append(PredicateTrue.get());
        actual.append(PredicateTrue.get());
        actual.append(new PredicateRangeSet<>(rs));

        List<Predicate<Long>> expected = List.of(PredicateTrue.get(), new PredicateRangeSet<>(rs));

        Iterator<?> it = actual.iterator();
        it.hasNext();
        it.next();

        it.next();
        it.remove();

        assertEquals(expected, actual);
    }
}
