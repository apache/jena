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

import org.apache.jena.riot.RiotException;

// Tests for the variant of TokenizerText that handles escaped surrogates
public class TestTokenizerTextNoSurrogates extends AbstractTestTokenizerTextSurrogates {

    @Override
    protected Tokenizer tokenizer(String string) {
        return TokenizerText.fromString(string);
    }

    // Reject all use of surrogates.

    // Structurally bad.
    @Override
    protected void surrogateBad(String string) {
        assertThrows(RiotException.class, ()->surrogateTest(string));
    }

    // Validly encoded into escape sequences.
    // Jena now rejects all surrogates, via any route.
    @Override
    protected void surrogateValidEsc(String string) {
        assertThrows(RiotException.class, ()->surrogateTest(string));
    }
}
