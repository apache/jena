/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.Collection ;
import java.util.List ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.modify.TemplateLib ;
import com.hp.hpl.jena.sparql.modify.request.QuadsAcc ;
import com.hp.hpl.jena.sparql.serializer.FormatterTemplate ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Triples template. */

public class Template implements TripleCollector
{
    static final int HashTemplateGroup     = 0xB1 ;
    QuadsAcc quads = new QuadsAcc() ;

    public void addTriple(Triple t) { quads.addTriple(t) ; }
    public int mark() { return quads.mark() ; }
    public void addTriple(int index, Triple t) { quads.addTriple(index, t) ; }
    public void addTriplePath(TriplePath path)
    { throw new ARQException("Triples-only collector") ; }

    public void addTriplePath(int index, TriplePath path)
    { throw new ARQException("Triples-only collector") ; }


    public List<Quad> getTemplates() { return quads.getQuads() ; }
    // -------------------------

    public void subst(Collection<Triple> acc, Map<Node, Node> bNodeMap, Binding b)
    {
        for ( Quad q : quads.getQuads() )
        {
            Triple t = TemplateLib.subst(q.asTriple(), b, bNodeMap) ;
            acc.add(t) ;
        }
    }

    private int calcHashCode = -1 ;  
    @Override
    public int hashCode()
    { 
        // BNode invariant hashCode. 
        int calcHashCode = Template.HashTemplateGroup ;
        for ( Quad q : quads.getQuads() )
            calcHashCode ^=  hash(q) ^ calcHashCode<<1 ; 
        return calcHashCode ;
    }
    
    private static int hash(Quad quad)
    {
        int hash = 0 ;
        hash = hashNode(quad.getGraph())   ^ hash<<1 ;
        hash = hashNode(quad.getSubject())   ^ hash<<1 ;
        hash = hashNode(quad.getPredicate()) ^ hash<<1 ;
        hash = hashNode(quad.getObject())    ^ hash<<1 ;
        return hash ;
    }

    private static int hashNode(Node node)
    {
        if ( node.isBlank() ) return 59 ;
        return node.hashCode() ;
    }
    
    public boolean equalIso(Object temp2, NodeIsomorphismMap labelMap)
    {
        if ( ! ( temp2 instanceof Template) ) return false ;
        Template tg2 = (Template)temp2 ;
        List<Quad> qList1 = this.quads.getQuads() ;
        List<Quad> qList2 = tg2.quads.getQuads() ;
        if ( qList1.size() != qList2.size() ) return false ;
        for ( int i = 0 ; i < qList1.size() ; i++ )
        {
            Quad q1 = qList1.get(i) ;
            Quad q2 = qList2.get(i) ;
            Utils.quadIso(q1, q2, labelMap) ;
        }
        return true ;
    }
    
    public void format(FormatterTemplate fmtTemplate)
    {
        fmtTemplate.format(this) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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