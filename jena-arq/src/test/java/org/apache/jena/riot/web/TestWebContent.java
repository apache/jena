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

package org.apache.jena.riot.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.WebContent;

public class TestWebContent {

    // CanonicaliseMediaTypes tests check that "Alt" forms map to the right type.
    // Mostly, the alternative names have been removed as general standards
    // compliance on the web has improved.
    //
    // Elsewhere, "text/plain" is specially handled in content negotiation,
    // not by canonicalised media types.
    // because files published on the web by simply placing in a directory of the
    // HTTP server on the web can return "text/plain".

    @Test
    public void testCanonicaliseMediaTypes1() {
        testCanonicalise(WebContent.contentTypeTurtle, WebContent.contentTypeTurtle);
    }

    @Test
    public void testCanonicaliseMediaTypes2() {
        testCanonicalise(WebContent.contentTypeN3, WebContent.contentTypeN3);
        testCanonicalise(WebContent.contentTypeN3Alt1, WebContent.contentTypeN3);
        testCanonicalise(WebContent.contentTypeN3Alt2, WebContent.contentTypeN3);
    }

    @Test
    public void testCanonicaliseMediaTypes3() {
        testCanonicalise(WebContent.contentTypeNTriples, WebContent.contentTypeNTriples);
    }

    @Test
    public void testCanonicaliseMediaTypes4() {
        testCanonicalise(WebContent.contentTypeNQuads, WebContent.contentTypeNQuads);
    }

    @Test
    public void testCanonicaliseMediaTypes5() {
        testCanonicalise(WebContent.contentTypeTriG, WebContent.contentTypeTriG);
    }

    private void testCanonicalise(String input, String expected) {
        String canonical = WebContent.contentTypeCanonical(input);
        assertEquals(expected, canonical);
    }
}
