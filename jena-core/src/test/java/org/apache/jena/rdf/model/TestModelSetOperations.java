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

import org.apache.jena.rdf.model.helpers.ModelHelper;

/**
 * A revamped version of the regression set-operation tests.
 */
@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestModelSetOperations extends AbstractModelTestBase {
    private Model model2;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        model2 = createModel();
    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
        model2.close();
    }

    @Test
    public void testDifference() {
        ModelHelper.modelAdd(model, "a P b; w R x");
        ModelHelper.modelAdd(model2, "w R x; y S z");
        final Model dm = model.difference(model2);
        for ( final StmtIterator it = dm.listStatements() ; it.hasNext() ; ) {
            final Statement s = it.nextStatement();
            assertTrue(model.contains(s) && !model2.contains(s));
        }
        for ( final StmtIterator it = model.union(model2).listStatements() ; it.hasNext() ; ) {
            final Statement s = it.nextStatement();
            assertEquals(model.contains(s) && !model2.contains(s), dm.contains(s));
        }
        assertTrue(dm.containsAny(model));
        assertTrue(dm.containsAny(model.listStatements()));
        assertFalse(dm.containsAny(model2));
        assertFalse(dm.containsAny(model2.listStatements()));
        assertTrue(model.containsAll(dm));

    }

    @Test
    public void testIntersection() {

        ModelHelper.modelAdd(model, "a P b; w R x");
        ModelHelper.modelAdd(model2, "w R x; y S z");
        final Model im = model.intersection(model2);
        assertFalse(model.containsAll(model2));
        assertFalse(model2.containsAll(model));
        assertTrue(model.containsAll(im));
        assertTrue(model2.containsAll(im));
        for ( final StmtIterator it = im.listStatements() ; it.hasNext() ; ) {
            final Statement s = it.nextStatement();
            assertTrue(model.contains(s) && model2.contains(s));
        }
        for ( final StmtIterator it = im.listStatements() ; it.hasNext() ; ) {
            assertTrue(model.contains(it.nextStatement()));
        }
        for ( final StmtIterator it = im.listStatements() ; it.hasNext() ; ) {
            assertTrue(model2.contains(it.nextStatement()));
        }
        assertTrue(model.containsAll(im.listStatements()));
        assertTrue(model2.containsAll(im.listStatements()));

    }

    @Test
    public void testUnion() {

        ModelHelper.modelAdd(model, "a P b; w R x");
        ModelHelper.modelAdd(model2, "w R x; y S z");
        final Model um = model.union(model2);
        assertFalse(model.containsAll(model2));
        assertFalse(model2.containsAll(model));
        assertTrue(um.containsAll(model));
        assertTrue(um.containsAll(model2));
        for ( final StmtIterator it = um.listStatements() ; it.hasNext() ; ) {
            final Statement s = it.nextStatement();
            assertTrue(model.contains(s) || model2.contains(s));
        }
        for ( final StmtIterator it = model.listStatements() ; it.hasNext() ; ) {
            assertTrue(um.contains(it.nextStatement()));
        }
        for ( final StmtIterator it = model2.listStatements() ; it.hasNext() ; ) {
            assertTrue(um.contains(it.nextStatement()));
        }
        assertTrue(um.containsAll(model.listStatements()));
        assertTrue(um.containsAll(model2.listStatements()));

    }
}
