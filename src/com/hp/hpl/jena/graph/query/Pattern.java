/*
  (c) Copyright 2002, 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Pattern.java,v 1.7 2003-08-08 14:29:13 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    A Pattern represents a matching triple; it is composed of S, P, and O Elements.
    
	@author hedgehog
*/

public class Pattern 
	{
	private Element S;
	private Element P;
	private Element O;
	
	public Pattern( Element S, Element P, Element O )
		{
		this.S = S; 
		this.P = P; 
		this.O = O;
		}
	
    /**
        Convert a Pattern into a TripleMatch by making a Triple who's Nodes are the
        conversions of the constituent elements.
    */	
    public TripleMatch asTripleMatch( Domain d )
        { 
        return Triple.createMatch
            ( S.asNodeMatch( d ), P.asNodeMatch( d ), O.asNodeMatch( d ) ); 
        }
    
    /**
        Answer true iff this pattern, given the values for variables as found in a given 
        Domain, matches the given triple; update the Domain with any variable bindings.
        
        @param d the Domain with the current bound variable values (and slots for the rest)
        @param t the concrete triple to match
        @return true iff this pattern matches the triple [and side-effects the domain]
    */
    public boolean match( Domain d, Triple t )
        {
        return S.match( d, t.getSubject() ) 
            && P.match( d, t.getPredicate() ) 
            && O.match( d, t.getObject() );
        }

     public String toString()
        { return "<pattern " + S + " @" + P + " " + O + ">"; }
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
