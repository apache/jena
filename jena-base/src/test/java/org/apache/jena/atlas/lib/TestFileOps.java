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

package org.apache.jena.atlas.lib;

import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.junit.Test ;

public class TestFileOps
{
    static void testParts(String fn, String path, String basename, String ext)
    {
        Tuple<String> t = FileOps.splitDirBaseExt(fn) ;
        assertEquals(path, t.get(0)) ;
        assertEquals(basename, t.get(1)) ;
        assertEquals(ext, t.get(2)) ;

        if ( basename != null )
            assertEquals(basename, FileOps.basename(fn)) ;
        if ( ext != null )
            assertEquals(ext, FileOps.extension(fn)) ;
    }

    static void testConcat(String dir, String fn, String expected) {
        String result = FileOps.concatPaths(dir, fn);
        assertEquals(expected, result);
    }


    @Test public void split01()
    { testParts("/aa/bb/cc.ext", "/aa/bb", "cc", "ext") ; }

    @Test public void split02()
    { testParts("/a/b/c", "/a/b", "c", null) ; }

    @Test public void split03()
    { testParts("cc.ext", null, "cc", "ext") ; }

    @Test public void split04()
    { testParts("/cc.ext", "", "cc", "ext") ; }

    @Test public void split05()
    { testParts("/", "", "", null) ; }

    @Test public void split06()
    { testParts("", null, "", null) ; }

    @Test public void split07()
    { testParts("xyz", null, "xyz", null) ; }

    @Test public void split08()
    { testParts("/xyz", "", "xyz", null) ; }

    @Test public void split09()
    { testParts("xyz/", "xyz", "", null) ; }

    @Test public void concat01()
    { testConcat("xyz", "abc", "xyz/abc"); }

    @Test public void concat02()
    { testConcat("xyz/", "abc", "xyz/abc"); }

    @Test public void concat03()
    { testConcat("xyz", "/abc", "/abc"); }

    @Test public void concat04()
    { testConcat("/xyz/", "abc", "/xyz/abc"); }

    @Test public void concat05()
    { testConcat("/", "abc", "/abc"); }

    @Test public void concat06()
    { testConcat("/xyz", "", "/xyz"); }
}
