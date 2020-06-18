/**
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

package org.apache.jena.datatypes.xsd.impl;

import org.apache.jena.datatypes.DatatypeFormatException ;

public class XSDYearMonthDurationType extends XSDAbstractDateTimeType {
    public XSDYearMonthDurationType() {
        super("duration") ;
        super.uri = XSD + "#yearMonthDuration" ; 
    }  
    
    @Override
    public Object parse(String lex) {
        Object obj = super.parse(lex) ;
        if ( lex.indexOf('D') != -1 || lex.indexOf('T') != -1 )
            throw new DatatypeFormatException("Not valid as xsd:yearMonthDuration: "+lex) ;
        return obj ;
    }
}
