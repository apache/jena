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

import java.io.StringWriter;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.junit.Assert ;
import org.junit.Test ;

// Testing is a bit light here but the RDF term output and
// the language level output covers the ground as well.

public class TestEscapeStr {

    private static char S_QUOTE = '\'';
    private static char D_QUOTE = '"';

    // General, for double quoted strings.
    @Test public void escape_str_01()   { test_esc("", "") ; }
    @Test public void escape_str_02()   { test_esc("A", "A") ; }
    @Test public void escape_str_03()   { test_esc("\n", "\\n") ; }
    @Test public void escape_str_04()   { test_esc("A\tB", "A\\tB") ; }
    @Test public void escape_str_05()   { test_esc("\"", "\\\"") ; }
    @Test public void escape_str_06()   { test_esc("'", "'") ; }

    private static void test_esc(String input, String expected) {
        String output = EscapeStr.stringEsc(input) ;
        Assert.assertEquals("Failed at escape", expected, output);
        String output2 = EscapeStr.unescapeStr(output);
        Assert.assertEquals("Failed at unescape", input, output2);
    }

    // Single line
    @Test public void escape_str_single_01()   { test_esc1("a'x",  S_QUOTE,  "a\\'x") ; }
    @Test public void escape_str_single_02()   { test_esc1("a\"x", D_QUOTE,  "a\\\"x") ; }

    @Test public void escape_str_single_03()   { test_esc1("a\"x", S_QUOTE,  "a\"x") ; }
    @Test public void escape_str_single_04()   { test_esc1("a'x",  D_QUOTE,  "a'x") ; }

    @Test public void escape_str_single_05()   { test_esc1("a'",   S_QUOTE,  "a\\'") ; }
    @Test public void escape_str_single_06()   { test_esc1("a\"",  D_QUOTE,  "a\\\"") ; }

    @Test public void escape_str_single_07()   { test_esc1("\"",  S_QUOTE,  "\"") ; }
    @Test public void escape_str_single_08()   { test_esc1("'",   D_QUOTE,  "'") ; }

    private static void test_esc1(String input, char quoteChar, String expected) {
        StringWriter sw = new StringWriter();
        AWriter w = IO.wrap(sw);
        EscapeStr.stringEsc(w, input, quoteChar, true);
        w.flush();
        String output = sw.toString();
        Assert.assertEquals(expected, output);
    }


    // Multiline quoting.
    // One character
    @Test public void escape_str_multi_01()    { test_esc3("a'x",  S_QUOTE,  "a'x") ; }
    @Test public void escape_str_multi_02()    { test_esc3("a\"x", D_QUOTE,  "a\"x") ; }

    @Test public void escape_str_multi_03()    { test_esc3("'x", S_QUOTE, "'x") ; }
    @Test public void escape_str_multi_04()    { test_esc3("'x", D_QUOTE, "'x") ; }

    // Last character
    @Test public void escape_str_multi_05()    { test_esc3("a'", S_QUOTE, "a\\'") ; }
    @Test public void escape_str_multi_06()    { test_esc3("a'", D_QUOTE, "a'") ; }

    @Test public void escape_str_multi_07()    { test_esc3("a\"", S_QUOTE, "a\"") ; }
    @Test public void escape_str_multi_08()    { test_esc3("a\"", D_QUOTE, "a\\\"") ; }

    @Test public void escape_str_multi_09()    { test_esc3("'", S_QUOTE, "\\'") ; }
    @Test public void escape_str_multi_10()    { test_esc3("'", D_QUOTE, "'") ; }

    @Test public void escape_str_multi_11()    { test_esc3("\"", S_QUOTE, "\"") ; }
    @Test public void escape_str_multi_12()    { test_esc3("\"", D_QUOTE, "\\\"") ; }

    // 2 in a row
    @Test public void escape_str_multi_2q_1()  { test_esc3("a''z", S_QUOTE, "a''z") ; }
    @Test public void escape_str_multi_2q_2()  { test_esc3("a''z", D_QUOTE, "a''z") ; }
    @Test public void escape_str_multi_2q_3()  { test_esc3("a''",  S_QUOTE, "a'\\'") ; }
    @Test public void escape_str_multi_2q_4()  { test_esc3("a''",  D_QUOTE, "a''") ; }

    // 3 in a row.
    @Test public void escape_str_multi_3q_1()  { test_esc3("a'''z", S_QUOTE, "a''\\'z") ; }
    @Test public void escape_str_multi_3q_2()  { test_esc3("a'''z", D_QUOTE, "a'''z") ; }
    @Test public void escape_str_multi_3q_3()  { test_esc3("a'''",  S_QUOTE, "a''\\'") ; }
    @Test public void escape_str_multi_3q_4()  { test_esc3("a'''",  D_QUOTE, "a'''") ; }

    // 4 in a row.
    @Test public void escape_str_multi_4q_1()    { test_esc3("a''''z", S_QUOTE, "a''\\''z") ; }
    @Test public void escape_str_multi_4q_2()    { test_esc3("a''''z", D_QUOTE, "a''''z") ; }

    @Test public void escape_str_multi_4q_3()    { test_esc3("a''''", S_QUOTE, "a''\\'\\'") ; }
    @Test public void escape_str_multi_4q_4()    { test_esc3("a''''", D_QUOTE, "a''''") ; }

    private static void test_esc3(String input, char quoteChar, String expected) {
        StringWriter sw = new StringWriter();
        AWriter w = IO.wrap(sw);
        EscapeStr.stringEsc(w, input, quoteChar, false);
        w.flush();
        String output = sw.toString();
        Assert.assertEquals(expected, output);
    }

    // Unescape
    @Test public void unescape_str_10()   { test_unesc("\\u0041", "A") ; }
    @Test public void unescape_str_11()   { test_unesc("\\U00000041", "A") ; }
    @Test public void unescape_str_12()   { test_unesc("12\\u004134", "12A34") ; }
    @Test public void unescape_str_13()   { test_unesc("12\\U0000004134", "12A34") ; }

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
