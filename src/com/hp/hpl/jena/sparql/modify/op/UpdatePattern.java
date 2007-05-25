/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.op;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;

import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateException;

/**
 * @author Andy Seaborne
 * @version $Id$
 */ 

public abstract class UpdatePattern extends GraphUpdateN
{
    private Element pattern = null ;
    public Element getElement() { return pattern ; }
    public void setPattern(Element pattern) { this.pattern = pattern ; }
    
    /** Parse the string into an Element.  Must include the surrounding {} in the string */  
    public void setPattern(String pattern) { this.pattern = QueryFactory.createElement(pattern) ; }
    
    protected abstract void exec(Graph graph, List bindings) ;
    
    private List bindings = new ArrayList() ;
    
    //@Override
    protected void startExec(GraphStore graphStore)
    {
        if ( pattern != null )
        {
            Plan plan = QueryExecutionFactory.createPlan(pattern, graphStore) ;
            QueryIterator qIter = plan.iterator() ;
            
            for( ; qIter.hasNext() ; )
            {
                Binding b = qIter.nextBinding() ;
                bindings.add(b) ;
            }
            qIter.close() ;
        }
        else
            bindings.add(BindingRoot.create()) ;
    }
    
    //@Override
    protected void finishExec()
    { bindings = null ; }
    
    //@Override
    protected void exec(Graph graph)
    {
        exec(graph, bindings) ;
    }

    protected static Collection subst(Template template, QueryIterator qIter)
    {
        Set acc = new HashSet() ;
        for ( ; qIter.hasNext() ; )
        {
            Map bNodeMap = new HashMap() ;
            Binding b = qIter.nextBinding() ;
            template.subst(acc, bNodeMap, b) ;
        }

        for ( Iterator iter = acc.iterator() ; iter.hasNext() ; )
        {
            Triple triple = (Triple)iter.next() ;
            if ( ! isGroundTriple(triple))
                throw new UpdateException("Unbound triple: "+FmtUtils.stringForTriple(triple)) ;
        }
        
        return acc ;
    }
    
    private static boolean isGroundTriple(Triple triple)
    {
        return 
            isGroundNode(triple.getSubject()) &&
            isGroundNode(triple.getPredicate()) &&
            isGroundNode(triple.getObject()) ;
    }

    private static boolean isGroundNode(Node node)
    {
        return node.isConcrete() ; 
    }
}


/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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