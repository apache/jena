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
package org.apache.jena.mem2.collection;

import org.apache.jena.graph.Node;
import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.junit.Assert.assertEquals;

public class FastHashMapTest2 {

    FastHashMap<Node, Object> sut = new FastNodeHashMap();


    @Test
    public void testConstructWithInitialSizeAndAdd() {
        sut = new FastNodeHashMap(3);
        sut.put(node("s"), null);
        sut.put(node("s1"), null);
        sut.put(node("s2"), null);
        sut.put(node("s3"), null);
        sut.put(node("s4"), null);
        assertEquals(5, sut.size());
    }

    @Test
    public void testGetValueAt() {
        sut.put(node("s"), 0);
        sut.put(node("s1"), 1);
        sut.put(node("s2"), 2);

        assertEquals(0, sut.getValueAt(0));
        assertEquals(1, sut.getValueAt(1));
        assertEquals(2, sut.getValueAt(2));
    }

    private static class FastNodeHashMap extends FastHashMap<Node, Object> {

        public FastNodeHashMap() {
            super();
        }

        public FastNodeHashMap(int initialSize) {
            super(initialSize);
        }

        @Override
        protected Object[] newValuesArray(int size) {
            return new Object[size];
        }

        @Override
        protected Node[] newKeysArray(int size) {
            return new Node[size];
        }
    }
}