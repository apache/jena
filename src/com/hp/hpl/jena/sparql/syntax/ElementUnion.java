/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.* ;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** 
 * 
 * @author Andy Seaborne
 */

public class ElementUnion extends Element
{
    List<Element> elements = new ArrayList<Element>() ;

    public ElementUnion()
    { }

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

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */