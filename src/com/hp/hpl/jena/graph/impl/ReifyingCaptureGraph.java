/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ReifyingCaptureGraph.java,v 1.5 2003-07-24 09:09:35 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;

/**
    A Graph that is layered over another graph and defers all its
    operations to it, except that reification triples are captured by
    its reifier.
<p>
    @author kers
*/
public class ReifyingCaptureGraph extends WrappedGraph
    {
    /**
        The capturing reifier that this graph uses instead of the base.
    */
    protected Reifier reifier = new SimpleReifier( this, true );
    
    /**
        Initialise the capture graph with the base graph.
        @param base the graph to which all real work is deferred
    */
    ReifyingCaptureGraph( Graph base )
        { super( base ); }
        
    /**
        Answer the reifier that is bound into this Graph
    */
    public Reifier getReifier() 
        { return reifier; }
       
    /**
        Add a triple to the graph, but allow the reifier to capture it first if it likes.
        @param t the triple to add to the base, unless its a reification triple
    */ 
    public void add( Triple t )
        { if (reifier.handledAdd( t ) == false) base.add( t ); }
        
    /**
        Remove a triple from the graph, but allow the reifier to capture it first if it likes.
        @param t the triple to remove from the base, unless its a reification triple
    */ 
    public void delete( Triple t )
        { if (reifier.handledRemove( t ) == false) base.delete( t ); }
        
    /**
        Answer a friendly printable string (if the base has one ...)
    */
    public String toString()
        { return "ReifyingCaptureGraph " + base.toString(); }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/