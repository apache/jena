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

import java.math.BigDecimal ;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;
import java.util.Date ;
import java.util.GregorianCalendar ;
import java.util.TimeZone ;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;

/** Miscellaneous operations - not query specific */

public class Utils
{
    static public String className(Object obj) { 
        if ( obj == null )
            return "null" ;
        return classShortName(obj.getClass()) ;
    }
    
    static public String classShortName(Class<?> cls)
    {
        String tmp = cls.getName() ;
        int i = tmp.lastIndexOf('.') ;
        tmp = tmp.substring(i+1) ;
        return tmp ;
    }
    
    public static String nowAsXSDDateTimeString()
    {
        return calendarToXSDDateTimeString(new GregorianCalendar()) ;
    }
    
    public static String todayAsXSDDateString()
    {
        return calendarToXSDDateString(new GregorianCalendar()) ;
    }

    public static String XSDDateTime2String(XSDDateTime xdt)
    {
        return xdt.toString() ;
    }
    
    /** Return "now" as readable string (date in yyyy/MM/dd format) */
    public static String nowAsString()
    { return nowAsString("yyyy/MM/dd HH:mm:ss z") ; }
    
    public static String nowAsString(String formatString)
    {
        DateFormat df = new SimpleDateFormat(formatString) ;
        return df.format(new Date()) ;
    }
    
//    public static XSDDateTime calendarToXSDDateTime(Calendar cal)
//    {
//        return new XSDDateTime(cal) ;
//    }
//
//    public static XSDDateTime calendarToXSDDate(Calendar cal)
//    {
//        // Ensure it is an XSDDate, not a dateTime.
//        return (XSDDateTime)XSDDatatype.XSDdate.parse(calendarToXSDDateString(cal)) ;
//    }

    
    public static String calendarToXSDDateTimeString(Calendar cal)
    {
        return calendarToXSDString(cal, "yyyy-MM-dd'T'HH:mm:ss.SSS") ;
    }
    
    public static String calendarToXSDDateString(Calendar cal)
    {
        return calendarToXSDString(cal, "yyyy-MM-dd") ;
    }
    
    public static String calendarToXSDTimeString(Calendar cal)
    {
        return calendarToXSDString(cal, "HH:mm:ss.SSS");
    }
    
    private static String calendarToXSDString(Calendar cal, String fmt)
    {
        // c.f. Constructor on Jena's XSDDateTime
        // Only issue is that it looses the timezone through (Xerces)
        // normalizing to UTC.
        SimpleDateFormat dFmt = new SimpleDateFormat(fmt) ;
        Date date = cal.getTime() ;
        String lex = dFmt.format(date) ;
        lex = lex+calcTimezone(cal) ;
        return lex ;
    }
    
    private static String calcTimezone(Calendar cal)
    {
        Date date = cal.getTime() ;
        TimeZone z = cal.getTimeZone() ;
        int tzOff = z.getRawOffset() ;
        int tz = tzOff ;

        if ( z.inDaylightTime(date) )
        {
            int tzDst = z.getDSTSavings() ;
            tz = tz + tzDst ;
        }
        
        String sign = "+" ;
        if ( tz < 0 )
        {
            sign = "-" ;
            tz = -tz ;
        }

        int tzH = tz/(60*60*1000) ;             // Integer divide towards zero.
        int tzM = (tz-tzH*60*60*1000)/(60*1000) ;
        
        String tzH_str = Integer.toString(tzH) ;
        String tzM_str = Integer.toString(tzM) ;
        
        if ( tzH < 10 )
            tzH_str = "0"+ tzH_str ;
        if ( tzM < 10 )
            tzM_str = "0"+ tzM_str ;
        return sign+tzH_str+":"+tzM_str ;
    }
    
    static public String stringForm(BigDecimal decimal)
    { 
        return decimal.toPlainString() ;
    }
    
    static public String stringForm(double d)
    { 
        if ( Double.isInfinite(d) )
        {
            if ( d < 0 ) return "-INF" ; 
            return "INF" ;
        }

        if ( Double.isNaN(d) ) return "NaN" ;
        
        // Otherwise, SPARQL form always has "e0"
        String x = Double.toString(d) ;
        if ( (x.indexOf('e') != -1) || (x.indexOf('E') != -1) )
            return x ;
        // Renormalize?
        return x+"e0" ;
    }
    
    static public String stringForm(float f)
    { 
        // No SPARQL short form.
        return Float.toString(f) ;
    }
}
