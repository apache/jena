/******************************************************************
 * File:        XSDDateTime.java
 * Created by:  Dave Reynolds
 * Created on:  17-Dec-2002
 * 
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: XSDDateTime.java,v 1.2 2009-09-25 09:58:14 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd;

import java.util.*;

import com.hp.hpl.jena.datatypes.xsd.impl.XSDAbstractDateTimeType;


/**
 * Represent an XSD date/time value. Rather than have a separate type for each
 * legal date/time value combination this is a combination type than does runtime
 * checks whether a given field is legal in the current circumstances.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2009-09-25 09:58:14 $
 */
public class XSDDateTime extends AbstractDateTime {
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
    
    // Unused /** table mapping xs type name to mask of legal values */
    //public static final HashMap maskMap = new HashMap();
        
    /** Set of legal fields for the particular date/time instance */
    protected short mask;
    
    /**
     * Constructor - should only be used by the internals but public scope because
     * the internals spread across multiple packages.
     * 
     * @param value the date/time value returned by the parsing
     * @param mask bitmask defining which components are valid in this instance
     * (e.g. dates don't have valid time fields).
     */
    public XSDDateTime(Object value, int mask) {
        super(value);
        this.mask = (short)mask;
    }
    
    
    /**
     * Constructor - create a full DateTime object from a java calendar instance.
     * 
     * @param date java calendar instance
     */
    public XSDDateTime(Calendar date) {
        super(convertCalendar(date));
        this.mask = FULL_MASK;
    }
    
    /**
     * Return the most specific xsd type which can represent
     * this date/time
     */
    public XSDDatatype getNarrowedDatatype() {
        switch (mask) {
        case TIME_MASK:
            return XSDDatatype.XSDtime;
        case MONTH_MASK:
            return XSDDatatype.XSDgMonth;
        case DAY_MASK:
            return XSDDatatype.XSDgDay;
        case YEAR_MASK:
            return XSDDatatype.XSDgYear;
        case MONTH_MASK | DAY_MASK:
            return XSDDatatype.XSDgMonthDay;
        case MONTH_MASK | YEAR_MASK:
            return XSDDatatype.XSDgYearMonth;
        case MONTH_MASK | YEAR_MASK | DAY_MASK:
            return XSDDatatype.XSDdate;
        default:
            return XSDDatatype.XSDdateTime;
        }
    }
    
    /**
     * Set the mask for this date/time to be that appropriate
     * for the given XSD subtype. If the type is a subtype of XSDdateTime the 
     * mask will be narrowed appropriately, other types will be silently ignored.
     */
    public void narrowType(XSDDatatype dt) {
        if (dt.equals(XSDDatatype.XSDtime)) {
            mask = TIME_MASK;
        } else if (dt.equals(XSDDatatype.XSDgMonth)) {
            mask = MONTH_MASK;
        } else if (dt.equals(XSDDatatype.XSDgDay)) {
            mask = DAY_MASK;
        } else if (dt.equals(XSDDatatype.XSDgYear)) {
            mask = YEAR_MASK;
        } else if (dt.equals(XSDDatatype.XSDgMonthDay)) {
            mask = MONTH_MASK | DAY_MASK;
        } else if (dt.equals(XSDDatatype.XSDgYearMonth)) {
            mask = YEAR_MASK | MONTH_MASK;
        } else if (dt.equals(XSDDatatype.XSDdate)) {
            mask = MONTH_MASK | YEAR_MASK | DAY_MASK;
        }  
    }
    
    /**
     * Convert a java calendar object to a new int[] in the format used by XSDAbstractDateTime
     */
    private static int[] convertCalendar(Calendar date) {
        int[] data = new int[TOTAL_SIZE];
        int offset = date.get(Calendar.ZONE_OFFSET) + date.get(Calendar.DST_OFFSET);
                                        //  Thanks to Greg Shueler for pointing out need for DST offset
        Calendar cal = date;
        if (offset != 0) {
            cal = (Calendar)date.clone();
            cal.add(Calendar.MILLISECOND, -offset);
        }
        data[AbstractDateTime.CY] = cal.get(Calendar.YEAR);
        data[AbstractDateTime.M] = cal.get(Calendar.MONTH) + 1;
        data[AbstractDateTime.D] = cal.get(Calendar.DAY_OF_MONTH);
        data[AbstractDateTime.h] = cal.get(Calendar.HOUR_OF_DAY);
        data[AbstractDateTime.m] = cal.get(Calendar.MINUTE);
        data[AbstractDateTime.s] = cal.get(Calendar.SECOND);
        int ms = cal.get(Calendar.MILLISECOND);
        data[AbstractDateTime.ms] = ms;
        data[AbstractDateTime.msscale] = (ms == 0) ? 0 : 3;
        data[AbstractDateTime.utc] = 'Z';
        return data;
    }

