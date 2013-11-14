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

/** XDS date/time 7-compoent model.
 * Includes parsing xsd:dateTime, xsd:date and xsd:g*
 */  

public class DateTimeStruct
{
    public boolean xsdDateTime  ;
    public String neg = null ;         // Null if none. 
    public String year = null ;
    public String month = null ;
    public String day = null ;
    public String hour = null ;
    public String minute = null ;
    public String second = null ;      // Inc. fractional parts
    public String timezone = null ;    // Null if none.

    public DateTimeStruct() {}
    
    public static class DateTimeParseException extends RuntimeException
    {
        public DateTimeParseException(String msg) { super(msg) ; } 
    }
    
    @Override
    public String toString()
    { 
        String ySep = "-" ;
        String tSep = ":" ;
        String x = year+ySep+month+ySep+day ;
        if ( xsdDateTime )
            x = x + "T"+hour+tSep+minute+tSep+second ;
        if ( neg != null )
            x = neg+x ;
        if ( timezone != null )
            x = x+timezone ;
        return x ; 
    }
    
    public static DateTimeStruct parseDateTime(String str)
    { return _parseYMD(str, true, true, true) ; }

    public static DateTimeStruct parseTime(String str)
    { return _parseTime(str) ; } 
    
    public static DateTimeStruct parseDate(String str)
    { return _parseYMD(str, true, true, false) ; } 

    public static DateTimeStruct parseGYear(String str)
    { return _parseYMD(str, false, false, false) ; } 

    public static DateTimeStruct parseGYearMonth(String str)
    { return _parseYMD(str, true, false, false) ; } 
    
    public static DateTimeStruct parseGMonth(String str)
    { return _parseMD(str, true, false) ; }
    
    public static DateTimeStruct parseGMonthDay(String str)    
    { return _parseMD(str, true, true) ; }
    
    public static DateTimeStruct parseGDay(String str)      
    { return _parseMD(str, false, true) ; }
    
    // Date with year: date, dateTime, gYear, gYearMonth but not gMonth, gMonthDay, 
    private static DateTimeStruct _parseYMD(String str, boolean month, boolean day, boolean includeTime) 
    { 
        DateTimeStruct struct = new DateTimeStruct() ;
        int idx = 0 ; // if whitespace fact processing -- skipWhitespace(str, 0) ;
        boolean negYear = false ;

        if ( str.charAt(idx) == '-' )
        {
            struct.neg = "-" ;
            idx ++ ;
        }
        
        struct.year = getDigits(str, idx) ;
        if ( struct.year.length() < 4 )
            throw new DateTimeParseException("Year too short (must be 4 or more digits)") ;
        
        idx += struct.year.length() ;
        
        if ( month )
        {
            check(str, idx, '-') ;
            idx += 1 ;
            struct.month = getDigits(2, str, idx) ;
            idx += 2 ;
        }        

        if ( day )
        {
            check(str, idx, '-') ;
            idx += 1 ;
            struct.day = getDigits(2, str, idx) ;
            idx += 2 ;
        }

        if ( includeTime )
        {        
            struct.xsdDateTime = true ;
            // ---- 
            check(str, idx, 'T') ;
            idx += 1 ;
            idx = _parseTime(struct, idx, str) ;
        }
        
        // Timezone
        idx = _parseTimezone(struct, str, idx) ;
        
        idx = skipWhitespace(str, idx) ;
        
        if ( idx != str.length() )
            throw new DateTimeParseException("Trailing characters after date/time") ;
        return struct ; 
    }
    
    // No year: gMonth, gMonthDay, gDay
    private static DateTimeStruct _parseMD(String str, boolean month, boolean day)
    {
        DateTimeStruct struct = new DateTimeStruct() ;
        int idx = 0 ;

        check(str, idx, '-') ;
        idx += 1 ;

        check(str, idx, '-') ;
        idx += 1 ;
        
        if ( month )
        {
            struct.month = getDigits(2, str, idx) ;
            idx += 2 ; 
        }
        
        if ( day )
        {
            check(str, idx, '-') ;
            idx += 1 ;
            struct.day = getDigits(2, str, idx) ;
            idx += 2 ; 
        }
        
        // Timezone
        idx = _parseTimezone(struct, str, idx) ;
        
        if ( idx != str.length() )
            throw new DateTimeParseException("Unexpected trailing characters in string") ;
        return struct ; 
    }
    
    private static DateTimeStruct _parseTime(String str)
    {
        DateTimeStruct struct = new DateTimeStruct() ;
        int idx = 0 ;
        idx = _parseTime(struct, 0, str) ;
        idx = _parseTimezone(struct, str, idx) ;
        idx = skipWhitespace(str, idx) ;
        if ( idx != str.length() )
            throw new DateTimeParseException("Trailing characters after date/time") ;
        return struct ;
    }        
    private static int _parseTime(DateTimeStruct struct, int idx, String str)
    {
        // Hour-minute-seconds
        struct.hour = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, ':') ;
        idx += 1 ;

