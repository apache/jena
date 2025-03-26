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

package org.apache.jena.langtag;

import static org.apache.jena.langtag.LangTags.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests for the operations related to basic parsing of language tags (SPARQL and Turtle grammar rules)
 */
public class TestBasicSyntaxLangTags {
    @Test public void basic_01() { basicSplitCheck("en", "en"); }
    @Test public void basic_02() { basicSplitCheck("en-GB", "en", "GB"); }
    @Test public void basic_03() { basicSplitCheck("en-gb", "en", "gb"); }
    @Test public void basic_04() { basicSplitCheck("en", "en"); }
    @Test public void basic_05() { basicSplitCheck("en-123", "en","123"); }
    @Test public void basic_06() { basicSplitCheck("e", "e"); }

    // Showing the split does not allocate subtags to their category. e.g. "x-private" is split.
    @Test public void basic_10() { basicSplitCheck("en-Latn-GB-boont-r-extended-sequence-x-private",
                                                   "en","Latn", "GB", "boont", "r", "extended", "sequence", "x", "private"); }

    @Test public void basic_bad_01() { basicSplitCheckBad(""); }
    @Test public void basic_bad_02() { basicSplitCheckBad("-"); }
    @Test public void basic_bad_03() { basicSplitCheckBad("--"); }
    @Test public void basic_bad_04() { basicSplitCheckBad("abc-xy%20"); }
    @Test public void basic_bad_05() { basicSplitCheckBad("abc def"); }
    @Test public void basic_bad_06() { basicSplitCheckBad("a12-def"); }
    @Test public void basic_bad_07() { basicSplitCheckBad("9-def"); }

    static void basicSplitCheck(String input, String...parts) {
        basicSplitTest(input, parts);
        checkTest(input);
    }

    static void basicSplitCheckBad(String input) {
        assertFalse(basicCheck(input));
        assertNull(splitOnDash(input));
        assertThrows(LangTagException.class, ()->splitOnDashEx(input));
        assertThrows(LangTagException.class, ()->basicCheckEx(input));
    }

    public static void basicSplitTest(String input, String...parts) {
        List<String> expected = (parts == null) ? null : Arrays.asList(parts);
        List<String> actual = splitOnDashEx(input);
        assertEquals(expected, actual, "Subject: "+input);
        List<String> actual2 = splitOnDash(input);
        assertEquals(actual, actual2, "Subject(2): "+input);
    }

    private static void checkTest(String input) {
        boolean actual =  basicCheckEx(input);
        assertTrue(actual, "Subject: "+input);
        boolean actual2 =  basicCheck(input);
        assertEquals(actual, actual2, "Subject(2): "+input);
    }
}
