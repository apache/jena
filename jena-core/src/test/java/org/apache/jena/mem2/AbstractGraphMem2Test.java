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

package org.apache.jena.mem2;

import org.apache.jena.datatypes.xsd.impl.XSDDouble;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public abstract class AbstractGraphMem2Test {

    protected Graph sut;

    protected abstract Graph createGraph();

    @Before
    public void setUp() throws Exception {
        sut = createGraph();
    }

    @Test
    public void testClear() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.size());
        sut.clear();
        assertEquals(0, sut.size());
        assertTrue(sut.isEmpty());
    }


    @Test
    public void testDelete() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.size());
        sut.delete(triple("x R y"));
        assertEquals(0, sut.size());
        assertTrue(sut.isEmpty());
    }

    @Test
    public void testFind() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.find(triple("x R y")).toList().size());
        assertEquals(0, sut.find(triple("x R z")).toList().size());
    }

    @Test
    public void testFind1() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.find(null, null, null).toList().size());
        assertEquals(1, sut.find(null, null, node("y")).toList().size());
        assertEquals(1, sut.find(null, node("R"), null).toList().size());
        assertEquals(1, sut.find(null, node("R"), node("y")).toList().size());
        assertEquals(1, sut.find(node("x"), null, null).toList().size());
        assertEquals(1, sut.find(node("x"), null, node("y")).toList().size());
        assertEquals(1, sut.find(node("x"), node("R"), null).toList().size());
        assertEquals(1, sut.find(node("x"), node("R"), node("y")).toList().size());
    }

    @Test
    public void testFind2() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.find(null, null, null).toList().size());
        assertEquals(0, sut.find(null, null, node("z")).toList().size());
        assertEquals(0, sut.find(null, node("S"), null).toList().size());
        assertEquals(0, sut.find(null, node("S"), node("y")).toList().size());
        assertEquals(0, sut.find(node("y"), null, null).toList().size());
        assertEquals(0, sut.find(node("y"), null, node("y")).toList().size());
        assertEquals(0, sut.find(node("y"), node("R"), null).toList().size());
        assertEquals(0, sut.find(node("y"), node("R"), node("y")).toList().size());
    }

    @Test
    public void testFindWithIteratorHasNextNext() {
        sut.add(triple("x R y"));
        var iter = sut.find(triple("x R y"));
        assertTrue(iter.hasNext());
        assertEquals(triple("x R y"), iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testFindSPO() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.find(node("x"), node("R"), node("y")).toList().size());
        assertEquals(0, sut.find(node("x"), node("R"), node("z")).toList().size());
    }

    @Test
    public void testFind___() {
        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(null, null, null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc, cBa, cBb, cBc));
    }

    @Test
    public void testFindS__() {
        assertFalse(sut.find(node("a"), null, null).hasNext());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(node("a"), null, null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.find(node("b"), null, null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.find(node("c"), null, null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.find(node("d"), null, null).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFind_P_() {
        assertFalse(sut.find(null, node("A"), null).hasNext());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(null, node("A"), null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc));

        findings = sut.find(null, node("B"), null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.find(null, node("C"), null).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFind__O() {
        assertFalse(sut.find(null, null, node("a")).hasNext());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(null, null, node("a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa, cBa));

        findings = sut.find(null, null, node("b")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAb, bAb, cBb));

        findings = sut.find(null, null, node("c")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAc, bAc, cBc));

        findings = sut.find(null, null, node("d")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFindSP_() {
        assertFalse(sut.find(node("a"), node("A"), null).hasNext());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(node("a"), node("A"), null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.find(node("b"), node("A"), null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.find(node("c"), node("B"), null).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.find(node("d"), node("C"), null).toList();
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.find(node("a"), node("B"), null).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFindS_O() {
        assertFalse(sut.find(node("a"), null, node("a")).hasNext());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(node("a"), null, node("a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa));

        findings = sut.find(node("b"), null, node("a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa));

        findings = sut.find(node("c"), null, node("a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.find(node("d"), null, node("a")).toList();
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.find(node("a"), null, node("d")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFind_PO() {
        assertFalse(sut.find(null, node("A"), node("a")).hasNext());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.find(null, node("A"), node("a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa));

        findings = sut.find(null, node("B"), node("a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.find(null, node("C"), node("a")).toList();
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.find(null, node("A"), node("d")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.stream().count());
    }

    @Test
    public void testStreamEmpty() {
        assertEquals(0, sut.stream().count());
    }

    @Test
    public void testStreamSPO() {
        assertEquals(0, sut.stream(node("x"), node("R"), node("y")).count());

        var t = triple("x R y");
        sut.add(t);
        var findings = sut.stream(t.getSubject(), t.getPredicate(), t.getObject()).collect(Collectors.toList());
        assertEquals(1, findings.size());
        assertEquals(findings.get(0), t);
    }

    @Test
    public void testStream___() {
        assertEquals(0, sut.stream(null, null, null).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(null, null, null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc, cBa, cBb, cBc));
    }

    @Test
    public void testStreamS__() {
        assertEquals(0, sut.stream(node("a"), null, null).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(aAa.getSubject(), null, null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.stream(bAa.getSubject(), null, null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.stream(cBa.getSubject(), null, null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.stream(node("d"), null, null).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream_P_() {
        assertEquals(0, sut.stream(null, node("A"), null).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(null, aAa.getPredicate(), null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc));

        findings = sut.stream(null, cBa.getPredicate(), null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.stream(null, node("C"), null).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream__O() {
        assertEquals(0, sut.stream(null, null, node("a")).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(null, null, aAa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa, cBa));

        findings = sut.stream(null, null, aAb.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAb, bAb, cBb));

        findings = sut.stream(null, null, aAc.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAc, bAc, cBc));

        findings = sut.stream(null, null, node("d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStreamSP_() {
        assertEquals(0, sut.stream(node("a"), node("A"), null).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(aAa.getSubject(), aAa.getPredicate(), null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.stream(bAa.getSubject(), bAa.getPredicate(), null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.stream(cBa.getSubject(), cBa.getPredicate(), null).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.stream(node("a"), node("C"), null).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(node("d"), node("D"), null).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStreamS_O() {
        assertEquals(0, sut.stream(node("a"), null, node("a")).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(aAa.getSubject(), null, aAa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa));

        findings = sut.stream(bAa.getSubject(), null, bAa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa));

        findings = sut.stream(cBa.getSubject(), null, cBa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.stream(node("d"), null, node("d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(node("d"), null, node("a")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(node("a"), null, node("d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream_PO() {
        assertEquals(0, sut.stream(null, node("A"), node("a")).count());

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        var findings = sut.stream(null, aAa.getPredicate(), aAa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa));

        findings = sut.stream(null, bAa.getPredicate(), bAa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa));

        findings = sut.stream(null, cBa.getPredicate(), cBa.getObject()).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.stream(null, node("C"), node("a")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(null, node("A"), node("d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(null, node("D"), node("d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testContains() {
        sut.add(triple("x R y"));
        assertTrue(sut.contains(triple("x R y")));
        assertFalse(sut.contains(triple("x R z")));
    }

    @Test
    public void testContains1() {
        sut.add(triple("x R y"));
        sut.add(triple("y S z"));
        sut.add(triple("z T a"));
        assertTrue(sut.contains(null, null, null));
        assertTrue(sut.contains(null, null, node("y")));
        assertTrue(sut.contains(null, node("R"), null));
        assertTrue(sut.contains(null, node("R"), node("y")));
        assertTrue(sut.contains(node("x"), null, null));
        assertTrue(sut.contains(node("x"), null, node("y")));
        assertTrue(sut.contains(node("x"), node("R"), null));
        assertTrue(sut.contains(node("x"), node("R"), node("y")));
    }

    @Test
    public void testContains2() {
        sut.add(triple("x R y"));
        sut.add(triple("y S z"));
        sut.add(triple("z T a"));
        assertTrue(sut.contains(null, null, null));
        assertFalse(sut.contains(null, null, node("x")));
        assertFalse(sut.contains(null, node("U"), null));
        assertFalse(sut.contains(null, node("R"), node("z")));
        assertFalse(sut.contains(node("a"), null, null));
        assertFalse(sut.contains(node("x"), null, node("x")));
        assertFalse(sut.contains(node("y"), node("R"), null));
        assertFalse(sut.contains(node("y"), node("T"), node("a")));
    }

    @Test
    public void testContainsSPO() {
        assertFalse(sut.contains(node("a"), node("A"), node("a")));

        var t = triple("a A a");
        sut.add(t);
        assertTrue(sut.contains(t.getSubject(), t.getPredicate(), t.getObject()));
        assertFalse(sut.contains(t.getSubject(), t.getPredicate(), node("b")));
        assertFalse(sut.contains(t.getSubject(), node("B"), t.getObject()));
        assertFalse(sut.contains(node("b"), t.getPredicate(), t.getObject()));
    }

    @Test
    public void testContains___() {
        assertFalse(sut.contains(null, null, null));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(null, null, null));
    }

    @Test
    public void testContainsS__() {
        assertFalse(sut.contains(node("a"), null, null));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(aAa.getSubject(), null, null));

        assertTrue(sut.contains(bAa.getSubject(), null, null));

        assertTrue(sut.contains(cBa.getSubject(), null, null));

        assertFalse(sut.contains(node("d"), null, null));
    }

    @Test
    public void testContains_P_() {
        assertFalse(sut.contains(null, node("A"), null));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(null, aAa.getPredicate(), null));

        assertTrue(sut.contains(null, cBa.getPredicate(), null));

        assertFalse(sut.contains(null, node("C"), null));
    }

    @Test
    public void testContains__O() {
        assertFalse(sut.contains(null, null, node("a")));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(null, null, aAa.getObject()));

        assertTrue(sut.contains(null, null, aAb.getObject()));

        assertTrue(sut.contains(null, null, aAc.getObject()));

        assertFalse(sut.contains(null, null, node("d")));
    }

    @Test
    public void testContainsSP_() {
        assertFalse(sut.contains(node("a"), node("A"), null));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(aAa.getSubject(), aAa.getPredicate(), null));

        assertTrue(sut.contains(bAa.getSubject(), bAa.getPredicate(), null));

        assertTrue(sut.contains(cBa.getSubject(), cBa.getPredicate(), null));

        assertFalse(sut.contains(node("a"), node("C"), null));

        assertFalse(sut.contains(node("d"), node("D"), null));
    }

    @Test
    public void testContainsS_O() {
        assertFalse(sut.contains(node("a"), null, node("a")));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(aAa.getSubject(), null, aAa.getObject()));

        assertTrue(sut.contains(bAa.getSubject(), null, bAa.getObject()));

        assertTrue(sut.contains(cBa.getSubject(), null, cBa.getObject()));

        assertFalse(sut.contains(node("d"), null, node("d")));

        assertFalse(sut.contains(node("d"), null, node("a")));

        assertFalse(sut.contains(node("a"), null, node("d")));
    }

    @Test
    public void testContains_PO() {
        assertFalse(sut.contains(null, node("A"), node("a")));

        final var aAa = triple("a A a");
        final var aAb = triple("a A b");
        final var aAc = triple("a A c");
        final var bAa = triple("b A a");
        final var bAb = triple("b A b");
        final var bAc = triple("b A c");
        final var cBa = triple("c B a");
        final var cBb = triple("c B b");
        final var cBc = triple("c B c");

        sut.add(aAa);
        sut.add(aAb);
        sut.add(aAc);
        sut.add(bAa);
        sut.add(bAb);
        sut.add(bAc);
        sut.add(cBa);
        sut.add(cBb);
        sut.add(cBc);

        assertTrue(sut.contains(null, aAa.getPredicate(), aAa.getObject()));

        assertTrue(sut.contains(null, bAa.getPredicate(), bAa.getObject()));

        assertTrue(sut.contains(null, cBa.getPredicate(), cBa.getObject()));

        assertFalse(sut.contains(null, node("C"), node("a")));

        assertFalse(sut.contains(null, node("A"), node("d")));

        assertFalse(sut.contains(null, node("D"), node("d")));
    }

    @Test
    public void testContainsValueObject() {
        sut.add(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"),
                NodeFactory.createLiteral("0.1", XSDDouble.XSDdouble)));
        assertTrue(sut.contains(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"),
                NodeFactory.createLiteral("0.1", XSDDouble.XSDdouble))));
        assertFalse(sut.contains(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"),
                NodeFactory.createLiteral("0.10", XSDDouble.XSDdouble))));
        assertFalse(sut.contains(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"),
                NodeFactory.createLiteral("0.11", XSDDouble.XSDdouble))));
    }

    @Test
    public void testContainsValueSubject() {
        var containedTriple = Triple.create(
                NodeFactory.createLiteral("0.1", XSDDouble.XSDdouble),
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"));
        sut.add(containedTriple);

        var match = Triple.create(
                NodeFactory.createLiteral("0.1", XSDDouble.XSDdouble),
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"));
        assertTrue(sut.contains(match));
        assertEquals(containedTriple, sut.find(match).next());

        match = Triple.create(
                NodeFactory.createLiteral("0.10", XSDDouble.XSDdouble),
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"));
        assertFalse(sut.contains(match));
        assertFalse(sut.find(match).hasNext());

        match = Triple.create(
                NodeFactory.createLiteral("0.11", XSDDouble.XSDdouble),
                NodeFactory.createURI("x"),
                NodeFactory.createURI("R"));
        assertFalse(sut.contains(match));
        assertFalse(sut.find(match).hasNext());
    }

    @Test
    public void testContainsValuePredicate() {
        sut.add(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createLiteral("0.1", XSDDouble.XSDdouble),
                NodeFactory.createURI("R")));
        assertTrue(sut.contains(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createLiteral("0.1", XSDDouble.XSDdouble),
                NodeFactory.createURI("R"))));
        assertFalse(sut.contains(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createLiteral("0.10", XSDDouble.XSDdouble),
                NodeFactory.createURI("R"))));
        assertFalse(sut.contains(Triple.create(
                NodeFactory.createURI("x"),
                NodeFactory.createLiteral("0.11", XSDDouble.XSDdouble),
                NodeFactory.createURI("R"))));
    }

}
