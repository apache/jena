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

// Xerces 2.11.0 does not have  xsd:dayTimeDuration.
// Treat as duration with checking.
public class XSDDayTimeDurationType extends XSDAbstractDateTimeType {
    public XSDDayTimeDurationType() {
        super("duration") ;
        super.uri = XSD + "#dayTimeDuration" ; 
    }    

    @Override
    public Object parse(String lex) {
        Object obj = super.parse(lex) ;
        // Must not have Y
        if ( lex.indexOf('Y') != -1 )
            // has year.
            throw new DatatypeFormatException("Not valid as xsd:dayTimeDuration: "+lex) ;
        
        int idx_T = lex.indexOf('T') ;
        int idx_M = lex.indexOf('M') ;
        
        if ( idx_T == -1 ) {
            // There is no T ; must have D and no M.
            if ( lex.indexOf('D') == -1 )
                throw new DatatypeFormatException("Not valid as xsd:dayTimeDuration: "+lex) ;
            if ( idx_M != -1 )
                // M, no T -> month
                throw new DatatypeFormatException("Not valid as xsd:dayTimeDuration: "+lex) ;
            return obj ;
        }

        // Has T
        // Must not have a M before T
        if ( idx_M != -1 && idx_M < idx_T )
            // M before T => month.
            throw new DatatypeFormatException("Not valid as xsd:dayTimeDuration: "+lex) ;
        return obj ;
    }
}