        struct.minute = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, ':') ;
        idx += 1 ;

        // seconds
        struct.second = getDigits(2, str, idx) ;
        idx += 2 ;
        if ( idx < str.length() && str.charAt(idx) == '.' )
        {
            idx += 1 ;
            int idx2 = idx ;
            for ( ; idx2 < str.length() ; idx2++ )
            {
                char ch = str.charAt(idx2) ;
                if ( ! Character.isDigit(ch) )
                    break ;
            }
            if ( idx == idx2 )
                throw new DateTimeParseException("Bad time part") ;
            struct.second = struct.second+'.'+str.substring(idx, idx2) ;
            idx = idx2 ;
        }
        return idx ;
    }
    
    private static int _parseTimezone(DateTimeStruct struct, String str, int idx)
    {
        if ( idx >= str.length() )
        {
            struct.timezone = null ;
            return idx ;
        }
        
        if ( str.charAt(idx) == 'Z' )
        {
            struct.timezone = "Z" ;
            idx += 1 ;
        }
        else
        {
            StringBuilder sb = new StringBuilder() ;

            if ( str.charAt(idx) == '+' )
                sb.append('+') ;
            else if ( str.charAt(idx) == '-' )
                sb.append('-') ;
            else
                throw new DateTimeParseException("Bad timezone") ;
            idx += 1 ;

            sb.append(getDigits(2, str, idx)) ;
            idx += 2 ;

            check(str, idx, ':') ;
            sb.append(':') ;
            idx += 1 ;

            sb.append(getDigits(2, str, idx)) ;
            idx += 2 ;
            struct.timezone = sb.toString() ;
        }
        return idx ;
    }


//    // DateTime or Date - not gregorian
//    // Replace with generic code.
//    private static DateTimeStruct _parse(String str, boolean includeTime)
//    {
//        // -? YYYY-MM-DD T hh:mm:ss.ss TZ
//        DateTimeStruct struct = new DateTimeStruct() ;
//        int idx = 0 ;
//
//        if ( str.startsWith("-") )
//        {
//            struct.neg = "-" ;
//            idx = 1 ;
//        }
//
//        // ---- Year-Month-Day
//        struct.year = getDigits(4, str, idx) ;
//        idx += 4 ;
//        check(str, idx, '-') ;
//        idx += 1 ;
//
//        struct.month = getDigits(2, str, idx) ;
//        idx += 2 ;
//        check(str, idx, '-') ;
//        idx += 1 ;
//
//        struct.day = getDigits(2, str, idx) ;
//        idx += 2 ;
//
//        struct.xsdDateTime = false ;
//
//        if ( includeTime )
//        {        
//            struct.xsdDateTime = true ;
//            // ---- 
//            check(str, idx, 'T') ;
//            idx += 1 ;
//
//            // ---- 
//            // Hour-minute-seconds
//            struct.hour = getDigits(2, str, idx) ;
//            idx += 2 ;
//            check(str, idx, ':') ;
//            idx += 1 ;
//
//            struct.minute = getDigits(2, str, idx) ;
//            idx += 2 ;
//            check(str, idx, ':') ;
//            idx += 1 ;
//
//            // seconds
//            struct.second = getDigits(2, str, idx) ;
//            idx += 2 ;
//            if ( idx < str.length() && str.charAt(idx) == '.' )
//            {
//                idx += 1 ;
//                int idx2 = idx ;
//                for ( ; idx2 < str.length() ; idx2++ )
//                {
//                    char ch = str.charAt(idx2) ;
//                    if ( ! Character.isDigit(ch) )
//                        break ;
//                }
//                if ( idx == idx2 )
//                    throw new DateTimeParseException() ;
//                struct.second = struct.second+'.'+str.substring(idx, idx2) ;
//                idx = idx2 ;
//            }
//        }
//        else
//        {
//            struct.hour =  null ;
//            struct.minute = null ;
//            struct.second = null ;
//
//        }
//        // timezone. Z or +/- 00:00
//
//        if ( idx < str.length() )
//        {
//            if ( str.charAt(idx) == 'Z' )
//            {
//                struct.timezone = "Z" ;
//                idx += 1 ;
//            }
//            else
//            {
//                StringBuilder sb = new StringBuilder() ;
//
//                if ( str.charAt(idx) == '+' )
//                    sb.append('+') ;
//                else if ( str.charAt(idx) == '-' )
//                    sb.append('-') ;
//                else
//                    throw new DateTimeParseException() ;
//                idx += 1 ;
//
//                sb.append(getDigits(2, str, idx)) ;
//                idx += 2 ;
//
//                check(str, idx, ':') ;
//                sb.append(':') ;
//                idx += 1 ;
//
//
//                sb.append(getDigits(2, str, idx)) ;
//                idx += 2 ;
//
//                struct.timezone = sb.toString() ;
//            }
//        }
//    
//        if ( idx != str.length() )
//            throw new DateTimeParseException() ;
//        return struct ;
//    }

    private static String getDigits(int num, String string, int start)
    {
        for ( int i = start ; i < (start+num) ; i++ )
        {
            char ch = string.charAt(i) ;
            // Only ASCII digits
            if ( ch < '0' || ch > '9' )
                throw new DateTimeParseException("Bad number (expected "+num+" digits)") ;
            continue ;
        }
        return string.substring(start, start+num) ;
    }
    
    private static String getDigits(String string, int start)
    {
        int i = start ;
        for ( ;; i++ )
        {
            if ( i >= string.length() )
                break ;
            char ch = string.charAt(i) ;
            // Only ASCII digits
            if ( ch < '0' || ch > '9' )
                break ;
            continue ;
        }
        return string.substring(start, i) ;
    }
    
    private static int skipWhitespace(String string, int idx)
    {
        while ( idx < string.length() )
        {
            char ch = string.charAt(idx) ;
            if ( ! Character.isWhitespace(ch) )
                return idx ;
            idx++ ;
        }
        return idx ;
    }
    
    private static void check(String string, int idx, char x)
    {
        if ( string.length() <= idx || string.charAt(idx) != x ) 
            throw new DateTimeParseException("Expected: "+x+" at index "+idx) ;
    }
}
