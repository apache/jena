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
package org.apache.jena.mem2.store.legacy;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.node;
import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;

public class FieldFilterTest {

    @Test
    public void filterOn_ANY_ANY() {
        final var sut = FieldFilter.filterOn(
                Triple.Field.fieldSubject, Node.ANY,
                Triple.Field.fieldObject, Node.ANY);
        assertFalse(sut.hasFilter());
        assertNull(sut.getFilter());
    }

    @Test
    public void filterOn_ConcreteNode_ANY() {
        final var sut = FieldFilter.filterOn(
                Triple.Field.fieldSubject, node("s"),
                Triple.Field.fieldObject, Node.ANY);
        assertTrue(sut.hasFilter());
        assertTrue(sut.getFilter().test(triple("s P o")));
        assertFalse(sut.getFilter().test(triple("t P o")));
    }

    @Test
    public void filterOn_ANY_ConcreteNode() {
        final var sut = FieldFilter.filterOn(
                Triple.Field.fieldSubject, Node.ANY,
                Triple.Field.fieldObject, node("o"));
        assertTrue(sut.hasFilter());
        assertTrue(sut.getFilter().test(triple("s P o")));
        assertFalse(sut.getFilter().test(triple("s P o2")));
    }

    @Test
    public void filterOn_ConcreteNode_ConcreteNode() {
        final var sut = FieldFilter.filterOn(
                Triple.Field.fieldSubject, node("s"),
                Triple.Field.fieldObject, node("o"));
        assertTrue(sut.hasFilter());
        assertTrue(sut.getFilter().test(triple("s P o")));
        assertFalse(sut.getFilter().test(triple("s P o2")));
        assertFalse(sut.getFilter().test(triple("t P o")));
    }
}