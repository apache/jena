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

package org.apache.jena.riot.tokens;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.RiotException;

public abstract class AbstractTestTokenizerTextSurrogates {

    abstract protected Tokenizer tokenizer(String string);

    // Classification of tests

    // Structurally bad.
    abstract protected void surrogateBad(String string);

    // Validly encoded into escape sequences.
    abstract protected void surrogateValidEsc(String string);

    protected final void surrogateTest(String string) {
        Tokenizer tokenizer = tokenizer(string);
        assertTrue(tokenizer.hasNext());
        Token t1 = tokenizer.next();
        assertFalse(tokenizer.hasNext());
    }

    // Literal surrogates, i.e. java escape used to put into the tokenizer input.
    // These don't get to the tokenizer escape processing code.
    // They should be as if the UTF-8 has supplemental codepoints.
    protected void surrogateRawGood(String string) {
        surrogateTest(string);
    }

    // Literal surrogates, i.e. java escape used to put into the tokenizer input but illegal in some way.
    protected final void surrogateRawBad(String string) {
        assertThrows(RiotException.class, ()->surrogateTest(string));
    }

    // ----

    // U+D800-U+DBFF is a high surrogate (first part of a pair)
    // U+DC00-U+DFFF is a low surrogate (second part of a pair)
    // so D800-DC00 is legal.

    @Test public void turtle_surrogate_pair_esc_esc_01() {
        // escaped high, escaped low
        surrogateValidEsc("'\\ud800\\udc00'");
    }

    @Test public void turtle_surrogate_pair_esc_esc_02() {
        // escaped high, escaped low
        surrogateValidEsc("'''\\ud800\\udc00'''");
    }

    @Test public void turtle_surrogate_pair_esc_esc_03() {
        // escaped high, escaped low
        surrogateValidEsc("<\\ud800\\udc00>");
    }

    @Test public void turtle_surrogate_pair_esc_raw_01() {
        // escaped high, raw low
        surrogateValidEsc("'\\ud800\udc00'");
    }

    @Test public void turtle_surrogate_pair_esc_raw_02() {
        // escaped high, raw low
        surrogateValidEsc("'''\\ud800\udc00'''");
    }

    @Test public void turtle_surrogate_pair_esc_raw_03() {
        // escaped high, raw low
        surrogateRawBad("<\\ud800\udc00>");
    }

    // Compilation failure - illegal escape character
//    @Test public void turtle_surrogate_pair_raw_esc_01() {
//        // raw high, escaped low
//        surrogate("'\ud800\\udc00'");
//    }

    @Test public void turtle_surrogate_pair_raw_raw_01() {
        // raw high, raw low
        surrogateRawGood("'\ud800\udc00'");
    }

    @Test public void turtle_surrogate_pair_raw_raw_02() {
        // raw high, raw low
        surrogateRawGood("'''\ud800\udc00'''");
    }

    @Test public void turtle_surrogate_pair_raw_raw_03() {
        // raw high, raw low
        surrogateRawGood("<\ud800\udc00>");
    }

    // Blank nodes label allow unicode but not unicode escapes.
    @Test public void turtle_surrogate_pair_raw_raw_04() {
        // raw high, raw low
        surrogateRawGood("_:b\ud800\udc00");
    }

    @Test public void turtle_surrogate_pair_raw_raw_05() {
        surrogateRawGood("ns:\ud800\udc00");
    }

    @Test public void turtle_surrogate_pair_raw_raw_06() {
        surrogateRawGood("\ud800\udc00:local");
    }

    @Test public void turtle_surrogate_pair_esc_esc_internal_01() {
        // escaped high, escaped low
        surrogateValidEsc("'a\\ud800\\udc00x'");
    }

    @Test public void turtle_surrogate_pair_esc_esc_internal_02() {
        // escaped high, escaped low
        surrogateValidEsc("'''a\\ud800\\udc00x'''");
    }

    @Test public void turtle_surrogate_pair_esc_esc_internal_03() {
        // escaped high, escaped low
        surrogateValidEsc("<a\\ud800\\udc00x>");
    }

