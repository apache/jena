/******************************************************************
 * File:        XSDAbstractDateTimeType.java
 * Created by:  Dave Reynolds
 * Created on:  04-Dec-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 * $Id: XSDAbstractDateTimeType.java,v 1.2 2004-12-06 13:50:31 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Base class for all date/time/duration type representations.
 * Includes support functions for parsing and comparing dates.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2004-12-06 13:50:31 $
 */
public class XSDAbstractDateTimeType extends XSDDatatype {

    /**
     * Constructor
     */
    public XSDAbstractDateTimeType(String typename) {
        super(typename);
    }
     
    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and just uses the java.lang.Number 
     * equality.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
       return value1.getValue().equals(value2.getValue());
    }
    
    /** Mask to indicate whether year is present */
    public static final short YEAR_MASK = 0x1;
    
    /** Mask to indicate whether month is present */
    public static final short MONTH_MASK = 0x2;
    
    /** Mask to indicate whether day is present */
    public static final short DAY_MASK = 0x4;
    
    /** Mask to indicate whether time is present */
    public static final short TIME_MASK = 0x8;
    
    /** Mask to indicate all date/time are present */
    public static final short FULL_MASK = 0xf;
    
    
//  --------------------------------------------------------------------
//  This code is adapated from Xerces 2.6.0 AbstractDateTimeDV.    
//  Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
//  reserved.
//  --------------------------------------------------------------------

     //define constants
     protected final static int CY = 0,  M = 1, D = 2, h = 3,
     m = 4, s = 5, ms = 6, utc=7, hh=0, mm=1;
        
     //size for all objects must have the same fields:
     //CCYY, MM, DD, h, m, s, ms + timeZone
     protected final static int TOTAL_SIZE = 8;

     //define constants to be used in assigning default values for
     //all date/time excluding duration
     protected final static int YEAR=2000;
     protected final static int MONTH=01;
     protected final static int DAY = 15;


     /**
      * Parses time hh:mm:ss.sss and time zone if any
      *
      * @param start
      * @param end
      * @param data
      * @exception RuntimeException
      */
     protected  void getTime (String buffer, int start, int end, int[] data, int[] timeZone) throws RuntimeException{

         int stop = start+2;

         //get hours (hh)
         data[h]=parseInt(buffer, start,stop);

         //get minutes (mm)

         if (buffer.charAt(stop++)!=':') {
                 throw new RuntimeException("Error in parsing time zone" );
         }
         start = stop;
         stop = stop+2;
         data[m]=parseInt(buffer, start,stop);

         //get seconds (ss)
         if (buffer.charAt(stop++)!=':') {
                 throw new RuntimeException("Error in parsing time zone" );
         }
         start = stop;
         stop = stop+2;
         data[s]=parseInt(buffer, start,stop);

         if (stop == end)
             return;
        
         //get miliseconds (ms)
         start = stop;
         int milisec = buffer.charAt(start) == '.' ? start : -1;

         //find UTC sign if any
         int sign = findUTCSign(buffer, start, end);

         //parse miliseconds
         if ( milisec != -1 ) {
             // The end of millisecond part is between . and
             // either the end of the UTC sign
             start = sign < 0 ? end : sign;
             data[ms]=parseInt(buffer, milisec+1, start);
         }

         //parse UTC time zone (hh:mm)
         if ( sign>0 ) {
             if (start != sign)
                 throw new RuntimeException("Error in parsing time zone" );
             getTimeZone(buffer, data, sign, end, timeZone);
         }
         else if (start != end) {
             throw new RuntimeException("Error in parsing time zone" );
         }
     }

     /**
      * Parses date CCYY-MM-DD
      *
      * @param start
      * @param end
      * @param data
      * @exception RuntimeException
      */
     protected int getDate (String buffer, int start, int end, int[] date) throws RuntimeException{

         start = getYearMonth(buffer, start, end, date);

         if (buffer.charAt(start++) !='-') {
             throw new RuntimeException("CCYY-MM must be followed by '-' sign");
         }
         int stop = start + 2;
         date[D]=parseInt(buffer, start, stop);
         return stop;
     }

     /**
      * Parses date CCYY-MM
      *
      * @param start
      * @param end
      * @param data
      * @exception RuntimeException
      */
     protected int getYearMonth (String buffer, int start, int end, int[] date) throws RuntimeException{

         if ( buffer.charAt(0)=='-' ) {
             // REVISIT: date starts with preceding '-' sign
             //          do we have to do anything with it?
             //
             start++;
         }
         int i = indexOf(buffer, start, end, '-');
         if ( i==-1 ) throw new RuntimeException("Year separator is missing or misplaced");
         int length = i-start;
         if (length<4) {
             throw new RuntimeException("Year must have 'CCYY' format");
         }
         else if (length > 4 && buffer.charAt(start)=='0'){
             throw new RuntimeException("Leading zeros are required if the year value would otherwise have fewer than four digits; otherwise they are forbidden");
         }
         date[CY]= parseIntYear(buffer, i);
         if (buffer.charAt(i)!='-') {
             throw new RuntimeException("CCYY must be followed by '-' sign");
         }
         start = ++i;
         i = start +2;
         date[M]=parseInt(buffer, start, i);
         return i; //fStart points right after the MONTH
     }

     /**
      * Shared code from Date and YearMonth datatypes.
      * Finds if time zone sign is present
      *
      * @param end
      * @param date
      * @exception RuntimeException
      */
     protected void parseTimeZone (String buffer, int start, int end, int[] date, int[] timeZone) throws RuntimeException{

         //fStart points right after the date

         if ( start<end ) {
             int sign = findUTCSign(buffer, start, end);
             if ( sign<0 ) {
                 throw new RuntimeException ("Error in month parsing");
             }
             else {
                 getTimeZone(buffer, date, sign, end, timeZone);
             }
         }
     }

     /**
      * Parses time zone: 'Z' or {+,-} followed by  hh:mm
      *
      * @param data
      * @param sign
      * @exception RuntimeException
      */
     protected void getTimeZone (String buffer, int[] data, int sign, int end, int[] timeZone) throws RuntimeException{
         data[utc]=buffer.charAt(sign);

         if ( buffer.charAt(sign) == 'Z' ) {
             if (end>(++sign)) {
                 throw new RuntimeException("Error in parsing time zone");
             }
             return;
         }
         if ( sign<=(end-6) ) {

             //parse [hh]
             int stop = ++sign+2;
             timeZone[hh]=parseInt(buffer, sign, stop);
             if (buffer.charAt(stop++)!=':') {
                 throw new RuntimeException("Error in parsing time zone" );
             }

             //parse [ss]
             timeZone[mm]=parseInt(buffer, stop, stop+2);

             if ( stop+2!=end ) {
                 throw new RuntimeException("Error in parsing time zone");
             }

         }
         else {
             throw new RuntimeException("Error in parsing time zone");
         }
     }

     /**
      * Computes index of given char within StringBuffer
      *
      * @param start
      * @param end
      * @param ch     character to look for in StringBuffer
      * @return index of ch within StringBuffer
      */
     protected  int indexOf (String buffer, int start, int end, char ch) {
         for ( int i=start;i<end;i++ ) {
             if ( buffer.charAt(i) == ch ) {
                 return i;
             }
         }
         return -1;
     }

     // check whether the character is in the range 0x30 ~ 0x39
     public static final boolean isDigit(char ch) {
         return ch >= '0' && ch <= '9';
     }
    
     // if the character is in the range 0x30 ~ 0x39, return its int value (0~9),
     // otherwise, return -1
     public static final int getDigit(char ch) {
         return isDigit(ch) ? ch - '0' : -1;
     }


     /**
      * Return index of UTC char: 'Z', '+', '-'
      *
      * @param start
      * @param end
      * @return index of the UTC character that was found
      */
     protected int findUTCSign (String buffer, int start, int end) {
         int c;
         for ( int i=start;i<end;i++ ) {
             c=buffer.charAt(i);
             if ( c == 'Z' || c=='+' || c=='-' ) {
                 return i;
             }

         }
         return -1;
     }

     /**
      * Given start and end position, parses string value
      *
      * @param value  string to parse
      * @param start  Start position
      * @param end    end position
      * @return  return integer representation of characters
      */
     protected  int parseInt (String buffer, int start, int end)
     throws NumberFormatException{
         //REVISIT: more testing on this parsing needs to be done.
         int radix=10;
         int result = 0;
         int digit=0;
         int limit = -Integer.MAX_VALUE;
         int multmin = limit / radix;
         int i = start;
         do {
             digit = getDigit(buffer.charAt(i));
             if ( digit < 0 ) throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
             if ( result < multmin ) throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
             result *= radix;
             if ( result < limit + digit ) throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
             result -= digit;

         }while ( ++i < end );
         return -result;
     }

     // parse Year differently to support negative value.
     protected int parseIntYear (String buffer, int end){
         int radix=10;
         int result = 0;
         boolean negative = false;
         int i=0;
         int limit;
         int multmin;
         int digit=0;

         if (buffer.charAt(0) == '-'){
             negative = true;
             limit = Integer.MIN_VALUE;
             i++;

         }
         else{
             limit = -Integer.MAX_VALUE;
         }
         multmin = limit / radix;
         while (i < end)
         {
             digit = getDigit(buffer.charAt(i++));
             if (digit < 0) throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
             if (result < multmin) throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
             result *= radix;
             if (result < limit + digit) throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
             result -= digit;
         }

         if (negative)
         {
             if (i > 1) return result;
             else throw new NumberFormatException("'"+buffer.toString()+"' has wrong format");
         }
         return -result;

     }

     public String dateToString(int[] date) {
         StringBuffer message = new StringBuffer(25);
         append(message, date[CY], 4);
         message.append('-');
         append(message, date[M], 2);
         message.append('-');
         append(message, date[D], 2);
         message.append('T');
         append(message, date[h], 2);
         message.append(':');
         append(message, date[m], 2);
         message.append(':');
         append(message, date[s], 2);
         message.append('.');
         message.append(date[ms]);
         append(message, (char)date[utc], 0);
         return message.toString();
     }
    
     protected void append(StringBuffer message, int value, int nch) {
         if (value < 0) {
             message.append('-');
             value = -value;
         }
         if (nch == 4) {
             if (value < 10)
                 message.append("000");
             else if (value < 100)
                 message.append("00");
             else if (value < 1000)
                 message.append("0");
             message.append(value);
         }
         else if (nch == 2) {
             if (value < 10)
                 message.append('0');
             message.append(value);
         }
         else {
             if (value != 0)
                 message.append((char)value);
         }
     }

    
//  --------------------------------------------------------------------
//  End of code is adapated from Xerces 2.6.0 AbstractDateTimeDV.    
//  --------------------------------------------------------------------

}



/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
