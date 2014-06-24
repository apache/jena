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

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class ElementUnion extends Element
{
    List<Element> elements = new ArrayList<>() ;

    public ElementUnion()
    { }

    public ElementUnion(Element el)
    { 
        //Used by the SPARQL 1.1 style UNION
        this() ;
        addElement(el) ;
    }

    
    public void addElement(Element el) { elements.add(el) ; }
    public List<Element> getElements() { return elements ; }
   
    @Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashUnion ; 
        calcHashCode ^=  getElements().hashCode() ;
        return calcHashCode ;
    }

    @Override
   public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementUnion) )
            return false ;
        ElementUnion eu2 = (ElementUnion)el2 ;
        if ( this.getElements().size() != eu2.getElements().size() )
            return false ;
        for ( int i = 0 ; i < this.getElements().size() ; i++ )
        {
            Element e1 = getElements().get(i) ;
            Element e2 = eu2.getElements().get(i) ;
            if ( ! e1.equalTo(e2, isoMap) )
                return true ;
        }
        return true ;
    }
 
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
