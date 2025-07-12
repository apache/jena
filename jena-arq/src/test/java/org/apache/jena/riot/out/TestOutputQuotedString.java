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

package org.apache.jena.riot.out;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.riot.out.quoted.QuotedStringOutput;
import org.apache.jena.riot.out.quoted.QuotedStringOutputNT;
import org.apache.jena.riot.out.quoted.QuotedStringOutputTTL;
import org.apache.jena.riot.out.quoted.QuotedStringOutputTTL_MultiLine;

public class TestOutputQuotedString {

    static void testSingleLine(QuotedStringOutput proc, String input, String expected) {
        StringWriterI w = new StringWriterI();
        proc.writeStr(w, input);
        String output = w.toString();
        expected = proc.getQuoteChar()+expected+proc.getQuoteChar();
        assertEquals(expected, output);
    }

    static void testMultiLine(QuotedStringOutput proc, String input, String expected) {
        StringWriterI w = new StringWriterI();
        proc.writeStrMultiLine(w, input);
        String output = w.toString();
        assertEquals(expected, output);
    }

    private QuotedStringOutput escProcNT = new QuotedStringOutputNT();
    private QuotedStringOutput escProcTTL_S2 = new QuotedStringOutputTTL('"');
    private QuotedStringOutput escProcTTL_S1 = new QuotedStringOutputTTL('\'');

    private QuotedStringOutput escProcTTL_M2 = new QuotedStringOutputTTL_MultiLine('"');
    private QuotedStringOutput escProcTTL_M1 = new QuotedStringOutputTTL_MultiLine('\'');

    @Test public void escape_nt_00() { testSingleLine(escProcNT, "", ""); }
    @Test public void escape_nt_01() { testSingleLine(escProcNT, "abc", "abc"); }
    @Test public void escape_nt_02() { testSingleLine(escProcNT, "abc\ndef", "abc\\ndef"); }

    @Test public void escape_nt_03() { testSingleLine(escProcNT, "\"", "\\\""); }
    @Test public void escape_nt_04() { testSingleLine(escProcNT, "'", "'"); }
    @Test public void escape_nt_05() { testSingleLine(escProcNT, "xyz\t", "xyz\\t"); }

    @Test public void escape_ttl_singleline_quote2_00() { testSingleLine(escProcTTL_S2, "", ""); }
    @Test public void escape_ttl_singleline_quote2_01() { testSingleLine(escProcTTL_S2, "abc", "abc"); }
    @Test public void escape_ttl_singleline_quote2_02() { testSingleLine(escProcTTL_S2, "abc\ndef", "abc\\ndef"); }

    @Test public void escape_ttl_singleline_quote2_03() { testSingleLine(escProcTTL_S2, "\"", "\\\""); }
    @Test public void escape_ttl_singleline_quote2_04() { testSingleLine(escProcTTL_S2, "'", "'"); }
    @Test public void escape_ttl_singleline_quote2_05() { testSingleLine(escProcTTL_S2, "xyz\t", "xyz\\t"); }

    @Test public void escape_ttl_singleline_quote1_00() { testSingleLine(escProcTTL_S1, "", ""); }
    @Test public void escape_ttl_singleline_quote1_01() { testSingleLine(escProcTTL_S1, "abc", "abc"); }
    @Test public void escape_ttl_singleline_quote1_02() { testSingleLine(escProcTTL_S1, "abc\ndef", "abc\\ndef"); }

    @Test public void escape_ttl_singleline_quote1_03() { testSingleLine(escProcTTL_S1, "\"", "\""); }
    @Test public void escape_ttl_singleline_quote1_04() { testSingleLine(escProcTTL_S1, "'", "\\'"); }
    @Test public void escape_ttl_singleline_quote1_05() { testSingleLine(escProcTTL_S1, "xyz\t", "xyz\\t"); }

    // Multiple with single line output processor.
    @Test public void escape_ttl_singleline_quote2_multiline_str0() { testMultiLine(escProcTTL_S2, "", "\"\""); }
    @Test public void escape_ttl_singleline_quote2_multiline_str1() { testMultiLine(escProcTTL_S2, "abc", "\"abc\""); }
    @Test public void escape_ttl_singleline_quote2_multiline_str2() { testMultiLine(escProcTTL_S2, "abc\ndef", "\"abc\\ndef\""); }

    @Test public void escape_ttl_singleline_quote2_multiline_str3() { testMultiLine(escProcTTL_S2, "\"", "\"\\\"\""); }
    @Test public void escape_ttl_singleline_quote2_multiline_str4() { testMultiLine(escProcTTL_S2, "'", "\"'\""); }
    @Test public void escape_ttl_singleline_quote2_multiline_str5() { testMultiLine(escProcTTL_S2, "xyz\t", "\"xyz\\t\""); }

    // Multiple with single line output processor.
    @Test public void escape_ttl_singleline_quote1_multiline_str0() { testMultiLine(escProcTTL_S1, "", "''"); }
    @Test public void escape_ttl_singleline_quote1_multiline_str1() { testMultiLine(escProcTTL_S1, "abc", "'abc'"); }
    @Test public void escape_ttl_singleline_quote1_multiline_str2() { testMultiLine(escProcTTL_S1, "abc\ndef", "'abc\\ndef'"); }

    @Test public void escape_ttl_singleline_quote1_multiline_str3() { testMultiLine(escProcTTL_S1, "\"", "'\"'"); }
    @Test public void escape_ttl_singleline_quote1_multiline_str4() { testMultiLine(escProcTTL_S1, "'", "'\\''"); }
    @Test public void escape_ttl_singleline_quote1_multiline_str5() { testMultiLine(escProcTTL_S1, "xyz\t", "'xyz\\t'"); }
}
