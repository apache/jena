/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeStruct
{
    String neg = null ;         // Null if none. 
    String year = null ;
    String month = null ;
    String day = null ;
    String hour = null ;
    String minute = null ;
    String second = null ;     // Inc. fractional parts
    String timezone = null ;    // Null if none.

    private DateTimeStruct() {}
    static DateTimeStruct parse(String str) { return parseDateTime(str) ; }
    
    static class DateTimeParseException extends RuntimeException
    {}
    
    public String toString()
    { 
        String ySep = "-" ;
        String tSep = ":" ;
        String x = year+ySep+month+ySep+day+"T"+hour+tSep+minute+tSep+second ;
        if ( neg != null )
            x = neg+x ;
        if ( timezone != null )
            x = x+timezone ;
        return x ; 
    }
    
    static String pattern = "(-?)(\\d{4})-(\\d{2})-(\\d{2})T.*"+        // year-month-day
                            "(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d+)?)" +  // hour:minute:second
                            "(Z|(?:(?:\\+|-)\\d{2}:\\d{2}))?" ;         // Timezone
    static Pattern p = Pattern.compile(pattern);
    private static DateTimeStruct parseDateTime(String str)
    {
        Matcher m = p.matcher(str) ;

        if ( m.matches() )
        {
//          System.out.println("Group count: "+m.groupCount()) ;
////        8, including the -
//          for ( int i = 0 ; i <= m.groupCount() ; i++ )
//          {
//          System.out.println("("+m.start(i)+","+m.end(i)+")") ;
//          String s = "" ;
//          if ( m.start(i) != -1 )
//          s = str.substring(m.start(i), m.end(i)) ;
//          System.out.println(i+">>"+s) ;
//          }
            DateTimeStruct DateTimeParser = new DateTimeStruct() ;

            DateTimeParser.neg = str(str, m, 1) ;
            DateTimeParser.year = str(str, m, 2) ;
            DateTimeParser.month = str(str, m, 3) ;
            DateTimeParser.day = str(str, m, 4) ;
            DateTimeParser.hour = str(str, m, 5) ;
            DateTimeParser.minute = str(str,m,6) ;
            DateTimeParser.second = str(str,m,7) ;
            DateTimeParser.timezone = str(str,m,8) ;
            return DateTimeParser ;
        }
        return null ;

    }

    private static String str(String str, Matcher m, int i)
    {
        if ( m.start(i) == -1 ) return "" ;
        return str.substring(m.start(i), m.end(i)) ;
    }

    private static DateTimeStruct _parseDateTime(String str)
    {
        // -? YYYY-MM-DD T hh:mm:ss.ss TZ
        DateTimeStruct DateTimeParser = new DateTimeStruct() ;
        int idx = 0 ;
        
        if ( str.startsWith("-") )
        {
            DateTimeParser.neg = "-" ;
            idx = 1 ;
        }
        
        // ---- Year-Month-Day
        DateTimeParser.year = getDigits(4, str, idx) ;
        idx += 4 ;
        check(str, idx, '-') ;
        idx += 1 ;
        
        DateTimeParser.month = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, '-') ;
        idx += 1 ;
        
        DateTimeParser.day = getDigits(2, str, idx) ;
        idx += 2 ;
        // ---- 
        check(str, idx, 'T') ;
        idx += 1 ;
        
        // ---- 
        // Hour-minute-seconds
        DateTimeParser.hour = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, ':') ;
        idx += 1 ;
        
        DateTimeParser.minute = getDigits(2, str, idx) ;
        idx += 2 ;
        check(str, idx, ':') ;
        idx += 1 ;
        
        // seconds
        DateTimeParser.second = getDigits(2, str, idx) ;
        idx += 2 ;
        if ( idx < str.length() && str.charAt(idx) == '.' )
        {
            idx += 1 ;
            int idx2 = idx ;
            for ( ; idx2 < str.length() ; idx2++ )
            {
                char ch = str.charAt(idx) ;
                if ( ! Character.isDigit(ch) )
                    break ;
            }
            if ( idx == idx2 )
                throw new DateTimeParseException() ;
            DateTimeParser.second = DateTimeParser.second+'.'+str.substring(idx, idx2) ;
            idx = idx2 ;
        }

        // timezone. Z or +/- 00:00
        
        if ( idx < str.length() )
        {
            if ( str.charAt(idx) == 'Z' )
            {
                DateTimeParser.timezone = "Z" ;
                idx += 1 ;
            }
            else
            {
                boolean signPlus = false ;
                if ( str.charAt(idx) == '+' )
                    signPlus = true ;
                else if ( str.charAt(idx) == '-' )
                    signPlus = false ;
                else
                    throw new DateTimeParseException() ;
                DateTimeParser.timezone = getDigits(2, str, idx) ;
                check(str, idx, ':') ;
                DateTimeParser.timezone = DateTimeParser.timezone+':'+getDigits(2, str, idx) ;
                idx += 5 ;
                 
            }
        }
        
        if ( idx != str.length() )
            throw new DateTimeParseException() ;
        return DateTimeParser ;
    }

    private static String getDigits(int num, String string, int start)
    {
        for ( int i = start ; i < (start+num) ; i++ )
        {
            char ch = string.charAt(i) ;
            if ( ! Character.isDigit(ch) )
                throw new DateTimeParseException() ;
            continue ;
        }
        return string.substring(start, start+num) ;
    }
    
    private static void check(String string, int start, char x)
    {
        if ( string.charAt(start) != x ) 
            throw new DateTimeParseException() ;
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