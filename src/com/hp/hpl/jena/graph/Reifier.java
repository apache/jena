/*
  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Reifier.java,v 1.20 2003-08-27 13:01:00 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph;


import com.hp.hpl.jena.util.iterator.*;

/**
    This interface represents the type of things that can hold reified triples
    for a Jena Graph.
    
    @author kers
*/

public interface Reifier extends GetTriple
    {
    public static final Style Standard = new Style( true, false );
    public static final Style Convenient = new Style( true, true );
    public static final Style Minimal = new Style( false, true );
    
    /**
        Reification styles have two boolean components: whether the
        graph+reifier will intercept reification triples or not [if not, the only
        in-Jena reification is through the reifyAs operation], and whether or
        not reification triples will be visible in the graph.
    */
    public static class Style
        {
        private boolean intercept;
        private boolean conceal;
        
        Style( boolean intercept, boolean conceal )
            {
            this.intercept = intercept;
            this.conceal = conceal;
            }
            
        public boolean intercepts()
            { return intercept; }
            
        public boolean conceals()
            { return conceal; }
            
        public String toString()
            { return "<style int=" + intercept + ", con=" + conceal + ">"; }
        }
        
    /**
         return a read-only Graph of the triples used for reification.
    */    
    Graph getHiddenTriples();
    
    /**
        Answer this reifier's style.
    */
    Style getStyle();
    
    /**
        get the Graph which uses this reifier.
    */
    Graph getParentGraph();
    
    /**
        note the triple _t_ as reified using _n_ as its representing node.
        If _n_ is already reifying something, a AlreadyReifiedException is thrown.
    */
    Node reifyAs( Node n, Triple t );
    
    /**
        true iff _n_ is associated with some triple.
    */
    boolean hasTriple( Node n );
    
    /**
        @return true iff there's > 0 mappings to this triple
    */
    boolean hasTriple( Triple t );
    
    /**
        return an iterator over all the nodes that are reifiying something in 
        this reifier.
    */
    ExtendedIterator allNodes();
    
    /**
        return an iterator over all the nodes that are reifiying t in 
        this reifier.
    */    
    ExtendedIterator allNodes( Triple t );
    
    /**
        remove any existing binding for _n_; hasNode(n) will return false
        and getTriple(n) will return null. 
    */
    void remove( Node n, Triple t );
    
    /**
        remove all bindings which map to this triple.
    */
    void remove( Triple t );
    
    /**
        true iff the Reifier has handled an add of the triple _t_.
    */
    boolean handledAdd( Triple t );
    
    /**
        true iff the Reifier has handled a remove of the triple _t_.
    */
    boolean handledRemove( Triple t );;
    }

/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
