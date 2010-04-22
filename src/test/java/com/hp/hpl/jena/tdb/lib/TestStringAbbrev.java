/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.lib;

import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;

public class TestStringAbbrev extends BaseTest
{
    @Test public void abbrev_01()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        test("Hello", "Hello", abbrev) ;
    }
    
    @Test public void abbrev_02()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        test(":Hello", ":_:Hello", abbrev) ;
    }
    
    @Test public void abbrev_03()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        test("::Hello", ":_::Hello", abbrev) ;
    }
    
    @Test public void abbrev_04()
    {
        StringAbbrev abbrev = new StringAbbrev() ;
        abbrev.add("x", "He") ;
        test("Hello", ":x:llo", abbrev) ;
        test("hello", "hello", abbrev) ;
        test(":hello", ":_:hello", abbrev) ;
    }
    
    private void test(String x, String y, StringAbbrev abbrev)
    {
        String z1 = abbrev.abbreviate(x) ;
        assertEquals(y, z1) ;
        String z2 = abbrev.expand(z1) ;
        assertEquals(x, z2) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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