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
package com.hp.hpl.jena.sparql.util;

import static com.hp.hpl.jena.sparql.util.Utils.calendarToXSDDateString;
import static com.hp.hpl.jena.sparql.util.Utils.calendarToXSDDateTimeString;
import static com.hp.hpl.jena.sparql.util.Utils.calendarToXSDTimeString;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class UtilsTest {

	@Test
	public void testCalendarToXSDDateTimeString() throws Exception {
		Calendar cal = new GregorianCalendar(1984, Calendar.MARCH, 22, 14, 32, 1);
		assertEquals("1984-03-22T14:32:01.000+01:00", calendarToXSDDateTimeString(cal));
		cal.setTimeZone(TimeZone.getTimeZone("MST"));
		assertEquals("1984-03-22T14:32:01.000-07:00", calendarToXSDDateTimeString(cal));
	}

	@Test
	public void testCalendarToXSDDateString() throws Exception {
		Calendar cal = new GregorianCalendar(1984, Calendar.MARCH, 22, 23, 59, 1);
		assertEquals("1984-03-22+01:00", calendarToXSDDateString(cal));
		cal.setTimeZone(TimeZone.getTimeZone("MST"));
		assertEquals("1984-03-22-07:00", calendarToXSDDateString(cal));

	}

	@Test
	public void testCalendarToXSDTimeString() throws Exception {
		Calendar cal = new GregorianCalendar(1984, Calendar.MARCH, 22, 14, 32, 1);
		assertEquals("14:32:01.000+01:00", calendarToXSDTimeString(cal));
		cal.setTimeZone(TimeZone.getTimeZone("MST"));
		assertEquals("14:32:01.000-07:00", calendarToXSDTimeString(cal));
	}
}