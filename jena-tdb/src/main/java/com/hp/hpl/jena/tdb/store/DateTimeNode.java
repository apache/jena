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

package com.hp.hpl.jena.tdb.store;

import java.math.BigDecimal ;

import javax.xml.datatype.DatatypeConfigurationException ;
import javax.xml.datatype.DatatypeConstants ;
import javax.xml.datatype.DatatypeFactory ;
import javax.xml.datatype.XMLGregorianCalendar ;

import org.apache.jena.atlas.lib.BitsInt ;
import org.apache.jena.atlas.lib.BitsLong ;
import org.apache.jena.atlas.lib.NumberUtils ;

import com.hp.hpl.jena.tdb.TDBException ;

public class DateTimeNode
{
    // XXX ToDo 00:00:00 vs 24:00:00
    // ---- Layout
    // Epoch base: 0000-01-01T00:00:00

    // Layout:
    // Bits 56-63 : type
    
    // Bits 49-55 (7 bits)  : timezone -- 15 min precision + special for Z and no timezone.
    // Bits 27-48 (22 bits) : date, year is 13 bits = 8000 years  (0 to 7999)
    // Bits 0-26  (27 bits) : time, to milliseconds 

    // Layout:
    // Hi: TZ YYYY MM DD HH MM SS.sss Lo:

    // Const-ize
    static final int DATE_LEN = 22 ;    // 13 bits year, 4 bits month, 5 bits day => 22 bits
    static final int TIME_LEN = 27 ;    // 5 bits hour + 6 bits minute + 16 bits seconds (to millisecond)
    
    static final int MILLI = 0 ;
    static final int MILLI_LEN = 16 ;

    static final int MINUTES = MILLI_LEN ;
    static final int MINUTES_LEN = 6 ;

    static final int HOUR = MILLI_LEN + MINUTES_LEN ;
    static final int HOUR_LEN = 5 ;

    
    static final int DAY = TIME_LEN  ;
    static final int DAY_LEN = 5 ;

    static final int MONTH = TIME_LEN + DAY_LEN ;
    static final int MONTH_LEN = 4 ;
    
    static final int YEAR = TIME_LEN + MONTH_LEN + DAY_LEN ;
    static final int YEAR_LEN = 13 ;
    
    
    static final int TZ = TIME_LEN + DATE_LEN ;
    static final int TZ_LEN = 7 ;
    static final int TZ_Z = 0x7F ;      // Value for Z
    static final int TZ_NONE = 0x7E ;   // Value for no timezone.
    
    static DatatypeFactory datatypeFactory = null ;
    static
    { 
        try 
        { datatypeFactory = DatatypeFactory.newInstance() ; }
        catch (DatatypeConfigurationException ex)
        { throw new TDBException("DateTimeNode", ex) ; }
    }
    
    // Packed in correct place.
    static long time(long v, int hour, int mins, int millisec)
    {
        // And bit offset for direct packing?
        // HH:MM:SS.ssss => 5 bits H, 6 bits M, 16 bits S ==> 27 bits
        v = BitsLong.pack(v, hour, HOUR, HOUR+HOUR_LEN) ;
        v = BitsLong.pack(v, mins, MINUTES, MINUTES+MINUTES_LEN) ;
        v = BitsLong.pack(v, millisec, MILLI, MILLI+MILLI_LEN) ;
        return v ;
    }
    
    // Packed in correct place.
    static long date(long v, int year, int month, int day)
    {
        // YYYY:MM:DD => 13 bits year, 4 bits month, 5 bits day => 22 bits
        v = BitsLong.pack(v, year, YEAR, YEAR+YEAR_LEN) ;
        v = BitsLong.pack(v, month, MONTH, MONTH+MONTH_LEN) ;
        v = BitsLong.pack(v, day,  DAY, DAY+DAY_LEN) ;
        return v ;
    }
    
    static long tz(long v, int tz_in_quarters)
    {
        v = BitsLong.pack(v, tz_in_quarters, TZ, TZ+TZ_LEN);
        return v ;
    }

    // From string.  Assumed legal.  Retains all info this way.
    // returns -1 for unpackable. 
    public static long packDate(String lex)
    {
        return packDateTime(lex) ;
    }

    // From string.  Assumed legal.
    // Returns -1 for unpackable.
    
    public static long packDateTime(String lex)
    { 
        try { return packDateTime$(lex) ; }
        catch (Exception ex) { return -1 ; }
    }
    
