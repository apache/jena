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

package org.apache.jena.mem2.store;

import org.apache.jena.datatypes.xsd.impl.XSDDouble;
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

public abstract class AbstractTripleStoreTest {

    protected TripleStore sut;

    protected abstract TripleStore createTripleStore();

    @Before
    public void setUp() throws Exception {
        sut = createTripleStore();
    }

    @Test
    public void testClear() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.countTriples());
        sut.clear();
        assertEquals(0, sut.countTriples());
        assertTrue(sut.isEmpty());
    }


    @Test
    public void testDelete() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.countTriples());
        sut.remove(triple("x R y"));
        assertEquals(0, sut.countTriples());
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
        assertEquals(1, sut.find(triple("?? ?? ??")).toList().size());
        assertEquals(1, sut.find(triple("?? ?? y")).toList().size());
        assertEquals(1, sut.find(triple("?? R ??")).toList().size());
        assertEquals(1, sut.find(triple("?? R y")).toList().size());
        assertEquals(1, sut.find(triple("x ?? ??")).toList().size());
        assertEquals(1, sut.find(triple("x ?? y")).toList().size());
        assertEquals(1, sut.find(triple("x R ??")).toList().size());
        assertEquals(1, sut.find(triple("x R y")).toList().size());
    }

    @Test
    public void testFind2() {
        sut.add(triple("x R y"));
        assertEquals(1, sut.find(triple("?? ?? ??")).toList().size());
        assertEquals(0, sut.find(triple("?? ?? z")).toList().size());
        assertEquals(0, sut.find(triple("?? S ??")).toList().size());
        assertEquals(0, sut.find(triple("?? S y")).toList().size());
        assertEquals(0, sut.find(triple("y ?? ??")).toList().size());
        assertEquals(0, sut.find(triple("y ?? y")).toList().size());
        assertEquals(0, sut.find(triple("y R ??")).toList().size());
        assertEquals(0, sut.find(triple("y R y")).toList().size());
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
        assertEquals(1, sut.find(triple("x R y")).toList().size());
        assertEquals(0, sut.find(triple("x R z")).toList().size());
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

        var findings = sut.find(triple("?? ?? ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc, cBa, cBb, cBc));
    }

    @Test
    public void testFindS__() {
        assertFalse(sut.find(triple("a ?? ??")).hasNext());

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

        var findings = sut.find(triple("a ?? ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.find(triple("b ?? ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.find(triple("c ?? ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.find(triple("d ?? ??")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFind_P_() {
        assertFalse(sut.find(triple("?? A ??")).hasNext());

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

        var findings = sut.find(triple("?? A ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc));

        findings = sut.find(triple("?? B ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.find(triple("?? C ??")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFind__O() {
        assertFalse(sut.find(triple("?? ?? a")).hasNext());

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

        var findings = sut.find(triple("?? ?? a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa, cBa));

        findings = sut.find(triple("?? ?? b")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAb, bAb, cBb));

        findings = sut.find(triple("?? ?? c")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAc, bAc, cBc));

        findings = sut.find(triple("?? ?? d")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFindSP_() {
        assertFalse(sut.find(triple("a A ??")).hasNext());

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

        var findings = sut.find(triple("a A ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.find(triple("b A ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.find(triple("c B ??")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.find(triple("d C ??")).toList();
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.find(triple("a B ??")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFindS_O() {
        assertFalse(sut.find(triple("a ?? a")).hasNext());

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

        var findings = sut.find(triple("a ?? a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa));

        findings = sut.find(triple("b ?? a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa));

        findings = sut.find(triple("c ?? a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.find(triple("d ?? a")).toList();
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.find(triple("a ?? d")).toList();
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testFind_PO() {
        assertFalse(sut.find(triple("?? A a")).hasNext());

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

        var findings = sut.find(triple("?? A a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa));

        findings = sut.find(triple("?? B a")).toList();
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.find(triple("?? C a")).toList();
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.find(triple("?? A d")).toList();
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
        assertEquals(0, sut.stream(triple("x R y")).count());

        var t = triple("x R y");
        sut.add(t);
        var findings = sut.stream(t).collect(Collectors.toList());
        assertEquals(1, findings.size());
        assertEquals(findings.get(0), t);
    }

    @Test
    public void testStream___() {
        assertEquals(0, sut.stream(triple("?? ?? ??")).count());

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

        var findings = sut.stream(triple("?? ?? ??")).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc, cBa, cBb, cBc));
    }

    @Test
    public void testStreamS__() {
        assertEquals(0, sut.stream(triple("a ?? ??")).count());

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

        var findings = sut.stream(Triple.createMatch(aAa.getSubject(), null, null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.stream(Triple.createMatch(bAa.getSubject(), null, null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.stream(Triple.createMatch(cBa.getSubject(), null, null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.stream(triple("d ?? ??")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream_P_() {
        assertEquals(0, sut.stream(triple("?? A ??")).count());

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

        var findings = sut.stream(Triple.createMatch(null, aAa.getPredicate(), null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc, bAa, bAb, bAc));

        findings = sut.stream(Triple.createMatch(null, cBa.getPredicate(), null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.stream(triple("?? C ??")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream__O() {
        assertEquals(0, sut.stream(triple("?? ?? a")).count());

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

        var findings = sut.stream(Triple.createMatch(null, null, aAa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa, cBa));

        findings = sut.stream(Triple.createMatch(null, null, aAb.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAb, bAb, cBb));

        findings = sut.stream(Triple.createMatch(null, null, aAc.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAc, bAc, cBc));

        findings = sut.stream(triple("?? ?? d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStreamSP_() {
        assertEquals(0, sut.stream(triple("a A ??")).count());

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

        var findings = sut.stream(Triple.createMatch(aAa.getSubject(), aAa.getPredicate(), null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, aAb, aAc));

        findings = sut.stream(Triple.createMatch(bAa.getSubject(), bAa.getPredicate(), null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa, bAb, bAc));

        findings = sut.stream(Triple.createMatch(cBa.getSubject(), cBa.getPredicate(), null)).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa, cBb, cBc));

        findings = sut.stream(triple("a C ??")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(triple("d D ??")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStreamS_O() {
        assertEquals(0, sut.stream(triple("a ?? a")).count());

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

        var findings = sut.stream(Triple.createMatch(aAa.getSubject(), null, aAa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa));

        findings = sut.stream(Triple.createMatch(bAa.getSubject(), null, bAa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(bAa));

        findings = sut.stream(Triple.createMatch(cBa.getSubject(), null, cBa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.stream(triple("d ?? d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(triple("d ?? a")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(triple("a ?? d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());
    }

    @Test
    public void testStream_PO() {
        assertEquals(0, sut.stream(triple("?? A a")).count());

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

        var findings = sut.stream(Triple.createMatch(null, aAa.getPredicate(), aAa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa));

        findings = sut.stream(Triple.createMatch(null, bAa.getPredicate(), bAa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(aAa, bAa));

        findings = sut.stream(Triple.createMatch(null, cBa.getPredicate(), cBa.getObject())).collect(Collectors.toList());
        assertThat(findings, IsIterableContainingInAnyOrder.containsInAnyOrder(cBa));

        findings = sut.stream(triple("?? C a")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(triple("?? A d")).collect(Collectors.toList());
        assertThat(findings, IsEmptyCollection.empty());

        findings = sut.stream(triple("?? D d")).collect(Collectors.toList());
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
        assertTrue(sut.contains(triple("?? ?? ??")));
        assertTrue(sut.contains(triple("?? ?? y")));
        assertTrue(sut.contains(triple("?? R ??")));
        assertTrue(sut.contains(triple("?? R y")));
        assertTrue(sut.contains(triple("x ?? ??")));
        assertTrue(sut.contains(triple("x ?? y")));
        assertTrue(sut.contains(triple("x R ??")));
        assertTrue(sut.contains(triple("x R y")));
    }

    @Test
    public void testContains2() {
        sut.add(triple("x R y"));
        sut.add(triple("y S z"));
        sut.add(triple("z T a"));
        assertTrue(sut.contains(triple("?? ?? ??")));
        assertFalse(sut.contains(triple("?? ?? x")));
        assertFalse(sut.contains(triple("?? U ??")));
        assertFalse(sut.contains(triple("?? R z")));
        assertFalse(sut.contains(triple("a ?? ??")));
        assertFalse(sut.contains(triple("x ?? x")));
        assertFalse(sut.contains(triple("y R ??")));
        assertFalse(sut.contains(triple("y T a")));
    }

    @Test
    public void testContainsSPO() {
        assertFalse(sut.contains(triple("a A a")));

        var t = triple("a A a");
        sut.add(t);
        assertTrue(sut.contains(t));
        assertFalse(sut.contains(Triple.createMatch(t.getSubject(), t.getPredicate(), node("b"))));
        assertFalse(sut.contains(Triple.createMatch(t.getSubject(), node("B"), t.getObject())));
        assertFalse(sut.contains(Triple.createMatch(node("b"), t.getPredicate(), t.getObject())));
    }

    @Test
    public void testContains___() {
        assertFalse(sut.contains(triple("?? ?? ??")));

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

        assertTrue(sut.contains(triple("?? ?? ??")));
    }

    @Test
    public void testContainsS__() {
        assertFalse(sut.contains(triple("a ?? ??")));

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

        assertTrue(sut.contains(Triple.createMatch(aAa.getSubject(), null, null)));

        assertTrue(sut.contains(Triple.createMatch(bAa.getSubject(), null, null)));

        assertTrue(sut.contains(Triple.createMatch(cBa.getSubject(), null, null)));

        assertFalse(sut.contains(triple("d ?? ??")));
    }

    @Test
    public void testContains_P_() {
        assertFalse(sut.contains(triple("?? A ??")));

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

        assertTrue(sut.contains(Triple.createMatch(null, aAa.getPredicate(), null)));

        assertTrue(sut.contains(Triple.createMatch(null, cBa.getPredicate(), null)));

        assertFalse(sut.contains(triple("?? C ??")));
    }

    @Test
    public void testContains__O() {
        assertFalse(sut.contains(triple("?? ?? a")));

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

        assertTrue(sut.contains(Triple.createMatch(null, null, aAa.getObject())));

        assertTrue(sut.contains(Triple.createMatch(null, null, aAb.getObject())));

        assertTrue(sut.contains(Triple.createMatch(null, null, aAc.getObject())));

        assertFalse(sut.contains(triple("?? ?? d")));
    }

    @Test
    public void testContainsSP_() {
        assertFalse(sut.contains(triple("a A ??")));

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

        assertTrue(sut.contains(Triple.createMatch(aAa.getSubject(), aAa.getPredicate(), null)));

        assertTrue(sut.contains(Triple.createMatch(bAa.getSubject(), bAa.getPredicate(), null)));

        assertTrue(sut.contains(Triple.createMatch(cBa.getSubject(), cBa.getPredicate(), null)));

        assertFalse(sut.contains(triple("a C ??")));

        assertFalse(sut.contains(triple("d D ??")));
    }

    @Test
    public void testContainsS_O() {
        assertFalse(sut.contains(triple("a ?? a")));

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

        assertTrue(sut.contains(Triple.createMatch(aAa.getSubject(), null, aAa.getObject())));

        assertTrue(sut.contains(Triple.createMatch(bAa.getSubject(), null, bAa.getObject())));

        assertTrue(sut.contains(Triple.createMatch(cBa.getSubject(), null, cBa.getObject())));

        assertFalse(sut.contains(triple("d ?? d")));

        assertFalse(sut.contains(triple("d ?? a")));

        assertFalse(sut.contains(triple("a ?? d")));
    }

    @Test
    public void testContains_PO() {
        assertFalse(sut.contains(triple("?? A a")));

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

        assertTrue(sut.contains(Triple.createMatch(null, aAa.getPredicate(), aAa.getObject())));

        assertTrue(sut.contains(Triple.createMatch(null, bAa.getPredicate(), bAa.getObject())));

        assertTrue(sut.contains(Triple.createMatch(null, cBa.getPredicate(), cBa.getObject())));

        assertFalse(sut.contains(triple("?? C a")));

        assertFalse(sut.contains(triple("?? A d")));

        assertFalse(sut.contains(triple("?? D d")));
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
