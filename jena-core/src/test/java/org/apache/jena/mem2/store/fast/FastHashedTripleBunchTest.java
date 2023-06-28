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

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.AbstractJenaSetTripleTest;
import org.apache.jena.mem2.collection.JenaSet;
import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.assertEquals;

public class FastHashedTripleBunchTest extends AbstractJenaSetTripleTest {

    @Override
    protected JenaSet<Triple> createTripleSet() {
        return new FastHashedTripleBunch();
    }

    @Test
    public void testConstructorWithArrayBunchEmpty() {
        final var arrayBunch = new FastArrayBunch() {

            @Override
            public boolean areEqual(Triple a, Triple b) {
                return a.equals(b);
            }
        };
        final var sut = new FastHashedTripleBunch(arrayBunch);
        assertEquals(0, sut.size());
    }

    @Test
    public void testConstructorWithArrayBunch() {
        final var arrayBunch = new FastArrayBunch() {

            @Override
            public boolean areEqual(Triple a, Triple b) {
                return a.equals(b);
            }
        };
        arrayBunch.tryAdd(triple("s P o"));
        arrayBunch.tryAdd(triple("s P o1"));
        arrayBunch.tryAdd(triple("s P o2"));
        final var sut = new FastHashedTripleBunch(arrayBunch);
        assertEquals(3, sut.size());
    }
}