    @Test public void turtle_surrogate_pair_esc_raw_internal_01() {
        // escaped high, raw low
        surrogateRawBad("'z\\ud800\udc00z'");
    }

    @Test public void turtle_surrogate_pair_esc_raw_internal_02() {
        // escaped high, raw low
        surrogateRawBad("'''z\\ud800\udc00z'''");
    }

    @Test public void turtle_surrogate_pair_esc_raw_internal_03() {
        // escaped high, raw low
        surrogateRawBad("<z\\ud800\udc00z>");
    }

    // Compilation failure - illegal escape character
//    @Test public void turtle_surrogate_pair_raw_esc() {
//        // raw high, escaped low
//        surrogate("'a\ud800\\udc00'z");
//    }

    // Java, not RDF, surrogates.
    @Test public void turtle_surrogate_pair_raw_raw_internal_01() {
        // raw high, raw low
        surrogateRawGood("'a\ud800\udc00z'");
    }

    @Test public void turtle_surrogate_pair_raw_raw_internal_02() {
        // raw high, raw low
        surrogateRawGood("'''a\ud800\udc00z'''");
    }

    // XXX Overlap
    @Test public void turtle_surrogate_pair_raw_raw_internal_03() {
        // raw high, raw low
        surrogateRawGood("<a\ud800\udc00z>");
    }

    @Test public void turtle_surrogate_pair_raw_raw_internal_04() {
        // raw high, raw low
        surrogateRawGood("_:ba\ud800\udc00z");
    }

    @Test public void turtle_surrogate_pair_raw_raw__internal05() {
        surrogateRawGood("ns:x\ud800\udc00y");
    }

    @Test public void turtle_surrogate_pair_raw_raw__internal06() {
        surrogateRawGood("x\ud800\udc00y:local");
    }

    @Test
    public void turtle_bad_surrogate_01() {
        surrogateBad("'\\ud800'");
    }

    @Test
    public void turtle_bad_surrogate_01a() {
        surrogateBad("'''\\ud800''''");
    }

    @Test
    public void turtle_bad_surrogate_02() {
        surrogateBad("'a\\ud800z'");
    }

    @Test
    public void turtle_bad_surrogate_02a() {
        surrogateBad("'''a\\ud800z'''");
    }

    @Test
    public void turtle_bad_surrogate_02b() {
        surrogateBad("<a\\ud800z>");
    }

    @Test
    public void turtle_bad_surrogate_03() {
        surrogateBad("'\\udfff'");
    }

    @Test
    public void turtle_bad_surrogate_04() {
        surrogateBad("'a\\udfffz'");
    }

    @Test
    public void turtle_bad_surrogate_05() {
        surrogateBad("'\\U0000d800'");
    }

    @Test
    public void turtle_bad_surrogate_06() {
        surrogateBad("'a\\U0000d800z'");
    }

    @Test
    public void turtle_bad_surrogate_07() {
        surrogateBad("'\\U0000dfff'");
    }

    @Test
    public void turtle_bad_surrogate_08() {
        surrogateBad("'a\\U0000dfffz'");
    }

    @Test
    public void turtle_bad_surrogate_09() {
        // Wrong way round: low-high;
        surrogateBad("'\\udc00\\ud800'");
    }

    @Test
    public void turtle_bad_surrogate_10() {
        // Wrong way round: low-high
        surrogateBad("'a\\udc00\ud800z'");
    }

    // Compilation failure - illegal escape character
//    @Test
//     public void turtle_bad_surrogate_11() {
//      // raw low - escaped high
//      assertThrows(RiotParseException.class, ()->
//                   surrogate("'\ud800\\ud800'");
//     }
//
//     @Test
//     public void turtle_bad_surrogate_12() {
//      // raw low - escaped high
//      assertThrows(RiotParseException.class, ()->
//                   surrogate("'a\ud800\\ud800z'");
//     }

    @Test
    public void turtle_bad_surrogate_13() {
        // escaped low - raw high
        surrogateBad("'\\udc00\ud800'");
    }

    @Test
    public void turtle_bad_surrogate_14() {
        // escaped low - raw high
        surrogateBad("'a\\dc00\ud800z'");
    }
}
