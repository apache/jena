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
public class TestGetFromModel extends AbstractModelTestBase {

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
    public void testGetAlt() {
        final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 160;
        model.createAlt(uri);
        final Alt a = model.getAlt(uri);
        assertEquals(uri, a.getURI());
        assertTrue(model.contains(a, RDF.type, RDF.Alt));
    }

    // public void testGetResourceFactory()
    // {
    // String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 120;
    // Resource r = model.getResource( uri, new ResTestObjF() );
    // assertEquals(uri, r.getURI() );
    // }

    @Test
    public void testGetBag() {
        final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 150;
        model.createBag(uri);
        final Bag b = model.getBag(uri);
        assertEquals(uri, b.getURI());
        assertTrue(model.contains(b, RDF.type, RDF.Bag));
    }

    @Test
    public void testGetPropertyOneArg() {
        final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 130;
        final Property p = model.getProperty(uri);
        assertEquals(uri, p.getURI());
    }

    @Test
    public void testGetPropertyTwoArgs() {
        final String ns = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 140 + "/";
        final Property p = model.getProperty(ns, "foo");
        assertEquals(ns + "foo", p.getURI());
    }

    @Test
    public void testGetResource() {
        final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/a" + 110;
        final Resource r = model.getResource(uri);
        assertEquals(uri, r.getURI());
    }

    @Test
    public void testGetSeq() {
        final String uri = "http://aldabaran.hpl.hp.com/rdf/test4/" + 170;
        model.createSeq(uri);
        final Seq s = model.getSeq(uri);
        assertEquals(uri, s.getURI());
        assertTrue(model.contains(s, RDF.type, RDF.Seq));
    }
}
