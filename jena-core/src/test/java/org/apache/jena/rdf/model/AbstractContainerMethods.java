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

import org.apache.jena.vocabulary.RDF;

public abstract class AbstractContainerMethods extends AbstractModelTestBase {

    protected Resource resource;

    protected abstract Container createContainer();

    protected abstract Resource getContainerType();

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        resource = model.createResource();
    }

    @Test
    public void testContainerOfIntegers() {
        final int num = 10;
        final Container c = createContainer();
        for ( int i = 0; i < num; i += 1 ) {
            c.add(i);
        }
        assertEquals(num, c.size());
        final NodeIterator it = c.iterator();
        for ( int i = 0; i < num; i += 1 ) {
            assertEquals(i, ((Literal)it.nextNode()).getInt());
        }
        assertFalse(it.hasNext());
    }

    @Test
    public void testContainerOfIntegersRemovingA() {
        final boolean[] retain = {true, true, true, false, false, false, false, false, true, true};
        testContainerOfIntegersWithRemoving(retain);
    }

    @Test
    public void testContainerOfIntegersRemovingB() {
        final boolean[] retain = {false, true, true, false, false, false, false, false, true, false};
        testContainerOfIntegersWithRemoving(retain);
    }

    @Test
    public void testContainerOfIntegersRemovingC() {
        final boolean[] retain = {false, false, false, false, false, false, false, false, false, false};
        testContainerOfIntegersWithRemoving(retain);
    }

    protected void testContainerOfIntegersWithRemoving(final boolean[] retain) {
        final int num = retain.length;
        final boolean[] found = new boolean[num];
        final Container c = createContainer();
        for ( int i = 0; i < num; i += 1 ) {
            c.add(i);
        }
        final NodeIterator it = c.iterator();
        for ( boolean aRetain : retain ) {
            it.nextNode();
            if ( aRetain == false ) {
                it.remove();
            }
        }
        final NodeIterator s = c.iterator();
        while (s.hasNext()) {
            final int v = ((Literal)s.nextNode()).getInt();
            assertFalse(found[v]);
            found[v] = true;
        }
        for ( int i = 0; i < num; i += 1 ) {
            assertEquals(retain[i], found[i], "element " + i);
        }
    }

    @Test
    public void testEmptyContainer() {
        final Container c = createContainer();
        assertTrue(model.contains(c, RDF.type, getContainerType()));
        assertEquals(0, c.size());
        assertFalse(c.contains(AbstractModelTestBase.tvBoolean));
        assertFalse(c.contains(AbstractModelTestBase.tvByte));
        assertFalse(c.contains(AbstractModelTestBase.tvShort));
        assertFalse(c.contains(AbstractModelTestBase.tvInt));
        assertFalse(c.contains(AbstractModelTestBase.tvLong));
        assertFalse(c.contains(AbstractModelTestBase.tvChar));
        assertFalse(c.contains(AbstractModelTestBase.tvFloat));
        assertFalse(c.contains(AbstractModelTestBase.tvString));
    }

    @Test
    public void testFillingContainer() {
        final Container c = createContainer();
        final String lang = "fr";
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        // Resource tvResObj = model.createResource( new ResTestObjF() );
        c.add(AbstractModelTestBase.tvBoolean);
        assertTrue(c.contains(AbstractModelTestBase.tvBoolean));
        c.add(AbstractModelTestBase.tvByte);
        assertTrue(c.contains(AbstractModelTestBase.tvByte));
        c.add(AbstractModelTestBase.tvShort);
        assertTrue(c.contains(AbstractModelTestBase.tvShort));
        c.add(AbstractModelTestBase.tvInt);
        assertTrue(c.contains(AbstractModelTestBase.tvInt));
        c.add(AbstractModelTestBase.tvLong);
        assertTrue(c.contains(AbstractModelTestBase.tvLong));
        c.add(AbstractModelTestBase.tvChar);
        assertTrue(c.contains(AbstractModelTestBase.tvChar));
        c.add(AbstractModelTestBase.tvFloat);
        assertTrue(c.contains(AbstractModelTestBase.tvFloat));
        c.add(AbstractModelTestBase.tvString);
        assertTrue(c.contains(AbstractModelTestBase.tvString));
        c.add(AbstractModelTestBase.tvString, lang);
        assertTrue(c.contains(AbstractModelTestBase.tvString, lang));
        c.add(tvLiteral);
        assertTrue(c.contains(tvLiteral));
        // c.add( tvResObj ); assertTrue(c.contains( tvResObj ) );
        c.add(AbstractModelTestBase.tvLitObj);
        assertTrue(c.contains(AbstractModelTestBase.tvLitObj));
        assertEquals(11, c.size());
    }
}
