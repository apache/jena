/******************************************************************
 * File:        AbstractDateTime.java
 * Created by:  Dave Reynolds
 * Created on:  15-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: AbstractDateTime.java,v 1.7 2003-12-15 15:26:38 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd;

/**
 * Base class for representation of XSD duration, time, date/time
 * and related datatype instances. We are using the Xerces internal
 * packages for the all heavy lifting which represent date/times
 * using an int array. These wrapper classes just provide more
 * convenient access to the date values.
 * <p>
 * This class includees code derived from Xerces 2.6.0 Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2003-12-15 15:26:38 $
 */
public class AbstractDateTime {

    /** The array of year/month etc values as ints */
    protected int[] data;
    
    /** The fractional seconds */
    protected double fractionalSeconds;
    
    //define constants
    protected final static int CY = 0,  M = 1, D = 2, h = 3,
    m = 4, s = 5, ms = 6, utc=7, hh=0, mm=1;
        
    //size for all objects must have the same fields:
    //CCYY, MM, DD, h, m, s, ms + timeZone
    protected final static int TOTAL_SIZE = 8;

    /** constant to indicate a less than relationship from compare() */
    public static final short LESS_THAN     = -1;
    /** constant to indicate an equals relationship from compare() */
    public static final short EQUAL         = 0;
    /** constant to indicate a greater than relationship from compare() */
    public static final short GREATER_THAN  = 1;
    /** constant to indicate an indeterminate relationship from compare() */
    public static final short INDETERMINATE = 2;

    /**
     * Constructor
     * @param value the date/time value returned by the parsing
     */
    public AbstractDateTime(Object value) {
        data = (int[]) value;
        if (data[utc] == 0) data[utc]='Z';
        extractFractionalSeconds();
    }
    
    /** 
     * Comparison function. Not quite the same as normal java compare
     * because XSD date/times are not always comparable.
     * 
     * @param other the time/date to compare to
     * @return an order flag - one of LESS_THAN, EQUAL, GREATER_THEN, INDETERMINATE
     */
    public int compare(AbstractDateTime other) {
        return compareDates(data, other.data, true);
    }
    
    /**
     * Convert the strange fractional second part from Xerces into a true fraction.
     */
    protected void extractFractionalSeconds() {
        if (data[ms] != 0) {
            int fs = data[ms];
            double log10 = Math.log((double)fs)/2.302585093;
            int exp = 1 + (int)(log10 / 10);
            fractionalSeconds = ((double)fs) / Math.pow(10.0, (double)exp);
        }
    }
    
    /**
     * Equality function
     */
    public boolean equals(Object obj) {
        if (obj instanceof AbstractDateTime) {
            AbstractDateTime adt = (AbstractDateTime) obj;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != adt.data[i]) return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * hash function
     */
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < data.length; i++) {
            hash = (hash << 1) ^ data[i];
        }
        return hash;
    }
    
