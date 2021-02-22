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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestIRIx extends AbstractTestIRIx {

    public TestIRIx(String name, IRIProvider provider) {
        super(name, provider);
    }

    // ---- RFC 3986 Grammar

    @Test public void uri_01()      { test("http://example/abc"); }

    @Test public void uri_02()      { test("http://example/αβγ"); }

    @Test public void uri_03()      { test("http://example/Ẓ"); }

    @Test public void uri_04()      { test("http://[::1]/abc"); }

    @Test public void uri_05()      { test("http://reg123/abc"); }

    @Test public void uri_06()      { test("http://1.2.3.4/abc"); }

    // ---- Compliance with HTTP RFC7230. https://tools.ietf.org/html/rfc7230#section-2.7
    @Test(expected=IRIException.class)
    public void http_01() { parse("http:"); }

    @Test(expected=IRIException.class)
    public void http_02() { parse("http:/"); }

    @Test(expected=IRIException.class)
    public void http_03() { parse("http://"); }

    @Test public void http_04() { parse("http://x"); }

    @Test(expected=IRIException.class)
    public void http_05()   { parse("http:abc"); }

    @Test(expected=IRIException.class)
    public void http_06()   { parse("http:///abc"); }

    @Test(expected=IRIException.class)
    // [] not in IPv6 address
    public void http_07()   { parse("http://h/ab[]"); }

    @Test public void http_08() { parse("http://example/~jena/file"); }

    // -- Compliance with URN scheme: https://tools.ietf.org/html/rfc8141

    @Test public void urn_01() { parse("urn:NID:NSS"); }

    @Test(expected=IRIException.class)
    public void urn_02() { parse("urn:x:abcd"); }

    @Test(expected=IRIException.class)
    public void urn_03() { parse("urn:ex:"); }

    @Test public void urn_04()  { notStrict("urn", ()->parse("urn:x:abc")); }

    @Test public void urn_05()  { notStrict("urn", ()->parse("urn:ex:")); }

    // -- Compliance with file scheme: https://tools.ietf.org/html/rfc8089

    @Test public void file_01() { parse("file:///path/name"); }

    @Test public void file_02() { parse("file:/path/name"); }

    @Test public void file_03() { parse("file:name"); }

    @Test public void file_04() { parse("file:/path/name"); }

    @Test public void file_05() { parse("file:name"); }

    @Test public void file_06() { parse("file:///c:/~user/file"); }

    // --- Use in RDF

    @Test public void reference_01() { reference("http://example/", true); }

    @Test public void reference_02() { reference("http://example/abcd", true); }

    @Test public void reference_03() { reference("//example/", false); }

    @Test public void reference_04() { reference("relative-uri", false); }

    @Test public void reference_05() { reference("http://example/", true); }

    @Test public void reference_06() { reference("http://example/", true); }

    @Test public void reference_07() { reference("http://example/", true); }

    @Test public void reference_08() { reference("file:///a:/~jena/file", true); }

    // -- Resolving
    @Test public void resolve_http_01() { resolve("http://example/", "path", "http://example/path"); }

    @Test public void resolve_http_02() { resolve("http://example/dirA/dirB/", "/path", "http://example/path"); }

    @Test public void resolve_http_03() { resolve("https://example/dirA/file", "path", "https://example/dirA/path"); }

    // <>
    @Test public void resolve_http_04() { resolve("http://example/doc", "", "http://example/doc"); }

    //<#>
    @Test public void resolve_http_05() { resolve("http://example/ns", "#", "http://example/ns#"); }

    @Test public void resolve_http_06() { resolve("http://example/ns", "#", "http://example/ns#"); }

    @Test public void resolve_file_01() { resolve("file:///dir1/dir2/", "path", "file:///dir1/dir2/path"); }

    @Test public void resolve_file_02() { resolve("file:///dir/file", "a/b/c", "file:///dir/a/b/c"); }

    @Test public void resolve_file_03() { resolve("file:///dir/file", "/a/b/c", "file:///a/b/c"); }

    @Test public void resolve_file_04() { resolve("file:///dir/file", "file:ABC", "file:///dir/ABC"); }

    @Test public void resolve_file_05() { resolve("file:///dir/file", "file:/ABC", "file:///ABC"); }

    // No trailing slash, not considered to be a "directory".
    @Test public void relative_http_01() { relative("http://example/dir", "http://example/dir/abcd", "dir/abcd"); }

    @Test public void relative_http_02() { relative("http://example/dir", "http://example/dir/abcd", "dir/abcd"); }

    @Test public void relative_http_03() { relative("http://example/dir/ab", "http://example/dir/abcd", "abcd"); }

    @Test public void relative_http_04() { relative("http://example/dir/", "http://example/dir/abcd#frag", "abcd#frag"); }

    @Test public void relative_http_05() { relative("http://example/abcd", "http://example/abcd#frag", "#frag"); }

    @Test public void relative_http_06() { relative("http://example/abcd", "http://example/abcd?query=qs", "?query=qs"); }

    @Test public void relative_http_07() { relative("http://example/abcd", "http://example/abcd?query=qs#f", "?query=qs#f"); }

    @Test public void relative_http_08() { relative("http://example/dir1/dir2/path", "http://example/otherDir/abcd", null); }

    @Test public void relative_http_09() { relative("http://example/path", "http://example/path", ""); }

    @Test public void relative_http_10() { relative("http://example/path", "http://example/path#", "#"); }

    @Test public void relative_file_01() { relative("file:///dir/", "file:///dir/abcd", "abcd"); }

    @Test public void relative_file_02() { relative("file:///", "file:///dir/abcd", "dir/abcd"); }

    // ---- Things expected.

    @Test public void misc_01()     { reference("wm:/abc", true); }

    private void relative(String baseUriStr, String otherStr, String expected) {
        IRIx base = IRIx.create(baseUriStr);
        IRIx relInput = IRIx.create(otherStr);
        IRIx relativized = base.relativize(relInput);
        String result = (relativized==null) ? null : relativized.str();
        assertEquals("Base=<"+baseUriStr+"> IRI=<"+otherStr+">", expected, result);
    }

    private void resolve(String baseUriStr, String otherStr, String expected) {
        String iriStr = IRIs.resolve(baseUriStr, otherStr);
        assertEquals("Base=<"+baseUriStr+"> Rel=<"+otherStr+">", expected, iriStr);
    }

    // Create - is it suitable for an RDF reference?
    private void reference(String uriStr) {
        IRIx iri = IRIx.create(uriStr);
        assertTrue("IRI = "+uriStr, iri.isReference());
    }

    private void reference(String uriStr, boolean expected) {
        IRIx iri = IRIx.create(uriStr);
        assertEquals("IRI = "+uriStr, expected, iri.isReference());
    }

    // Parser/check/see if the string is the same.
    private void test(String uriStr) {
        IRIx iri = IRIx.create(uriStr);
        String x = iri.str();
        assertEquals(uriStr, x);
    }

    // Parse, validate against scheme-specific rules.
    private void parse(String string) {
        IRIx iri = IRIx.create(string);
    }
}
