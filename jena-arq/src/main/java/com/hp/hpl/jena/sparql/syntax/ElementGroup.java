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

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** A number of graph query elements.
 *  Evaluation is a conjunction(AND) of the elements of the groups */

public class ElementGroup extends Element
{
    List<Element> elements = new ArrayList<>() ;

    public ElementGroup()
    {  }

    public void addElement(Element el)
    { 
        elements.add(el) ;
    }

    // In SPARQL, triple patterns are in basic graph patterns.
    // This is a helper method.
    
    public void addTriplePattern(Triple t)
    { 
        ensureBGP().addTriple(t) ;
    }

    public void addElementFilter(ElementFilter el)
    { 
        addElement(el) ;
    }
    
    // Ensure the current top element is a basic graph pattern.
    
    private ElementTriplesBlock ensureBGP()
    {
        if ( elements.size() == 0 )
            return pushBGP() ;

        Element top = top() ;
        ElementTriplesBlock bgp = null ;
        if ( top instanceof ElementTriplesBlock )
            return (ElementTriplesBlock)top ;
        return pushBGP() ;
    }
    
    private ElementTriplesBlock pushBGP()
    {
        ElementTriplesBlock bgp = new ElementTriplesBlock() ;
        elements.add(bgp) ;
        return bgp ;
    }
    
    private void setTop(Element el) { elements.set(elements.size()-1, el) ; }
    private Element top() { return elements.get(elements.size()-1) ; }
    
    public int mark() { return elements.size() ; }
    
    public List<Element> getElements() { return elements; }
    public boolean isEmpty() { return elements.size() == 0 ; }

    @Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashGroup ; // So the empty group isn't zero.
        calcHashCode ^=  getElements().hashCode() ; 
        return calcHashCode ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementGroup) )
            return false ;
        ElementGroup eg2 = (ElementGroup)el2 ;
        if ( this.getElements().size() != eg2.getElements().size() )
            return false ;
        for ( int i = 0 ; i < this.getElements().size() ; i++ )
        {
            Element e1 = getElements().get(i) ;
            Element e2 = eg2.getElements().get(i) ;
            if ( ! e1.equalTo(e2, isoMap) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
}