    private static long packDateTime$(String lex)
    {
        long v = 0 ;
        // Whitespace facet processing.
        lex = lex.trim() ;
        
        boolean containsZ = (lex.indexOf('Z') > 0 ) ;
        
        // Bug in Java 1.6 (build 5 at least)
        // T24:00:00 not accepted.
        // See also TestNodeId.nodeId_date_time_7
        
        XMLGregorianCalendar xcal = datatypeFactory.newXMLGregorianCalendar(lex) ;
        
        if ( xcal.getFractionalSecond() != null )
        { 
            BigDecimal fs = xcal.getFractionalSecond() ;
            // Were there sub-millisecond resolution fractional seconds?
            // This isn't perfect but it needs a very long fractional part to break it,
            // less than observable quantum of time.
            if ( fs.doubleValue() != xcal.getMillisecond()/1000.0 )
                return -1 ;
        }
        
        int y = xcal.getYear() ;
        
        if ( y < 0 || y >= 8000 )
            return -1 ;
        
        v = date(v, xcal.getYear(), xcal.getMonth(), xcal.getDay() ) ;
        v = time(v, xcal.getHour(), xcal.getMinute(), xcal.getSecond()*1000+xcal.getMillisecond()) ;
        
        if ( containsZ )
            return tz(v, TZ_Z) ;
        
        int tz = xcal.getTimezone() ;
        if ( tz == DatatypeConstants.FIELD_UNDEFINED )
            return tz(v, TZ_NONE) ;

        // Timezone is weird. 
        if ( tz%15 != 0 )
            return -1 ;
        
        tz = tz/15 ;
        return tz(v, tz) ;
    }

    public static String unpackDateTime(long v)
    {
        return unpack(v, true) ;
    }

    public static String unpackDate(long v)
    {
        return unpack(v, false) ;
    }

    // Avoid calls to String.format
    private static String unpack(long v, boolean isDateTime)
    {
        // YYYY:MM:DD => 13 bits year, 4 bits month, 5 bits day => 22 bits
        int years = (int)BitsLong.unpack(v, YEAR, YEAR+YEAR_LEN) ;
        int months = (int)BitsLong.unpack(v, MONTH, MONTH+MONTH_LEN) ;
        int days = (int)BitsLong.unpack(v, DAY, DAY+DAY_LEN) ;
        
        // Hours: 5, mins 6, milli 16, TZ 7 => 34 bits 
        int hours = (int)BitsLong.unpack(v, HOUR, HOUR+HOUR_LEN) ;
        int minutes = (int)BitsLong.unpack(v, MINUTES, MINUTES+MINUTES_LEN) ; 
        int milliSeconds = (int)BitsLong.unpack(v, MILLI, MILLI+MILLI_LEN) ;
        
        int tz = (int)BitsLong.unpack(v, TZ, TZ+TZ_LEN);
        
        int sec = milliSeconds / 1000 ;
        int fractionSec = milliSeconds % 1000 ;
        
        StringBuilder sb = new StringBuilder(50) ;
        NumberUtils.formatInt(sb, years, 4) ;
        sb.append('-') ;
        NumberUtils.formatInt(sb, months, 2) ;
        sb.append('-') ;
        NumberUtils.formatInt(sb, days, 2) ;
        if ( isDateTime )
        {
            sb.append('T') ;
            NumberUtils.formatInt(sb, hours, 2) ;
            sb.append(':') ;
            NumberUtils.formatInt(sb, minutes, 2) ;
            sb.append(':') ;
            NumberUtils.formatInt(sb, sec, 2) ;

            // Formatting needed : int->any
            if ( fractionSec != 0 )
            {
                sb.append(".") ;
                // TODO Do better
                if ( fractionSec%100 == 0 )
                    NumberUtils.formatInt(sb, fractionSec/100, 1) ;
                else if ( fractionSec%10 == 0 )
                    NumberUtils.formatInt(sb, fractionSec/10, 2) ;
                else
                    NumberUtils.formatInt(sb, fractionSec, 3) ;
                
            }
        }
        // tz in 15min units
        // Special values.
        if ( tz == TZ_Z )
        {
            sb.append("Z") ;
            return sb.toString();
        }
        
        if ( tz == TZ_NONE )
            return sb.toString() ; 
            
        // Sign extend.
        if ( BitsLong.isSet(v, TZ+TZ_LEN-1) )
            tz = BitsInt.set(tz, TZ_LEN, 32) ;
        
        if ( tz < 0 )
        {
            tz = -tz ;
            sb.append('-') ;
        }
        else
            sb.append('+') ;
            
        int tzH = tz/4 ;
        int tzM = (tz%4)*15 ;
        NumberUtils.formatUnsignedInt(sb, tzH, 2) ;
        sb.append(':') ;
        NumberUtils.formatUnsignedInt(sb, tzM, 2) ;
        return sb.toString();
    }
}
