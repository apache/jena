/*
  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Union.java,v 1.7 2004-11-01 16:38:26 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A class representing the dynamic union of two graphs. Addition only affects the left 
    operand, deletion affects both. 
    @see MultiUnion
	@author hedgehog
*/

public class Union extends Dyadic implements Graph 
	{
	public Union( Graph L, Graph R )
		{ super( L, R ); }
		
    /**
        To add a triple to the union, add it to the left operand; this is asymmetric.
    */
	public void performAdd( Triple t )
		{ L.add( t ); }

    /**
        To remove a triple, remove it from <i>both</i> operands.
    */
	public void performDelete( Triple t )
		{
		L.delete( t );
		R.delete( t );
		}

    public boolean contains( Triple t )
        { return L.contains( t ) || R.contains( t ); }
        
    /**
        To find in the union, find in the components, concatenate the results, and omit
        duplicates. That last is a performance penalty, but I see no way to remove it
        unless we know the graphs do not overlap.
    */
	public ExtendedIterator graphBaseFind( final TripleMatch t ) 
	    {
	    Set seen = HashUtils.createSet();
        return recording( L.find( t ), seen ).andThen( rejecting( R.find( t ), seen ) ); 
	    // return L.find( t ) .andThen( rejecting( R.find( t ), L ) ); 
		}
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
