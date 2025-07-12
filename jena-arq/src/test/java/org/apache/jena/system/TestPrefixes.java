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

package org.apache.jena.system;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.system.Prefixes;

public class TestPrefixes {

    // U+1F600 is 😀 "grinning face"
    // U+1F631 is 😱 "face screaming in fear"
    // Create carefully to make sure the exact Java characters are used.
    private static String emoji1 = new String(Character.toChars(0x1F600));
    private static String emoji2 = new String(Character.toChars(0x1F631));

    @Test public void prefixes_good_01() { good(""); }

    @Test public void prefixes_good_02() { good("abc"); }

    @Test public void prefixes_good_03() { good("a-bc"); }

    @Test public void prefixes_good_04() { good("a.b.c"); }

    @Test public void prefixes_good_05() { good("a0117"); }

    @Test public void prefixes_good_06() { good(emoji1); }

    @Test public void prefixes_good_07() { good(emoji1+"."+emoji2); }

    @Test public void prefixes_bad_01() { bad("-"); }

    @Test public void prefixes_bad_02() { bad("."); }

    @Test public void prefixes_bad_03() { bad("9ab"); }

    @Test public void prefixes_bad_04() { bad("<>"); }

    @Test public void prefixes_bad_05() { bad(":"); }

    @Test public void prefixes_bad_06() { bad("ex:"); }

    @Test
    public void prefixes_bad_NPE() {
        assertThrows(NullPointerException.class, ()->bad(null));
    }

    private static void good(String string) {
        assertTrue(Prefixes.isLegalPrefix(string));
    }

    private static void bad(String string) {
        assertFalse(Prefixes.isLegalPrefix(string));
    }
}
