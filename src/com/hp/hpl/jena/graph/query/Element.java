/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Element.java,v 1.3 2003-08-04 14:03:13 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    An Element of a matching triple. Elements have associated indexes, their place
    in the Domain storing the matching values. Subclasses represent constants,
    binding occurances of variables, and bound instances of variables.
    
	@author hedgehog
*/

public class Element 
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
		{ this( 0 ); }
		
    /**
        The constant ANY matches anything and binds nothing
    */
	public static final Element ANY = new Element()
        {
        public boolean accepts( Domain d, Node n ) { return true; }
        public String toString() { return "<any>"; }
        };
	
	public boolean accepts( Domain d, Node n ) 
		{ throw new UnsupportedOperationException( this.getClass() + ".accepts" ); }
	
	public void matched( Domain d, Object x ) 
		{}
        
    /**
        Answer the Node value that this Element represents in the Domain d; over-ridden
        in sub-classes.
    */
    public Node asNode( Domain d )
        { return null; }
        
    public String toString()
    	{ return "<" + this.getClass() + " element>"; }
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
