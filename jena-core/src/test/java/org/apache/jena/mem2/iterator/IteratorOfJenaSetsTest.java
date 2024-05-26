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
package org.apache.jena.mem2.iterator;

import org.apache.jena.mem2.collection.FastHashSet;
import org.apache.jena.mem2.collection.JenaSet;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class IteratorOfJenaSetsTest {

    private static final JenaSet<String> EMPTY_LIST = new StringSet();
    private IteratorOfJenaSets<String> sut;
    private Iterator<JenaSet<String>> parentIterator;

    @Before
    public void setUp() {
        parentIterator = Arrays.asList(
                new StringSet("1.1", "1.2"),
                new StringSet("2.1", "2.2"),
                (JenaSet<String>) new StringSet("3.1", "3.2")
        ).iterator();
    }

    @Test
    public void testHasNext() {
        sut = new IteratorOfJenaSets<>(parentIterator);
        assertTrue(sut.hasNext());
    }

    @Test
    public void testNext() {
        sut = new IteratorOfJenaSets<>(parentIterator);
        assertTrue(sut.hasNext());
        var findings = new ArrayList<String>();
        for (int i = 0; i < 6; i++) {
            findings.add(sut.next());
        }
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder("1.1", "1.2", "2.1", "2.2", "3.1", "3.2"));

    }

    @Test
    public void testNextWithNoElementsInSets() {
        parentIterator = Arrays.asList(
                EMPTY_LIST,
                EMPTY_LIST,
                EMPTY_LIST
        ).iterator();
        sut = new IteratorOfJenaSets<>(parentIterator);
        assertThrows(NoSuchElementException.class, () -> sut.next());
    }

    @Test
    public void testNextWithNoSetInParentIterator() {
        parentIterator = Collections.emptyIterator();
        sut = new IteratorOfJenaSets<>(parentIterator);
        assertThrows(NoSuchElementException.class, () -> sut.next());
    }

    @Test
    public void testForEachRemaining() {
        sut = new IteratorOfJenaSets<>(parentIterator);
        var findings = new ArrayList<String>();
        sut.forEachRemaining(element -> {
            findings.add(element);
        });
        assertEquals(6, findings.size());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder("1.1", "1.2", "2.1", "2.2", "3.1", "3.2"));
    }

    private static class StringSet extends FastHashSet<String> {

        public StringSet(String... strings) {
            super(strings.length);
            for (String s : strings) {
                tryAdd(s);
            }
        }

        @Override
        protected String[] newKeysArray(int size) {
            return new String[size];
        }
    }
}