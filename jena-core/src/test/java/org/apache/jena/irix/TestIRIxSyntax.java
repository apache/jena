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

package org.apache.jena.irix;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Basic tests of RFC 3986 syntax.
 *
 * {@link TestRFC3986} contained tests with more scheme errors and warnings. It also compares to jena-iri.
 */
@RunWith(Parameterized.class)
public class TestIRIxSyntax extends AbstractTestIRIx {

    public TestIRIxSyntax(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test public void http_01()      { parse("http://example/abc"); }

    @Test public void http_02()      { parse("http://example/αβγ"); }

    @Test public void http_03()      { parse("http://example/Ẓ"); }

    @Test public void http_04()      { parse("http://[::1]/abc"); }

    @Test public void http_05()      { parse("http://reg123/abc"); }

    @Test public void http_06()      { parse("http://1.2.3.4/abc"); }

    // ---- Compliance with HTTP RFC7230. https://tools.ietf.org/html/rfc7230#section-2.7

    @Test(expected=IRIException.class)
    public void http_51() { parse("http:"); }

    @Test(expected=IRIException.class)
    public void http_52() { parse("http:/"); }

    @Test(expected=IRIException.class)
    public void http_53() { parse("http://"); }

    @Test public void http_54() { parse("http://x"); }

    @Test(expected=IRIException.class)
    public void http_55()   { parse("http:abc"); }

    @Test(expected=IRIException.class)
    public void http_56()   { parse("http:///abc"); }

    @Test(expected=IRIException.class)
    // [] not in IPv6 address
    public void http_57()   { parse("http://h/ab[]"); }

    @Test public void http_58() { parse("http://example/~jena/file"); }

    // -- Compliance with URN scheme: https://tools.ietf.org/html/rfc8141

    @Test public void urn_01() { parse("urn:NID:NSS"); }

    @Test(expected=IRIException.class)
    public void urn_02() { parse("urn:x:abcd"); }

    @Test(expected=IRIException.class)
    public void urn_03() { parse("urn:ex:"); }

    @Test public void urn_04()  { notStrict("urn", ()->parse("urn:x:abc")); }

    @Test public void urn_05()  { notStrict("urn", ()->parse("urn:ex:")); }

    @Test public void urn_06()  { parse("urn:NID:NSS?=abc"); }

    @Test public void urn_07()  { parse("urn:NID:NSS?+abc"); }

    @Test public void urn_08()  { parse("urn:NID:NSS#frag"); }

    @Test public void urn_09()  { parse("urn:NID:NSS#"); }

    private static String testUUID = "aa045fc2-a781-11eb-9041-afa3877612ee";

    @Test public void parse_uuid_01() { parse("uuid:"+testUUID); }

    @Test public void parse_uuid_02() { parse("uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }

    @Test public void parse_uuid_03() { parse("UUID:"+testUUID); }

    @Test public void parse_uuid_04() { parse("urn:uuid:"+testUUID); }

    @Test public void parse_uuid_05() { parse("urn:uuid:"+(testUUID.toUpperCase(Locale.ROOT))); }

    @Test public void parse_uuid_06() { parse("URN:UUID:"+testUUID); }

    // -- Compliance with file scheme: https://tools.ietf.org/html/rfc8089

    @Test public void file_01() { parse("file:///path/name"); }

    @Test public void file_02() { parse("file:/path/name"); }

    @Test public void file_03() { parse("file:name"); }

    @Test public void file_04() { parse("file:/path/name"); }

    @Test public void file_05() { parse("file:name"); }

    @Test public void file_06() { parse("file:///c:/~user/file"); }

    // Parse, only collect violations from scheme-specific rules.
    private void parse(String string) {
        IRIx iri = IRIx.create(string);
    }
}
