/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import junit.framework.TestCase ;
import org.junit.Test ;


public class TestDateTimeParsing extends TestCase
{
    @Test public void testDT_1()  { dateTimeTest("2007-08-31T12:34:56Z") ; }
    @Test public void testDT_2()  { dateTimeTest("2007-08-31T12:34:56") ; } 
    @Test public void testDT_3()  { dateTimeTest("2007-08-31T12:34:56.003") ; } 
    @Test public void testDT_4()  { dateTimeTest("2007-08-31T12:34:56.003+05:00") ; } 
    @Test public void testDT_5()  { dateTimeTest("-2007-08-31T12:34:56.003-05:00") ; } 
    @Test public void testDT_6()  { dateTimeTest("-2007-08-31T12:34:56") ; } 
    
    @Test public void testDT_7()  { dateTimeBad("+2007-08-31T12:34:56") ; }
    @Test public void testDT_8()  { dateTimeBad("2007-08-31") ; }
    
    @Test public void testDT_10()
    { 
        DateTimeStruct dt = DateTimeStruct.parseDateTime("2007-08-31T12:34:56") ;
        check(dt, null, 
              "2007", "08", "31",
              "12", "34", "56", null) ;
    }
        
    @Test public void testDT_11()
    { 
        DateTimeStruct dt = DateTimeStruct.parseDateTime("2007-08-31T12:34:56Z") ;
        check(dt, null, 
              "2007", "08", "31",
              "12", "34", "56", "Z") ;
    }
        
    @Test public void testDT_12()
    { 
        DateTimeStruct dt = DateTimeStruct.parseDateTime("-2007-08-31T12:34:56.003-05:00") ;
        check(dt, "-", 
              "2007", "08", "31",
              "12", "34", "56.003", "-05:00") ;
    }
    
    // ----
    
    @Test public void testD_1()  { dateTest("2007-08-31Z") ; }
    @Test public void testD_2()  { dateTest("2007-08-31") ; } 
    
    @Test public void testD_10()
    { 
        DateTimeStruct dt = DateTimeStruct.parseDate("2007-08-31") ;
        check(dt, null, 
              "2007", "08", "31",
              null) ;
    }
        
    @Test public void testGYear_1()
    {
        DateTimeStruct dt = DateTimeStruct.parseGYear("2007") ;
        check(dt, null, 
              "2007", null, null,
              null) ;
    }
    
    @Test public void testGYear_2()
    {
        DateTimeStruct dt = DateTimeStruct.parseGYear("2007Z") ;
        check(dt, null, 
              "2007", null, null,
              "Z") ;
    }

    @Test public void testGYear_3()
    {
        DateTimeStruct dt = DateTimeStruct.parseGYear("2007+08:00") ;
        check(dt, null, 
              "2007", null, null,
              "+08:00") ;
    }

    @Test public void testGYearMonth_1()
    {
        DateTimeStruct dt = DateTimeStruct.parseGYearMonth("2007-10") ;
        check(dt, null, 
              "2007", "10", null,
              null) ;
    }
    
    @Test public void testGYearMonth_2()
    {
        DateTimeStruct dt = DateTimeStruct.parseGYearMonth("2007-10Z") ;
        check(dt, null, 
              "2007", "10", null,
              "Z") ;
    }

    @Test public void testGYearMonth_3()
    {
        DateTimeStruct dt = DateTimeStruct.parseGYearMonth("2007-10-08:00") ;
        check(dt, null, 
              "2007", "10", null,
              "-08:00") ;
    }

    @Test public void testGMonth_1()
    {
        DateTimeStruct dt = DateTimeStruct.parseGMonth("--10") ;
        check(dt, null, 
              null, "10", null,
              null) ;
    }
    
    @Test public void testGMonth_2()
    {
        DateTimeStruct dt = DateTimeStruct.parseGMonth("--10Z") ;
        check(dt, null, 
              null, "10", null,
              "Z") ;
    }

    @Test public void testGMonth_3()
    {
        DateTimeStruct dt = DateTimeStruct.parseGMonth("--10-08:00") ;
        check(dt, null, 
              null, "10", null,
              "-08:00") ;
    }

