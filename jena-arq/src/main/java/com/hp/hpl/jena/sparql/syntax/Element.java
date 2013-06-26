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

package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.serializer.FormatterElement ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** Element - abstract class for all pattern elements */

public abstract class Element
{
    public abstract void visit(ElementVisitor v) ;
    
    @Override
    public abstract int hashCode() ;
    // If the labelMap is null, do .equals() on nodes, else map from
    // bNode varables in one to bNodes variables in the other 
    public abstract boolean equalTo(Element el2, NodeIsomorphismMap isoMap) ;
    
    @Override
    final public boolean equals(Object el2)
    { 
        if ( this == el2 ) return true ;

        if ( ! ( el2 instanceof Element ) )
            return false ;
        return equalTo((Element)el2, null) ;
    }
    
    @Override
    public String toString()
    {
        return FormatterElement.asString(this) ;
    }
    
    // Constants used in hashing to stop an element and it's subelement
    // (if just one) having the same hash.
    
    static final int HashBasicGraphPattern    = 0xA1 ;
    static final int HashGroup                = 0xA2 ;
    static final int HashUnion                = 0xA3 ;
    static final int HashOptional             = 0xA4 ;
    // static final int HashGraph                = 0xA5 ; // Not needed
    static final int HashExists               = 0xA6 ;
    static final int HashNotExists            = 0xA7 ;
    static final int HashPath                 = 0xA8 ;
    static final int HashFetch                = 0xA9 ;
}
