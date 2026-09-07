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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.shared.InvalidPropertyURIException;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestResources extends AbstractModelTestBase {

    protected void checkNumericContent(final Container cont2, final int num) {
        final NodeIterator nit = cont2.iterator();
        for ( int i = 0 ; i < num ; i += 1 ) {
            assertEquals(i, ((Literal)nit.nextNode()).getInt());
        }
        assertFalse(nit.hasNext());
    }

    protected void retainOnlySpecified(final Container cont2, final int num, final boolean[] retain) {
        final NodeIterator nit = cont2.iterator();
        for ( int i = 0 ; i < num ; i++ ) {
            nit.nextNode();
            if ( retain[i] == false ) {
                nit.remove();
            }
        }
        assertFalse(nit.hasNext());
    }

    protected void seeWhatsThere(final Container cont2, final boolean[] found) {
        final NodeIterator nit = cont2.iterator();
        while (nit.hasNext()) {
            final int v = ((Literal)nit.nextNode()).getInt();
            assertFalse(found[v]);
            found[v] = true;
        }
    }

    protected Set<Object> setOf(final Object x) {
        final Set<Object> result = new HashSet<>();
        result.add(x);
        return result;
    }

    private void containerTest(final Model model, final Container cont1, final Container cont2) {
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        // Resource tvResObj = model.createResource( new ResTestObjF() );
        final Object tvLitObj = new LitTestObj(1234);
        model.createBag();
        model.createAlt();
        model.createSeq();
        final String lang = "en";
        //
        assertEquals(0, cont1.size());
        assertEquals(0, cont2.size());
        //
        assertTrue(cont1.add(AbstractModelTestBase.tvBoolean).contains(AbstractModelTestBase.tvBoolean));
        assertTrue(cont1.add(AbstractModelTestBase.tvByte).contains(AbstractModelTestBase.tvByte));
        assertTrue(cont1.add(AbstractModelTestBase.tvShort).contains(AbstractModelTestBase.tvShort));
        assertTrue(cont1.add(AbstractModelTestBase.tvInt).contains(AbstractModelTestBase.tvInt));
        assertTrue(cont1.add(AbstractModelTestBase.tvLong).contains(AbstractModelTestBase.tvLong));
        assertTrue(cont1.add(AbstractModelTestBase.tvFloat).contains(AbstractModelTestBase.tvFloat));
        assertTrue(cont1.add(AbstractModelTestBase.tvDouble).contains(AbstractModelTestBase.tvDouble));
        assertTrue(cont1.add(AbstractModelTestBase.tvChar).contains(AbstractModelTestBase.tvChar));
        assertTrue(cont1.add(AbstractModelTestBase.tvString).contains(AbstractModelTestBase.tvString));
        assertFalse(cont1.contains(AbstractModelTestBase.tvString, lang));
        assertTrue(cont1.add(AbstractModelTestBase.tvString, lang).contains(AbstractModelTestBase.tvString, lang));
        assertTrue(cont1.add(tvLiteral).contains(tvLiteral));
        // assertTrue(cont1.add( tvResObj ).contains( tvResObj ) );
        assertTrue(cont1.add(tvLitObj).contains(tvLitObj));
        assertEquals(12, cont1.size());
        //
        final int num = 10;
        for ( int i = 0 ; i < num ; i += 1 ) {
            cont2.add(i);
        }
        assertEquals(num, cont2.size());
        checkNumericContent(cont2, num);
        //
        final boolean[] found = new boolean[num];
        final boolean[] retain = {true, true, true, false, false, false, false, false, true, true};
        retainOnlySpecified(cont2, num, retain);
        seeWhatsThere(cont2, found);
        for ( int i = 0 ; i < num ; i += 1 ) {
            assertEquals(retain[i], found[i], i + "th element of array");
        }
    }

    @Test
    public void testCreateAnonResource() {
        final Resource r = model.createResource();
        assertTrue(r.isAnon());
        assertNull(r.getURI());
        assertNull(r.getNameSpace());
        assertNull(r.getLocalName());
    }

    @Test
    public void testCreateAnonResourceWithNull() {
        final Resource r = model.createResource((String)null);
        assertTrue(r.isAnon());
        assertNull(r.getURI());
        assertNull(r.getNameSpace());
        assertNull(r.getLocalName());
    }

    @Test
    public void testCreateNamedResource() {
        final String uri = "http://aldabaran.hpl.hp.com/foo";
        assertEquals(uri, model.createResource(uri).getURI());
    }

    @Test
    public void testCreateNullPropertyFails() {
        try {
            model.createProperty(null);
            fail("should not create null property");
        } catch (final InvalidPropertyURIException e) {
            JenaTestLib.pass();
        }
    }

    @Test
    public void testCreatePropertyOneArg() {
        final Property p = model.createProperty("abc/def");
        assertEquals("abc/", p.getNameSpace());
        assertEquals("def", p.getLocalName());
        assertEquals("abc/def", p.getURI());
    }

    @Test
    public void testCreatePropertyStrangeURI() {
        final String uri = RDF.getURI() + "_345";
        final Property p = model.createProperty(uri);
        assertEquals(RDF.getURI(), p.getNameSpace());
        assertEquals("_345", p.getLocalName());
        assertEquals(uri, p.getURI());
    }

    @Test
    public void testCreatePropertyStrangeURITwoArgs() {
        final String local = "_345";
        final Property p = model.createProperty(RDF.getURI(), local);
        assertEquals(RDF.getURI(), p.getNameSpace());
        assertEquals(local, p.getLocalName());
        assertEquals(RDF.getURI() + local, p.getURI());
    }

    @Test
    public void testCreatePropertyTwoArgs() {
        final Property p = model.createProperty("abc/", "def");
        assertEquals("abc/", p.getNameSpace());
        assertEquals("def", p.getLocalName());
        assertEquals("abc/def", p.getURI());
    }

    @Test
    public void testCreateTypedAnonResource() {
        final Resource r = model.createResource(RDF.Property);
        assertTrue(r.isAnon());
        assertTrue(model.contains(r, RDF.type, RDF.Property));
    }

    @Test
    public void testCreateTypedNamedresource() {
        final String uri = "http://aldabaran.hpl.hp.com/foo";
        final Resource r = model.createResource(uri, RDF.Property);
        assertEquals(uri, r.getURI());
        assertTrue(model.contains(r, RDF.type, RDF.Property));
    }

    @Test
    public void testEnhancedResources() {
        final Resource r = model.createResource();
        resourceTest(model, r, 0);

        resourceTest(model, model.createBag(), 1);
        containerTest(model, model.createBag(), model.createBag());

        resourceTest(model, model.createAlt(), 1);
        containerTest(model, model.createAlt(), model.createAlt());

        resourceTest(model, model.createSeq(), 1);
        containerTest(model, model.createSeq(), model.createSeq());
    }

    private void resourceTest(final Model model, final Resource r, final int numProps) {
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        final Resource tvResource = model.createResource();
        final String lang = "fr";
        //
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvBoolean).hasLiteral(RDF.value, AbstractModelTestBase.tvBoolean));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvByte).hasLiteral(RDF.value, AbstractModelTestBase.tvByte));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvShort).hasLiteral(RDF.value, AbstractModelTestBase.tvShort));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvInt).hasLiteral(RDF.value, AbstractModelTestBase.tvInt));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvLong).hasLiteral(RDF.value, AbstractModelTestBase.tvLong));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvChar).hasLiteral(RDF.value, AbstractModelTestBase.tvChar));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvFloat).hasLiteral(RDF.value, AbstractModelTestBase.tvFloat));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvDouble).hasLiteral(RDF.value, AbstractModelTestBase.tvDouble));
        assertTrue(r.addProperty(RDF.value, AbstractModelTestBase.tvString).hasProperty(RDF.value, AbstractModelTestBase.tvString));
        assertTrue(r.addProperty(RDF.value, AbstractModelTestBase.tvString, lang).hasProperty(RDF.value,
                                                                                                     AbstractModelTestBase.tvString, lang));
        assertTrue(r.addLiteral(RDF.value, AbstractModelTestBase.tvObject).hasLiteral(RDF.value, AbstractModelTestBase.tvObject));
        assertTrue(r.addProperty(RDF.value, tvLiteral).hasProperty(RDF.value, tvLiteral));
        assertTrue(r.addProperty(RDF.value, tvResource).hasProperty(RDF.value, tvResource));
        assertTrue(r.getRequiredProperty(RDF.value).getSubject().equals(r));
        //
        final Property p = model.createProperty("foo/", "bar");
        try {
            r.getRequiredProperty(p);
            fail("should detect missing property");
        } catch (final PropertyNotFoundException e) {
            JenaTestLib.pass();
        }
        //
        assertEquals(13, Iter.toSet(r.listProperties(RDF.value)).size());
        assertEquals(setOf(r), Iter.toSet(r.listProperties(RDF.value).mapWith(Statement::getSubject)));
        //
        assertEquals(0, Iter.toSet(r.listProperties(p)).size());
        assertEquals(new HashSet<Resource>(), Iter.toSet(r.listProperties(p).mapWith(Statement::getSubject)));
        //
        assertEquals(13 + numProps, Iter.toSet(r.listProperties()).size());
        assertEquals(setOf(r), Iter.toSet(r.listProperties().mapWith(Statement::getSubject)));
        //
        r.removeProperties();
        assertEquals(0, r.listProperties().toList().size());
    }
}
