/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jena.atlas.AtlasException;
import org.junit.jupiter.api.Test ;

public class TestStrUtils
{
    static char marker = '_' ;
    static char esc[] = { ' ' , '_' } ;

    static void testEnc(String x) {
        testEnc(x, null);
    }

    static void testEnc(String x, String z) {
        String y = StrUtils.encodeHex(x, marker, esc);
        if ( z != null )
            assertEquals(z, y);
        String x2 = StrUtils.decodeHex(y, marker);
        assertEquals(x, x2);
    }

    // Decode only.
    static void testDecode(String input, String expected) {
        String x2 = StrUtils.decodeHex(input, marker);
        assertEquals(expected, x2);
    }

    @Test public void enc01() { testEnc("abc") ; }
    @Test public void enc02() { testEnc("") ; }
    @Test public void enc03() { testEnc("_", "_5F" ) ; }
    @Test public void enc04() { testEnc(" ", "_20" ) ; }
    @Test public void enc05() { testEnc("_ _", "_5F_20_5F" ) ; }
    @Test public void enc06() { testEnc("_5F", "_5F5F" ) ; }
    @Test public void enc07() { testEnc("_2") ; }
    @Test public void enc08() { testEnc("AB_CD", "AB_5FCD") ; }

    // JENA-1890: Multibyte characters before the "_"
    // 사용_설명서 (Korean: "User's Guide")

    @Test public void enc09() { testEnc("\uC0AC\uC6A9_\uC124\uBA85\uC11C"); }
    // Same string, but using the glyphs for the codepoints, not the \ u value. Same string after Java parsing.
    @Test public void enc09a() { testEnc("사용_설명서"); }

    // The decode code works more generally than the encoder.
    // This tests the decode of the UTF=-8 byte encoding of 사용_설명서
    // Note "_5F" which is "_" encoded.
    @Test public void enc10() { testDecode("_EC_82_AC_EC_9A_A9_5F_EC_84_A4_EB_AA_85_EC_84_9C", "사용_설명서"); }
    @Test public void enc11() { testDecode("_41", "A"); }

    @Test public void enc20() { assertThrows(AtlasException.class, ()->testDecode("_4", null)); }
    @Test public void enc21() { assertThrows(AtlasException.class, ()->testDecode("_", null)); }
    @Test public void enc22() { assertThrows(AtlasException.class, ()->testDecode("_X1", null)); }
    @Test public void enc23() { assertThrows(AtlasException.class, ()->testDecode("_1X", null)); }

    @Test public void lastChar01() { testLastChar("abc",  'c'); }
    @Test public void lastChar02() { testLastChar(".",  '.'); }
    @Test public void lastChar03() { testLastChar("",  (char)0); }

    private static void testLastChar(String x, char expectedLastChar) {
        char lastChar = StrUtils.lastChar(x);
        assertEquals(expectedLastChar, lastChar);
    }

    @Test public void prefix_ignorecase_1() {
        boolean b = StrUtils.strStartsWithIgnoreCase("foobar", "FOO");
        assertTrue(b);
    }

    @Test public void prefix_ignorecase_2() {
        boolean b = StrUtils.strStartsWithIgnoreCase("foobar", "bar");
        assertFalse(b);
    }
    @Test public void prefix_ignorecase_3() {
        boolean b = StrUtils.strStartsWithIgnoreCase("foo", "foobar");
        assertFalse(b);
    }

    @Test public void suffix_ignorecase_1() {
        boolean b = StrUtils.strEndsWithIgnoreCase("foobar", "BAR");
        assertTrue(b);
    }

    @Test public void suffix_ignorecase_2() {
        boolean b = StrUtils.strEndsWithIgnoreCase("foobar", "oo");
        assertFalse(b);
    }
    @Test public void suffix_ignorecase_3() {
        boolean b = StrUtils.strEndsWithIgnoreCase("bar", "foobar");
        assertFalse(b);
    }
}
