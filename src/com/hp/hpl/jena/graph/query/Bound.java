/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Bound.java,v 1.5 2003-08-08 13:02:46 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;
import com.hp.hpl.jena.graph.*;

/**
    An element which represents an already-bound variable.
    
	@author hedgehog
*/

public class Bound extends Element
	{
    /**
        Initialise a Bound element: remember <code>n</code> as it is the index into the
        Domain at which its value is stored.
    */
	public Bound( int n ) { super( n ); }
	
    /**
        Answer true iff the node <code>x</code> matches the previously-seen value at
        Donain[index]. The matching uses datatype-value semantics, implemented by
        <code>Node::sameValueAs()</code>.
    */  
    public boolean match( Domain d, Node x )
        { return x.sameValueAs(d.get( index ) ); }
     
    public Node asNodeMatch( Domain d ) 
        { return (Node) d.get( index ); }
        
    public String toString()
    	{ return "<Bound " + index + ">"; }
	}

/*
    (c) Copyright Hewlett-Packard Company 2002, 2003
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
