/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Element.java,v 1.5 2003-08-27 13:00:59 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    An Element of a matching triple. Elements have associated indexes, their place
    in the Domain storing the matching values. Subclasses represent constants,
    binding occurances of variables, and bound instances of variables.
    
	@author hedgehog
*/

public abstract class Element 
	{
	protected int index;
	
    /**
        Answer this Element's index in the Domains it is compiled for.
    */
	public int getIndex()
		{ return index; }
		
    /**
        Initialise this Element with its allocated index.
    */
	protected Element( int index )
		{ this.index = index; }
		
    /**
        Initialiser invoked by sub-classes which need no index.
    */
	protected Element() 
		{ this( -1 ); }
		
    /**
        The constant ANY matches anything and binds nothing
    */
	public static final Element ANY = new Element()
        {
        public boolean match( Domain d, Node n ) { return true; }
        public Node asNodeMatch( Domain d ) { return Node.ANY; }
        public String toString() { return "<any>"; }
        };
        
    /**
        Answer true if this Element matches x given the bindings in d. May side-effect d
        by (re)binding if this element is a variable.
        @param d the variable bindings to read/update for variables
        @param x the value to match
        @return true if the match succeeded
    */
    public abstract boolean match( Domain d, Node x );
        
    /**
        Answer a Node suitable as a pattern-match element in a TripleMatch approximating
        this Element. Thus Bind elements map to null (or Node.ANY).
        @param d the domain holding the variable bindings
        @return the matched value (null if none, ie binding occurance or ANY)
    */
    public abstract Node asNodeMatch( Domain d );
        
    public String toString()
    	{ return "<" + this.getClass() + " element>"; }
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
