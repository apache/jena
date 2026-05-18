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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.RiotException;

// Tests for the variant of TokenizerText that handles escaped surrogates
public class TestTokenizerTextAllowEscSurrogates extends AbstractTestTokenizerTextSurrogates {

    @Override
    protected Tokenizer tokenizer(String string) {
        return TokenizerTextSurrogates.fromString(string);
    }

    // Structurally bad.
    @Override
    protected void surrogateBad(String string) {
        assertThrows(RiotException.class, ()->surrogateTest(string));
    }

    // Validly encoded into escape sequences.
    // Jena now rejects all surrogates, via any route.
    // Testing the legacy TokenizerTextSurrogates
    @Override
    protected void surrogateValidEsc(String string) {
        surrogateTest(string);
    }

    // These are "surrogateRawBad" in the "no surrogates" test suite
    // but valid (and junk) if accepting surrogates.
    // 6.1.0 compatibility.
    // The esc-raw case is wrong in TokenizerTextSurrogates.checkCodepoint.
    // It inserts a high surrogate early which then gets a real raw low surrogate and "works".
    // Probably not what was intended!

    @Override
    @Test public void turtle_surrogate_pair_esc_raw_internal_01() {
        //surrogateValidEsc("'z\\ud800\udc00z'");
        surrogateValidEsc("'\\ud800\udc00z'");
    }

    @Override
    @Test public void turtle_surrogate_pair_esc_raw_internal_02() {
        surrogateValidEsc("'''z\\ud800\udc00z'''");
    }

    @Override
    @Test public void turtle_surrogate_pair_esc_raw_internal_03() {
        // escaped high, raw low
        surrogateValidEsc("<z\\ud800\udc00z>");
    }

    @Override
    @Test public void turtle_surrogate_pair_esc_raw_03() {
        surrogateValidEsc("<\\ud800\udc00>");
    }
}
