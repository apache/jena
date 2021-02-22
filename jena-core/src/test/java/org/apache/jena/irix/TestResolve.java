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
public class TestResolve extends AbstractTestIRIx {

    public TestResolve(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test
    public void resolve_01() {
        testResolve("http://example/dir/", "", "http://example/dir/");
    }

    @Test
    public void resolve_02() {
        testResolve("http://example/dir/", "A", "http://example/dir/A");
    }

    @Test
    public void resolve_03() {
        testResolve("http://example/dir", "A", "http://example/A");
    }

    @Test
    public void resolve_04() {
        testResolve("http://example/dir", "A/", "http://example/A/");
    }

    @Test
    public void resolve_05() {
        testResolve("http://example/dir1/dir2/dir3/dir4", "..", "http://example/dir1/dir2/");
    }

    @Test
    public void resolve_06() {
        testResolve("http://example/dir1/dir2/", "..", "http://example/dir1/");
    }

    @Test
    public void resolve_07() {
        testResolve("http://example/dir1/dir2", "..", "http://example/");
    }

    @Test
    public void resolve_08() {
        testResolve("http://example/dir1/dir2/f3", "..", "http://example/dir1/");
    }

    @Test
    public void resolve_09() {
        testResolve("http://example/dir1/dir2/", "../a", "http://example/dir1/a");
    }

    @Test
    public void resolve_10() {
        testResolve("http://example/dir1/dir2/f3", "../a", "http://example/dir1/a");
    }

    @Test
    public void resolve_11() {
        testResolve("http://example/dir1/f2", "../a", "http://example/a");
    }

    @Test
    public void resolve_12() {
        testResolve("http://example/dir1/dir2/", "..", "http://example/dir1/");
    }

    @Test
    public void resolve_13() {
        testResolve("http://example/dir/", "..", "http://example/");
    }

    @Test
    public void resolve_14() {
        testResolve("http://example/dir", "..", "http://example/");
    }

    @Test
    public void resolve_15() {
        testResolve("http://example/", "..", "http://example/");
    }

    @Test
    public void resolve_16() {
        testResolve("http://example", "..", "http://example/");
    }

    @Test
    public void resolve_17() {
        testResolve("http://example/path?query#frag", "http://host", "http://host");
    }

    @Test
    public void resolve_18() {
        testResolve("http://example/", "abc", "http://example/abc");
    }

    @Test
    public void resolve_19() {
        testResolve("http://example", "abc", "http://example/abc");
    }

    @Test
    public void resolve_20() {
        testResolve("http://example/dir/file", ".", "http://example/dir/");
    }

    @Test
    public void resolve_21() {
        testResolve("http://example/dir/", ".", "http://example/dir/");
    }

    @Test
    public void resolve_22() {
        testResolve("http://example/dir1/dir2/", ".", "http://example/dir1/dir2/");
    }

    @Test
    public void resolve_23() {
        testResolve("http://example/", ".", "http://example/");
    }

    @Test
    public void resolve_24() {
        testResolve("http://example/#fragment", "path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_25() {
        testResolve("http://example/", "../path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_26() {
        testResolve("http://example/?query", "../path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_27() {
        testResolve("http://example/?query", "../path?q=arg", "http://example/path?q=arg");
    }

    @Test
    public void resolve_28() {
        testResolve("http://example/path", "?query", "http://example/path?query");
    }

    @Test
    public void resolve_29() {
        testResolve("http://example/path", "#frag", "http://example/path#frag");
    }

    @Test
    public void resolve_30() {
        testResolve("http://example/path", "..#frag", "http://example/#frag");
    }

    @Test
    public void resolve_31() {
        testResolve("http://example/path#fragment", "..#frag", "http://example/#frag");
    }

    @Test
    public void resolve_32() {
        testResolve("http://example/dir1/dir2/", "/OtherPath", "http://example/OtherPath");
    }

    @Test
    public void resolve_33() {
        testResolve("http://example/dir1/dir2/", "//EX/OtherPath", "http://EX/OtherPath");
    }

    private void testResolve(String base, String rel, String expected) {
        IRIx baseIRI = IRIx.create(base);
        IRIx relIRI = IRIx.create(rel);
        IRIx iri2 = baseIRI.resolve(relIRI);
        String s1 = iri2.str();
        assertEquals(expected, s1);
    }
}
