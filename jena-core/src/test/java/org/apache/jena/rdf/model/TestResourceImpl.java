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

import org.apache.jena.rdf.model.helpers.ModelHelper;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

/**
 * TestResourceImpl - fresh tests, make sure as-ing works a bit.
 */
@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestResourceImpl extends AbstractModelTestBase {

    @Test
    public void testAddLiteralPassesLiteralUnmodified() {
        final Resource r = model.createResource();
        final Literal lit = model.createLiteral("spoo");
        r.addLiteral(RDF.value, lit);
        assertTrue(model.contains(null, RDF.value, lit), "model should contain unmodified literal");
    }

    @Test
    public void testAddTypedPropertyBoolean() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, true);
        assertEquals(model.createTypedLiteral(true), r.getProperty(RDF.value).getLiteral());
    }

    @Test
    public void testAddTypedPropertyChar() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 'x');
        assertEquals(model.createTypedLiteral('x'), r.getProperty(RDF.value).getLiteral());
    }

    @Test
    public void testAddTypedPropertyDouble() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1.0d);
        assertEquals(model.createTypedLiteral(1.0d), r.getProperty(RDF.value).getLiteral());
    }

    @Test
    public void testAddTypedPropertyFloat() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1.0f);
        assertEquals(model.createTypedLiteral(1.0f), r.getProperty(RDF.value).getLiteral());
    }

    @Test
    public void testAddTypedPropertyInt() {
        // Model model = ModelFactory.createDefaultModel();
        // Resource r = model.createResource();
        // r.addLiteral( RDF.value, 1 );
        // assertEquals(model.createTypedLiteral( 1 ), r.getProperty( RDF.value
        // ).getLiteral() );
    }

    @Test
    public void testAddTypedPropertyLong() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1L);
        assertEquals(model.createTypedLiteral(1L), r.getProperty(RDF.value).getLiteral());
    }

    @Test
    public void testAddTypedPropertyObject() {
        final Object z = new Object();
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, z);
        assertEquals(model.createTypedLiteral(z), r.getProperty(RDF.value).getLiteral());
    }

    @Test
    public void testAddTypedPropertyString() {

    }

    /**
     * Test that a literal node cannot be as'ed into a resource.
     */
    @Test
    public void testAsLiteral() {
        try {
            ModelHelper.literal(model, "17").as(Resource.class);
            fail("literals cannot be resources");
        } catch (final ResourceRequiredException e) {
            JenaTestLib.pass();
        }
    }

    /**
     * Test that a non-literal node can be as'ed into a resource
     */
    @Test
    public void testCannotAsNonLiteral() {
        ModelHelper.resource(model, "plumPie").as(Resource.class);
    }

    @Test
    public void testGetLocalNameReturnsLocalName() {
        assertEquals("xyz", ModelHelper.resource("eh:xyz").getLocalName());
    }

    @Test
    public void testGetModel() {

        assertSame(model, model.createResource("eh:/wossname").getModel());
    }

    @Test
    public void testGetPropertyResourceValueReturnsNull() {
        final Model model = modelWithStatements("x p 17");
        final Resource r = model.createResource("eh:/x");
        assertNull(r.getPropertyResourceValue(ModelHelper.property("q")));
        assertNull(r.getPropertyResourceValue(ModelHelper.property("p")));
    }

    @Test
    public void testGetPropertyResourceValueReturnsResource() {
        final Model model = modelWithStatements("x p 17; x p y");
        final Resource r = model.createResource("eh:/x");
        final Resource value = r.getPropertyResourceValue(ModelHelper.property("p"));
        assertEquals(ModelHelper.resource("y"), value);
    }

    @Test
    public void testHasTypedPropertyBoolean() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, false);
        assertTrue(r.hasLiteral(RDF.value, false));
    }

    @Test
    public void testHasTypedPropertyChar() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 'x');
        assertTrue(r.hasLiteral(RDF.value, 'x'));
    }

    @Test
    public void testHasTypedPropertyDouble() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1.0d);
        assertTrue(r.hasLiteral(RDF.value, 1.0d));
    }

    @Test
    public void testHasTypedPropertyFloat() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1.0f);
        assertTrue(r.hasLiteral(RDF.value, 1.0f));
    }

    @Test
    public void testHasTypedPropertyInt() {
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1);
        assertTrue(r.hasLiteral(RDF.value, 1));
    }

    @Test
    public void testHasTypedPropertyLong() {

        final Resource r = model.createResource();
        r.addLiteral(RDF.value, 1L);
        assertTrue(r.hasLiteral(RDF.value, 1L));
    }

    @Test
    public void testHasTypedPropertyObject() {
        final Object z = new Object();
        final Resource r = model.createResource();
        r.addLiteral(RDF.value, z);
        assertTrue(r.hasLiteral(RDF.value, z));
    }

    @Test
    public void testHasTypedPropertyString() {

    }

    @Test
    public void testHasURI() {
        assertTrue(ModelHelper.resource("eh:xyz").hasURI("eh:xyz"));
        assertFalse(ModelHelper.resource("eh:xyz").hasURI("eh:1yz"));
        assertFalse(ResourceFactory.createResource().hasURI("42"));
    }

    @Test
    public void testNameSpace() {
        assertEquals("eh:", ModelHelper.resource("eh:xyz").getNameSpace());
        assertEquals("http://d/", ModelHelper.resource("http://d/stuff").getNameSpace());
        assertEquals("ftp://dd.com/12345", ModelHelper.resource("ftp://dd.com/12345").getNameSpace());
        assertEquals("http://domain/spoo#", ModelHelper.resource("http://domain/spoo#anchor").getNameSpace());
        assertEquals("ftp://abd/def#ghi#", ModelHelper.resource("ftp://abd/def#ghi#e11-2").getNameSpace());
    }
}
