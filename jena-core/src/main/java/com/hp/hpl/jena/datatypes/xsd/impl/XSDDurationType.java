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

package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;

/**
 * The XSD duration type, the only job of this extra layer is to
 * wrap the return value in a more convenient accessor type. We could
 * avoid this proliferation of trivial types by use of reflection but
 * since that causes allergic reactions in some we use brute force.
 * <p>
 * This class includees code derived from Xerces 2.6.0 
 * Copyright (c) 1999-2002 The Apache Software Foundation.
 * All rights reserved.
 * </p>
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
   @Override
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
                   int msEnd = end;
                   while (str.charAt(msEnd-1) == '0') msEnd--;
                   date[ms] = negate * parseInt(str, mlsec+1, msEnd);
                   date[msscale] = msEnd - mlsec - 1;
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
