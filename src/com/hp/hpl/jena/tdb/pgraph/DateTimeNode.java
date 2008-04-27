/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

import lib.BitsLong;

public class DateTimeNode
{
    // UTC or not UTC, that is the question.
    // Whether to normalize to UTC.  Support costs ... no, people expect timezone.  
    
    // --- Sizing
    
    // 1 year = 60*60*24*1000 = 86,400,000 milliseconds < 27 bits
    // Normalize to UTC: 56 bits is 2^29 years. 
    
    
    // Timezone. = +- hours:minutes. +-14:00 
    // 28 hours = 1680 minutes = 2^11 => 11 bits.
    // Timezone in 15 min increments: 28*4 = 112 < 7 bits
    // ==> 7 bits
    
    // Leaves 49 bits.

    // Date: decision: precision of year.
    
    //   Suppose YYY since 1900 => 1000 < 2^10 : 10 bits year
    //   MM = 12 < 16 = 4 bits.
    //   DD = 31 < 32 = 5 bits.
    //     So 9 bits for MM:DD
    //   YYY in 0000-8000 = 800 < 2^13 (8K)
    // Epoch to 1000-01-01T00:00:00Z - 2999-12-31T23:59:59.9999Z ==> 2000 years < 2^11 
    //  ==> 20 bits
    //  ==> 22 bits for 8000 years
    
    // Time: HH:MM:SS => seconds means 24*60*60 = 86400 < 2^17 = 131072 
    // Block coded:
    //   HH: 24 < 2^5
    //   MM: 60 < 2^6
    //   SS: 60 < 2^6
    // ==> 17 bits to second accuracy
    // Milliseconds: 60,000 < 2^^16
    // ==> 27 bits to millisecond accuracy
    
    // ====> 47 bits of 49.

    // ---- Layout
    // Epoch base: 0000-01-01T00:00:00

    // Layout:
    // Bits 56-63 : type
    
    // Bits 49-55 (7 bits)  : timezone -- 15 min precision + special for Z
    // Bits 27-48 (22 bits) : date, year is 13 bits = 8000 years 
    // Bits 0-26  (27 bits) : time, to milliseconds 

    // Layout:
    // Hi: TZ YYYY MM DD HH MM SS.sss Lo:

    // Const-ize
    static final int DATE_LEN = 22 ;    // 13
    static final int TIME_LEN = 27 ;    // 5 + 6 + 16
    
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
    
    // Packed in correct place.
    static long time(long v, int hour, int mins, int millisec)
    {
        // And bit offset for direct packing?
        // HH:MM:SS.ssss => 5 bits H, 6 bits M, 16 bits S ==> 27 bits
        v = BitsLong.pack(v, hour, HOUR, HOUR+HOUR_LEN) ;
        v = BitsLong.pack(v, mins, MINUTES, MINUTES_LEN) ;
        v = BitsLong.pack(v, millisec, MILLI, MILLI+MILLI_LEN) ;
        return v ;
    }
    
    // Packed in correct place.
    static long date(long v, int year, int month, int day)
    {
        // YYYY:MM:DD => 13 bits year, 4 bits month, 5 bits day => 22 bits
        v = BitsLong.pack(v, year, YEAR, YEAR+YEAR_LEN) ;
        v = BitsLong.pack(v, month, MONTH, MONTH+MONTH_LEN) ;
        v = BitsLong.pack(v, day,  DAY, DAY_LEN) ;
        return v ;
    }
    
    static long tz(long v, int tz_in_quarters)
    {
        v = BitsLong.pack(v, tz_in_quarters, TZ, TZ_LEN);
        return v ;
    }

    public static void debug(NodeId nodeId)
    {
        System.out.printf("%08X", nodeId.value) ; 
    }
    
    public static long packDateTime(XSDDateTime dateTime)
    {
        // XXX Unfinished
        System.err.println("DateTimeNode.packDateTime: unfinished") ;
        long v = 0 ;
        int years = dateTime.getYears() ;
        int months = dateTime.getMonths() ;
        int days = dateTime.getDays() ;
        v = date(v, years, months ,days) ;
        
        int hours = dateTime.getHours() ;
        int mins = dateTime.getMinutes() ;
        double secs = dateTime.getSeconds() ;
        v = time(v, hours, mins, 0) ;
        // timezone
        return v ;
    }

    public static long packDate(XSDDateTime dateTime)
    {
        int y = dateTime.getYears() ;
        int m = dateTime.getMonths() ;
        int d = dateTime.getDays() ;
        long v = 0 ;
        v = date(v, y, m ,d) ;
        return v ;
    }

    public static XSDDateTime unpackDateTime(long v)
    {
        return null ;
    }

    public static XSDDateTime unpackDate(long v)
    {
        return null ;
    }

}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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