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

package org.apache.jena.util.iterator;

import static junit.framework.TestCase.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
    test the WrappedIterator class.
*/
import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class TestWrappedIterator {

    @Test
    public void testWrappedIterator() {
        Iterator<String> i = Arrays.asList(new String[]{"bill", "and", "ben"}).iterator();
        ExtendedIterator<String> e = WrappedIterator.create(i);
        assertTrue(e.hasNext(), "wrapper has at least one element");
        assertEquals("bill", e.next());
        assertTrue(e.hasNext(), "wrapper has at least two elements");
        assertEquals("and", e.next());
        assertTrue(e.hasNext(), "wrapper has at least three elements");
        assertEquals("ben", e.next());
        assertFalse(e.hasNext(),"wrapper is now empty");
    }

    @Test
    public void testUnwrapExtendedIterator() {
        ExtendedIterator<Triple> i = GraphTestLib.graphWith("a R b").find(Triple.ANY);
        Assertions.assertSame(i, WrappedIterator.create(i));
    }

    @Test
    public void testWrappedNoRemove() {
        Iterator<Node> base = GraphTestLib.nodeSet("a b c").iterator();
        base.next();
        base.remove();
        ExtendedIterator<Node> wrapped = WrappedIterator.createNoRemove(base);
        wrapped.next();
        assertThrows(UnsupportedOperationException.class, ()->wrapped.remove(), "wrapped-no-remove iterator should deny .remove()");
    }
}
