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

/**
 * Basic parser tests and IRIx operations.
 *
 * {@link TestRFC3986} contained tests with exceptions scheme errors and warnings.
 */
@RunWith(Parameterized.class)
public class TestIRIxOps extends AbstractTestIRIx {


    public TestIRIxOps(String name, IRIProvider provider) {
        super(name, provider);
    }

    // --- Use in RDF

    @Test public void reference_01() { reference("http://example/", true); }

    @Test public void reference_02() { reference("http://example/abcd", true); }

    @Test public void reference_03() { reference("//example/", false); }

    @Test public void reference_04() { reference("relative-uri", false); }

    @Test public void reference_05() { reference("http://example/", true); }

    @Test public void reference_06() { reference("http://example/", true); }

    @Test public void reference_07() { reference("http://example/", true); }

    @Test public void reference_08() { reference("file:///a:/~jena/file", true); }

    @Test public void reference_09() { reference("http://example/abcd#frag", true); }

    // -- isAbsolute, isRelative : These are not opposites in RFC 3986. (String, isAbsolute, isRelative)

    @Test public void abs_rel_01()   { test_abs_rel("http://example/abc", true, false); }

    @Test public void abs_rel_02()   { test_abs_rel("abc", false, true); }

    @Test public void abs_rel_03()   { test_abs_rel("http://example/abc#def", false, false); }

    @Test public void abs_rel_04()   { test_abs_rel("abc#def", false, true); }

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

    @Test public void relative_http_08() { relative("http://example/dir1/dir2/path", "http://example/otherDir/abcd", "/otherDir/abcd"); }

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
    private void test_abs_rel(String uriStr, boolean isAbsolute, boolean isRelative) {
        IRIx iri = IRIx.create(uriStr);
        assertEquals("Absolute test: IRI = "+uriStr, isAbsolute, iri.isAbsolute());
        assertEquals("Relative test: IRI = "+uriStr, isRelative, iri.isRelative());
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

    // Parse, only collect violations from scheme-specific rules.
    private void parse(String string) {
        IRIx iri = IRIx.create(string);
    }
}
