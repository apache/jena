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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.impl.TripleStore;

public class TestGraphTripleStoreMem {
    public TripleStore getTripleStore() {
        return new GraphTripleStoreMem(Graph.emptyGraph);
    }

    protected TripleStore store;

    @BeforeEach
    public void setUp() {
        store = getTripleStore();
    }

    @Test public void testEmpty() {
        checkEmpty(store);
    }

    @Test public void testAddOne() {
        store.add(GraphTestLib.triple("x P y"));
        assertEquals(false, store.isEmpty());
        assertEquals(1, store.size());
        assertEquals(true, store.contains(GraphTestLib.triple("x P y")));
        assertEquals(GraphTestLib.nodeSet("x"), Iter.toSet(store.listSubjects()));
        assertEquals(GraphTestLib.nodeSet("y"), Iter.toSet(store.listObjects()));
        assertEquals(GraphTestLib.tripleSet("x P y"), Iter.toSet(store.find(GraphTestLib.triple("?? ?? ??"))));
    }

    @Test public void testListSubjects() {
        someStatements(store);
        assertEquals(GraphTestLib.nodeSet("a x _z r q"), Iter.toSet(store.listSubjects()));
    }

    @Test public void testListObjects() {
        someStatements(store);
        assertEquals(GraphTestLib.nodeSet("b y i _j _t 17"), Iter.toSet(store.listObjects()));
    }

    @Test public void testContains() {
        someStatements(store);
        assertEquals(true, store.contains(GraphTestLib.triple("a P b")));
        assertEquals(true, store.contains(GraphTestLib.triple("x P y")));
        assertEquals(true, store.contains(GraphTestLib.triple("a P i")));
        assertEquals(true, store.contains(GraphTestLib.triple("_z Q _j")));
        assertEquals(true, store.contains(GraphTestLib.triple("x R y")));
        assertEquals(true, store.contains(GraphTestLib.triple("r S _t")));
        assertEquals(true, store.contains(GraphTestLib.triple("q R 17")));
        /* */
        assertEquals(false, store.contains(GraphTestLib.triple("a P x")));
        assertEquals(false, store.contains(GraphTestLib.triple("a P _j")));
        assertEquals(false, store.contains(GraphTestLib.triple("b Z r")));
        assertEquals(false, store.contains(GraphTestLib.triple("_a P x")));
    }

    @Test public void testFind() {
        someStatements(store);
        assertEquals(GraphTestLib.tripleSet(""), Iter.toSet(store.find(GraphTestLib.triple("no such thing"))));
        assertEquals(GraphTestLib.tripleSet("a P b; a P i"), Iter.toSet(store.find(GraphTestLib.triple("a P ??"))));
        assertEquals(GraphTestLib.tripleSet("a P b; x P y; a P i"), Iter.toSet(store.find(GraphTestLib.triple("?? P ??"))));
        assertEquals(GraphTestLib.tripleSet("x P y; x R y"), Iter.toSet(store.find(GraphTestLib.triple("x ?? y"))));
        assertEquals(GraphTestLib.tripleSet("_z Q _j"), Iter.toSet(store.find(GraphTestLib.triple("?? ?? _j"))));
        assertEquals(GraphTestLib.tripleSet("q R 17"), Iter.toSet(store.find(GraphTestLib.triple("?? ?? 17"))));
    }

    @Test public void testRemove() {
        store.add(GraphTestLib.triple("nothing before ace"));
        store.add(GraphTestLib.triple("ace before king"));
        store.add(GraphTestLib.triple("king before queen"));
        store.delete(GraphTestLib.triple("ace before king"));
        assertEquals(GraphTestLib.tripleSet("king before queen; nothing before ace"),
                     Iter.toSet(store.find(GraphTestLib.triple("?? ?? ??"))));
        store.delete(GraphTestLib.triple("king before queen"));
        assertEquals(GraphTestLib.tripleSet("nothing before ace"), Iter.toSet(store.find(GraphTestLib.triple("?? ?? ??"))));
    }

    public void someStatements(TripleStore ts) {
        ts.add(GraphTestLib.triple("a P b"));
        ts.add(GraphTestLib.triple("x P y"));
        ts.add(GraphTestLib.triple("a P i"));
        ts.add(GraphTestLib.triple("_z Q _j"));
        ts.add(GraphTestLib.triple("x R y"));
        ts.add(GraphTestLib.triple("r S _t"));
        ts.add(GraphTestLib.triple("q R 17"));
    }

    private void checkEmpty(TripleStore ts) {
        assertEquals(true, ts.isEmpty());
        assertEquals(0, ts.size());
        assertEquals(false, ts.find(GraphTestLib.triple("?? ?? ??")).hasNext());
        assertEquals(false, ts.listObjects().hasNext());
        assertEquals(false, ts.listSubjects().hasNext());
        assertFalse(ts.contains(GraphTestLib.triple("x P y")));
    }

}
