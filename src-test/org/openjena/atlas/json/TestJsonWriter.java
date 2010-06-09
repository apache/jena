/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json;

import static org.openjena.atlas.json.LibJsonTest.writeRead ;
import org.junit.Test ;
import org.openjena.atlas.json.JsonString ;
import org.openjena.atlas.json.JsonValue ;
import org.openjena.atlas.junit.BaseTest ;

public class TestJsonWriter extends BaseTest
{
    @Test public void js_write_str_1()  { test("foo") ; }
    
    @Test public void js_write_str_2()  { test("foo bar") ; }

    @Test public void js_write_str_3()  { test("foo\nbar") ; }
    
    @Test public void js_write_str_4()  { test("x\ty", "\"x\\ty\"") ; }

    @Test public void js_write_str_5()  { test("\r", "\"\\r\"") ; }

    @Test public void js_write_str_6()  { test("\u0000", "\"\\u0000\"") ; }

    @Test public void js_write_str_7()  { test("\u0001", "\"\\u0001\"") ; }

    @Test public void js_write_str_8()  { test("\u001F", "\"\\u001F\"") ; }

    @Test public void js_write_str_9()  { test("\u007F", "\"\\u007F\"") ; }

    @Test public void js_write_str_10() { test("\u009F", "\"\\u009F\"") ; }

    @Test public void js_write_str_11() { test("\u2001", "\"\\u2001\"") ; }
    
    
    private static void test(String str, String expected)
    {
        JsonValue v = new JsonString(str) ;
        str = v.toString() ;
        assertEquals(expected, str) ; 
    }
    
    private static void test(String str)
    {
        JsonValue v = new JsonString(str) ;
        writeRead(v) ;
    }
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