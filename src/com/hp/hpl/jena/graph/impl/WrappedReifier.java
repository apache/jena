/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: WrappedReifier.java,v 1.7 2005-02-21 11:52:12 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
	WrappedReifier: a class that wraps a reifier [needed by WrappedGraph].

	@author kers
*/
public class WrappedReifier implements Reifier 
    {
    private Reifier base;
    private Graph parent;
	/**
	 * 
	*/
	public WrappedReifier( Reifier base, Graph parent )  
        { this.base = base; this.parent = parent; }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#getStyle()
	*/
	public ReificationStyle getStyle() { return base.getStyle(); }
	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#getParentGraph()
	*/
	public Graph getParentGraph() { return parent; }
    
    public ExtendedIterator find( TripleMatch m ) { return base.find( m ); }
    
    public ExtendedIterator findExposed( TripleMatch m ) { return base.findExposed( m ); }
    
    public ExtendedIterator findEither( TripleMatch m, boolean showHidden ) 
        { return base.findEither( m, showHidden ); }
    
    public int size() { return base.size(); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#reifyAs(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	*/
	public Node reifyAs( Node n, Triple t ) { return base.reifyAs( n, t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Node)
	*/
	public boolean hasTriple( Node n ) { return base.hasTriple( n ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#hasTriple(com.hp.hpl.jena.graph.Triple)
	*/
	public boolean hasTriple( Triple t ) { return base.hasTriple( t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#allNodes()
	*/
	public ExtendedIterator allNodes() { return base.allNodes(); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#allNodes(com.hp.hpl.jena.graph.Triple)
	*/
	public ExtendedIterator allNodes( Triple t ) { return base.allNodes( t ); }
	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Triple)
	*/
	public void remove( Node n, Triple t ) { base.remove( n, t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#remove(com.hp.hpl.jena.graph.Triple)
	*/
	public void remove( Triple t ) { base.remove( t ); }
	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#handledAdd(com.hp.hpl.jena.graph.Triple)
	*/
	public boolean handledAdd( Triple t ) { return base.handledAdd( t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.Reifier#handledRemove(com.hp.hpl.jena.graph.Triple)
	*/
	public boolean handledRemove( Triple t ) { return base.handledRemove( t ); }

	/** 
	 	@see com.hp.hpl.jena.graph.GetTriple#getTriple(com.hp.hpl.jena.graph.Node)
	*/
	public Triple getTriple( Node n ) { return base.getTriple( n ); }

    /**
     	@see com.hp.hpl.jena.graph.Reifier#close()
    */
    public void close() { base.close(); }

    }

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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