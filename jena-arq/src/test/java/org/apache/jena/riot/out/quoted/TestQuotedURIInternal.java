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

package org.apache.jena.riot.out.quoted;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.out.TestOutputQuotedURI;

/**
 * Tests for the different options in QuoteURI.
 * {@link  TestOutputQuotedURI} tests the API.
 */
public class TestQuotedURIInternal {
    // Must be in the same package as QuotedURI.

    // Normal
    @Test
    public void write_uri_11() {
        testQuotedURI("http://example/", "http://example/", QuotedURI::writeDirect);
    }

    @Test
    public void write_uri_12() {
        testQuotedURI("http://example/", "http://example/", QuotedURI::writeUnicodeEscapeBadChars);
    }

    @Test
    public void write_uri_13() {
        testQuotedURI("http://example/", "http://example/", QuotedURI::writeExceptionOnBadChar);
    }

    @Test
    public void write_uri_21() {
        testQuotedURI("http://example/αβγ", "http://example/αβγ", QuotedURI::writeDirect);
    }

    @Test
    public void write_uri_22() {
        testQuotedURI("http://example/αβγ", "http://example/αβγ", QuotedURI::writeUnicodeEscapeBadChars);
    }

    @Test
    public void write_uri_23() {
        testQuotedURI("http://example/αβγ", "http://example/αβγ", QuotedURI::writeExceptionOnBadChar);
    }

    // Error cases
    @Test
    public void write_uri_51() {
        testQuotedURI("http://example/abc def", "http://example/abc def", QuotedURI::writeDirect);
    }

    @Test
    public void write_uri_52() {
        testQuotedURI("http://example/abc def", "http://example/abc\\u0020def", QuotedURI::writeUnicodeEscapeBadChars);
    }

    @Test
    public void write_uri_61() {
        testQuotedURI("http://example/abc{}def", "http://example/abc\\u007B\\u007Ddef", QuotedURI::writeUnicodeEscapeBadChars);
    }

    @Test
    public void write_uri_62() {
        testQuotedURI("http://example/abc{}def", "http://example/abc%7B%7Ddef", QuotedURI::writePercentEncodedeBadChars);
    }

    @Test
    public void write_uri_71() {
        assertThrows(RiotException.class, ()->
            testQuotedURI("http://example/abc def", "http://example/abc", QuotedURI::writeExceptionOnBadChar)
        );
    }

    private static void testQuotedURI(String input, String expected, BiConsumer<AWriter, String> quoteOperation) {
        IndentedLineBuffer x = new IndentedLineBuffer();
        quoteOperation.accept(x, input);
        String s = x.asString();
        assertEquals(expected, s);
    }
}
