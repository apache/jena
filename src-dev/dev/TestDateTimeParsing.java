/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import dev.DateTimeStruct.DateTimeParseException;
import junit.framework.TestCase;

public class TestDateTimeParsing extends TestCase
{
    public void testDT_1()  { test("2007-08-31T12:34:56Z") ; }
    
    public void testDT_2()  { test("2007-08-31T12:34:56") ; } 
        
    public void testDT_3()  { test("2007-08-31T12:34:56.003") ; } 
    public void testDT_4()  { test("2007-08-31T12:34:56.003+05:00") ;} 
    public void testDT_5()  { test("-2007-08-31T12:34:56.003-05:00") ;} 
    public void testDT_6()  { test("-2007-08-31T12:34:56") ; } 
    
    public void testDT_7()  { bad("+2007-08-31T12:34:56") ; }
    
    private static void test(String str)
    {
        DateTimeStruct dt = DateTimeStruct.parse(str) ;
        assertEquals(str, dt.toString()) ;
    }
    
    private static void bad(String str)
    {
        try {
            DateTimeStruct dt = DateTimeStruct.parse(str) ;
            fail("No exception; "+str) ;
        }
        catch (DateTimeParseException ex) {}
    }
    
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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