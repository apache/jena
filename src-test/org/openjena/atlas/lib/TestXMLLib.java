/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import org.junit.Assert ;
import org.junit.Test ;

public class TestXMLLib
{
    @Test public void ws_collapse_01()  { test("abc", "abc") ; }
    @Test public void ws_collapse_02()  { test(" abc", "abc") ; }
    @Test public void ws_collapse_03()  { test(" abc ", "abc") ; }
    @Test public void ws_collapse_04()  { test(" a b c ", "a b c") ; }
    @Test public void ws_collapse_05()  { test("\babc", "\babc") ; }
    @Test public void ws_collapse_06()  { test("", "") ; }
    @Test public void ws_collapse_07()  { test(" ", "") ; }
    @Test public void ws_collapse_08()  { test(" \t\t\t\t\t\t\t   ", "") ; }
    
    // String.trim : "Returns a copy of the string, with leading and trailing whitespace omitted."
    // but later says it trims anything <= 0x20.  There are lots of control characters in x01-x1F. 
    // We only want to trim \n \r \t and space. 
    
    private static void test(String str1, String str2)
    {
        String result = XMLLib.WScollapse(str1) ;
        Assert.assertEquals(str2, result) ;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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