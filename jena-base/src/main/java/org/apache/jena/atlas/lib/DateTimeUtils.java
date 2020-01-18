/**
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar ;
import java.util.Date ;
import java.util.GregorianCalendar ;

import org.apache.commons.lang3.time.FastDateFormat ;

public class DateTimeUtils {
    // Use xxx to get +00:00 format with DateTimeFormatter
    private static final DateTimeFormatter dateTimeFmt_display  = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss zz");
    private static final DateTimeFormatter dateFmt_yyyymmdd     = DateTimeFormatter.ofPattern("yyyy-MM-ddxxx");
    private static final DateTimeFormatter dateTimeFmt_XSD_ms0  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
    private static final DateTimeFormatter dateTimeFmt_XSD_ms   = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");
    private static final DateTimeFormatter timeFmt_XSD_ms0      = DateTimeFormatter.ofPattern("HH:mm:ssxxx");
    private static final DateTimeFormatter timeFmt_XSD_ms       = DateTimeFormatter.ofPattern("HH:mm:ss.SSSxxx");

    public static String nowAsXSDDateTimeString() {
        return calendarToXSDDateTimeString(new GregorianCalendar()) ;
    }

    public static String todayAsXSDDateString() {
        return calendarToXSDDateString(new GregorianCalendar()) ;
    }

    /** Return "now" as readable string (date in yyyy/MM/dd format) */
    public static String nowAsString() {
        return nowAsString(dateTimeFmt_display) ;
    }

    public static String nowAsString(String formatString) {
        FastDateFormat df = FastDateFormat.getInstance(formatString) ;
        return df.format(new Date()) ;
    }

    public static String nowAsString(DateTimeFormatter dateFormat) {
        ZonedDateTime now = ZonedDateTime.now();
        return dateFormat.format(now);
    }

    private static boolean hasZeroMilliSeconds(Calendar cal) {
        return ! cal.isSet(Calendar.MILLISECOND) || cal.get(Calendar.MILLISECOND) == 0 ;
    }

    // Canonical form : if ms == 0, don't include in the string.
    public static String calendarToXSDDateTimeString(Calendar cal) {
        DateTimeFormatter fmt = hasZeroMilliSeconds(cal)
            ? dateTimeFmt_XSD_ms0
            : dateTimeFmt_XSD_ms ;
        return calendarToXSDString(cal, fmt) ;
    }

    public static String calendarToXSDDateString(Calendar cal) {
        String x = calendarToXSDString(cal, dateFmt_yyyymmdd) ;
        if ( x.endsWith("Z") )
            x = x.substring(0, x.length()-1)+"+00:00";
        return x;
    }

    // Canonical form : if ms == 0, don't include in the string.
    public static String calendarToXSDTimeString(Calendar cal) {
        DateTimeFormatter fmt = hasZeroMilliSeconds(cal)
            ? timeFmt_XSD_ms0
            : timeFmt_XSD_ms ;
        return calendarToXSDString(cal, fmt) ;
    }

    private static String calendarToXSDString(Calendar cal, DateTimeFormatter fmt) {
        ZonedDateTime zdt = ((GregorianCalendar)cal).toZonedDateTime();
        String lex = fmt.format(zdt) ;
        return lex ;
    }
}
