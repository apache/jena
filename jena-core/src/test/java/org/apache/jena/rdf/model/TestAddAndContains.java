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

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestAddAndContains extends AbstractModelTestBase {

    protected Resource S;

    protected Property P;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        S = model.createResource("http://nowhere.man/subject");
        P = model.createProperty("http://nowhere.man/predicate");
    }

    @Override
    @AfterEach
    public void tearDown() {
        S = null;
        P = null;
        super.tearDown();
    }

    @Test
    public void testAddContainLiteralByStatement() {
        final Literal L = model.createTypedLiteral(210);
        final Statement s = model.createStatement(S, RDF.value, L);
        assertTrue(model.add(s).contains(s));
        assertTrue(model.contains(S, RDF.value));
    }

    @Test
    public void testAddContainsBoolean() {
        model.addLiteral(S, P, AbstractModelTestBase.tvBoolean);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvBoolean));
    }

    @Test
    public void testAddContainsByte() {
        model.addLiteral(S, P, AbstractModelTestBase.tvByte);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvByte));
    }

    @Test
    public void testAddContainsChar() {
        model.addLiteral(S, P, AbstractModelTestBase.tvChar);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvChar));
    }

    @Test
    public void testAddContainsDouble() {
        model.addLiteral(S, P, AbstractModelTestBase.tvDouble);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvDouble));
    }

    @Test
    public void testAddContainsFloat() {
        model.addLiteral(S, P, AbstractModelTestBase.tvFloat);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvFloat));
    }

    @Test
    public void testAddContainsInt() {
        model.addLiteral(S, P, AbstractModelTestBase.tvInt);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvInt));
    }

    @Test
    public void testAddContainsLanguagedString() {
        model.add(S, P, "test string", "en");
        assertFalse(model.contains(S, P, "test string"));
        assertTrue(model.contains(S, P, "test string", "en"));
    }

    @Test
    public void testAddContainsLong() {
        model.addLiteral(S, P, AbstractModelTestBase.tvLong);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvLong));
    }

    @Test
    public void testAddContainsPlainString() {
        model.add(S, P, "test string");
        assertTrue(model.contains(S, P, "test string"));
        assertFalse(model.contains(S, P, "test string", "en"));
    }

    // public void testAddContainsObject()
    // {
    // LitTestObj O = new LitTestObj( 12345 );
    // model.addLiteral( S, P, O );
    // assertTrue(model.containsLiteral( S, P, O ) );
    // }

    @Test
    public void testAddContainsResource() {
        final Resource r = model.createResource();
        model.add(S, P, r);
        assertTrue(model.contains(S, P, r));
    }

    @Test
    public void testAddContainsShort() {
        model.addLiteral(S, P, AbstractModelTestBase.tvShort);
        assertTrue(model.containsLiteral(S, P, AbstractModelTestBase.tvShort));
    }

    @Test
    public void testAddDuplicateLeavesSizeSame() {
        final Statement s = model.createStatement(S, RDF.value, "something");
        model.add(s);
        final long size = model.size();
        model.add(s);
        assertEquals(size, model.size());
    }

    @Test
    public void testEmpty() {
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvBoolean));
        assertFalse(model.contains(S, P, model.createResource()));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvByte));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvShort));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvInt));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvLong));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvChar));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvFloat));
        assertFalse(model.containsLiteral(S, P, AbstractModelTestBase.tvDouble));
        assertFalse(model.containsLiteral(S, P, new LitTestObj(12345)));
        assertFalse(model.contains(S, P, "test string"));
        assertFalse(model.contains(S, P, "test string", "en"));
    }

}
