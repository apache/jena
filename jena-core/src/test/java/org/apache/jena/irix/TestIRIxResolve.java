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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestIRIxResolve extends AbstractTestIRIx_3986 {

    public TestIRIxResolve(String name, IRIProvider provider) {
        super(name, provider);
    }

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


    @Test
    public void resolve_01() {
        resolve("http://example/dir/", "", "http://example/dir/");
    }

    @Test
    public void resolve_02() {
        resolve("http://example/dir/", "A", "http://example/dir/A");
    }

    @Test
    public void resolve_03() {
        resolve("http://example/dir", "A", "http://example/A");
    }

    @Test
    public void resolve_04() {
        resolve("http://example/dir", "A/", "http://example/A/");
    }

    @Test
    public void resolve_05() {
        resolve("http://example/dir1/dir2/dir3/dir4", "..", "http://example/dir1/dir2/");
    }

    @Test
    public void resolve_06() {
        resolve("http://example/dir1/dir2/", "..", "http://example/dir1/");
    }

    @Test
    public void resolve_07() {
        resolve("http://example/dir1/dir2", "..", "http://example/");
    }

    @Test
    public void resolve_08() {
        resolve("http://example/dir1/dir2/f3", "..", "http://example/dir1/");
    }

    @Test
    public void resolve_09() {
        resolve("http://example/dir1/dir2/", "../a", "http://example/dir1/a");
    }

    @Test
    public void resolve_10() {
        resolve("http://example/dir1/dir2/f3", "../a", "http://example/dir1/a");
    }

    @Test
    public void resolve_11() {
        resolve("http://example/dir1/f2", "../a", "http://example/a");
    }

    @Test
    public void resolve_12() {
        resolve("http://example/dir1/dir2/", "..", "http://example/dir1/");
    }

    @Test
    public void resolve_13() {
        resolve("http://example/dir/", "..", "http://example/");
    }

    @Test
    public void resolve_14() {
        resolve("http://example/dir", "..", "http://example/");
    }

    @Test
    public void resolve_15() {
        resolve("http://example/", "..", "http://example/");
    }

    @Test
    public void resolve_16() {
        resolve("http://example", "..", "http://example/");
    }

    @Test
    public void resolve_17() {
        resolve("http://example/path?query#frag", "http://host", "http://host");
    }

    @Test
    public void resolve_18() {
        resolve("http://example/", "abc", "http://example/abc");
    }

    @Test
    public void resolve_19() {
        resolve("http://example", "abc", "http://example/abc");
    }

    @Test
    public void resolve_20() {
        resolve("http://example/dir/file", ".", "http://example/dir/");
    }

    @Test
    public void resolve_21() {
        resolve("http://example/dir/", ".", "http://example/dir/");
    }

    @Test
    public void resolve_22() {
        resolve("http://example/dir1/dir2/", ".", "http://example/dir1/dir2/");
    }

    @Test
    public void resolve_23() {
        resolve("http://example/", ".", "http://example/");
    }

    @Test
    public void resolve_24() {
        resolve("http://example/#fragment", "path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_25() {
        resolve("http://example/", "../path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_26() {
        resolve("http://example/?query", "../path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_27() {
        resolve("http://example/?query", "../path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_28() {
        resolve("http://example/path", "?query", "http://example/path?query");
    }

    @Test
    public void resolve_29() {
        resolve("http://example/path", "#frag", "http://example/path#frag");
    }

    @Test
    public void resolve_30() {
        resolve("http://example/path", "..#frag", "http://example/#frag");
    }

    @Test
    public void resolve_31() {
        resolve("http://example/path#fragment", "..#frag", "http://example/#frag");
    }

    @Test
    public void resolve_32() {
        resolve("http://example/dir1/dir2/", "/OtherPath", "http://example/OtherPath");
    }

    @Test
    public void resolve_33() {
        resolve("http://example/dir1/dir2/", "//EX/OtherPath", "http://EX/OtherPath");
    }

    @Test
    public void resolve_34() {
        String uuid = "urn:uuid:e79b5752-a82e-11eb-8c4e-cba73c34870a";
        resolve("http://example/base#", uuid, uuid);
    }

    private void resolve(String base, String rel, String expected) {
        IRIx baseIRI = test_create(base);
        IRIx relIRI = test_create(rel);
        IRIx iri2 = baseIRI.resolve(relIRI);
        String s1 = iri2.str();
        assertEquals(expected, s1);
    }
}
