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
package org.apache.jena.mem2.store.fast;

import org.apache.jena.mem2.store.AbstractTripleStoreTest;
import org.apache.jena.mem2.store.TripleStore;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FastTripleStoreTest extends AbstractTripleStoreTest {

    @Override
    protected TripleStore createTripleStore() {
        return new FastTripleStore();
    }

    @Test
    public void testAddMoreTriplesThanFitInArrayBunchSameSubject() {
        for (int i = 0; i < FastTripleStore.MAX_ARRAY_BUNCH_SIZE_SUBJECT + 1; i++) {
            sut.add(triple("s P" + i + " o" + i));
        }
        assertEquals(FastTripleStore.MAX_ARRAY_BUNCH_SIZE_SUBJECT + 1, sut.countTriples());
    }

    @Test
    public void testAddMoreTriplesThanFitInArrayBunchSamePredicate() {
        for (int i = 0; i < FastTripleStore.MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT + 1; i++) {
            sut.add(triple("s" + i + " P o" + i));
        }
        assertEquals(FastTripleStore.MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT + 1, sut.countTriples());
    }

    @Test
    public void testAddMoreTriplesThanFitInArrayBunchSameObject() {
        for (int i = 0; i < FastTripleStore.MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT + 1; i++) {
            sut.add(triple("s" + i + " P" + i + " o"));
        }
        assertEquals(FastTripleStore.MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT + 1, sut.countTriples());
    }

    @Test
    public void testFindStreamAndContains_POOptimizationWithFewerPredicateMatches() {
        for (int i = 0; i < FastTripleStore.THRESHOLD_FOR_SECONDARY_LOOKUP + 1; i++) {
            sut.add(triple("s" + i + " P" + i + " o"));
        }
        assertEquals(FastTripleStore.THRESHOLD_FOR_SECONDARY_LOOKUP + 1, sut.countTriples());

        assertEquals(1, sut.find(triple("?? P0 o")).toList().size());
        assertEquals(1, sut.stream(triple("?? P0 o")).count());
        assertTrue(sut.contains(triple("?? P0 o")));

        assertEquals(0, sut.find(triple("?? PX o")).toList().size());
        assertEquals(0, sut.stream(triple("?? PX o")).count());
        assertFalse(sut.contains(triple("?? PX o")));

        assertEquals(0, sut.find(triple("?? P0 oX")).toList().size());
        assertEquals(0, sut.stream(triple("?? P0 oX")).count());
        assertFalse(sut.contains(triple("?? P0 oX")));
    }

    @Test
    public void testFindStreamAndContains_POOptimizationWithMorePredicateMatches() {
        for (int i = 0; i < FastTripleStore.THRESHOLD_FOR_SECONDARY_LOOKUP + 1; i++) {
            sut.add(triple("s" + i + " P o"));
        }
        sut.add(triple("sX P o0"));
        assertEquals(FastTripleStore.THRESHOLD_FOR_SECONDARY_LOOKUP + 2, sut.countTriples());

        assertEquals(FastTripleStore.THRESHOLD_FOR_SECONDARY_LOOKUP + 1, sut.find(triple("?? P o")).toList().size());
        assertEquals(FastTripleStore.THRESHOLD_FOR_SECONDARY_LOOKUP + 1, sut.stream(triple("?? P o")).count());
        assertTrue(sut.contains(triple("?? P o")));

        assertEquals(0, sut.find(triple("?? PX o")).toList().size());
        assertEquals(0, sut.stream(triple("?? PX o")).count());
        assertFalse(sut.contains(triple("?? PX o")));

        assertEquals(0, sut.find(triple("?? P0 oX")).toList().size());
        assertEquals(0, sut.stream(triple("?? P0 oX")).count());
        assertFalse(sut.contains(triple("?? P0 oX")));
    }
}