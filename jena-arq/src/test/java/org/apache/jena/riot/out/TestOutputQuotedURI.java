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

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.out.quoted.QuotedURI;
import org.apache.jena.riot.out.quoted.TestQuotedURIInternal;

/**
 * Tests for URI output.
 * See also {@link TestQuotedURIInternal} for other tests that call the implementation options.
 */
public class TestOutputQuotedURI {

    @Test public void quoted_uri_api_1() {
        testQuotedURI("http://example/", "<http://example/>", true);
    }

    @Test public void quoted_uri_api_2() {
        testQuotedURI("http://example/αβγ", "<http://example/αβγ>", true);
    }

    @Test public void quoted_uri_api_3() {
        testQuotedURI("http://example/١٢٣", "<http://example/١٢٣>", true);
    }

    @Test public void quoted_uri_api_encoding_1() {
        // If percent encoding. Space in, something out.
        //testQuotedURI("http://example/abc def", "<http://example/abc%20def>", false);
        // If Unicode UCHAR escaping. Note double \\ - use Java escape to get a single \ into the string.
        testQuotedURI("http://example/abc def", "<http://example/abc\\u0020def>", false);
    }

    @Test public void quoted_uri_api_encoding_2() {
        // Test encoding setup. Raw tab in, something for the tab out. Tab is a control character
        // If percent encoding.
        //testQuotedURI("http://example/abc\u0009def", "<http://example/abc%09def>", false);
        // If Unicode UCHAR escaping.
        testQuotedURI("http://example/abc\u0009def", "<http://example/abc\\u0009def>", false);
    }

    @Test public void quoted_uri_api_encoding_3() {
        // Test encoding setup. Illegal {} in the URI string
        // If percent encoding.
        //testQuotedURI("http://example/_{_{}_", "<http://example/_%7B_7D_>", false);
        // If Unicode UCHAR escaping.
        testQuotedURI("http://example/_{_}_", "<http://example/_\\u007B_\\u007D_>", false);
    }

    private static void testQuotedURI(String input, String expected, boolean withWarnings) {
        Runnable r = ()->{
            QuotedURI quoter = new QuotedURI();
            IndentedLineBuffer x = new IndentedLineBuffer();
            quoter.writeURI(x, input);
            String s = x.asString();
            assertEquals(expected, s);
        };
        if ( withWarnings )
            r.run();
        else
            LogCtl.withLevel(SysRIOT.getLogger(), "ERROR", r);
    }
}
