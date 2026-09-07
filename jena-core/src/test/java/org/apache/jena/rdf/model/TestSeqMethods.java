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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestSeqMethods extends AbstractContainerMethods {

    protected LitTestObj aLitTestObj;

    protected Literal tvLiteral;

    protected Resource tvResource;

    // protected Resource tvResObj;
    protected Object anObject;

    protected Bag tvBag;
    protected Alt tvAlt;
    protected Seq tvSeq;
    protected static final String lang = "fr";
    protected static final int num = 10;

    protected boolean[] bools(final String s) {
        final boolean[] result = new boolean[s.length()];
        for ( int i = 0 ; i < s.length() ; i += 1 ) {
            result[i] = s.charAt(i) == 't';
        }
        return result;
    }

    @Override
    protected Container createContainer() {
        return model.createSeq();
    }

    public void error(final String test, final int n) {
        fail(test + " -- " + n);
    }

    @Override
    protected Resource getContainerType() {
        return RDF.Seq;
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        aLitTestObj = new LitTestObj(12345);
        tvLiteral = model.createLiteral("test 12 string 2");
        tvResource = model.createResource();
        // tvResObj = model.createResource( new ResTestObjF() );
        anObject = new LitTestObj(1234);
        tvBag = model.createBag();
        tvAlt = model.createAlt();
        tvSeq = model.createSeq();
    }

    @Test
    public void testMoreIndexing() {
        final int num = 10;
        final Seq seq = model.createSeq();
        for ( int i = 0 ; i < num ; i += 1 ) {
            seq.add(i);
        }

        try {
            seq.add(0, false);
            fail("cannot at at position 0");
        } catch (final SeqIndexBoundsException e) {
            JenaTestLib.pass();
        }

        seq.add(num + 1, false);
        assertEquals(num + 1, seq.size());

        seq.remove(num + 1);
        try {
            seq.add(num + 2, false);
            fail("cannot add past the end");
        } catch (final SeqIndexBoundsException e) {
            JenaTestLib.pass();
        }

        final int size = seq.size();
        for ( int i = 1 ; i <= (num - 1) ; i += 1 ) {
            seq.add(i, 1000 + i);
            assertEquals(1000 + i, seq.getInt(i));
            assertEquals(0, seq.getInt(i + 1));
            assertEquals(size + i, seq.size());
            assertEquals(num - i - 1, seq.getInt(size));
        }
    }

    protected void testRemove(final boolean[] retain) {
        final int num = retain.length;
        final Seq seq = model.createSeq();
        for ( int i = 0 ; i < num ; i += 1 ) {
            seq.add(i);
        }
        //
        final List<RDFNode> retained = new ArrayList<>();
        //
        final NodeIterator nIter = seq.iterator();
        for ( boolean aRetain : retain ) {
            final RDFNode x = nIter.nextNode();
            if ( aRetain ) {
                retained.add(x);
            } else {
                nIter.remove();
            }
        }
        //
        assertFalse(nIter.hasNext());
        assertEquals(retained, seq.iterator().toList());
    }

    @Test
    public void testRemoveA() {
        testRemove(bools("tttffffftt"));
    }

    @Test
    public void testRemoveB() {
        testRemove(bools("ftftttttft"));
    }

    @Test
    public void testRemoveC() {
        testRemove(bools("ffffffffff"));
    }

    @Test
    public void testSeq4() {
        final String test = "temp";
        int n = 58305;
        final Seq seq4 = model.createSeq();
        n = ((n / 100) * 100) + 100;
        n++;
        seq4.add(AbstractModelTestBase.tvBoolean);
        n++;
        if ( !(seq4.getBoolean(1) == AbstractModelTestBase.tvBoolean) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvByte);
        n++;
        if ( !(seq4.getByte(2) == AbstractModelTestBase.tvByte) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvShort);
        n++;
        if ( !(seq4.getShort(3) == AbstractModelTestBase.tvShort) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvInt);
        n++;
        if ( !(seq4.getInt(4) == AbstractModelTestBase.tvInt) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvLong);
        n++;
        if ( !(seq4.getLong(5) == AbstractModelTestBase.tvLong) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvChar);
        n++;
        if ( !(seq4.getChar(6) == AbstractModelTestBase.tvChar) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvFloat);
        n++;
        if ( !(seq4.getFloat(7) == AbstractModelTestBase.tvFloat) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvDouble);
        n++;
        if ( !(seq4.getDouble(8) == AbstractModelTestBase.tvDouble) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvString);
        n++;
        if ( !(seq4.getString(9).equals(AbstractModelTestBase.tvString)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq4.getLanguage(9).equals("")) ) {
            error(test, n);
        }
        n++;
        seq4.add(AbstractModelTestBase.tvString, TestSeqMethods.lang);
        n++;
        if ( !(seq4.getString(10).equals(AbstractModelTestBase.tvString)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq4.getLanguage(10).equals(TestSeqMethods.lang)) ) {
            error(test, n);
        }
        n++;
        seq4.add(anObject);
        n++;
        // if (!(seq4.getObject( 11, new LitTestObjF() ).equals( anObject )))
        // error(
        // test, n );
        n++;
        seq4.add(tvResource);
        n++;
        if ( !(seq4.getResource(12).equals(tvResource)) ) {
            error(test, n);
        }
        n++;
        seq4.add(tvLiteral);
        n++;
        if ( !(seq4.getLiteral(13).equals(tvLiteral)) ) {
            error(test, n);
        }
        n++;
        // seq4.add( tvResObj );
        // n++;
        // if (!(seq4.getResource( 14, new ResTestObjF() ).equals( tvResObj )))
        // error(
        // test, n );
        n++;
        seq4.add(tvBag);
        n++;
        if ( !(seq4.getBag(14).equals(tvBag)) ) {
            error(test, n);
        }
        n++;
        seq4.add(tvAlt);
        n++;
        if ( !(seq4.getAlt(15).equals(tvAlt)) ) {
            error(test, n);
        }
        n++;
        seq4.add(tvSeq);
        n++;
        if ( !(seq4.getSeq(16).equals(tvSeq)) ) {
            error(test, n);
        }
        n++;
        try {
            seq4.getInt(17);
            error(test, n);
        } catch (final SeqIndexBoundsException e) {
            // as required
        }
        n++;
        try {
            seq4.getInt(0);
            error(test, n);
        } catch (final SeqIndexBoundsException e) {
            // as required
        }
    }

    @Test
    public void testSeq5() {
        final Seq seq5 = model.createSeq();
        final String test = "seq5";
        int n = 0;
        for ( int i = 0 ; i < TestSeqMethods.num ; i++ ) {
            seq5.add(i);
        }

        try {
            n++;
            seq5.add(0, false);
            error(test, n);
        } catch (final SeqIndexBoundsException e) {
            // as required
        }
        seq5.add(TestSeqMethods.num + 1, false);
        if ( seq5.size() != (TestSeqMethods.num + 1) ) {
            error(test, n);
        }
        seq5.remove(TestSeqMethods.num + 1);
        try {
            n++;
            seq5.add(TestSeqMethods.num + 2, false);
            error(test, n);
        } catch (final SeqIndexBoundsException e) {
            // as required
        }

        n = ((n / 100) * 100) + 100;
        final int size = seq5.size();
        for ( int i = 1 ; i <= (TestSeqMethods.num - 1) ; i++ ) {
            n++;
            seq5.add(i, 1000 + i);
            n++;
            if ( !(seq5.getInt(i) == (1000 + i)) ) {
                error(test, n);
            }
            n++;
            if ( !(seq5.getInt(i + 1) == 0) ) {
                error(test, n);
            }
            n++;
            if ( !(seq5.size() == (size + i)) ) {
                error(test, n);
            }
            n++;
            if ( !(seq5.getInt(size) == (TestSeqMethods.num - i - 1)) ) {
                error(test, n);
            }
        }
    }

    @Test
    public void testSeq6() {
        final String test = "seq6";
        int n = 0;
        final Seq seq6 = model.createSeq();
        seq6.add(model.createResource());
        seq6.add(1, AbstractModelTestBase.tvBoolean);
        n++;
        if ( !(seq6.getBoolean(1) == AbstractModelTestBase.tvBoolean) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvByte);
        n++;
        if ( !(seq6.getByte(1) == AbstractModelTestBase.tvByte) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvShort);
        n++;
        if ( !(seq6.getShort(1) == AbstractModelTestBase.tvShort) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvInt);
        n++;
        if ( !(seq6.getInt(1) == AbstractModelTestBase.tvInt) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvLong);
        n++;
        if ( !(seq6.getLong(1) == AbstractModelTestBase.tvLong) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvChar);
        n++;
        if ( !(seq6.getChar(1) == AbstractModelTestBase.tvChar) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvFloat);
        n++;
        if ( !(seq6.getFloat(1) == AbstractModelTestBase.tvFloat) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvDouble);
        n++;
        if ( !(seq6.getDouble(1) == AbstractModelTestBase.tvDouble) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvString);
        n++;
        if ( !(seq6.getString(1).equals(AbstractModelTestBase.tvString)) ) {
            error(test, n);
        }
        seq6.add(1, AbstractModelTestBase.tvString, TestSeqMethods.lang);
        n++;
        if ( !(seq6.getString(1).equals(AbstractModelTestBase.tvString)) ) {
            error(test, n);
        }
        seq6.add(1, tvResource);
        n++;
        if ( !(seq6.getResource(1).equals(tvResource)) ) {
            error(test, n);
        }
        seq6.add(1, tvLiteral);
        n++;
        if ( !(seq6.getLiteral(1).equals(tvLiteral)) ) {
            error(test, n);
        }
        seq6.add(1, anObject);
        n++;
        // if (!(seq6.getObject( 1, new LitTestObjF() ).equals( anObject )))
        // error(
        // test, n );

        n = ((n / 100) * 100) + 100;
        n++;
        if ( !(seq6.indexOf(anObject) == 1) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(tvLiteral) == 2) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(tvResource) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvString, TestSeqMethods.lang) == 4) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvString) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvDouble) == 6) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvFloat) == 7) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvChar) == 8) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvLong) == 9) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvInt) == 10) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvShort) == 11) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvByte) == 12) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(AbstractModelTestBase.tvBoolean) == 13) ) {
            error(test, n);
        }
        n++;
        if ( !(seq6.indexOf(1234543) == 0) ) {
            error(test, n);
        }
    }

    @Test
    public void testSeq7() {
        final Seq seq7 = model.createSeq();
        final String test = "seq7";
        int n = 0;
        n = ((n / 100) * 100) + 100;
        for ( int i = 0 ; i < TestSeqMethods.num ; i++ ) {
            seq7.add(i);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvBoolean);
        n++;
        if ( !(seq7.getBoolean(5) == AbstractModelTestBase.tvBoolean) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvByte);
        n++;
        if ( !(seq7.getByte(5) == AbstractModelTestBase.tvByte) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvShort);
        n++;
        if ( !(seq7.getShort(5) == AbstractModelTestBase.tvShort) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvInt);
        n++;
        if ( !(seq7.getInt(5) == AbstractModelTestBase.tvInt) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvLong);
        n++;
        if ( !(seq7.getLong(5) == AbstractModelTestBase.tvLong) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvChar);
        n++;
        if ( !(seq7.getChar(5) == AbstractModelTestBase.tvChar) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvFloat);
        n++;
        if ( !(seq7.getFloat(5) == AbstractModelTestBase.tvFloat) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvDouble);
        n++;
        if ( !(seq7.getDouble(5) == AbstractModelTestBase.tvDouble) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, AbstractModelTestBase.tvString);
        n++;
        if ( !(seq7.getString(5).equals(AbstractModelTestBase.tvString)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getLanguage(5).equals("")) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        seq7.set(5, AbstractModelTestBase.tvString, TestSeqMethods.lang);
        n++;
        if ( !(seq7.getString(5).equals(AbstractModelTestBase.tvString)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getLanguage(5).equals(TestSeqMethods.lang)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, tvLiteral);
        n++;
        if ( !(seq7.getLiteral(5).equals(tvLiteral)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, tvResource);
        n++;
        if ( !(seq7.getResource(5).equals(tvResource)) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        seq7.set(5, anObject);
        n++;
        // if (!(seq7.getObject( 5, new LitTestObjF() )).equals( anObject ))
        // error(
        // test, n );
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
        n = ((n / 100) * 100) + 100;
        // seq7.set( 5, tvResObj );
        // n++;
        // if (!(seq7.getResource( 5, new ResTestObjF() ).equals( tvResObj )))
        // error(
        // test, n );
        n++;
        if ( !(seq7.getInt(4) == 3) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.getInt(6) == 5) ) {
            error(test, n);
        }
        n++;
        if ( !(seq7.size() == TestSeqMethods.num) ) {
            error(test, n);
        }
    }

    @Test
    public void testSeqAccessByIndexing() {
        // LitTestObj tvObject = new LitTestObj(12345);
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        final Resource tvResource = model.createResource();
        // Resource tvResObj = model.createResource(new ResTestObjF());
        final Object tvLitObj = new LitTestObj(1234);
        final Bag tvBag = model.createBag();
        final Alt tvAlt = model.createAlt();
        final Seq tvSeq = model.createSeq();
        //
        final Seq seq = model.createSeq();
        seq.add(true);
        assertEquals(true, seq.getBoolean(1));
        seq.add((byte)1);
        assertEquals((byte)1, seq.getByte(2));
        seq.add((short)2);
        assertEquals((short)2, seq.getShort(3));
        seq.add(-1);
        assertEquals(-1, seq.getInt(4));
        seq.add(-2);
        assertEquals(-2, seq.getLong(5));
        seq.add('!');
        assertEquals('!', seq.getChar(6));
        seq.add(123.456f);
        assertEquals(123.456f, seq.getFloat(7), 0.00005);
        seq.add(12345.67890);
        assertEquals(12345.67890, seq.getDouble(8), 0.00000005);
        seq.add("some string");
        assertEquals("some string", seq.getString(9));
        seq.add(tvLitObj);
        // assertEquals(tvLitObj, seq.getObject( 10, new LitTestObjF() ) );
        seq.add(tvResource);
        assertEquals(tvResource, seq.getResource(11));
        // seq.add( tvResObj );
        // assertEquals(tvResObj, seq.getResource( 12, new ResTestObjF() ) );
        seq.add(tvLiteral);
        assertEquals(tvLiteral, seq.getLiteral(12));
        seq.add(tvBag);
        assertEquals(tvBag, seq.getBag(13));
        seq.add(tvAlt);
        assertEquals(tvAlt, seq.getAlt(14));
        seq.add(tvSeq);
        assertEquals(tvSeq, seq.getSeq(15));
        //
        try {
            seq.getInt(16);
            fail("there is no element 16");
        } catch (final SeqIndexBoundsException e) {
            JenaTestLib.pass();
        }
        try {
            seq.getInt(0);
            fail("there is no element 0");
        } catch (final SeqIndexBoundsException e) {
            JenaTestLib.pass();
        }
    }

    @Test
    public void testSeqAdd() {
        final Seq seq = model.createSeq();
        assertEquals(0, seq.size());
        assertTrue(model.contains(seq, RDF.type, RDF.Seq));
        //
        seq.add(AbstractModelTestBase.tvBoolean);
        assertTrue(seq.contains(AbstractModelTestBase.tvBoolean));
        assertFalse(seq.contains(!AbstractModelTestBase.tvBoolean));
        //
        seq.add(AbstractModelTestBase.tvByte);
        assertTrue(seq.contains(AbstractModelTestBase.tvByte));
        assertFalse(seq.contains((byte)101));
        //
        seq.add(AbstractModelTestBase.tvShort);
        assertTrue(seq.contains(AbstractModelTestBase.tvShort));
        assertFalse(seq.contains((short)102));
        //
        seq.add(AbstractModelTestBase.tvInt);
        assertTrue(seq.contains(AbstractModelTestBase.tvInt));
        assertFalse(seq.contains(-101));
        //
        seq.add(AbstractModelTestBase.tvLong);
        assertTrue(seq.contains(AbstractModelTestBase.tvLong));
        assertFalse(seq.contains(-102));
        //
        seq.add(AbstractModelTestBase.tvChar);
        assertTrue(seq.contains(AbstractModelTestBase.tvChar));
        assertFalse(seq.contains('?'));
        //
        seq.add(123.456f);
        assertTrue(seq.contains(123.456f));
        assertFalse(seq.contains(456.123f));
        //
        seq.add(-123.456d);
        assertTrue(seq.contains(-123.456d));
        assertFalse(seq.contains(-456.123d));
        //
        seq.add("a string");
        assertTrue(seq.contains("a string"));
        assertFalse(seq.contains("a necklace"));
        //
        seq.add(model.createLiteral("another string"));
        assertTrue(seq.contains("another string"));
        assertFalse(seq.contains("another necklace"));
        //
        seq.add(new LitTestObj(12345));
        assertTrue(seq.contains(new LitTestObj(12345)));
        assertFalse(seq.contains(new LitTestObj(54321)));
        //
        // Resource present = model.createResource( new ResTestObjF() );
        // Resource absent = model.createResource( new ResTestObjF() );
        // seq.add( present );
        // assertTrue(seq.contains( present ) );
        // assertFalse(seq.contains( absent ) );
        //
        assertEquals(11, seq.size());
    }

    @Test
    public void testSeqAddInts() {
        final int num = 10;
        final Seq seq = model.createSeq();
        for ( int i = 0 ; i < num ; i += 1 ) {
            seq.add(i);
        }
        assertEquals(num, seq.size());
        final List<RDFNode> L = seq.iterator().toList();
        assertEquals(num, L.size());
        for ( int i = 0 ; i < num ; i += 1 ) {
            assertEquals(i, ((Literal)L.get(i)).getInt());
        }
    }

    @Test
    public void testSeqInsertByIndexing() {
        // LitTestObj tvObject = new LitTestObj(12345);
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        final Resource tvResource = model.createResource();
        // Resource tvResObj = model.createResource(new ResTestObjF());
        final Object tvLitObj = new LitTestObj(1234);
        final Bag tvBag = model.createBag();
        final Alt tvAlt = model.createAlt();
        final Seq tvSeq = model.createSeq();

        final Seq seq = model.createSeq();
        seq.add(model.createResource());
        seq.add(1, true);
        assertEquals(true, seq.getBoolean(1));
        seq.add(1, (byte)1);
        assertEquals((byte)1, seq.getByte(1));
        seq.add(1, (short)2);
        assertEquals((short)2, seq.getShort(1));
        seq.add(1, -1);
        assertEquals(-1, seq.getInt(1));
        seq.add(1, -2);
        assertEquals(-2, seq.getLong(1));
        seq.add(1, '!');
        assertEquals('!', seq.getChar(1));
        seq.add(1, 123.456f);
        assertEquals(123.456f, seq.getFloat(1), 0.00005);
        seq.add(1, 12345.67890);
        assertEquals(12345.67890, seq.getDouble(1), 0.00000005);
        seq.add(1, "some string");
        assertEquals("some string", seq.getString(1));
        seq.add(1, tvLitObj);
        // assertEquals(tvLitObj, seq.getObject( 1, new LitTestObjF() ) );
        seq.add(1, tvResource);
        assertEquals(tvResource, seq.getResource(1));
        // seq.add( 1, tvResObj );
        // assertEquals(tvResObj, seq.getResource( 1, new ResTestObjF() ) );
        seq.add(1, tvLiteral);
        assertEquals(tvLiteral, seq.getLiteral(1));
        seq.add(1, tvBag);
        assertEquals(tvBag, seq.getBag(1));
        seq.add(1, tvAlt);
        assertEquals(tvAlt, seq.getAlt(1));
        seq.add(1, tvSeq);
        assertEquals(tvSeq, seq.getSeq(1));
        //
        assertEquals(0, seq.indexOf(1234543));
        assertEquals(1, seq.indexOf(tvSeq));
        assertEquals(2, seq.indexOf(tvAlt));
        assertEquals(3, seq.indexOf(tvBag));
        assertEquals(4, seq.indexOf(tvLiteral));
        assertEquals(5, seq.indexOf(tvResource));
        assertEquals(6, seq.indexOf(tvLitObj));
        assertEquals(7, seq.indexOf("some string"));
        assertEquals(8, seq.indexOf(12345.67890));
        assertEquals(9, seq.indexOf(123.456f));
        assertEquals(10, seq.indexOf('!'));
        assertEquals(11, seq.indexOf(-2));
        assertEquals(12, seq.indexOf(-1));
        assertEquals(13, seq.indexOf((short)2));
        assertEquals(14, seq.indexOf((byte)1));
        assertEquals(15, seq.indexOf(true));
    }

    @Test
    public void testSet() {
        // NodeIterator nIter;
        // StmtIterator sIter;
        final Literal tvLiteral = model.createLiteral("test 12 string 2");
        final Resource tvResource = model.createResource();
        // Resource tvResObj = model.createResource(new ResTestObjF());
        // Bag tvBag = model.createBag();
        // Alt tvAlt = model.createAlt();
        // Seq tvSeq = model.createSeq();
        final int num = 10;
        final Seq seq = model.createSeq();

        for ( int i = 0 ; i < num ; i++ ) {
            seq.add(i);
        }

        seq.set(5, AbstractModelTestBase.tvBoolean);
        assertEquals(AbstractModelTestBase.tvBoolean, seq.getBoolean(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvByte);
        assertEquals(AbstractModelTestBase.tvByte, seq.getByte(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvShort);
        assertEquals(AbstractModelTestBase.tvShort, seq.getShort(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvInt);
        assertEquals(AbstractModelTestBase.tvInt, seq.getInt(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvLong);
        assertEquals(AbstractModelTestBase.tvLong, seq.getLong(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvString);
        assertEquals(AbstractModelTestBase.tvString, seq.getString(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvBoolean);
        assertEquals(AbstractModelTestBase.tvBoolean, seq.getBoolean(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvFloat);
        assertEquals(AbstractModelTestBase.tvFloat, seq.getFloat(5), 0.00005);
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvDouble);
        assertEquals(AbstractModelTestBase.tvDouble, seq.getDouble(5), 0.000000005);
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, tvLiteral);
        assertEquals(tvLiteral, seq.getLiteral(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, tvResource);
        assertEquals(tvResource, seq.getResource(5));
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        seq.set(5, AbstractModelTestBase.tvLitObj);
        // assertEquals(tvLitObj, seq.getObject( 5, new LitTestObjF() ) );
        assertEquals(3, seq.getInt(4));
        assertEquals(5, seq.getInt(6));
        assertEquals(num, seq.size());

        // seq.set( 5, tvResObj );
        // assertEquals(tvResObj, seq.getResource( 5, new ResTestObjF() ) );
        // assertEquals(3, seq.getInt( 4 ) );
        // assertEquals(5, seq.getInt( 6 ) );
        // assertEquals(num, seq.size() );
    }

}
