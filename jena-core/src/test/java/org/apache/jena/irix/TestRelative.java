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
public class TestRelative extends AbstractTestIRIx {

    public TestRelative(String name, IRIProvider provider) {
        super(name, provider);
    }

    @Test
    public void relative_01() { testRelative("http://host/dir/", "http://host/dir/file", "file"); }

    @Test
    public void relative_02() { testRelative("http://host/dir/", "http://elsewhere/dir/file", null); }

    @Test
    public void relative_03() { testRelative("https://host/dir/", "http://host/dir/file", null); }

    @Test
    public void relative_04() { testRelative("http://host:1234/dir/", "http://host:1234/dir/file", "file"); }

    @Test
    public void relative_05() { testRelative("https://host:1234/dir/", "http://host:5678/dir/file", null); }

    @Test
    public void relative_06() { testRelative("http://ex/path/?query", "http://ex/path/file", null); }

    @Test
    public void relative_07() { testRelative("http://ex/path/#frag", "http://ex/path/file", "file"); }

    @Test
    public void relative_08() { testRelative("http://ex/path/", "http://ex/path/file?q=x", "file?q=x"); }

    @Test
    public void relative_09() { testRelative("http://ex/path/", "http://ex/path/file#frag", "file#frag"); }
    @Test
    public void relative_10() { testRelative("http://example/ns#", "http://example/x", "x") ; }

    @Test
    public void relative_11() { testRelative("http://example/ns#", "http://example/ns#x", "#x") ; }

    private void testRelative(String baseStr, String pathStr, String expected) {
        IRIx base = IRIx.create(baseStr);
        IRIx path = IRIx.create(pathStr);
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

