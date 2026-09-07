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
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestAddModel extends AbstractModelTestBase {
    private Model model2;

    protected void assertContainsAll(final Model model, final Model model2) {
        for ( final StmtIterator s = model2.listStatements() ; s.hasNext() ; ) {
            assertTrue(model.contains(s.nextStatement()));
        }
    }

    protected void assertSameStatements(final Model model, final Model model2) {
        assertContainsAll(model, model2);
        assertContainsAll(model2, model);
    }

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
    public void testAddByIterator() {

        ModelHelper.modelAdd(model, "a P b; c P d; x Q 1; y Q 2");
        model2.add(model.listStatements());
        assertEquals(model.size(), model2.size());
        assertSameStatements(model, model2);
        model.add(model.createResource(), RDF.value, model.createResource());
        model.add(model.createResource(), RDF.value, model.createResource());
        model.add(model.createResource(), RDF.value, model.createResource());
        final StmtIterator s = model.listStatements();
        model2.remove(s.nextStatement()).remove(s);
        assertEquals(0, model2.size());
    }

    @Test
    public void testAddByModel() {

        ModelHelper.modelAdd(model, "a P b; c P d; x Q 1; y Q 2");
        model2.add(model);
        assertEquals(model.size(), model2.size());
        assertSameStatements(model, model2);
    }

    @Test
    public void testRemoveByModel() {

        ModelHelper.modelAdd(model, "a P b; c P d; x Q 1; y Q 2");
        model2.add(model).remove(model);
        assertEquals(0, model2.size());
        assertFalse(model2.listStatements().hasNext());
    }
}
