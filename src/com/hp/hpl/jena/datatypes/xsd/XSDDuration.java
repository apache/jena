/******************************************************************
 * File:        Duration.java
 * Created by:  Dave Reynolds
 * Created on:  16-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: XSDDuration.java,v 1.3 2003-04-15 21:02:57 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd;

import org.apache.xerces.impl.dv.XSSimpleType;

/**
 * Represent an XSD duration value. We use a seven dimensional space
 * with years, months, days, hours, minutes, seconds and fractional seconds.
 * This deviates from the spec which allows arbitrary position 
 * decimals for seconds.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-04-15 21:02:57 $
 */
public class XSDDuration extends AbstractDateTime {

    /**
     * Constructor - should only be used by the internals but public scope because
     * the internals spread across multiple packages.
     * 
     * @param value the date/time value returned by the parsing
     * @param dtype the XSD type representation
     */
    public XSDDuration(Object value, XSSimpleType dtype) {
        super(value, dtype);
    }
    
    /**
     * Return the number of years in the duration
     */
    public int getYears() {
        return data[CY];
    }
    
    /**
     * Return the number of months in the duration
     */
    public int getMonths() {
        return data[MONTH];
    }
    
    /**
     * Return the number of years in the duration
     */
    public int getDays() {
        return data[DAY];
    }
    
    /**
     * Return the number of hours in the duration
     */
    public int getHours() {
        return data[HOUR];
    }
    
    /**
     * Return the number of minutes in the duration
     */
    public int getMinutes() {
        return data[MINUTE];
    }
    
    /**
     * Return the number of full seconds in the duration
     */
    public int getFullSeconds() {
        return data[SECOND];
    }
    
    /**
     * Return the number of seconds in the duration, including fractional part
     */
    public double getSeconds() {
        return data[SECOND] + fractionalSeconds;
    }
    
    /**
     * Return the time component of the duration - i.e. just the hours/mins/seconds,
     * and returns the values in seconds.
     */
    public double getTimePart() {
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
