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

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestResourceMethods extends AbstractModelTestBase {
    protected Resource r;

    protected final String lang = "en";

    protected Literal tvLiteral;

    protected Resource tvResource;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        tvLiteral = model.createLiteral("test 12 string 2");
        tvResource = model.createResource();
        r = model.createResource().addLiteral(RDF.value, AbstractModelTestBase.tvBoolean)
                 .addLiteral(RDF.value, AbstractModelTestBase.tvByte).addLiteral(RDF.value, AbstractModelTestBase.tvShort)
                 .addLiteral(RDF.value, AbstractModelTestBase.tvInt).addLiteral(RDF.value, AbstractModelTestBase.tvLong)
                 .addLiteral(RDF.value, AbstractModelTestBase.tvChar).addLiteral(RDF.value, AbstractModelTestBase.tvFloat)
                 .addLiteral(RDF.value, AbstractModelTestBase.tvDouble).addProperty(RDF.value, AbstractModelTestBase.tvString)
                 .addProperty(RDF.value, AbstractModelTestBase.tvString, lang).addLiteral(RDF.value, AbstractModelTestBase.tvObject)
                 .addProperty(RDF.value, tvLiteral).addProperty(RDF.value, tvResource);
    }

    @Test
    public void testAllSubjectsCorrect() {
        testHasSubjectR(model.listStatements());
        testHasSubjectR(r.listProperties());
    }

    @Test
    public void testBoolean() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvBoolean));
    }

    @Test
    public void testByte() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvByte));
    }

    @Test
    public void testChar() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvChar));
    }

    @Test
    public void testCorrectSubject() {
        assertEquals(r, r.getRequiredProperty(RDF.value).getSubject());
    }

    @Test
    public void testCountsCorrect() {
        assertEquals(13, Iter.toList(model.listStatements()).size());
        assertEquals(13, Iter.toList(r.listProperties(RDF.value)).size());
        assertEquals(0, Iter.toList(r.listProperties(RDF.type)).size());
    }

    @Test
    public void testDouble() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvDouble));
    }

    @Test
    public void testFloat() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvFloat));
    }

    protected void testHasSubjectR(final StmtIterator it) {
        while (it.hasNext()) {
            assertEquals(r, it.nextStatement().getSubject());
        }
    }

    @Test
    public void testInt() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvInt));
    }

    @Test
    public void testLiteral() {
        assertTrue(r.hasProperty(RDF.value, tvLiteral));
    }

    @Test
    public void testLong() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvLong));
    }

    @Test
    public void testNoSuchPropertyException() {
        try {
            r.getRequiredProperty(RDF.type);
            fail("missing property should throw exception");
        } catch (final PropertyNotFoundException e) {
            JenaTestLib.pass();
        }
    }

    @Test
    public void testNoSuchPropertyNull() {
        assertNull(r.getProperty(RDF.type));
    }

    @Test
    public void testObject() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvObject));
    }

    @Test
    public void testRemoveProperties() {
        r.removeProperties();
        assertEquals(false, model.listStatements(r, null, (RDFNode)null).hasNext());
    }

    @Test
    public void testResource() {
        assertTrue(r.hasProperty(RDF.value, tvResource));
    }

    @Test
    public void testShort() {
        assertTrue(r.hasLiteral(RDF.value, AbstractModelTestBase.tvShort));
    }

    @Test
    public void testString() {
        assertTrue(r.hasProperty(RDF.value, AbstractModelTestBase.tvString));
    }

    @Test
    public void testStringWithLanguage() {
        assertTrue(r.hasProperty(RDF.value, AbstractModelTestBase.tvString, lang));
    }
}
