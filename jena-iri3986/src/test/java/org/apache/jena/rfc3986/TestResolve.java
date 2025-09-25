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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestResolve {

    @Test public void resolve_blank_ref_1() { testResolve("http://example/dir/", "", "http://example/dir/"); }
    @Test public void resolve_blank_ref_2() { testResolve("http://example/dir/x", "", "http://example/dir/x"); }
    @Test public void resolve_blank_ref_3() { testResolve("http://example/", "", "http://example/"); }
    @Test public void resolve_blank_ref_4() { testResolve("http://example/x", "", "http://example/x"); }
    @Test public void resolve_blank_ref_5() { testResolve("http://example/x//y", "", "http://example/x//y"); }
    @Test public void resolve_blank_ref_6() { testResolve("http://example//x/y", "", "http://example//x/y"); }

    @Test public void resolve_abs_01() { testResolve("http://example/path?query#frag", "http://host", "http://host"); }
    @Test public void resolve_abs_02() { testResolve("http://example/path?query#frag", "http://host/dir1/dir2/", "http://host/dir1/dir2/"); }

    @Test public void resolve_abs_03() {
        // Assumes normalization of rel (arg2).
        // jena-iri does this on resolve.
        // iri3986 does not by default
        testResolveNormalise("http://example/path?query#frag", "http://host/dir1/../dir2/", "http://host/dir2/");
    }

    @Test public void resolve_abs_10() { testResolve("http://example/dir1/dir2/", "/OtherPath", "http://example/OtherPath"); }
    @Test public void resolve_abs_11() { testResolve("http://example/dir1/dir2/", "//EX/OtherPath", "http://EX/OtherPath"); }
    @Test public void resolve_abs_12() { testResolve("http:", "//EX/OtherPath", "http://EX/OtherPath"); }

    @Test public void resolve_abs_20() { testResolve("https://example/", "//", "https://"); }
    @Test public void resolve_abs_21() { testResolve("https://example/", "//host", "https://host"); }
    @Test public void resolve_abs_22() { testResolve("https://example/", "//host/", "https://host/"); }
    @Test public void resolve_abs_23() { testResolve("https://example/", "//host/path", "https://host/path"); }

    @Test public void resolve_ref_1() { testResolve("http://example/dir/", "A", "http://example/dir/A"); }
    @Test public void resolve_ref_2() { testResolve("http://example/dir", "A", "http://example/A"); }
    @Test public void resolve_ref_3() { testResolve("http://example/dir", "A/", "http://example/A/"); }
    @Test public void resolve_ref_4() { testResolve("http://example/dir/", "A/", "http://example/dir/A/"); }

    // Different scheme.
    @Test public void resolve_ref_5() { testResolve("http://example/", "https:subdir/", "https:subdir/"); }
    @Test public void resolve_ref_6() { testResolve("http://example/", "https:subdir", "https:subdir"); }
    @Test public void resolve_ref_7() { testResolve("http://example/", "urn:foo/", "urn:foo/"); }

    @Test public void resolve_dot_01() { testResolve("http://example/dir1/dir2/", ".", "http://example/dir1/dir2/"); }
    @Test public void resolve_dot_02() { testResolve("http://example/", ".", "http://example/"); }
    @Test public void resolve_dot_03() { testResolve("http://example/dir1/dir2/x", ".", "http://example/dir1/dir2/"); }
    @Test public void resolve_dot_04() { testResolve("http://example/x", ".", "http://example/"); }
    @Test public void resolve_dot_05() { testResolve("http://example/dir/", "./.", "http://example/dir/"); }
    @Test public void resolve_dot_06() { testResolve("http://example/", "./.", "http://example/"); }
    @Test public void resolve_dot_07() { testResolve("http://example/x", "./.", "http://example/"); }
    @Test public void resolve_dot_08() { testResolve("http://example/dir/x", "./.", "http://example/dir/"); }
    @Test public void resolve_dot_09() { testResolve("http://example", ".", "http://example/"); }

    @Test public void resolve_dotdot_00() { testResolve("http://example/dir1/dir2/dir3/", "..", "http://example/dir1/dir2/"); }
    @Test public void resolve_dotdot_01() { testResolve("http://example/", "..", "http://example/"); }
    @Test public void resolve_dotdot_02() { testResolve("http://example/x", "..", "http://example/"); }
    @Test public void resolve_dotdot_03() { testResolve("http://example/dir/", "..", "http://example/"); }
    @Test public void resolve_dotdot_10() { testResolve("http://example/dir1/dir2/dir3/", "../..", "http://example/dir1/"); }
    @Test public void resolve_dotdot_11() { testResolve("http://example/dir1/dir2/dir3/", "../../..", "http://example/"); }
    @Test public void resolve_dotdot_12() { testResolve("http://example/dir1/dir2/dir3/", "../../../..", "http://example/"); }
    @Test public void resolve_dotdot_13() { testResolve("http://example/dir1/dir2/dir3/x", "..", "http://example/dir1/dir2/"); }
    @Test public void resolve_dotdot_14() { testResolve("http://example/dir1/dir2/dir3/x", "../..", "http://example/dir1/"); }
    @Test public void resolve_dotdot_15() { testResolve("http://example/dir1/dir2/dir3/x", "../../..", "http://example/"); }
    @Test public void resolve_dotdot_16() { testResolve("http://example/dir1/dir2/dir3/x", "../../../..", "http://example/"); }
    @Test public void resolve_dotdot_20() { testResolve("http://example", "..", "http://example/"); }

    @Test public void resolve_01() { testResolve("http://example/dir1/dir2/", "../.", "http://example/dir1/"); }
    @Test public void resolve_02() { testResolve("http://example/dir1/dir2/", "./..", "http://example/dir1/"); }
    @Test public void resolve_03() { testResolve("http://example/dir1/dir2/dir3/x", ".././..", "http://example/dir1/"); }
    @Test public void resolve_04() { testResolve("http://example/dir1/dir2/", "./../A", "http://example/dir1/A"); }
    @Test public void resolve_05() { testResolve("http://example/dir1/dir2/", "./../A/.", "http://example/dir1/A/"); }
    @Test public void resolve_06() { testResolve("http://example/dir1/dir2/", "../A", "http://example/dir1/A"); }
    @Test public void resolve_07() { testResolve("http://example/dir1/dir2/f3", "../A", "http://example/dir1/A"); }
    @Test public void resolve_08() { testResolve("http://example/f2", "../A", "http://example/A"); }
    @Test public void resolve_09() { testResolve("http://example/", "../A", "http://example/A"); }

    @Test public void resolve_parts_01() { testResolve("http://example/#fragment", "path?q=arg", "http://example/path?q=arg"); }
    @Test public void resolve_parts_02() { testResolve("http://example/", "../path?q=arg", "http://example/path?q=arg"); }
    @Test public void resolve_parts_03() { testResolve("http://example/?query", "../path?q=arg", "http://example/path?q=arg"); }
    @Test public void resolve_parts_04() { testResolve("http://example/?query", "../path?q=arg", "http://example/path?q=arg"); }
    @Test public void resolve_parts_05() { testResolve("http://example/path", "?query", "http://example/path?query"); }
    @Test public void resolve_parts_06() { testResolve("http://example/path", "#frag", "http://example/path#frag"); }
    @Test public void resolve_parts_07() { testResolve("http://example/path", "..#frag", "http://example/#frag"); }
    @Test public void resolve_parts_08() { testResolve("http://example/path#fragment", "..#frag", "http://example/#frag"); }

    @Test public void resolve_rel_base_01() { testResolve("a/b", "c/d", "a/c/d"); }
    @Test public void resolve_rel_base_02() { testResolve("a/b/", "c/d", "a/b/c/d"); }
    @Test public void resolve_rel_base_03() { testResolve("a", ".", ""); }
    @Test public void resolve_rel_base_04() { testResolve("d/", ".", "d/"); }
    @Test public void resolve_rel_base_05() { testResolve("a", "..", ""); }
    @Test public void resolve_rel_base_06() { testResolve("dir/a", "..", ""); }
    @Test public void resolve_rel_base_07() { testResolve("dir1/dir2/", "..", "dir1/"); }
    @Test public void resolve_rel_base_08() { testResolve("a//b", ".", "a//"); }
    @Test public void resolve_rel_base_09() { testResolve("a/b", "//EX/OtherPath", "//EX/OtherPath"); }
    @Test public void resolve_rel_base_10() { testResolve("a/b", "/OtherPath", "/OtherPath"); }

    // Test, no additional normalization.
    private void testResolve(String base, String rel, String expected) {
        IRI3986 baseiri = IRI3986.create(base);
        IRI3986 reliri = IRI3986.create(rel);
        IRI3986 iri2 = baseiri.resolve(reliri);
        String s1 = iri2.str();
        assertEquals(expected, s1);
    }

    // Test, with additional normalization.
    private void testResolveNormalise(String base, String rel, String expected) {
        IRI3986 baseiri = IRI3986.create(base);
        IRI3986 reliri = IRI3986.create(rel);
        // Normalize after resolving even if no change on resolving.
        IRI3986 iri2 = baseiri.resolve(reliri).normalize();
        String s1 = iri2.str();
        assertEquals(expected, s1);
    }
}