    @Test public void testGMonthDay_1()
    {
        DateTimeStruct dt = DateTimeStruct.parseGMonthDay("--10-31") ;
        check(dt, null, 
              null, "10", "31",
              null) ;
    }
    
    @Test public void testGMonthDay_2()
    {
        DateTimeStruct dt = DateTimeStruct.parseGMonthDay("--10-31Z") ;
        check(dt, null, 
              null, "10", "31",
              "Z") ;
    }

    @Test public void testGMonthDay_3()
    {
        DateTimeStruct dt = DateTimeStruct.parseGMonthDay("--10-31-08:00") ;
        check(dt, null, 
              null, "10", "31",
              "-08:00") ;
    }

    @Test public void testGDay_1()
    {
        DateTimeStruct dt = DateTimeStruct.parseGDay("---31") ;
        check(dt, null, 
              null, null, "31",
              null) ;
    }
    
    @Test public void testGDay_2()
    {
        DateTimeStruct dt = DateTimeStruct.parseGDay("---31Z") ;
        check(dt, null, 
              null, null, "31",
              "Z") ;
    }

    @Test public void testGDay_3()
    {
        DateTimeStruct dt = DateTimeStruct.parseGDay("---31-08:00") ;
        check(dt, null, 
              null, null, "31",
              "-08:00") ;
    }

    // ----

    private static void dateTimeTest(String str)
    {
        DateTimeStruct dt = DateTimeStruct.parseDateTime(str) ;
        assertTrue(dt.xsdDateTime) ;
        check(dt) ;
        assertEquals(str, dt.toString()) ;
    }
    
    private static void dateTimeBad(String str)
    {
        try {
            DateTimeStruct dt = DateTimeStruct.parseDateTime(str) ;
            fail("No exception; "+str) ;
        }
        catch (com.hp.hpl.jena.sparql.util.DateTimeStruct.DateTimeParseException ex) {}
    }
    
    private static void dateTest(String str)
    {
        DateTimeStruct dt = DateTimeStruct.parseDate(str) ;
        assertFalse(dt.xsdDateTime) ;
        check(dt) ;
        assertEquals(str, dt.toString()) ;
    }
    
    private static void dateBad(String str)
    {
        try {
            DateTimeStruct dt = DateTimeStruct.parseDateTime(str) ;
            fail("No exception; "+str) ;
        }
        catch (DateTimeStruct.DateTimeParseException ex) {}
    }
    
    
    private static void check(DateTimeStruct dt)
    {
        assertTrue(dt.neg == null || dt.neg.equals("-")) ;

        if ( dt.year != null )
            assertEquals(4, dt.year.length()) ;
        
        if ( dt.month != null )
            assertEquals(2, dt.month.length()) ;
        
        if ( dt.day != null )
            assertEquals(2, dt.day.length()) ;
        
        if ( dt.xsdDateTime )
        {
            assertNotNull(dt.hour) ;
            assertEquals(2, dt.hour.length()) ;
            
            assertNotNull(dt.minute) ;
            assertEquals(2, dt.minute.length()) ;
            
            assertNotNull(dt.second) ;
            assertTrue(dt.hour.length() >= 0) ;
        }
        else
        {
            assertNull(dt.hour) ;
            assertNull(dt.minute) ; 
            assertNull(dt.second) ; 
        }
        assertTrue(dt.timezone == null || dt.timezone.equals("Z") || dt.timezone.length() == 6 )  ;
    }
    
    private static void check(DateTimeStruct dt,
                              String neg, String year, String month, String day,
                              String hour, String minute, String second, String timezone)
    {
        check(dt) ;
        assertEquals(neg, dt.neg) ;
        assertEquals(year, dt.year) ;
        assertEquals(month, dt.month) ;
        assertEquals(day, dt.day) ;
        assertEquals(hour, dt.hour) ;
        assertEquals(minute, dt.minute) ;
        assertEquals(second, dt.second) ;
        assertEquals(timezone, dt.timezone) ;
    }
    
    private static void check(DateTimeStruct dt,
                              String neg, String year, String month, String day,
                              String timezone)
    {
        check(dt, neg, year, month, day, null, null, null, timezone) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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