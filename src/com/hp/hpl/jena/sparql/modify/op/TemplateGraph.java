/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.op;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TemplateGraph extends TemplateGroup
{
    // Not subtle.  May remove or rethink if this proves not useful and a performance bottleneck. 
    private Graph graph ;

    public TemplateGraph(Graph graph)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        while(iter.hasNext())
        {
            Triple triple = iter.next();
            super.addTriple(triple) ;
        }
        iter.close() ;
    }

    @Override
    public boolean equalIso(Object other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof TemplateGraph ) ) return false ;
        return super.equalIso(other, labelMap) ;
    }

    @Override
    public int hashCode()
    {
        return graph.hashCode() ;
    }

    @Override
    public void subst(Collection<Triple> acc, Map<Node, Node> bNodeMap, Binding b)
    {
        for ( Iterator<Template> iter = templates() ; iter.hasNext() ; )
        {
            TemplateTriple tt = (TemplateTriple)iter.next() ;
            acc.add(tt.getTriple()) ;
        }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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