    /**
     * Return the date time as a java Calendar object. 
     * If the timezone has been specified then the object is normalized to GMT.
     * If the zone has not been specified then we use the default timezone.
     * 
     * @throws IllegalDateTimeFieldException if this is not a full date + time
     */
    public Calendar asCalendar () throws IllegalDateTimeFieldException {
        TimeZone tz = data[utc] == 'Z' ? TimeZone.getTimeZone("GMT") : TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(data[CY], data[M] - 1, data[D], data[h], data[m], data[s]);
        calendar.set(Calendar.MILLISECOND, (int)Math.round(1000.0 * fractionalSeconds));
        // was this to work around problems with some Linux JDKs
        // calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
    
    /**
     * Return the number of years in the dateTime.
     * @throws IllegalDateTimeFieldException if there is no legal year component
     */
    public int getYears() throws IllegalDateTimeFieldException {
        if ((mask & YEAR_MASK) == 0) throw new IllegalDateTimeFieldException("Year not available");
        return data[CY];
    }
    
    /**
     * Return the month in the dateTime, this is in ISO8601 format so january = 1
     * @throws IllegalDateTimeFieldException if there is no legal month component
     */
    public int getMonths() throws IllegalDateTimeFieldException {
        if ((mask & MONTH_MASK) == 0) throw new IllegalDateTimeFieldException("Month not available");
        return data[M];
    }
    
    /**
     * Return the number of years in the dateTime
     * @throws IllegalDateTimeFieldException if there is no legal day component
     */
    public int getDays() throws IllegalDateTimeFieldException {
        if ((mask & DAY_MASK) == 0) throw new IllegalDateTimeFieldException("Day not available");
        return data[D];
    }
    
    /**
     * Return the number of hours in the dateTime
     * @throws IllegalDateTimeFieldException if there is no legal time component
     */
    public int getHours() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[h];
    }
    
    /**
     * Return the number of minutes in the dateTime
     * @throws IllegalDateTimeFieldException if there is no legal time component
     */
    public int getMinutes() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[m];
    }
    
    /**
     * Return the number of full seconds in the dateTime
     * @throws IllegalDateTimeFieldException if there is no legal time component
     */
    public int getFullSeconds() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[s];
    }
    
    /**
     * Return the number of seconds in the dateTime, including fractional part
     * @throws IllegalDateTimeFieldException if there is no legal time component
     */
    public double getSeconds() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[s] + fractionalSeconds;
    }
    
    /**
     * Return the time component of the dateTime - i.e. just the hours/mins/seconds,
     * and returns the values in seconds.
     * @throws IllegalDateTimeFieldException if there is no legal time component
     */
    public double getTimePart() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return ((data[h]) * 60l + data[m]) * 60l + getSeconds();
    }
    
    /**
     * Return legal serialized form.
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        if ((mask & YEAR_MASK) != 0) {
            buff.append(data[CY]);
        } else {
            buff.append("-");
        }
        if ((mask & (MONTH_MASK | DAY_MASK)) != 0) {
            buff.append("-");
            if ((mask & MONTH_MASK) != 0) {
                if (data[M] <= 9) buff.append("0");
                buff.append(data[M]);
            } else {
                buff.append("-");
            }
            if ((mask & DAY_MASK) != 0) {
                if (mask != DAY_MASK) buff.append("-");
                if (data[D] <= 9) buff.append("0");
                buff.append(data[D]);
            }
        }
        if ((mask & TIME_MASK) != 0 ) {
            buff.append("T");
            buff.append(timeLexicalForm());
        }

        if ( data[utc] != 0 )
            buff.append("Z");

        return buff.toString();
    }
    
    /**
     * Return the lexical form of the time component.
     */
    public String timeLexicalForm() {
        StringBuffer buff = new StringBuffer();
        if(data[h]<10) buff.append("0");
        buff.append(data[h]);

        buff.append(":");
        if(data[m]<10) buff.append("0");
        buff.append(data[m]);

        buff.append(":");
        if(data[s]<10) buff.append("0");
        buff.append(data[s]);

        if (data[ms] != 0) {
            buff.append(".");
            XSDAbstractDateTimeType.appendFractionalTime(buff, data[ms], data[msscale]);
        }
        return buff.toString();
    }
    
}

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
