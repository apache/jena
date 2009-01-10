/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.Collection;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;


/** A single triple template.
 *  bNodes are "magic" in that they are duplicated on substitution.  
 * 
 * @author Andy Seaborne
 */

public class TemplateTriple extends Template
{
    Triple triple ;
    
    public TemplateTriple(Node s, Node p, Node o)
    {
        triple = new Triple(s, p ,o) ;
    }
    
    public TemplateTriple(Triple t)
    {
        triple = t ;
    }
    
    public Triple getTriple() { return triple ; } 
    
    @Override
    public void subst(Collection<Triple> acc, Map<Node, Node> bNodeMap, Binding b)
    {
        Node s = triple.getSubject() ; 
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        if ( s.isBlank() )
            s = newBlank(s, bNodeMap) ;

        if ( p.isBlank() )
            p = newBlank(p, bNodeMap) ;

        if ( o.isBlank() )
            o = newBlank(o, bNodeMap) ;

        Triple t = new Triple(s, p, o) ; 
        Triple t2 = BindingUtils.substituteIntoTriple(t, b) ;
        acc.add(t2) ;
    }
    
    @Override
    public int hashCode()
    { 
        if ( triple == null ) return 2 ;
        // Without any blank nodes
        int hash = 0 ;
        hash = hashNode(triple.getSubject())   ^ hash<<1 ;
        hash = hashNode(triple.getPredicate()) ^ hash<<1 ;
        hash = hashNode(triple.getObject())    ^ hash<<1 ;
        return hash ;
    }


    private int hashNode(Node node)
    {
        if ( node.isBlank() ) return 57 ;
        return node.hashCode() ;
    }

    @Override
    public boolean equalIso(Object temp2, NodeIsomorphismMap labelMap)
    {
        if ( temp2 == null ) return false ;

        if ( ! ( temp2 instanceof TemplateTriple) )
            return false ;
        TemplateTriple tt2 = (TemplateTriple)temp2 ;
        
        if ( labelMap == null )
            return this.getTriple().equals(tt2.getTriple()) ;
        
        return Utils.tripleIso(triple, getTriple(), labelMap) ;
        
//        Node s1 = this.getTriple().getSubject() ;
//        Node p1 = this.getTriple().getPredicate() ;
//        Node o1 = this.getTriple().getObject() ;
//        
//        Node s2 = tt2.getTriple().getSubject() ;
//        Node p2 = tt2.getTriple().getPredicate() ;
//        Node o2 = tt2.getTriple().getObject() ;
//        
//        if ( ! nodeEquals(s1, s2, labelMap) )
//            return false ;
//        if ( ! nodeEquals(p1, p2, labelMap) )
//            return false ;
//        if ( ! nodeEquals(o1, o2, labelMap) )
//            return false ;
//        
//        return true ;
    }
    
    
    
    @Override
    public void visit(TemplateVisitor visitor)
    {
        visitor.visit(this) ;
    }
    
    // Map blank nodes.
    private static boolean nodeEquals(Node n1, Node n2, NodeIsomorphismMap isoMap)
    {
        if ( n1.isBlank() && n2.isBlank() )
            return isoMap.makeIsomorhpic(n1, n2) ; 
        return n1.equals(n2) ;
    }
    
    private Node newBlank(Node n, Map<Node, Node> bNodeMap)
    {
        if ( ! bNodeMap.containsKey(n) ) 
            bNodeMap.put(n, Node.createAnon() );
        return bNodeMap.get(n) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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