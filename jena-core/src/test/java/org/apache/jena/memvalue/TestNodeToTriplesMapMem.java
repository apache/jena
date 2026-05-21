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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.*;
import org.apache.jena.graph.Triple.Field;

import static org.junit.jupiter.api.Assertions.*;

public class TestNodeToTriplesMapMem {

    protected NodeToTriplesMapMem ntS = new NodeToTriplesMapMem(Field.fieldSubject, Field.fieldPredicate, Field.fieldObject);

    protected NodeToTriplesMapMem ntP = new NodeToTriplesMapMem(Field.fieldPredicate, Field.fieldObject, Field.fieldSubject);

    protected NodeToTriplesMapMem ntO = new NodeToTriplesMapMem(Field.fieldObject, Field.fieldPredicate, Field.fieldSubject);

    protected static final Node x = GraphTestLib.node("x");

    protected static final Node y = GraphTestLib.node("y");

    @Test
    public void testZeroSize() {
        testZeroSize("fresh NTM", ntS);
    }

    protected void testZeroSize(String title, NodeToTriplesMapMem nt) {
        assertEquals(0, nt.size(), title + " should have size 0");
        assertEquals(true, nt.isEmpty(), title + " should be isEmpty()");
        assertEquals(false, nt.domain().hasNext(), title + " should have empty domain");
    }

    @Test
    public void testAddOne() {
        ntS.add(GraphTestLib.triple("x P y"));
        testJustOne(x, ntS);
    }

    @Test
    public void testAddOneTwice() {
        addTriples(ntS, "x P y; x P y");
        testJustOne(x, ntS);
    }

    protected void testJustOne(Node x, NodeToTriplesMapMem nt) {
        assertEquals(1, nt.size());
        assertEquals(false, nt.isEmpty());
        assertEquals(just(x), Iter.toSet(nt.domain()));
    }

    @Test
    public void testAddTwoUnshared() {
        addTriples(ntS, "x P a; y Q b");
        assertEquals(2, ntS.size());
        assertEquals(false, ntS.isEmpty());
        assertEquals(both(x, y), Iter.toSet(ntS.domain()));
    }

    @Test
    public void testAddTwoShared() {
        addTriples(ntS, "x P a; x Q b");
        assertEquals(2, ntS.size());
        assertEquals(false, ntS.isEmpty());
        assertEquals(just(x), Iter.toSet(ntS.domain()));
    }

    @Test
    public void testClear() {
        addTriples(ntS, "x P a; x Q b; y R z");
        ntS.clear();
        testZeroSize("cleared NTM", ntS);
    }

    @Test
    public void testAllIterator() {
        String triples = "x P b; y P d; y P f";
        addTriples(ntS, triples);
        assertEquals(GraphTestLib.tripleSet(triples), Iter.toSet(ntS.iterateAll()));
    }

    @Test
    public void testOneIterator() {
        addTriples(ntS, "x P b; y P d; y P f");
        assertEquals(GraphTestLib.tripleSet("x P b"), ntS.iterator(x, null).toSet());
        assertEquals(GraphTestLib.tripleSet("y P d; y P f"), ntS.iterator(y, null).toSet());
    }

    @Test
    public void testRemove() {
        addTriples(ntS, "x P b; y P d; y R f");
        ntS.remove(GraphTestLib.triple("y P d"));
        assertEquals(2, ntS.size());
        assertEquals(GraphTestLib.tripleSet("x P b; y R f"), ntS.iterateAll().toSet());
    }

    @Test
    public void testRemoveByIterator() {
        addTriples(ntS, "x nice a; a nasty b; x nice c");
        addTriples(ntS, "y nice d; y nasty e; y nice f");
        Iterator<Triple> it = ntS.iterateAll();
        while (it.hasNext()) {
            Triple t = it.next();
            if ( t.getPredicate().equals(GraphTestLib.node("nasty")) )
                it.remove();
        }
        assertEquals(GraphTestLib.tripleSet("x nice a; x nice c; y nice d; y nice f"), ntS.iterateAll().toSet());
    }

    @Test
    public void testRemoveByIteratorTriggerMove() {
        /* need hash collisions to be able to test moves caused by iterator#remove */
        var nodeA = new Node_URI("A") {
            @Override
            public int hashCode() {
                return 1;
            }
        };
        var nodeB = new Node_URI("B") {
            @Override
            public int hashCode() {
                return 1;
            }
        };
        var nodeC = new Node_URI("C") {
            @Override
            public int hashCode() {
                return 1;
            }
        };
        ntS.add(Triple.create(nodeA, NodeFactory.createURI("loves"), nodeB));
        ntS.add(Triple.create(nodeB, NodeFactory.createURI("loves"), nodeC));
        ntS.add(Triple.create(nodeC, NodeFactory.createURI("loves"), nodeA));

        var triplesToFind = ntS.iterateAll().toSet();

        Iterator<Triple> it = ntS.iterateAll();
        while (it.hasNext()) {
            Triple t = it.next();
            triplesToFind.remove(t);
            if ( t.getSubject().equals(nodeA) )
                it.remove();
        }
        assertTrue(triplesToFind.isEmpty());

        var expectedRemainingTripples = new HashSet<Triple>();
        expectedRemainingTripples.add(Triple.create(nodeB, NodeFactory.createURI("loves"), nodeC));
        expectedRemainingTripples.add(Triple.create(nodeC, NodeFactory.createURI("loves"), nodeA));
        assertEquals(expectedRemainingTripples, ntS.iterateAll().toSet());
    }

