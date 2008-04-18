/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.pgraph;

import lib.BitsLong;

public class DateTimeNode
{
    // UTC or not UTC, that is the question.
    // Whether to normalize to UTC.  Support costs ... no, people expect timezone.  
    
    // --- Sizing
    
    // 1 year = 60*60*24*1000 = 86,400,000 milliseconds < 27 bits
    // It normalize to UTC: 56 bits is 2^29 years. 
    
    
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
    //   YYY in 1900-3000 = 1100 < 2^11
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


    
    // Packed to low end.
    static long time(int hour, int mins, int sec)
    {
        // And bit offset for direct packing?
        // HH:MM:SS => 5 bits H, 6 bits M, 6 bits S ==> 17 bits
        long v = 0 ;
        v = BitsLong.pack(v, hour, 0, 5) ; 
        v = BitsLong.pack(v, mins, 5, 11) ;
        v = BitsLong.pack(v, sec,  11, 17) ;
        return v ;
    }
    
    // Packed to low end
    static long date(int year, int month, int day)
    {
        // YYYY:MM:DD => 18 bits year, 4 bits month, 4 bits day => 26 bits
        long v = 0 ;
        v = BitsLong.pack(v, year, 0, 18) ; 
        v = BitsLong.pack(v, month, 18, 22) ;
        v = BitsLong.pack(v, day,  22, 26) ;
        return v ;
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