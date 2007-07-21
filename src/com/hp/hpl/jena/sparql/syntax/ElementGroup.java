/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.* ;

import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.util.LabelMap;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/** A number of graph query elements.
 *  Evaluation is a conjunction(AND) of the elements of the groups  
 * 
 * @author Andy Seaborne
 * @version $Id: ElementGroup.java,v 1.36 2007/01/31 13:01:25 andy_seaborne Exp $
 */

public class ElementGroup extends Element
{
    private static Log log = LogFactory.getLog(ElementGroup.class) ;
    List elements = new ArrayList() ;

    public ElementGroup()
    {  }

    public void addElement(Element el)
    { 
        elements.add(el) ;
    }

    // In SPARQL, triple patterns are in basic graph patterns.      
    // This is only used by RDQL.
    
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
    private Element top() { return (Element)elements.get(elements.size()-1) ; }
    
    public int mark() { return elements.size() ; }
    
    public List getElements() { return elements; }
    public boolean isEmpty() { return elements.size() == 0 ; }

    //@Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashGroup ; // So the empty group isn't zero.
        calcHashCode ^=  getElements().hashCode() ; 
        return calcHashCode ;
    }

    //@Override
    public boolean equalTo(Element el2, LabelMap labelMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementGroup) )
            return false ;
        ElementGroup eg2 = (ElementGroup)el2 ;
        if ( this.getElements().size() != eg2.getElements().size() )
            return false ;
        for ( int i = 0 ; i < this.getElements().size() ; i++ )
        {
            Element e1 = (Element)getElements().get(i) ;
            Element e2 = (Element)eg2.getElements().get(i) ;
            if ( ! e1.equalTo(e2, labelMap) )
                return false ;
        }
        return true ;
    }
    
    public void visit(ElementVisitor v) { v.visit(this) ; }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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