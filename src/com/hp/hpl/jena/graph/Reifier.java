/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Reifier.java,v 1.1.1.1 2002-12-19 19:13:32 bwm Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
<br>
    This interface represents the type of things that can hold reified triples
    for a Jena Graph.
<br>    
    PRELIMINARY - waiting for the fire.
*/

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

public interface Reifier
    {
    /**
        return an immutable Graph of the reified triples, allowing them to be
        queried using the usual Graph operations.
    */ 
    Graph getReifiedTriples();
    
    /**
        get the Graph which uses this reifier.
    */
    Graph getParentGraph();
    
    /**
        note the triple _t_ as reified, and return a Node which represents
        the reified triple. The Node will be a fresh blank node.
    */
    Node reify( Triple t );
    
    /**
        node the triple _t_ as reified using _n_ as its representing node.
        If _n_ is already reifying something, a AlreadyReifiedException is thrown.
    */
    Node reifyAs( Node n, Triple t );
    
    /**
        true iff _n_ is associated with some triple.
    */
    boolean hasTriple( Node n );
    
    /**
        get the triple associated with the node _n_, null if there isn't one.
    */
    Triple getTriple( Node n );
       
    /**
        return an iterator over all the nodes that are reifiying something in 
        this reifier.
    */
    ClosableIterator allNodes();
    
    /**
        remove any existing binding for _n_; hasNode(n) will return false
        and getTriple(n) will return null. 
    */
    void remove( Node n );
    
    /**
        the exception raised by _reifyAs_ if a node is already bound to
        a reified triple.
    */
    static class AlreadyReifiedException extends RDFException 
        { AlreadyReifiedException( Node n ) { super( n.toString() ); } };
    }
    
/*
    (c) Copyright Hewlett-Packard Company 2002
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