    @Test
    public void testIteratorWIthPatternOnEmpty() {
        assertEquals(GraphTestLib.tripleSet(""), ntS.iterateAll(GraphTestLib.triple("a P b")).toSet());
    }

    @Test
    public void testIteratorWIthPatternOnSomething() {
        addTriples(ntS, "x P a; y P b; y R c");
        assertEquals(GraphTestLib.tripleSet("x P a"), ntS.iterateAll(GraphTestLib.triple("x P ??")).toSet());
        assertEquals(GraphTestLib.tripleSet("y P b; y R c"), ntS.iterateAll(GraphTestLib.triple("y ?? ??")).toSet());
        assertEquals(GraphTestLib.tripleSet("x P a; y P b"), ntS.iterateAll(GraphTestLib.triple("?? P ??")).toSet());
        assertEquals(GraphTestLib.tripleSet("y R c"), ntS.iterateAll(GraphTestLib.triple("?? ?? c")).toSet());
    }

    @Test
    public void testUnspecificRemoveS() {
        addTriples(ntS, "x P a; y Q b; z R c");
        ntS.remove(GraphTestLib.triple("x P a"));
        assertEquals(GraphTestLib.tripleSet("y Q b; z R c"), ntS.iterateAll().toSet());
    }

    @Test
    public void testUnspecificRemoveP() {
        addTriples(ntP, "x P a; y Q b; z R c");
        ntP.remove(GraphTestLib.triple("y Q b"));
        assertEquals(GraphTestLib.tripleSet("x P a; z R c"), ntP.iterateAll().toSet());
    }

    @Test
    public void testUnspecificRemoveO() {
        addTriples(ntO, "x P a; y Q b; z R c");
        ntO.remove(GraphTestLib.triple("z R c"));
        assertEquals(GraphTestLib.tripleSet("x P a; y Q b"), ntO.iterateAll().toSet());
    }

    @Test
    public void testAddBooleanResult() {
        assertEquals(true, ntS.add(GraphTestLib.triple("x P y")));
        assertEquals(false, ntS.add(GraphTestLib.triple("x P y")));
        /* */
        assertEquals(true, ntS.add(GraphTestLib.triple("y Q z")));
        assertEquals(false, ntS.add(GraphTestLib.triple("y Q z")));
        /* */
        assertEquals(true, ntS.add(GraphTestLib.triple("y R s")));
        assertEquals(false, ntS.add(GraphTestLib.triple("y R s")));
    }

    @Test
    public void testRemoveBooleanResult() {
        assertEquals(false, ntS.remove(GraphTestLib.triple("x P y")));
        ntS.add(GraphTestLib.triple("x P y"));
        assertEquals(false, ntS.remove(GraphTestLib.triple("x Q y")));
        assertEquals(true, ntS.remove(GraphTestLib.triple("x P y")));
        assertEquals(false, ntS.remove(GraphTestLib.triple("x P y")));
    }

    @Test
    public void testContains() {
        addTriples(ntS, "x P y; a P b");
        assertTrue(ntS.contains(GraphTestLib.triple("x P y")));
        assertTrue(ntS.contains(GraphTestLib.triple("a P b")));
        assertFalse(ntS.contains(GraphTestLib.triple("x P z")));
        assertFalse(ntS.contains(GraphTestLib.triple("y P y")));
        assertFalse(ntS.contains(GraphTestLib.triple("x R y")));
        assertFalse(ntS.contains(GraphTestLib.triple("e T f")));
        assertFalse(ntS.contains(GraphTestLib.triple("_x F 17")));
    }

    // TODO more here

    protected void addTriples(NodeToTriplesMapMem nt, String facts) {
        Triple[] t = GraphTestLib.tripleArray(facts);
        for ( Triple aT : t ) {
            nt.add(aT);
        }
    }

    protected static <T> Set<T> just(T x) {
        Set<T> result = new HashSet<>();
        result.add(x);
        return result;
    }

    protected static Set<Object> both(Object x, Object y) {
        Set<Object> result = just(x);
        result.add(y);
        return result;
    }

}
