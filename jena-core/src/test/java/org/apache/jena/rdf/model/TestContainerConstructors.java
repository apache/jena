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

import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestContainerConstructors extends AbstractModelTestBase {

    @Test
    public void testCreateAnonAlt() {
        final Alt tv = model.createAlt();
        assertTrue(tv.isAnon());
        assertTrue(model.contains(tv, RDF.type, RDF.Alt));
    }

    @Test
    public void testCreateAnonBag() {
        final Bag tv = model.createBag();
        assertTrue(tv.isAnon());
        assertTrue(model.contains(tv, RDF.type, RDF.Bag));
    }

    @Test
    public void testCreateAnonSeq() {
        final Seq tv = model.createSeq();
        assertTrue(tv.isAnon());
        assertTrue(model.contains(tv, RDF.type, RDF.Seq));
    }

    @Test
    public void testCreateNamedAlt() {
        final String uri = "http://aldabaran/sirius";
        final Alt tv = model.createAlt(uri);
        assertEquals(uri, tv.getURI());
        assertTrue(model.contains(tv, RDF.type, RDF.Alt));
    }

    @Test
    public void testCreateNamedBag() {
        final String uri = "http://aldabaran/foo";
        final Bag tv = model.createBag(uri);
        assertEquals(uri, tv.getURI());
        assertTrue(model.contains(tv, RDF.type, RDF.Bag));
    }

    @Test
    public void testCreateNamedSeq() {
        final String uri = "http://aldabaran/andromeda";
        final Seq tv = model.createSeq(uri);
        assertEquals(uri, tv.getURI());
        assertTrue(model.contains(tv, RDF.type, RDF.Seq));
    }
}
