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

public class XSDDateTimeStampType extends XSDDateTimeType {
        public XSDDateTimeStampType(String name) {
            //  Pretend to to be dateTime then tweak the uri.
            super("dateTime") ;
            super.javaClass = null ;
            super.uri = XSD + "#"+name ; 
        }    
        
        @Override
        public Object parse(String lex) {
            Object obj = super.parse(lex) ;
            // At this point it is OK as an xsd:dateTime - check it has a timezone.
            //  timezoneFrag ::= 'Z' | ('+' | '-') (('0' digit | '1' [0-3]) ':' minuteFrag | '14:00')
            if ( lex.indexOf('Z') != -1 ) 
                return obj ;
            // Check a legal xsd:dateTime ends with timezoneFrag
            // Z or a +/- at length-6
            
            // Avoid regex!
            int n = lex.length() ;
            char z = lex.charAt(n-6) ;
            if ( z != '+' && z != '-' )
                throw new DatatypeFormatException("Not valid as xsd:dateTimeStamp: "+lex) ;
//                || ! RiotChars.isDigit(lex.charAt(n-5))
//                || ! RiotChars.isDigit(lex.charAt(n-4))
//                || lex.charAt(n-3) != ':'
//                || ! RiotChars.isDigit(lex.charAt(n-2))
//                || ! RiotChars.isDigit(lex.charAt(n-1)) )
            return obj ;
            // Alternative:
            //static Pattern p = Pattern.compile("\\d\\d:\\d\\d$") ;
            //return p.matcher(x).find() ;
        }
    }
