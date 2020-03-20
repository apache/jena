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

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class TestDateTimeUtils {

	@Test
	public void testCalendarToXSDDateTimeString_1() {
		Calendar cal = createCalendar(1984, Calendar.MARCH, 22, 14, 32, 1, 0, "Z") ;
		assertEquals("1984-03-22T14:32:01+00:00", DateTimeUtils.calendarToXSDDateTimeString(cal));
		cal.setTimeZone(TimeZone.getTimeZone("MST"));
		assertEquals("1984-03-22T07:32:01-07:00", DateTimeUtils.calendarToXSDDateTimeString(cal));
	}

    @Test
    public void testCalendarToXSDDateTimeString_2() {
        Calendar cal = createCalendar(1984, Calendar.MARCH, 22, 14, 32, 1, 50, "Z") ;
        assertEquals("1984-03-22T14:32:01.050+00:00", DateTimeUtils.calendarToXSDDateTimeString(cal));
        cal.setTimeZone(TimeZone.getTimeZone("MST"));
        assertEquals("1984-03-22T07:32:01.050-07:00", DateTimeUtils.calendarToXSDDateTimeString(cal));
    }


    @Test
	public void testCalendarToXSDDateString() {
		Calendar cal = createCalendar(1984, Calendar.MARCH, 22, 23, 59, 1, 0, "Z");
		cal.setTimeZone(TimeZone.getTimeZone("Z")) ;
		assertEquals("1984-03-22+00:00", DateTimeUtils.calendarToXSDDateString(cal));
		cal.setTimeZone(TimeZone.getTimeZone("MST"));
		assertEquals("1984-03-22-07:00", DateTimeUtils.calendarToXSDDateString(cal));
	}
    
    @Test
	public void testCalendarToXSDTimeString_1() {
		Calendar cal = createCalendar(1984, Calendar.MARCH, 22, 14, 32, 1, 0, "GMT+01:00");
		assertEquals("14:32:01+01:00", DateTimeUtils.calendarToXSDTimeString(cal));
		// Different timezone - moves the cal point-in-time.
		cal.setTimeZone(TimeZone.getTimeZone("MST"));
		assertEquals("06:32:01-07:00", DateTimeUtils.calendarToXSDTimeString(cal));
	}
	
    @Test
    public void testCalendarToXSDTimeString_2() {
        Calendar cal = createCalendar(1984, Calendar.MARCH, 22, 14, 32, 1, 500, "GMT+01:00");
        assertEquals("14:32:01.500+01:00", DateTimeUtils.calendarToXSDTimeString(cal));
        // Different timezone - moves the cal point-in-time.
        cal.setTimeZone(TimeZone.getTimeZone("MST"));
        assertEquals("06:32:01.500-07:00", DateTimeUtils.calendarToXSDTimeString(cal));
    }
    
	private static Calendar createCalendar(int year, int month, int dayOfMonth, int hourOfDay,
	                                       int minute, int second, int milli, String tz) {
	    GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone(tz)) ;
	    cal.set(year, month, dayOfMonth, hourOfDay, minute, second) ;
	    cal.set(Calendar.MILLISECOND, milli) ;
	    return cal ;
	}
}