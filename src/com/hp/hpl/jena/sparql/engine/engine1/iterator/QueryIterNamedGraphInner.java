/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.util.Utils;


public class QueryIterNamedGraphInner extends QueryIterPlan
{
    Binding binding ;
    Node graphNode ;
    Iterator graphURIs ;
    PlanElement cElt ;
    QueryIterator subIter ;
    boolean started = false ;
    
    public QueryIterNamedGraphInner(Binding binding, Node sourceNode, Iterator _graphURIs,
                                    PlanElement cElt, ExecutionContext context)
    {
        super(context) ;
        this.graphURIs = _graphURIs ;
        this.cElt = cElt ;
        this.binding = binding ;
        subIter = null ;
        graphNode = sourceNode ;
        started = false ;
    }

    protected boolean hasNextBinding()
    {
        for(;;)
        {
            if ( subIter == null )
                subIter = nextIterator() ;
            
            if ( subIter == null )
                return false ;
            
            if ( subIter.hasNext() )
                return true ;
            
            subIter.close() ;
            subIter = nextIterator() ;
            if ( subIter == null )
                return false ;
        }
    }

    protected Binding moveToNextBinding()
    {
        if ( subIter == null )
            throw new NoSuchElementException(Utils.className(this)+".nextB") ;
            
        return subIter.nextBinding() ;
    }

    protected void closeIterator()
    {
        if ( subIter != null )
            subIter.close() ;
        subIter = null ;
    }
    
    // This is very like QueryIteratorRepeatApply except its not repeating over bindings
    // There is a tradeoff of generalising QueryIteratorRepeatApply to Objects
    // and hence no type safety. Or duplicating code (generics?)
    
    private QueryIterator nextIterator()
    {
        if ( ! graphURIs.hasNext() )
            return null ;
        
        String uri = (String)graphURIs.next() ;
        
        Graph g = getExecContext().getDataset().getNamedGraph(uri) ;
        if ( g == null )
            return null ;
        
        // Create a new context with a different active graph
        ExecutionContext execCxt2 = new ExecutionContext(getExecContext(), g) ;
        
        // Binding the variable (if "GRAPH ?var")
        Binding b = binding ;
        if ( graphNode != null && graphNode.isVariable() )
        {
            if ( ! Var.isVar(graphNode) )
                throw new ARQInternalErrorException("Node_Variable but not a Var: "+graphNode) ;
            b = new Binding1(binding, Var.alloc(graphNode), Node.createURI(uri)) ;
        }
        QueryIterator qIterOneBinding = new QueryIterSingleton(b, execCxt2) ;
        QueryIterator qIter = cElt.build(qIterOneBinding, execCxt2) ;
        return qIter ;  
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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