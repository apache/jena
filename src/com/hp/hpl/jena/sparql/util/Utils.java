/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;

/** Miscellaneous operations - not query specific */

public class Utils
{
    static public String className(Object obj)
    { return classShortName(obj.getClass()) ; }
    
    static public String classShortName(Class<?> cls)
    {
        String tmp = cls.getName() ;
        int i = tmp.lastIndexOf('.') ;
        tmp = tmp.substring(i+1) ;
        return tmp ;
    }
    
    // See also:
    // Java 1.5
    // javax.xml.datatype.XMLGregorianCalendar
    // javax.xml.datatype.DatatypeFactory.newXMLGregorianCalendar 

    // warning all calendar based operations force the timezone
    // to the default if not present. 
    
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
        return calendarToXSDString(cal, "yyyy-MM-dd'T'HH:mm:ss.S") ;
    }
    
    public static String calendarToXSDDateString(Calendar cal)
    {
        return calendarToXSDString(cal, "yyyy-MM-dd") ;
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
    
    /** Compare two object-things for quality - allow null to be equals to null */
    
    public static boolean equal(Object obj1, Object obj2)
    {
        // Don't call this equals because static import does nto work very well with it (looks like local object.equals)
        if ( obj1 == obj2) return true ;
        if ( obj1 == null )
            return obj2 == null ;
        // obj1 != null
        if ( obj2 == null )
            return false ;
        return obj1.equals(obj2) ;
    }
    
    /** Do two lists have the same elements? */ 
    public static <T> boolean equalsListAsSet(List<T> list1, List<T> list2)
    {
        return list1.containsAll(list2) && list2.containsAll(list1) ;
    }
    
    
    /** HashCode - allow nulls */
    public static int hashCodeObject(Object obj) { return hashCodeObject(obj, -4) ; }
    
    /** HashCode - allow nulls */
    public static int hashCodeObject(Object obj, int nullHashCode)
    {
        if ( obj == null )
            return nullHashCode ; 
        return obj.hashCode() ;
    }

    // Java 1.4 .toString == Java 1.5 .toPlainString
    // Java 1.5 .toString => different to .toString 1.4
    // Portable(?!) - round to scale 0 and get the toString
    
    static public String stringForm(BigDecimal decimal)
    { 
        // Java 1.5-ism -- 
        //return decimal.toPlainString() ;
        return decimal.toString() ;
    }
    
    static public String stringForm(double d)
    { 
        // SPARQL form always has "e0"
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
    
    public static boolean triplePathIso(TriplePath tp1, TriplePath tp2, NodeIsomorphismMap isoMap)
    {
        if ( tp1.isTriple() ^ tp2.isTriple() ) 
            return false ;

        if ( tp1.isTriple() )
            return Utils.tripleIso(tp1.asTriple(), tp2.asTriple(), isoMap) ;
        else
            return Utils.nodeIso(tp1.getSubject(), tp2.getSubject(), isoMap) && 
                   Utils.nodeIso(tp1.getObject(), tp2.getObject(), isoMap) &&
                   tp1.getPath().equalTo(tp2.getPath(), isoMap) ;
    }
    
    public static boolean tripleIso(Triple t1, Triple t2, NodeIsomorphismMap labelMap)
    {
        Node s1 = t1.getSubject() ;
        Node p1 = t1.getPredicate() ;
        Node o1 = t1.getObject() ;
        
        Node s2 = t2.getSubject() ;
        Node p2 = t2.getPredicate() ;
        Node o2 = t2.getObject() ;
        
        if ( ! nodeIso(s1, s2, labelMap) )
            return false ;
        if ( ! nodeIso(p1, p2, labelMap) )
            return false ;
        if ( ! nodeIso(o1, o2, labelMap) )
            return false ;

        return true ;
    }
    
    public static boolean nodeIso(Node n1, Node n2, NodeIsomorphismMap isoMap)
    {
        if ( isoMap != null && Var.isBlankNodeVar(n1) && Var.isBlankNodeVar(n2) )
            return isoMap.makeIsomorhpic(n1, n2) ;
        return n1.equals(n2) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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