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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.helpers.ModelHelper;

/**
 * Tests of the Model-level bulk update API.
 */

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestModelBulkUpdate extends AbstractModelTestBase {

    @Test
    public void testBulkByModel() {
        assertEquals(0, model.size(), "precondition: model must be empty");
        final Model A = modelWithStatements("clouds offer rain; trees offer shelter");
        final Model B = modelWithStatements("x R y; y Q z; z P x");
        model.add(A);
        ModelHelper.assertIsoModels(A, model);
        model.add(B);
        model.remove(A);
        ModelHelper.assertIsoModels(B, model);
        model.remove(B);
        assertEquals(0, model.size(), "");
    }

    @Test
    public void testBulkRemoveSelf() {
        final Model m = modelWithStatements("they sing together; he sings alone");
        m.remove(m);
        assertEquals(0, m.size(), "");
    }

    public void testContains(final Model m, final List<Statement> statements) {
        for ( Statement statement : statements ) {
            assertTrue(m.contains(statement), "it should be here");
        }
    }

    public void testContains(final Model m, final Statement[] statements) {
        for ( final Statement statement : statements ) {
            assertTrue(m.contains(statement), "it should be here");
        }
    }

    @Test
    public void testMBU() {
        final Statement[] sArray = ModelHelper.statements(model, "moon orbits earth; earth orbits sun");
        final List<Statement> sList = Arrays.asList(ModelHelper.statements(model, "I drink tea; you drink coffee"));
        model.add(sArray);
        testContains(model, sArray);
        model.add(sList);
        testContains(model, sList);
        testContains(model, sArray);
        /* */
        model.remove(sArray);
        testOmits(model, sArray);
        testContains(model, sList);
        model.remove(sList);
        testOmits(model, sArray);
        testOmits(model, sList);
    }

    public void testOmits(final Model m, final List<Statement> statements) {
        for ( Statement statement : statements ) {
            assertFalse(m.contains(statement), "it should not be here");
        }
    }

    public void testOmits(final Model m, final Statement[] statements) {
        for ( final Statement statement : statements ) {
            assertFalse(m.contains(statement), "it should not be here");
        }
    }
}
