/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Fixed.java,v 1.4 2003-08-04 14:03:13 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;
import com.hp.hpl.jena.graph.*;

/**
    An Element that matches a single specified value.
    
	@author hedgehog
*/
public class Fixed extends Element 
	{
	private Node value;
	
    /**
        Initialise this element with its single matching value: remember that value.
    */
	public Fixed( Node x ) 
        { this.value = x; }
        
	public Node getValue() 
        { return value; }
	
	public String toString() 
        { return "<fixed " + value + ">"; }
			
    /**
        Accept a node <code>x</code> iff it is value-equal to the fixed value of this element.
        Equality is determined by Node::sameValueAs.
    */
	public boolean accepts( Domain d, Node x ) 
        { return x.sameValueAs(value); }
    
    /**
        This element represents the fixed value it is initialised with.
    */
    public Node asNode( Domain d ) 
        { return value; }
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
