/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Bind.java,v 1.7 2005-02-21 11:52:13 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    A binding instance of a variable. It accepts any node and records it in the supplied
    Domain at the index allocated to it when it is created.
    
	@author hedgehog
*/
public class Bind extends Element 
	{	
    /**
        Initialise a Bind element: remember the index <code>n</code> which is the
        place in Domain's where it may store its value.
    */
	public Bind( int n ) { super( n ); }
	
    /**
        Answer true after updating the domain to record the value this element binds.
        @param d the domain in which to note this element is bound to <code>x</code>.
        @return true [after side-effecting d]
    */		
    public boolean match( Domain d, Node x )
        {
        d.setElement( index, x );
        return true;    
        }
        
    /**
        Answer Node.ANY, as a binding occurance of a variable can match anything.
    */
    public Node asNodeMatch( Domain d )
        { return Node.ANY; } 
        
	public String toString()
		{ return "<Bind " + index + ">"; }
	}

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
