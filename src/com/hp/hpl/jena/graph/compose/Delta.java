/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Delta.java,v 1.2 2003-03-04 17:54:59 ian_dickinson Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.mem.*;


/**
	@author hedgehog
	L is the collection of things added,
	R is the collection of things removed,
	base is the original graph (which is not mutated)
*/

public class Delta extends Dyadic implements Graph 
	{
	private Graph base;
	
	public Delta( Graph base )
		{
		super( new GraphMem(), new GraphMem() );
		this.base = base;
		}
		
	public Graph getAdditions()
		{
		return L;
		}
		
	public Graph getDeletions()
		{
		return R;
		}
		
	public void add( Triple t )
		{
		L.add( t );
		R.delete( t );
		}

	public void delete( Triple t )
		{
		L.delete( t );
		R.add( t );
		}
		 
	public ExtendedIterator find( TripleMatch tm ) 
		{
        return base.find( tm ) .filterDrop( ifIn( R.find( null, null, null ) ) ) .andThen( L.find( tm ) );
		}

	public void close() 
		{
		super.close();
		base.close();
		}

	public int size()
		{
		return base.size() + L.size() - R.size();
		}
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
