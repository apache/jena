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
package org.apache.jena.mem;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.store.TripleStore;
import org.apache.jena.util.iterator.NullIterator;
import org.junit.Test;

import java.util.stream.Stream;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GraphMemTest {

    @Test
    public void clear() {
        TripleStore mockStore = mock();

        when(mockStore.find(any())).thenReturn(NullIterator.emptyIterator());

        var sut = new GraphMem(mockStore);
        sut.clear();

        inOrder(mockStore).verify(mockStore, times(1)).find(any());
        inOrder(mockStore).verify(mockStore, times(1)).clear();
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void performAdd() {
        TripleStore mockStore = mock();

        var triple = triple("a b x");

        var sut = new GraphMem(mockStore);
        sut.performAdd(triple);

        inOrder(mockStore).verify(mockStore, times(1)).add(triple);
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void performDelete() {
        TripleStore mockStore = mock();

        var triple = triple("a b x");

        var sut = new GraphMem(mockStore);
        sut.performDelete(triple);

        inOrder(mockStore).verify(mockStore, times(1)).remove(triple);
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void stream() {
        TripleStore mockStore = mock();

        when(mockStore.stream()).thenReturn(Stream.empty());

        var sut = new GraphMem(mockStore);
        sut.stream();

        inOrder(mockStore).verify(mockStore, times(1)).stream();
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void StreamMatch() {
        TripleStore mockStore = mock();

        var s = node("s");
        var p = node("p");
        var o = node("o");
        var triple = Triple.create(s, p, o);

        when(mockStore.stream(triple)).thenReturn(Stream.of(triple));

        var sut = new GraphMem(mockStore);
        sut.stream(s, p, o);

        inOrder(mockStore).verify(mockStore, times(1)).stream(triple);
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void graphBaseFind() {
        TripleStore mockStore = mock();

        var triple = triple("a b x");

        when(mockStore.find(triple)).thenReturn(NullIterator.emptyIterator());

        var sut = new GraphMem(mockStore);
        sut.graphBaseFind(triple);

        inOrder(mockStore).verify(mockStore, times(1)).find(triple);
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void graphBaseContains() {
        TripleStore mockStore = mock();

        var triple = triple("a b x");

        when(mockStore.contains(triple)).thenReturn(false);

        var sut = new GraphMem(mockStore);
        sut.graphBaseContains(triple);

        inOrder(mockStore).verify(mockStore, times(1)).contains(triple);
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void graphBaseSize() {
        TripleStore mockStore = mock();

        when(mockStore.countTriples()).thenReturn(0);

        var sut = new GraphMem(mockStore);
        sut.graphBaseSize();

        inOrder(mockStore).verify(mockStore, times(1)).countTriples();
        verifyNoMoreInteractions(mockStore);
    }

    @Test
    public void testCopy() {
        TripleStore mockStore = mock();
        TripleStore mockStoreCopy = mock();

        when(mockStore.copy()).thenReturn(mockStoreCopy);

        GraphMem sut = new GraphMem(mockStore);
        GraphMem copy = sut.copy();

        assertNotNull(copy);
        assertEquals(mockStoreCopy, copy.tripleStore);
    }
}
