/******************************************************************
 * File:        XSDYearMonthType.java
 * Created by:  Dave Reynolds
 * Created on:  04-Dec-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 * $Id: XSDYearMonthType.java,v 1.3 2005-02-21 12:02:27 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.xsd.AbstractDateTime;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

/**
 * Type processor for gYearMonth, most of the machinery is in the
 * base XSDAbstractDateTimeType class.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2005-02-21 12:02:27 $
 */
public class XSDYearMonthType extends XSDAbstractDateTimeType {

    /**
     * Constructor
     */
    public XSDYearMonthType(String typename) {
        super(typename);
    }

    /**
     * Parse a validated date. This is invoked from
     * XSDDatatype.convertValidatedDataValue rather then from a local
     * parse method to make the implementation of XSDGenericType easier.
     */
    public Object parseValidated(String str) {
        int len = str.length();
        int[] date = new int[TOTAL_SIZE];
        int[] timeZone = new int[2];

        // get date
        int end = getYearMonth(str, 0, len, date);
        date[D] = DAY;
        parseTimeZone (str, end, len, date, timeZone);

        if ( date[utc]!=0 && date[utc]!='Z' ) {
            AbstractDateTime.normalize(date, timeZone);
        }

        return new XSDDateTime(date, YEAR_MASK | MONTH_MASK);
    }
    
}




/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
