/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Mapping.java,v 1.4 2003-07-15 13:26:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
    this class is used to record the mapping from [variable] Node's to
    the indexes they are bound to in a Query.
*/

public class Mapping 
	{
	private HashMap map;
	
	private int index = 0;
	
    /**
        create a new, empty mapping.
    */
	public Mapping()
		{ this.map = new HashMap(); }
		
    /**
        get the index of a node in the mapping; undefined if the
        node is not mapped.
        
        @param v the node to look up
        @return the index of v in the mapping
    */
	public int indexOf( Node v )
		{ return ((Integer) map.get( v )).intValue(); }
		
    /**
        allocate an index to the node _v_. _v_ must not already
        be mapped.
        
        @param v the node to be given an index
        @return the value of the allocated index
    */
	public int newIndex( Node v )
		{
		int result = index++;
		map.put( v, new Integer( result ) );
		return result;
		}
		
    /**
        Answer the number of names currently held in the map
        @return the number of names in the map
    */
    public int size()
        { return map.size(); }
        
    /**
        @param v the node to look up
        @return true iff this mapping maps _v_ to an index
    */
	public boolean maps( Node v )
		{ return map.containsKey( v ); }
        
    /**
        @return a string representing this mapping
    */
    public String toString()
        { return map.toString(); }
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
