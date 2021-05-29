/**
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

import org.junit.Assert ;
import org.junit.Test ;

// Testing is a bit light here but the RDF term output and
// the language level output covers the ground as well.

public class TestEscapeStr {

    @Test public void escape_str_01()   { test_esc("", "") ; }
    @Test public void escape_str_02()   { test_esc("A", "A") ; }
    @Test public void escape_str_03()   { test_esc("\n", "\\n") ; }
    @Test public void escape_str_04()   { test_esc("A\tB", "A\\tB") ; }
    @Test public void escape_str_05()   { test_esc("\"", "\\\"") ; }

    @Test public void unescape_str_10()   { test_unesc("\\u0041", "A") ; }
    @Test public void unescape_str_11()   { test_unesc("\\U00000041", "A") ; }
    @Test public void unescape_str_12()   { test_unesc("12\\u004134", "12A34") ; }
    @Test public void unescape_str_13()   { test_unesc("12\\U0000004134", "12A34") ; }

    private static void test_esc(String input, String expected) {
        String output = EscapeStr.stringEsc(input) ;
        Assert.assertEquals(expected, output);
    }

    private void test_unesc(String input, String expected) {
        String output = EscapeStr.unescapeStr(input) ;
        Assert.assertEquals(expected, output);
    }

    @Test public void unescape_unicode_1()   { test_unesc_unicode("", "") ; }
    @Test public void unescape_unicode_2()   { test_unesc_unicode("abc\\u0020def", "abc def") ; }
    @Test public void unescape_unicode_3()   { test_unesc_unicode("\\u0020", " ") ; }
    @Test public void unescape_unicode_4()   { test_unesc_unicode("abc\\U00000020def", "abc def") ; }
    @Test public void unescape_unicode_5()   { test_unesc_unicode("\\U00000020", " ") ; }

    // Leaves non-unicode untouched.
    @Test public void unescape_unicode_10()   { test_unesc_unicode("\\1\\2", "\\1\\2") ; }
    @Test public void unescape_unicode_11()   { test_unesc_unicode("\\n\\t", "\\n\\t") ; }
    @Test public void unescape_unicode_12()   { test_unesc_unicode("\\(\\)", "\\(\\)") ; }
    @Test public void unescape_unicode_13()   { test_unesc_unicode("\\\\", "\\\\") ; }

    private void test_unesc_unicode(String input, String expected) {
        String output = EscapeStr.unescapeUnicode(input) ;
        Assert.assertEquals(expected, output);
    }
}
