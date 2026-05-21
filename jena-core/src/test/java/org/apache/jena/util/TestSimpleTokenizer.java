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

package org.apache.jena.util;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TestSimpleTokenizer {

    @Test
    public void testTokenizer() {
        SimpleTokenizer tokenizer = new SimpleTokenizer("a(foo,bar)  'i am a literal' so there", "()[], \t\n\r'", "'", true);
        assertEquals("a", tokenizer.nextToken());
        assertEquals("(", tokenizer.nextToken());
        assertEquals("foo", tokenizer.nextToken());
        assertEquals(",", tokenizer.nextToken());
        assertEquals("bar", tokenizer.nextToken());
        assertEquals(")", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("'", tokenizer.nextToken());
        assertEquals("i am a literal", tokenizer.nextToken());
        assertEquals("'", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("so", tokenizer.nextToken());
        assertEquals(" ", tokenizer.nextToken());
        assertEquals("there", tokenizer.nextToken());
        assertFalse(tokenizer.hasMoreTokens());

        tokenizer = new SimpleTokenizer("a(foo,bar)  'i am a literal' so there", "()[], \t\n\r'", "'", false);
        assertEquals("a", tokenizer.nextToken());
        assertEquals("foo", tokenizer.nextToken());
        assertEquals("bar", tokenizer.nextToken());
        assertEquals("i am a literal", tokenizer.nextToken());
        assertEquals("so", tokenizer.nextToken());
        assertEquals("there", tokenizer.nextToken());
        assertFalse(tokenizer.hasMoreTokens());

    }

}
