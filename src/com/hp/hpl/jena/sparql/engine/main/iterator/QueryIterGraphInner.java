/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpSubstitute;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Utils;


public class QueryIterGraphInner extends QueryIter
{
    private Binding parentBinding ;
    
    //Either-or
    private Node graphNode = null ;
    // ----    
    private Var graphVar = null ;
    private Iterator graphURIs ;
    // ----
    
    private OpGraph opGraph ;
    private boolean started = false ;
    private QueryIterator subIter = null ;

    public QueryIterGraphInner(Binding parent, Var graphVar, Iterator graphURIs, OpGraph opGraph, ExecutionContext execCxt)
    {
        super(execCxt) ;
        this.parentBinding = parent ;
        this.graphVar = graphVar ;
        this.graphURIs = graphURIs ;
        this.opGraph = opGraph ;
    }

    public QueryIterGraphInner(Binding parent, Node graphNode, OpGraph opGraph, ExecutionContext execCxt)
    {
        super(execCxt) ;
        this.parentBinding = parent ;
        this.graphNode = graphNode ;
        this.opGraph = opGraph ;
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
            throw new NoSuchElementException(Utils.className(this)+".moveToNextBinding") ;
            
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
        String uri = null ;
        // Iterator
        if ( graphURIs != null )
        {
            // GRAPH ?variable 
            if ( ! graphURIs.hasNext() )
                return null ;
            uri = (String)graphURIs.next() ;
        }
        else
        {
            // Constant of bound variable.
            if ( ! graphNode.isURI() )
                return null ;
            uri = graphNode.getURI() ;
        }
            

        Graph g = getExecContext().getDataset().getNamedGraph(uri) ;
        if ( g == null )
            return null ;
            
        Node gn = Node.createURI(uri) ;
        // Create a new context with a different active graph
        ExecutionContext execCxt2 = new ExecutionContext(getExecContext(), g) ;
            
            // Binding the variable (if "GRAPH ?var") otherwise it's a grapNode.
        Binding b = parentBinding ;
        if ( graphVar != null )
            b = new Binding1(b, graphVar, gn) ;
        
        QueryIterator qIter = buildIterator(b, gn, opGraph, execCxt2) ; 
        return qIter ;
    }
    

    private static QueryIterator buildIterator(Binding binding, Node graphURI, OpGraph opGraph, ExecutionContext cxt)
    {
        if ( !graphURI.isURI() )
            // e.g. variable bound to a literal or blank node.
            throw new ARQInternalErrorException("QueryIterGraphInner.buildIterator") ;
            //return null ;
        Op op = OpSubstitute.substitute(opGraph.getSubOp(), binding) ;
        Graph g = cxt.getDataset().getNamedGraph(graphURI.getURI()) ;
        if ( g == null )
            return null ;
        
        ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
        QueryIterator subInput = new QueryIterSingleton(binding, cxt) ;
        return QC.compile(op, subInput, cxt2) ;
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