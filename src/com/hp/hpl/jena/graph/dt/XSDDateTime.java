/******************************************************************
 * File:        XSDDateTime.java
 * Created by:  Dave Reynolds
 * Created on:  17-Dec-2002
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: XSDDateTime.java,v 1.1.1.1 2002-12-19 19:13:46 bwm Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import org.apache.xerces.impl.dv.XSSimpleType;
import java.util.*;

/**
 * Represent an XSD date/time value. Rather than have a separate type for each
 * legal date/time value combination this is a combination type than does runtime
 * checks whether a given field is legal in the current circumstances.
 * <p>TODO: revist and consider have separate types for each.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1.1.1 $ on $Date: 2002-12-19 19:13:46 $
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
    
    /** table mapping xs type name to mask of legal values */
    public static final HashMap maskMap = new HashMap();
    
    // initialize the mask map
    static {
        maskMap.put("dateTime",   new Integer(FULL_MASK));
        maskMap.put("date",       new Integer(YEAR_MASK | MONTH_MASK | DAY_MASK));
        maskMap.put("time",       new Integer(TIME_MASK));
        maskMap.put("gYearMonth", new Integer(YEAR_MASK | MONTH_MASK));
        maskMap.put("gMonthDay",  new Integer(MONTH_MASK | DAY_MASK));
        maskMap.put("gYear",      new Integer(YEAR_MASK));
        maskMap.put("gMonth",     new Integer(MONTH_MASK));
        maskMap.put("gDay",       new Integer(DAY_MASK));
    }
    
    /** Set of legal fields for the particular date/time instance */
    protected short mask;
    
    /**
     * Constructor - only used internally to the package
     * 
     * @param value the date/time value returned by the parsing
     * @param dtype the XSD type representation
     */
    XSDDateTime(Object value, XSSimpleType dtype) {
        super(value, dtype);
        mask = ((Integer)maskMap.get(dtype.getName())).shortValue();
    }
    
    /**
     * Return the date time as a java Calendar object. 
     * If the timezone has been specified then the object is normalized to GMT.
     * If the zone has not been specified then we use the default timezone.
     * 
     * @throw IllegalDateTimeFieldException if this is not a full date + time
     */
    public Calendar asCalendar () throws IllegalDateTimeFieldException {
        TimeZone tz = data[UTC] == 'Z' ? TimeZone.getTimeZone("GMT") : TimeZone.getDefault();
        Calendar calendar = new GregorianCalendar(tz);
        calendar.set(data[CY], data[MONTH], data[DAY], data[HOUR], data[MINUTE], data[SECOND]);
        return calendar;
    }
    
    /**
     * Return the number of years in the dateTime.
     * @throw IllegalDateTimeFieldException if there is no legal year component
     */
    public int getYears() throws IllegalDateTimeFieldException {
        if ((mask & YEAR_MASK) == 0) throw new IllegalDateTimeFieldException("Year not available");
        return data[CY];
    }
    
    /**
     * Return the number of months in the dateTime
     * @throw IllegalDateTimeFieldException if there is no legal month component
     */
    public int getMonths() throws IllegalDateTimeFieldException {
        if ((mask & MONTH_MASK) == 0) throw new IllegalDateTimeFieldException("Month not available");
        return data[MONTH];
    }
    
    /**
     * Return the number of years in the dateTime
     * @throw IllegalDateTimeFieldException if there is no legal day component
     */
    public int getDays() throws IllegalDateTimeFieldException {
        if ((mask & DAY_MASK) == 0) throw new IllegalDateTimeFieldException("Day not available");
        return data[DAY];
    }
    
    /**
     * Return the number of hours in the dateTime
     * @throw IllegalDateTimeFieldException if there is no legal time component
     */
    public int getHours() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[HOUR];
    }
    
    /**
     * Return the number of minutes in the dateTime
     * @throw IllegalDateTimeFieldException if there is no legal time component
     */
    public int getMinutes() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[MINUTE];
    }
    
    /**
     * Return the number of full seconds in the dateTime
     * @throw IllegalDateTimeFieldException if there is no legal time component
     */
    public int getFullSeconds() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[SECOND];
    }
    
    /**
     * Return the number of seconds in the dateTime, including fractional part
     * @throw IllegalDateTimeFieldException if there is no legal time component
     */
    public double getSeconds() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return data[SECOND] + fractionalSeconds;
    }
    
    /**
     * Return the time component of the dateTime - i.e. just the hours/mins/seconds,
     * and returns the values in seconds.
     * @throw IllegalDateTimeFieldException if there is no legal time component
     */
    public double getTimePart() throws IllegalDateTimeFieldException {
        if ((mask & TIME_MASK) == 0) throw new IllegalDateTimeFieldException("Time not available");
        return ((data[HOUR]) * 60l + data[MINUTE]) * 60l + getSeconds();
    }
    
    
}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
