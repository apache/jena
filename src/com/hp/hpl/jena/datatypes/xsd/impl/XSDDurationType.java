/******************************************************************
 * File:        XSDDurationType.java
 * Created by:  Dave Reynolds
 * Created on:  16-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: XSDDurationType.java,v 1.7 2003-12-04 15:58:00 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;

/**
 * The XSD duration type, the only job of this extra layer is to
 * wrap the return value in a more convenient accessor type. We could
 * avoid this proliferation of trivial types by use of reflection but
 * since that causes allergic reactions in some we use brute force.
 * <p>
 * This class includees code derived from Xerces 2.6.0 Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 * </p>
 *            
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2003-12-04 15:58:00 $
 */
public class XSDDurationType extends XSDAbstractDateTimeType {
    
    /**
     * Constructor
     */
    public XSDDurationType() {
        super("duration");
        javaClass = XSDDuration.class;
    }
        
    /**
    * Parse a validated date. This is invoked from
    * XSDDatatype.convertValidatedDataValue rather then from a local
    * parse method to make the implementation of XSDGenericType easier.
    */
   public Object parseValidated(String str) {
       int len = str.length();
       int[] date=new int[TOTAL_SIZE];

       int start = 0;
       char c=str.charAt(start++);
       if ( c!='P' && c!='-' ) {
           throw new DatatypeFormatException("Internal error: validated duration failed to parse(1)");
       }
       else {
           date[utc]=(c=='-')?'-':0;
           if ( c=='-' && str.charAt(start++)!='P' ) {
               throw new DatatypeFormatException("Internal error: validated duration failed to parse(2)");
           }
       }

       int negate = 1;
       //negative duration
       if ( date[utc]=='-' ) {
           negate = -1;

       }

       int endDate = indexOf (str, start, len, 'T');
       if ( endDate == -1 ) {
           endDate = len;
       }
       //find 'Y'
       int end = indexOf (str, start, endDate, 'Y');
       if ( end!=-1 ) {
           //scan year
           date[CY]=negate * parseInt(str,start,end);
           start = end+1;
       }

       end = indexOf (str, start, endDate, 'M');
       if ( end!=-1 ) {
           //scan month
           date[M]=negate * parseInt(str,start,end);
           start = end+1;
       }

       end = indexOf (str, start, endDate, 'D');
       if ( end!=-1 ) {
           //scan day
           date[D]=negate * parseInt(str,start,end);
           start = end+1;
       }

       if ( len == endDate && start!=len ) {
           throw new DatatypeFormatException("Internal error: validated duration failed to parse(3)");
       }
       if ( len !=endDate ) {

           end = indexOf (str, ++start, len, 'H');
           if ( end!=-1 ) {
               //scan hours
               date[h]=negate * parseInt(str,start,end);
               start=end+1;
           }

           end = indexOf (str, start, len, 'M');
           if ( end!=-1 ) {
               //scan min
               date[m]=negate * parseInt(str,start,end);
               start=end+1;
           }

           end = indexOf (str, start, len, 'S');
           if ( end!=-1 ) {
               //scan seconds
               int mlsec = indexOf (str, start, end, '.');
               if ( mlsec >0 ) {
                   date[s]  = negate * parseInt (str, start, mlsec);
                   date[ms] = negate * parseInt (str, mlsec+1, end);
               }
               else {
                   date[s]=negate * parseInt(str, start,end);
               }
               start=end+1;
           }
       }
       return new XSDDuration(date);
   }
    
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
