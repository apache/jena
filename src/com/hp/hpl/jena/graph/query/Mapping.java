/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Mapping.java,v 1.14 2005-02-21 11:52:15 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;

import java.util.*;

/**
    this class is used to record the mapping from [variable] Node's to
    the indexes they are bound to in a Query. Nodes bound to negative values
    are predeclared; the negative value is converted on index allocation.
*/

public class Mapping implements VariableIndexes
	{
	private Map map;
	
	private int index = 0;
    private int preIndex = 0;
	
    /**
        Create a new mapping in which all variables are unbound and the variables
        of <code>preDeclare</code> will be allocated the first slots in the map in their
        natural order. [This is so that the query domain elements that come out of the
        matching process will be positioned to be suitable as query answers.]
    */    
    public Mapping( Node [] preDeclare )
        {
        this.map = CollectionFactory.createHashedMap();
        index = preDeclare.length;    
        for (int i = 0; i < preDeclare.length; i += 1) preDeclare( preDeclare[i] );
        }
        
    private void preDeclare( Node v )
        { map.put( v, new Integer( --preIndex ) ); }
		
    /**
        get the index of a node in the mapping; undefined if the
        node is not mapped.
        
        @param v the node to look up
        @return the index of v in the mapping
    */
	public int indexOf( Node v )
		{ 
        int res = lookUp(v);
        if (res < 0) throw new Query.UnboundVariableException( v );
        return res;
        }
        
    public int indexOf( String name )
        { return indexOf( Node.createVariable( name ) ); }

	/**
		get the index of a node in the mapping; return -1
		if the node is not mapped.       
		@param v the node to look up
		@return the index of v in the mapping
	*/
	public int lookUp( Node v )
		{ 
		Integer i = (Integer) map.get( v );
		if (i == null || i.intValue() < 0) return -1;
		return i.intValue();
		}

    /**
        allocate an index to the node _v_. _v_ must not already
        be mapped.
        
        @param v the node to be given an index
        @return the value of the allocated index
    */
	public int newIndex( Node v )
		{
        Integer already = (Integer) map.get( v );
        int result = already == null ? index++ : -already.intValue() - 1;
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
        Answer true iff we have already bound v (predeclaration doesn't count)
        @param v the node to look up
        @return true iff this mapping has seen a binding occurance of v
    */
	public boolean hasBound( Node v )
		{ return map.containsKey( v )  && ((Integer) map.get( v )).intValue() > -1; }
        
    /**
        @return a string representing this mapping
    */
    public String toString()
        { return map.toString(); }
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
