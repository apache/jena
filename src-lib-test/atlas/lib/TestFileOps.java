/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import atlas.lib.FileOps;
import atlas.lib.Tuple;
import atlas.test.BaseTest;
import org.junit.Test;

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

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */