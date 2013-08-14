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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Tuple ;
import org.junit.Test ;

public class TestFileOps extends BaseTest
{
    /*
     * t("") ;
        t("/a/b/c") ;
        t("/aa/bb/cc.ext") ;
        t("cc.ext") ;
        t("/cc.ext") ;
        t("/") ;
        t("xyz") ;
        t("xyz/") ;
     */

    static void test(String fn, String path, String basename, String ext)
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
    
    @Test public void split01() 
    { test("/aa/bb/cc.ext", "/aa/bb", "cc", "ext") ; }

    @Test public void split02() 
    { test("/a/b/c", "/a/b", "c", null) ; }
    
    @Test public void split03() 
    { test("cc.ext", null, "cc", "ext") ; }
    
    @Test public void split04() 
    { test("/cc.ext", "", "cc", "ext") ; }
    
    @Test public void split05() 
    { test("/", "", "", null) ; }
    
    @Test public void split06() 
    { test("", null, "", null) ; }
    
    @Test public void split07() 
    { test("xyz", null, "xyz", null) ; }
    
    @Test public void split08() 
    { test("/xyz", "", "xyz", null) ; }
    
    @Test public void split09() 
    { test("xyz/", "xyz", "", null) ; }
}
