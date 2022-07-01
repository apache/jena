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

package org.apache.jena.atlas.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestIRILib {

    @Test public void encodeDecode01() { encodeDecode("", ""); }

    @Test public void encodeDecode02() { encodeDecode("aa", "aa"); }

    @Test public void encodeDecode03() { encodeDecode("Größe", "Gr%C3%B6%C3%9Fe"); }

    // Test mechanisms with the ASCII encoder.
    private void encodeDecode(String testString, String expected) {
        String encoded = IRILib.encodeNonASCII(testString);
        assertEquals(expected, encoded);
        String decoded = IRILib.decodeHex(encoded);
        assertEquals(testString, decoded);
    }

    private void encodeDecodeQueryFrag(String testString, String expected) {
        String encoded = IRILib.encodeUriQueryFrag(testString);
        assertEquals(expected, encoded);
        String decoded = IRILib.decodeHex(encoded);
        assertEquals(testString, decoded);
    }

    @Test
    public void codec_queryFrag_01() {
        encodeDecodeQueryFrag("Größe", "Größe");
    }

    // Test use of query string encoder on a URI.
    // e.g. Graph Store Protocol usage ?graph=http://example/
    // RFC 3986: '?' in the encoded form is safe - a query strign starts
    // with a '?' and then from then on '?' is a plain character.
    @Test
    public void codec_queryFrag_02() {
        encodeDecodeQueryFrag("http://example/graph?name=value#zzzz", "http://example/graph?name%3Dvalue%23zzzz");
    }
}

