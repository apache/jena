/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.syntax;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.core.BasicPattern;
import com.hp.hpl.jena.query.core.LabelMap;
import com.hp.hpl.jena.query.core.Var;

/** A SPARQL BasicGraphPattern
 * 
 * @author Andy Seaborne
 * @version $Id: ElementTriplesBlock.java,v 1.2 2007/01/31 17:41:22 andy_seaborne Exp $
 */

public class ElementTriplesBlock extends Element implements TripleCollector
{
    private BasicPattern pattern = new BasicPattern() ; 

    public ElementTriplesBlock()
    {  }

    public boolean isEmpty() { return pattern.size() == 0 ; }
    
    public void addTriple(Triple t)
    { pattern.add(t) ; }
    
    public int mark() { return pattern.size() ; }
    
    public void addTriple(int index, Triple t)
    { pattern.add(index, t) ; }
    
    public BasicPattern getTriples() { return pattern ; }
    public Iterator triples() { return pattern.iterator(); }
    
    //@Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashBasicGraphPattern ;
        calcHashCode ^=  pattern.hashCode() ; 
        return calcHashCode ;
    }

    //@Override
    public boolean equalTo(Element el2, LabelMap labelMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementTriplesBlock) )
            return false ;
        
        ElementTriplesBlock eg2 = (ElementTriplesBlock)el2 ;
        if ( this.pattern.size() != eg2.pattern.size() )
            return false ;
        for ( int i = 0 ; i < this.pattern.size() ; i++ )
        {
            Triple t1 = pattern.get(i) ;
            Triple t2 = eg2.pattern.get(i) ;
            
            // Need to be labelmap same.
            if ( ! equalTo(t1, t2, labelMap) )
                return false ;
        }
        return true ;
    }

    public void visit(ElementVisitor v) { v.visit(this) ; }
    
    // Code to test triples for sameness with remapping. 
    
    private static boolean equalTo(Triple t1, Triple t2, LabelMap labelMap)
    {
        Node s1 = t1.getSubject() ;
        Node p1 = t1.getPredicate() ;
        Node o1 = t1.getObject() ;
        
        Node s2 = t2.getSubject() ;
        Node p2 = t2.getPredicate() ;
        Node o2 = t2.getObject() ;
        
        if ( ! nodeEquals(s1, s2, labelMap) )
            return false ;
        if ( ! nodeEquals(p1, p2, labelMap) )
            return false ;
        if ( ! nodeEquals(o1, o2, labelMap) )
            return false ;

        return true ;
    }
    
    private static boolean nodeEquals(Node n1, Node n2, LabelMap labelMap)
    {
        if ( Var.isBlankNodeVar(n1) && Var.isBlankNodeVar(n2) )
        {
            String label1 = n1.getName();
            String label2 = n2.getName();
            String maybe = labelMap.get(label1) ;
            if ( maybe == null )
            {
                labelMap.put(label1, label2) ;
                return true ;
            }
            return maybe.equals(label2) ;
        }
        return n1.equals(n2) ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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