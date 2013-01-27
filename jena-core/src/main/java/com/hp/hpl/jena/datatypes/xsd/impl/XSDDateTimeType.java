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

import com.hp.hpl.jena.datatypes.xsd.*;

/**
 * Type processor for dateTime, most of the machinery is in the
 * base XSDAbstractDateTimeType class.
 */
public class XSDDateTimeType extends XSDAbstractDateTimeType {

    /**
     * Constructor
     */
    public XSDDateTimeType(String typename) {
        super(typename);
        javaClass = XSDDateTime.class;
    }

    /**
     * Parse a validated date. This is invoked from
     * XSDDatatype.convertValidatedDataValue rather then from a local
     * parse method to make the implementation of XSDGenericType easier.
     */
    @Override
    public Object parseValidated(String str) {
         int len = str.length();
         int[] date = new int[TOTAL_SIZE];
         int[] timeZone = new int[2];

         int end = indexOf (str, 0, len, 'T');

         // both time and date
         getDate(str, 0, end, date);
         getTime(str, end+1, len, date, timeZone);

         if ( date[utc]!=0 && date[utc]!='Z') {
             AbstractDateTime.normalize(date, timeZone);
         }
         return new XSDDateTime(date, XSDDateTime.FULL_MASK);
    }
    
}
