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
public class TestIRIxRelativize extends AbstractTestIRIx_3986 {

    public TestIRIxRelativize(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test public void relativize_http_01() { testRelativize("http://host/dir/", "http://host/dir/file", "file"); }

    @Test public void relativize_http_02() { testRelativize("http://host/dir/", "http://elsewhere/dir/file", null); }

    @Test public void relativize_http_03() { testRelativize("https://host/dir/", "http://host/dir/file", null); }

    @Test public void relativize_http_04() { testRelativize("http://example/dir/", "http://example/dir/abcd#frag", "abcd#frag"); }

    @Test public void relativize_http_05() { testRelativize("http://example/dir", "http://example/dir/abcd", "dir/abcd"); }

    @Test public void relativize_http_06() { testRelativize("http://example/dir", "http://example/dir/abcd", "dir/abcd"); }

    @Test public void relativize_http_07() { testRelativize("http://example/dir/ab", "http://example/dir/abcd", "abcd"); }

    @Test public void relativize_http_08() { testRelativize("http://example/abcd", "http://example/abcd#frag", "#frag"); }

    @Test public void relativize_http_09() { testRelativize("http://example/abcd", "http://example/abcd?query=qs", "?query=qs"); }

    @Test public void relativize_http_10() { testRelativize("http://example/abcd", "http://example/abcd?query=qs#f", "?query=qs#f"); }

    @Test public void relativize_http_11() { testRelativize("http://example/dir1/dir2/path", "http://example/otherDir/abcd", "/otherDir/abcd"); }

    @Test public void relativize_http_12() { testRelativize("http://host:1234/dir/", "http://host:1234/dir/file", "file"); }

    @Test public void relativize_http_13() { testRelativize("https://host:1234/dir/", "http://host:5678/dir/file", null); }

    @Test public void relativize_http_14() { testRelativize("http://ex/path/?query", "http://ex/path/file", null); }

    @Test public void relativize_http_15() { testRelativize("http://ex/path/#frag", "http://ex/path/file", "file"); }

    @Test public void relativize_http_16() { testRelativize("http://ex/path/", "http://ex/path/file?q=x", "file?q=x"); }

    @Test public void relativize_http_17() { testRelativize("http://ex/path/", "http://ex/path/file#frag", "file#frag"); }

    @Test public void relativize_http_18() { testRelativize("http://example/ns#", "http://example/x", "x"); }

    @Test public void relativize_http_19() { testRelativize("http://example/ns#", "http://example/ns#x", "#x"); }

    @Test public void relativize_http_20() { testRelativize("http://example/path", "http://example/path", ""); }

    @Test public void relativize_http_21() { testRelativize("http://example/path", "http://example/path#", "#"); }


    @Test public void relativize_file_01() { testRelativize("file:///dir/", "file:///dir/abcd", "abcd"); }

    @Test public void relativize_file_02() { testRelativize("file:///", "file:///dir/abcd", "dir/abcd"); }

    private void testRelativize(String baseStr, String pathStr, String expected) {
        IRIx base = IRIx.create(baseStr);
        IRIx path = IRIx.create(pathStr);
        IRIx rel = base.relativize(path);
        String result = (rel==null)?null:rel.str();
        assertEquals("Base=<"+baseStr+"> IRI=<"+pathStr+">", expected, result);
        if ( expected != null ) {
            IRIx path2 = base.resolve(rel);
            assertEquals(path, path2);
            assertEquals(path.str(), path2.str());
        }
    }
}

