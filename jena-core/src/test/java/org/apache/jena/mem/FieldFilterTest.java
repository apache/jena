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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.junit.Test;

import static org.apache.jena.graph.test.GraphTestBase.node;
import static org.apache.jena.graph.test.GraphTestBase.triple;
import static org.junit.Assert.*;

public class FieldFilterTest {

    @Test
    public void filterOnTwoNodes() {
        var sut = FieldFilter.filterOn(Triple.Field.fieldSubject, node("a"), Triple.Field.fieldPredicate, node("P"));
        assertTrue(sut.hasFilter());
        var filter = sut.getFilter();
        assertTrue(filter.test(triple( "a P b" )));
        assertFalse(filter.test(triple( "c P b" )));
        assertFalse(filter.test(triple( "a Q b" )));
    }

    @Test
    public void filterOnFirstNode() {
        var sut = FieldFilter.filterOn(Triple.Field.fieldSubject, node("a"), Triple.Field.fieldPredicate, Node.ANY);
        assertTrue(sut.hasFilter());
        var filter = sut.getFilter();
        assertTrue(filter.test(triple( "a P b" )));
        assertFalse(filter.test(triple( "c P b" )));
        assertTrue(filter.test(triple( "a Q b" )));
    }

    @Test
    public void filterOnSecondNode() {
        var sut = FieldFilter.filterOn(Triple.Field.fieldSubject, Node.ANY, Triple.Field.fieldPredicate, node("P"));
        assertTrue(sut.hasFilter());
        var filter = sut.getFilter();
        assertTrue(filter.test(triple( "a P b" )));
        assertTrue(filter.test(triple( "c P b" )));
        assertFalse(filter.test(triple( "a Q b" )));
    }

    @Test
    public void filterOnAny() {
        var sut = FieldFilter.filterOn(Triple.Field.fieldSubject, Node.ANY, Triple.Field.fieldPredicate, Node.ANY);
        assertFalse(sut.hasFilter());
        assertNull(sut.getFilter());
    }
}