//  --------------------------------------------------------------------
//  This code is adapated from Xerces 2.6.0 AbstractDateTimeDV.    
//  Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
//  reserved.
//  --------------------------------------------------------------------
    
    /**
     * Compare algorithm described in dateDime (3.2.7).
     *
     * @param date1  normalized date representation of the first value
     * @param date2  normalized date representation of the second value
     * @param strict
     * @return less, greater, less_equal, greater_equal, equal
     */
    protected short compareDates(int[] date1, int[] date2, boolean strict) {
        if ( date1[utc]==date2[utc] ) {
            return compareOrder(date1, date2);
        }
        short c1, c2;
        
        int[] tempDate = new int[TOTAL_SIZE];
        int[] timeZone = new int[2];

        if ( date1[utc]=='Z' ) {

            //compare date1<=date1<=(date2 with time zone -14)
            //
            cloneDate(date2, tempDate); //clones date1 value to global temporary storage: fTempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            tempDate[utc]='+';
            normalize(tempDate, timeZone);
            c1 = compareOrder(date1, tempDate);
            if (c1 == LESS_THAN)
                return c1;

            //compare date1>=(date2 with time zone +14)
            //
            cloneDate(date2, tempDate); //clones date1 value to global temporary storage: tempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            tempDate[utc]='-';
            normalize(tempDate, timeZone);
            c2 = compareOrder(date1, tempDate);
            if (c2 == GREATER_THAN)
                return c2;

            return INDETERMINATE;
        }
        else if ( date2[utc]=='Z' ) {

            //compare (date1 with time zone -14)<=date2
            //
            cloneDate(date1, tempDate); //clones date1 value to global temporary storage: tempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            tempDate[utc]='-';
            normalize(tempDate, timeZone);
            c1 = compareOrder(tempDate, date2);
            if (c1 == LESS_THAN)
                return c1;

            //compare (date1 with time zone +14)<=date2
            //
            cloneDate(date1, tempDate); //clones date1 value to global temporary storage: tempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            tempDate[utc]='+';
            normalize(tempDate, timeZone);
            c2 = compareOrder(tempDate, date2);
            if (c2 == GREATER_THAN)
                return c2;

            return INDETERMINATE;
        }
        return INDETERMINATE;

    }

    /**
     * Given normalized values, determines order-relation
     * between give date/time objects.
     *
     * @param date1  date/time object
     * @param date2  date/time object
     * @return 0 if date1 and date2 are equal, a value less than 0 if date1 is less than date2, a value greater than 0 if date1 is greater than date2
     */
    protected short compareOrder (int[] date1, int[] date2) {

        for ( int i=0;i<TOTAL_SIZE;i++ ) {
            if ( date1[i]<date2[i] ) {
                return -1;
            }
            else if ( date1[i]>date2[i] ) {
                return 1;
            }
        }
        return 0;
    }
    /**
      * If timezone present - normalize dateTime  [E Adding durations to dateTimes]
      * Public to allow reuse with type objects.
      * 
      * @param date   CCYY-MM-DDThh:mm:ss+03
      * @return CCYY-MM-DDThh:mm:ssZ
      */
     public static void normalize (int[] date, int[] timeZone) {

         // REVISIT: we have common code in addDuration() for durations
         //          should consider reorganizing it.
         //

         //add minutes (from time zone)
         int negate = 1;
         if (date[utc]=='+') {
             negate = -1;
         }
         int temp = date[m] + negate*timeZone[mm];
         int carry = fQuotient (temp, 60);
         date[m]= mod(temp, 60, carry);

         //add hours
         temp = date[h] + negate*timeZone[hh] + carry;
         carry = fQuotient(temp, 24);
         date[h]=mod(temp, 24, carry);

         date[D]=date[D]+carry;

         while ( true ) {
             temp=maxDayInMonthFor(date[CY], date[M]);
             if (date[D]<1) {
                 date[D] = date[D] + maxDayInMonthFor(date[CY], date[M]-1);
                 carry=-1;
             }
             else if ( date[D]>temp ) {
                 date[D]=date[D]-temp;
                 carry=1;
             }
             else {
                 break;
             }
             temp=date[M]+carry;
             date[M]=modulo(temp, 1, 13);
             date[CY]=date[CY]+fQuotient(temp, 1, 13);
         }
         date[utc]='Z';
     }


    /**
     * Resets object representation of date/time
     *
     * @param data   date/time object
     */
    protected void resetDateObj (int[] data) {
        for ( int i=0;i<TOTAL_SIZE;i++ ) {
            data[i]=0;
        }
    }

    /**
     * Given {year,month} computes maximum
     * number of days for given month
     *
     * @param year
     * @param month
     * @return integer containg the number of days in a given month
     */
    protected static int maxDayInMonthFor(int year, int month) {
        //validate days
        if ( month==4 || month==6 || month==9 || month==11 ) {
            return 30;
        }
        else if ( month==2 ) {
            if ( isLeapYear(year) ) {
                return 29;
            }
            else {
                return 28;
            }
        }
        else {
            return 31;
        }
    }

    private static boolean isLeapYear(int year) {

        //REVISIT: should we take care about Julian calendar?
        return((year%4 == 0) && ((year%100 != 0) || (year%400 == 0)));
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int mod (int a, int b, int quotient) {
        //modulo(a, b) = a - fQuotient(a,b)*b
        return (a - quotient*b) ;
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int fQuotient (int a, int b) {

        //fQuotient(a, b) = the greatest integer less than or equal to a/b
        return (int)Math.floor((float)a/b);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int modulo (int temp, int low, int high) {
        //modulo(a - low, high - low) + low
        int a = temp - low;
        int b = high - low;
        return (mod (a, b, fQuotient(a, b)) + low) ;
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected static int fQuotient (int temp, int low, int high) {
        //fQuotient(a - low, high - low)

        return fQuotient(temp - low, high - low);
    }
    
    //
    //Private help functions
    //

    private void cloneDate (int[] finalValue, int[] tempDate) {
        System.arraycopy(finalValue, 0, tempDate, 0, TOTAL_SIZE);
    }
   
//  --------------------------------------------------------------------
//  End of code is adapated from Xerces 2.6.0 AbstractDateTimeDV.    
//  --------------------------------------------------------------------

}

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
