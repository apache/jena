/******************************************************************
 * File:        AbstractDateTime.java
 * Created by:  Dave Reynolds
 * Created on:  15-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: AbstractDateTime.java,v 1.2 2003-01-31 11:32:02 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.AbstractDateTimeDV;

/**
 * Base class for representation of XSD duration, time, date/time
 * and related datatype instances. We are using the Xerces internal
 * packages for the all heavy lifting which represent date/times
 * using an int array. These wrapper classes just provide more
 * conventient access to the date values.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-01-31 11:32:02 $
 */
public class AbstractDateTime {

    /** The array of year/month etc values as ints */
    protected int[] data;
    
    /** The fractional seconds */
    protected double fractionalSeconds;
    
    /** The Xerces type declaration object - used for manipulations */
    protected XSSimpleTypeDecl typeDecl;

    /** The data validator which can perform conversion and serialization
     * operations */
    protected static AbstractDateTimeDV dtDV;
    
    // access index constants
    protected final static int CY = 0,  MONTH = 1, DAY = 2, HOUR = 3,
    MINUTE = 4, SECOND = 5, MS = 6, UTC=7, HH=0, MM=1;
    
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
     * @param dtype the XSD type representation
     */
    public AbstractDateTime(Object value, XSSimpleType dtype) {
        data = (int[]) value;
        extractFractionalSeconds();
        typeDecl = (XSSimpleTypeDecl)dtype;
    }
    
    /** 
     * Comparison function. Not quite the same as normal java compare
     * because XSD date/times are not always comparable.
     * 
     * @param other the time/date to compare to
     * @return an order flag - one of LESS_THAN, EQUAL, GREATER_THEN, INDETERMINATE
     */
    public int compare(AbstractDateTime other) {
        return dtDV.compare(this, other);
    }
    
    /**
     * Serialization
     */
    public String toString() {
        return typeDecl.getStringValue(data);
    }
    
    /**
     * Convert the strange fractional second part from Xerces into a true fraction.
     */
    protected void extractFractionalSeconds() {
        if (data[MS] != 0) {
            int fs = data[MS];
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
