/*
  (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Free.java,v 1.3 2003-08-27 12:56:40 andy_seaborne Exp $
*/

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.shared.JenaException;

/**
    A binding instance of a variable. It accepts any node and records it in the supplied
    Domain at the index allocated to it when it is created.
    
	@author hedgehog
*/
public class Free extends Element 
	{
		
    /**
        Track a (free) variable element: it will later be bound during compilation.
    */
    
	private Node_Variable var;
	private int listIx;            // index of variable in varList.
	private int mapIx;            // index of (arg) variable in Mapping	
	private boolean isListed;  // true when variable is in varList
	private boolean isArgument;  // true when variable can be bound by an argument
	
	public Free( Node n ) {
		super( );
		var = (Node_Variable) n;
		isArgument = false;
		isListed = false;
	}
	
	public int getListing()
	{
		if ( isListed ) return listIx;
		else throw new JenaException("UnListed variable");
	}

	public void setListing ( int ix )
		{
		if ( isListed )
			throw new JenaException("Relisting of variable");
		isListed = true;
		listIx = ix;
		}
		
	public void setIsArg ( int ix ) { isArgument = true; mapIx = ix; }
	public boolean isArg() { return isArgument; }
	
	public int getMapping()
	{
		if ( isArgument ) return mapIx;
		else throw new JenaException("Unmapped variable");
	}

	public boolean isListed() { return isListed; }
	
	public Node_Variable var() {
		return var;
	}

	public boolean match( Domain d, Node x )
		{throw new JenaException("Attempt to match a free variable");		
		}
	
	public Node asNodeMatch( Domain d ) {
		throw new JenaException("asNodeMatch not supported");
	}
	
	public String toString()
		{ return "<Free " + listIx + ">"; }
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
