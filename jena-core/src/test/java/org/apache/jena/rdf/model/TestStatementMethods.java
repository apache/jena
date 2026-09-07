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

import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestStatementMethods extends AbstractModelTestBase {

    protected Resource r;

    protected void checkChangedStatementSP(final Statement changed) {
        assertEquals(r, changed.getSubject());
        assertEquals(RDF.value, changed.getPredicate());
    }

    protected void checkCorrectStatements(final Statement sTrue, final Statement changed) {
        assertFalse(model.contains(sTrue));
        assertFalse(model.containsLiteral(r, RDF.value, true));
        assertTrue(model.contains(changed));
    }

    protected Statement loadInitialStatement() {
        final Statement sTrue = model.createLiteralStatement(r, RDF.value, true);
        model.add(sTrue);
        return sTrue;
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        r = model.createResource();
    }

    @Test
    public void testAlt() {
        final Alt tvAlt = model.createAlt();
        assertEquals(tvAlt, model.createStatement(r, RDF.value, tvAlt).getAlt());
    }

    @Test
    public void testBag() {
        final Bag tvBag = model.createBag();
        assertEquals(tvBag, model.createStatement(r, RDF.value, tvBag).getBag());
    }

    @Test
    public void testBoolean() {
        final Statement s = model.createLiteralStatement(r, RDF.value, true);
        assertEquals(model.createTypedLiteral(true), s.getObject());
        assertEquals(true, s.getBoolean());
    }

    @Test
    public void testByte() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvByte);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvByte), s.getObject());
        assertEquals(AbstractModelTestBase.tvByte, s.getLong());
    }

    @Test
    public void testChangeObjectBoolean() {
        final Statement sTrue = loadInitialStatement();
        final Statement sFalse = sTrue.changeLiteralObject(false);
        checkChangedStatementSP(sFalse);
        assertEquals(model.createTypedLiteral(false), sFalse.getObject());
        assertEquals(false, sFalse.getBoolean());
        checkCorrectStatements(sTrue, sFalse);
        assertTrue(model.containsLiteral(r, RDF.value, false));
    }

    @Test
    public void testChangeObjectByte() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvByte);
        checkChangedStatementSP(changed);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvByte), changed.getObject());
        assertEquals(AbstractModelTestBase.tvByte, changed.getByte());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvByte));
    }

    @Test
    public void testChangeObjectChar() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvChar);
        checkChangedStatementSP(changed);
        assertEquals(AbstractModelTestBase.tvChar, changed.getChar());
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvChar), changed.getObject());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvChar));
    }

    @Test
    public void testChangeObjectDouble() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvDouble);
        checkChangedStatementSP(changed);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvDouble), changed.getObject());
        assertEquals(AbstractModelTestBase.tvDouble, changed.getDouble(), AbstractModelTestBase.dDelta);
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvDouble));
    }

    @Test
    public void testChangeObjectFloat() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvFloat);
        checkChangedStatementSP(changed);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvFloat), changed.getObject());
        assertEquals(AbstractModelTestBase.tvFloat, changed.getFloat(), AbstractModelTestBase.fDelta);
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvFloat));
    }

    @Test
    public void testChangeObjectInt() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvInt);
        checkChangedStatementSP(changed);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvInt), changed.getObject());
        assertEquals(AbstractModelTestBase.tvInt, changed.getInt());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvInt));
    }

    @Test
    public void testChangeObjectLiteral() {
        final Statement sTrue = loadInitialStatement();
        model.remove(sTrue);
        assertFalse(model.contains(sTrue));
        assertFalse(model.containsLiteral(r, RDF.value, true));
    }

    // public void testResObj()
    // {
    // Resource tvResObj = model.createResource( new ResTestObjF() );
    // assertEquals(tvResObj, model.createStatement( r, RDF.value, tvResObj
    // ).getResource() );
    // }

    // public void testLitObj()
    // {
    // assertEquals(tvLitObj, model.createLiteralStatement( r, RDF.value,
    // tvLitObj ).getObject( new LitTestObjF() ) );
    // }

    @Test
    public void testChangeObjectLong() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvLong);
        checkChangedStatementSP(changed);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvLong), changed.getObject());
        assertEquals(AbstractModelTestBase.tvLong, changed.getLong());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvLong));
    }

    @Test
    public void testChangeObjectShort() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvShort);
        checkChangedStatementSP(changed);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvShort), changed.getObject());
        assertEquals(AbstractModelTestBase.tvShort, changed.getShort());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvShort));
    }

    @Test
    public void testChangeObjectString() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeObject(AbstractModelTestBase.tvString);
        checkChangedStatementSP(changed);
        assertEquals(AbstractModelTestBase.tvString, changed.getString());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.contains(r, RDF.value, AbstractModelTestBase.tvString));
    }

    @Test
    public void testChangeObjectStringWithLanguage() {
        final String lang = "en";
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeObject(AbstractModelTestBase.tvString, lang);
        checkChangedStatementSP(changed);
        assertEquals(AbstractModelTestBase.tvString, changed.getString());
        assertEquals(lang, changed.getLanguage());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.contains(r, RDF.value, AbstractModelTestBase.tvString, lang));
    }

    @Test
    public void testChangeObjectYByte() {
        final Statement sTrue = loadInitialStatement();
        final Statement changed = sTrue.changeLiteralObject(AbstractModelTestBase.tvByte);
        checkChangedStatementSP(changed);
        assertEquals(AbstractModelTestBase.tvByte, changed.getByte());
        checkCorrectStatements(sTrue, changed);
        assertTrue(model.containsLiteral(r, RDF.value, AbstractModelTestBase.tvByte));
    }

    @Test
    public void testChar() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvChar);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvChar), s.getObject());
        assertEquals(AbstractModelTestBase.tvChar, s.getChar());
    }

    @Test
    public void testDouble() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvDouble);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvDouble), s.getObject());
        assertEquals(AbstractModelTestBase.tvDouble, s.getDouble(), AbstractModelTestBase.dDelta);
    }

    @Test
    public void testFloat() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvFloat);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvFloat), s.getObject());
        assertEquals(AbstractModelTestBase.tvFloat, s.getFloat(), AbstractModelTestBase.fDelta);
    }

    @Test
    public void testGetLiteralFailure() {
        try {
            model.createStatement(r, RDF.value, r).getLiteral();
            fail("should trap non-literal object");
        } catch (final LiteralRequiredException e) {
            JenaTestLib.pass();
        }
    }

    @Test
    public void testGetResource() {
        assertEquals(r, model.createStatement(r, RDF.value, r).getResource());
    }

    @Test
    public void testGetResourceFailure() {
        try {
            model.createLiteralStatement(r, RDF.value, false).getResource();
            fail("should trap non-resource object");
        } catch (final ResourceRequiredException e) {
            JenaTestLib.pass();
        }
    }

    @Test
    public void testGetTrueBoolean() {
        assertEquals(true, model.createLiteralStatement(r, RDF.value, true).getLiteral().getBoolean());
    }

    @Test
    public void testInt() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvInt);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvInt), s.getObject());
        assertEquals(AbstractModelTestBase.tvInt, s.getInt());
    }

    // public void testChangeObjectResObject()
    // {
    // Resource tvResObj = model.createResource( new ResTestObjF() );
    // Statement sTrue = loadInitialStatement();
    // Statement changed = sTrue.changeObject( tvResObj );
    // checkChangedStatementSP( changed );
    // assertEquals(tvResObj, changed.getResource() );
    // checkCorrectStatements( sTrue, changed );
    // assertTrue(model.contains( r, RDF.value, tvResObj ) );
    // }

    @Test
    public void testLong() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvLong);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvLong), s.getObject());
        assertEquals(AbstractModelTestBase.tvLong, s.getLong());
    }

    @Test
    public void testSeq() {
        final Seq tvSeq = model.createSeq();
        assertEquals(tvSeq, model.createStatement(r, RDF.value, tvSeq).getSeq());
    }

    @Test
    public void testShort() {
        final Statement s = model.createLiteralStatement(r, RDF.value, AbstractModelTestBase.tvShort);
        assertEquals(model.createTypedLiteral(AbstractModelTestBase.tvShort), s.getObject());
        assertEquals(AbstractModelTestBase.tvShort, s.getShort());
    }

    @Test
    public void testString() {
        assertEquals(AbstractModelTestBase.tvString,
                            model.createStatement(r, RDF.value, AbstractModelTestBase.tvString).getString());
    }

    @Test
    public void testStringWithLanguage() {
        final String lang = "fr";
        assertEquals(AbstractModelTestBase.tvString,
                            model.createStatement(r, RDF.value, AbstractModelTestBase.tvString, lang).getString());
        assertEquals(lang, model.createStatement(r, RDF.value, AbstractModelTestBase.tvString, lang).getLanguage());
    }
}
