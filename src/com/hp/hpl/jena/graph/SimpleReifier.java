/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SimpleReifier.java,v 1.1.1.1 2002-12-19 19:13:33 bwm Exp $
*/

package com.hp.hpl.jena.graph;

/**
	@author kers
<br>
    A base-level implementation of Reifier, intended to be straightforward
    and obvious].
*/

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

public class SimpleReifier implements Reifier
    {

    private Graph parent;
    private Graph triples;    
    private HashMap map;    
       
    /* construct a simple reifier that bound to the parent graph */
    public SimpleReifier( Graph parent )
        {
        this.parent = parent;
        this.triples = new GraphMem();
        this.map = new HashMap();
        }
        
    /** return the parent graph we are bound to */
    public Graph getParentGraph()
        { return parent; }
        
    /** return the triple bound to _n_ */
    public Triple getTriple( Node n )
        { return (Triple) map.get( n ); }
        
    /** true iff there is a triple bound to _n_ */
    public boolean hasTriple( Node n )
    	{ return map.get( n ) != null; }
        
    /** */
    public ClosableIterator allNodes()
        { return new ClosableIteratorImpl( map.keySet().iterator() ); }
        
    /** return the graph of reified triples. */
    public Graph getReifiedTriples()
        { return triples; }
        
    /** reify a triple _t_, allocating a new blank node to represent it */
    public Node reify( Triple t )
        { return reifyAs( Node.makeAnon(), t ); }        
        
    /** 
        reifiy a triple _t_ with tag _tag_. If a different triple is already
        reified under _tag_, throw an AlreadyReifiedException.
    */
    public Node reifyAs( Node tag, Triple t )
    	{
        Triple already = (Triple) map.get( tag );
        if (already != null && !already.equals( t ))
            throw new Reifier.AlreadyReifiedException( tag );
        triples.add( t );
        map.put( tag, t );
        return tag; 
    	}

    /** unbind _n_ */    	
	public void remove( Node n )
		{
		Triple T = (Triple) map.remove( n );
		if (T != null) triples.delete( T );
		}
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
