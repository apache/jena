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
public class TestIRIxRelative extends AbstractTestIRIx_3986 {

    public TestIRIxRelative(String name, IRIProvider provider) {
        super(name, provider);
    }

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

    @Test public void relative_01() { relative("http://host/dir/", "http://host/dir/file", "file"); }

    @Test public void relative_02() { relative("http://host/dir/", "http://elsewhere/dir/file", null); }

    @Test public void relative_03() { relative("https://host/dir/", "http://host/dir/file", null); }

    @Test public void relative_04() { relative("http://host:1234/dir/", "http://host:1234/dir/file", "file"); }

    @Test public void relative_05() { relative("https://host:1234/dir/", "http://host:5678/dir/file", null); }

    @Test public void relative_06() { relative("http://ex/path/?query", "http://ex/path/file", null); }

    @Test public void relative_07() { relative("http://ex/path/#frag", "http://ex/path/file", "file"); }

    @Test public void relative_08() { relative("http://ex/path/", "http://ex/path/file?q=x", "file?q=x"); }

    @Test public void relative_09() { relative("http://ex/path/", "http://ex/path/file#frag", "file#frag"); }

    @Test public void relative_10() { relative("http://example/ns#", "http://example/x", "x"); }

    @Test public void relative_11() { relative("http://example/ns#", "http://example/ns#x", "#x"); }

    private void relative(String baseStr, String pathStr, String expected) {
        IRIx base = test_create(baseStr);
        IRIx path = test_create(pathStr);
        IRIx rel = base.relativize(path);
        String result = (rel==null)?null:rel.str();
        assertEquals(expected, result);
        if ( expected != null ) {
            IRIx path2 = base.resolve(rel);
            assertEquals(path, path2);
            assertEquals(path.str(), path2.str());
        }
    }
}

