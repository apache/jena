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

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** An optional element in a query. */

public class ElementOptional extends Element
{
    Element optionalPart ;

    public ElementOptional(Element optionalPart)
    {
        this.optionalPart = optionalPart ;
    }

    public Element getOptionalElement() { return optionalPart ; }

    @Override
    public int hashCode()
    {
        int hash = Element.HashOptional ;
        hash = hash ^ getOptionalElement().hashCode() ;
        return hash ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementOptional ) ) 
            return false ;
        
        ElementOptional opt2 = (ElementOptional)el2 ;
        return getOptionalElement().equalTo(opt2.getOptionalElement(), isoMap) ;
    }
    
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
