/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** A number of templates, grouped together.
 *  Kept as a list so printing maintains order.
 * @author Andy Seaborne
 */

public class TemplateGroup extends Template implements TripleCollector
{
    List<Template> templates = new ArrayList<Template>() ;
    
    public TemplateGroup()      { }
    
    public void add(Template templ)
    {
        templates.add(templ) ;
    }
    
    public void addTriple(Triple t) { templates.add(new TemplateTriple(t)) ; }
    public int mark() { return templates.size() ; }
    public void addTriple(int index, Triple t)
    { templates.add(index, new TemplateTriple(t)) ; }
    
    public void addTriplePath(TriplePath path)
    { throw new ARQException("Triples-only collector") ; }

    public void addTriplePath(int index, TriplePath path)
    { throw new ARQException("Triples-only collector") ; }
    

    public List<Template> getTemplates() { return templates ; }
    public Iterator<Template> templates() { return templates.iterator() ; }
    
    @Override
    public void subst(Collection<Triple> acc, Map<Node, Node> bNodeMap, Binding b)
    {
        for ( Iterator<Template> iter = templates.iterator() ; iter.hasNext() ; )
        {
            Template t = iter.next() ;
            t.subst(acc, bNodeMap, b) ;
        }
    }

    private int calcHashCode = -1 ;  
    @Override
    public int hashCode()
    { 
        int calcHashCode = Template.HashTemplateGroup ;
        calcHashCode ^=  getTemplates().hashCode() ; 
        return calcHashCode ;
    }

    @Override
    public boolean equalIso(Object temp2, NodeIsomorphismMap labelMap)
    {
        if ( temp2 == null ) return false ;

        if ( ! ( temp2 instanceof TemplateGroup) )
            return false ;
        TemplateGroup tg2 = (TemplateGroup)temp2 ;
        if ( this.getTemplates().size() != tg2.getTemplates().size() )
            return false ;
        for ( int i = 0 ; i < this.getTemplates().size() ; i++ )
        {
            Template t1 = getTemplates().get(i) ;
            Template t2 = tg2.getTemplates().get(i) ;
            if ( ! t1.equalIso(t2, labelMap) )
                return false ;
        }
        return true ;
    }
    
    @Override
    public void visit(TemplateVisitor visitor)
    {
        visitor.visit(this) ;
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