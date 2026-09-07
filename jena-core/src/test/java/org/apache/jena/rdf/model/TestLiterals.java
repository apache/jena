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

import org.apache.jena.test.JenaTestLib;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestLiterals extends AbstractModelTestBase {

    protected void assertInRange(final long min, final long x, final long max) {
        if ( (min <= x) && (x <= max) ) {
            return;
        } else {
            fail("outside range: " + x + " min: " + min + " max: " + max);
        }
    }

    protected void assertOutsideRange(final long min, final long x, final long max) {
        if ( (min <= x) && (x <= max) ) {
            fail("inside range: " + x + " min: " + min + " max: " + max);
        }
    }

    @Test
    public void testBooleans() {
        assertTrue(model.createTypedLiteral(true).getBoolean());
        assertFalse(model.createTypedLiteral(false).getBoolean());
    }

    protected void testByte(final Model model, final byte tv) {
        final Literal l = model.createTypedLiteral(tv);
        assertEquals(tv, l.getByte());
        assertEquals(tv, l.getShort());
        assertEquals(tv, l.getInt());
        assertEquals(tv, l.getLong());
    }

    @Test
    public void testByteLiterals() {
        testByte(model, (byte)0);
        testByte(model, (byte)-1);
        testByte(model, Byte.MIN_VALUE);
        testByte(model, Byte.MAX_VALUE);
    }

    protected void testCharacter(final Model model, final char tv) {
        assertEquals(tv, model.createTypedLiteral(tv).getChar());
    }

    @Test
    public void testCharacterLiterals() {
        testCharacter(model, 'A');
        testCharacter(model, 'a');
        testCharacter(model, '#');
        testCharacter(model, '@');
        testCharacter(model, '0');
        testCharacter(model, '9');
        testCharacter(model, '\u1234');
        testCharacter(model, '\u5678');
    }

    protected void testDouble(final Model model, final double tv) {
        assertEquals(tv, model.createTypedLiteral(tv).getDouble(), AbstractModelTestBase.dDelta);
    }

    @Test
    public void testDoubleLiterals() {
        testDouble(model, 0.0);
        testDouble(model, 1.0);
        testDouble(model, -1.0);
        testDouble(model, 12345.678901);
        testDouble(model, Double.MIN_VALUE);
        testDouble(model, Double.MAX_VALUE);
    }

    protected void testFloat(final Model model, final float tv) {
        assertEquals(tv, model.createTypedLiteral(tv).getFloat(), AbstractModelTestBase.fDelta);
    }

    @Test
    public void testFloatLiterals() {
        testFloat(model, 0.0f);
        testFloat(model, 1.0f);
        testFloat(model, -1.0f);
        testFloat(model, 12345.6789f);
        testFloat(model, Float.MIN_VALUE);
        testFloat(model, Float.MAX_VALUE);
    }

    // public void testLiteralObjects()
    // {
    // // testLiteralObject( model, 0 );
    // // testLiteralObject( model, 12345 );
    // // testLiteralObject( model, -67890 );
    // }

    protected void testInt(final Model model, final int tv) {
        final Literal l = model.createTypedLiteral(tv);
        try {
            assertEquals(tv, l.getByte());
            assertInRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
        } catch (final IllegalArgumentException e) {
            assertOutsideRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
        }
        try {
            assertEquals(tv, l.getShort());
            assertInRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
        } catch (final IllegalArgumentException e) {
            assertOutsideRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
        }
        assertEquals(tv, l.getInt());
        assertEquals(tv, l.getLong());
    }

    @Test
    public void testIntLiterals() {
        testInt(model, 0);
        testInt(model, -1);
        testInt(model, Integer.MIN_VALUE);
        testInt(model, Integer.MAX_VALUE);
    }

    protected void testLanguagedString(final Model model, final String tv, final String lang) {
        final Literal l = model.createLiteral(tv, lang);
        assertEquals(tv, l.getString());
        assertEquals(tv, l.getLexicalForm());
        assertEquals(lang, l.getLanguage());
    }

    @Test
    public void testLanguagedStringLiterals() {
        testLanguagedString(model, "", "en");
        testLanguagedString(model, "chat", "fr");
    }

    protected void testLong(final Model model, final long tv) {
        final Literal l = model.createTypedLiteral(tv);
        try {
            assertEquals(tv, l.getByte());
            assertInRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
        } catch (final IllegalArgumentException e) {
            assertOutsideRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
        }
        try {
            assertEquals(tv, l.getShort());
            assertInRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
        } catch (final IllegalArgumentException e) {
            assertOutsideRange(Short.MIN_VALUE, tv, Short.MAX_VALUE);
        }
        try {
            assertEquals(tv, l.getInt());
            assertInRange(Integer.MIN_VALUE, tv, Integer.MAX_VALUE);
        } catch (final IllegalArgumentException e) {
            assertOutsideRange(Integer.MIN_VALUE, tv, Integer.MAX_VALUE);
        }
        assertEquals(tv, l.getLong());
    }

    @Test
    public void testLongLiterals() {
        testLong(model, 0);
        testLong(model, -1);
        testLong(model, Long.MIN_VALUE);
        testLong(model, Long.MAX_VALUE);
    }

    protected void testPlainString(final Model model, final String tv) {
        final Literal l = model.createLiteral(tv);
        assertEquals(tv, l.getString());
        assertEquals(tv, l.getLexicalForm());
        assertEquals("", l.getLanguage());
    }

    @Test
    public void testPlainStringLiterals() {
        testPlainString(model, "");
        testPlainString(model, "A test string");
        testPlainString(model, "Another test string");
    }

    protected void testShort(final Model model, final short tv) {
        final Literal l = model.createTypedLiteral(tv);
        try {
            assertEquals(tv, l.getByte());
            assertInRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
        } catch (final IllegalArgumentException e) {
            assertOutsideRange(Byte.MIN_VALUE, tv, Byte.MAX_VALUE);
        }
        assertEquals(tv, l.getShort());
        assertEquals(tv, l.getInt());
        assertEquals(tv, l.getLong());
    }

    @Test
    public void testShortLiterals() {
        testShort(model, (short)0);
        testShort(model, (short)-1);
        testShort(model, Short.MIN_VALUE);
        testShort(model, Short.MAX_VALUE);
    }

    @Test
    public void testStringLiteralEquality() {
        assertEquals(model.createLiteral("A"), model.createLiteral("A"));
        assertEquals(model.createLiteral("Alpha"), model.createLiteral("Alpha"));
        JenaTestLib.assertDiffer(model.createLiteral("Alpha"), model.createLiteral("Beta"));
        JenaTestLib.assertDiffer(model.createLiteral("A", "en"), model.createLiteral("A"));
        JenaTestLib.assertDiffer(model.createLiteral("A"), model.createLiteral("A", "en"));
        JenaTestLib.assertDiffer(model.createLiteral("A", "en"), model.createLiteral("A", "fr"));
        assertEquals(model.createLiteral("A", "en"), model.createLiteral("A", "en"));
    }

    // protected void testLiteralObject( Model model, int x )
    // {
    // LitTestObj tv = new LitTestObj( x );
    // LitTestObjF factory = new LitTestObjF();
    // assertEquals(tv, model.createTypedLiteral( tv ).getObject( factory ) );
    // }
}
