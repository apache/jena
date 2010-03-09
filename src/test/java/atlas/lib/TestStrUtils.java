/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.lib;

import atlas.lib.StrUtils;
import atlas.test.BaseTest;
import org.junit.Test;

public class TestStrUtils extends BaseTest
{
    static char marker = '_' ;
    static char esc[] = { ' ' , '_' } ; 
    
    static void test(String x)
    {
        test(x, null) ;
    }
    
    static void test(String x, String z)
    {
        String y = StrUtils.encode(x, marker, esc) ;
        if ( z != null )
            assertEquals(z, y) ;
        String x2 = StrUtils.decode(y, marker) ;
        assertEquals(x, x2) ;
    }
    
    @Test public void enc01() { test("abc") ; } 

    @Test public void enc02() { test("") ; } 

    @Test public void enc03() { test("_", "_5F" ) ; } 
    
    @Test public void enc04() { test(" ", "_20" ) ; } 
    
    @Test public void enc05() { test("_ _", "_5F_20_5F" ) ; } 
    
    @Test public void enc06() { test("_5F", "_5F5F" ) ; } 
    
    @Test public void enc07() { test("_2") ; } 
    
    @Test public void enc08() { test("AB_CD", "AB_5FCD") ; } 
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