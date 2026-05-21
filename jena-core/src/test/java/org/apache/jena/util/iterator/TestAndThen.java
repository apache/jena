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

package org.apache.jena.util.iterator;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.test.JenaTestLib;

public class TestAndThen {

    @Test
    public void testAndThen() {
        ExtendedIterator<String> L = JenaTestLib.iteratorOfStrings("a b c");
        ExtendedIterator<String> R = JenaTestLib.iteratorOfStrings("d e f");
        JenaTestLib.assertInstanceOf(NiceIterator.class, L);
        JenaTestLib.assertInstanceOf(NiceIterator.class, R);
        assertEquals(JenaTestLib.listOfStrings("a b c d e f"), Iter.toList(L.andThen(R)));
    }

    @Test
    public void testAndThenExtension() {
        ExtendedIterator<String> L = JenaTestLib.iteratorOfStrings("a b c");
        ExtendedIterator<String> R = JenaTestLib.iteratorOfStrings("d e f");
        ExtendedIterator<String> X = JenaTestLib.iteratorOfStrings("g h i");
        ExtendedIterator<String> LR = L.andThen(R);
        ExtendedIterator<String> LRX = LR.andThen(X);
        assertSame(LR, LRX);
        List<String> aToI = JenaTestLib.listOfStrings("a b c d e f g h i");
        assertEquals(aToI, Iter.toList(LRX));
    }

    @Test
    public void testClosingConcatenationClosesRemainingIterators() {
        LoggingClosableIterator<String> L = new LoggingClosableIterator<>(JenaTestLib.iteratorOfStrings("only"));
        LoggingClosableIterator<String> M = new LoggingClosableIterator<>(JenaTestLib.iteratorOfStrings("single"));
        LoggingClosableIterator<String> R = new LoggingClosableIterator<>(JenaTestLib.iteratorOfStrings("it"));
        ExtendedIterator<String> cat = L.andThen(M).andThen(R);
        cat.next();
        cat.close();
        assertTrue("middle iterator should have been closed", M.isClosed());
        assertTrue("final iterator should have been closed", R.isClosed());
    }

    @Test
    public void testRemove1() {
        List<String> L = JenaTestLib.listOfStrings("a b c");
        List<String> R = JenaTestLib.listOfStrings("d e f");

        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());

        ExtendedIterator<String> LR = Lit.andThen(Rit);

        while (LR.hasNext()) {
            String s = LR.next();

            if ( "c".equals(s) ) {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }

        assertEquals("ab", concatAsString(L));
        assertEquals("def", concatAsString(R));
    }

    @Test
    public void testRemove2() {
        List<String> L = JenaTestLib.listOfStrings("a b c");
        List<String> R = JenaTestLib.listOfStrings("d e f");

        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());

        ExtendedIterator<String> LR = Lit.andThen(Rit);

        while (LR.hasNext()) {
            String s = LR.next();

            if ( "d".equals(s) ) {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }

        assertEquals("abc", concatAsString(L));
        assertEquals("ef", concatAsString(R));
    }

    @Test
    public void testRemove3() {
        List<String> L = JenaTestLib.listOfStrings("a b c");
        List<String> R = JenaTestLib.listOfStrings("d e f");

        ExtendedIterator<String> Lit = WrappedIterator.create(L.iterator());
        ExtendedIterator<String> Rit = WrappedIterator.create(R.iterator());

        ExtendedIterator<String> LR = Lit.andThen(Rit);

        while (LR.hasNext()) {
            LR.next();
        }
        LR.remove();

        assertEquals("abc", concatAsString(L));
        assertEquals("de", concatAsString(R));
    }

    private String concatAsString(List<String> strings) {
        String toReturn = "";
        for ( String s : strings ) {
            toReturn += s;
        }
        return toReturn;
    }

}
