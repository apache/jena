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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.rdf.model.helpers.ModelHelper;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestLiteralsInModel extends AbstractModelTestBase {
    private Resource X;
    private Property P;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        X = ModelHelper.resource("X");
        P = ModelHelper.property("P");
    }

    @Test
    public void testAddWithBooleanObject() {
        model.addLiteral(X, P, true);
        assertTrue(model.contains(X, P, model.createTypedLiteral(true)));
        assertTrue(model.containsLiteral(X, P, true));
    }

    @Test
    public void testAddWithCharObject() {
        model.addLiteral(X, P, 'x');
        assertTrue(model.contains(X, P, model.createTypedLiteral('x')));
        assertTrue(model.containsLiteral(X, P, 'x'));
    }

    @Test
    public void testAddWithDoubleObject() {
        model.addLiteral(X, P, 14.0d);
        assertTrue(model.contains(X, P, model.createTypedLiteral(14.0d)));
        assertTrue(model.containsLiteral(X, P, 14.0d));
    }

    @Test
    public void testAddWithFloatObject() {
        model.addLiteral(X, P, 14.0f);
        assertTrue(model.contains(X, P, model.createTypedLiteral(14.0f)));
        assertTrue(model.containsLiteral(X, P, 14.0f));
    }

    @Test
    public void testAddWithIntObject() {
        model.addLiteral(X, P, 99);
        assertTrue(model.contains(X, P, model.createTypedLiteral(99)));
        assertTrue(model.containsLiteral(X, P, 99));
    }

    @Test
    public void testAddWithLiteralObject() {
        final Literal lit = model.createLiteral("spoo");
        model.addLiteral(X, P, lit);
        assertTrue(model.contains(X, P, lit));
        assertTrue(model.containsLiteral(X, P, lit));
    }

    @Test
    public void testAddWithLongObject() {
        model.addLiteral(X, P, 99L);
        assertTrue(model.contains(X, P, model.createTypedLiteral(99L)));
        assertTrue(model.containsLiteral(X, P, 99L));
    }

    // that version of addLiteral is deprecated; test removed.
    // public void testAddWithAnObject()
    // {
    // Object z = new Date();
    // model.addLiteral( X, P, z );
    // assertTrue(model.contains( X, P, model.createTypedLiteral( z ) ) );
    // assertTrue(model.containsLiteral( X, P, z ) );
    // }
}
