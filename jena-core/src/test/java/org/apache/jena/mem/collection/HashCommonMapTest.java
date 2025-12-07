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
package org.apache.jena.mem.collection;

import org.apache.jena.graph.Node;
import org.junit.Test;

import java.util.function.UnaryOperator;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.junit.Assert.assertEquals;

public class HashCommonMapTest extends AbstractJenaMapNodeTest {

    @Override
    protected JenaMap<Node, Object> createNodeMap() {
        return new HashCommonNodeObjectMap(10);
    }

    @Test
    public void testCopyConstructor() {
        var original = new HashCommonNodeObjectMap(10);
        original.put(node("s"), 0);
        original.put(node("s1"), 1);
        original.put(node("s2"), 2);
        assertEquals(3, original.size());

        var copy = new HashCommonNodeObjectMap(original);
        assertEquals(3, copy.size());
        assertEquals(0, (int) copy.get(node("s")));
        assertEquals(1, (int) copy.get(node("s1")));
        assertEquals(2, (int) copy.get(node("s2")));
    }

    @Test
    public void testCopyConstructorWithValueMapping() {
        var original = new HashCommonNodeObjectMap(10);
        original.put(node("s"), 0);
        original.put(node("s1"), 1);
        original.put(node("s2"), 2);
        assertEquals(3, original.size());

        var copy = new HashCommonNodeObjectMap(original, i -> (int) i + 1);
        assertEquals(3, copy.size());
        assertEquals(1, (int) copy.get(node("s")));
        assertEquals(2, (int) copy.get(node("s1")));
        assertEquals(3, (int) copy.get(node("s2")));

        assertEquals(0, (int) original.get(node("s")));
        assertEquals(1, (int) original.get(node("s1")));
        assertEquals(2, (int) original.get(node("s2")));
    }

    @Test
    public void testCopyConstructorAddAndDeleteHasNoSideEffects() {
        var original = new HashCommonNodeObjectMap(10);
        original.put(node("s"), 0);
        original.put(node("s1"), 1);
        original.put(node("s2"), 2);
        assertEquals(3, original.size());

        var copy = new HashCommonNodeObjectMap(original);
        copy.tryRemove(node("s1"));
        copy.put(node("s3"), 3);
        copy.put(node("s4"), 4);

        assertEquals(4, copy.size());
        assertEquals(0, (int) copy.get(node("s")));
        assertEquals(2, (int) copy.get(node("s2")));
        assertEquals(3, (int) copy.get(node("s3")));
        assertEquals(4, (int) copy.get(node("s4")));


        assertEquals(3, original.size());
        assertEquals(0, (int) original.get(node("s")));
        assertEquals(1, (int) original.get(node("s1")));
        assertEquals(2, (int) original.get(node("s2")));
    }

    private static class HashCommonNodeObjectMap extends HashCommonMap<Node, Object> {
        protected HashCommonNodeObjectMap(int initialCapacity) {
            super(initialCapacity);
        }

        protected HashCommonNodeObjectMap(HashCommonMap<Node, Object> mapToCopy) {
            super(mapToCopy);
        }

        protected HashCommonNodeObjectMap(HashCommonMap<Node, Object> mapToCopy, UnaryOperator<Object> valueProcessor) {
            super(mapToCopy, valueProcessor);
        }

        @Override
        public void clear() {
            super.clear(10);
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