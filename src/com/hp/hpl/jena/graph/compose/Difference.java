/*
  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Difference.java,v 1.5 2004-11-01 16:38:26 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    Class representing the dynamic set difference L - R of two graphs. This is updatable;
    the updates are written through to one or other of the base graphs.
	@author hedgehog
*/

public class Difference extends Dyadic implements Graph 
	{
    /**
        Initialise a graph representing the difference L - R.
    */
	public Difference( Graph L, Graph R )
		{ super( L, R ); }
		
    /**
        Add a triple to the difference: add it to the left operand, and remove it from the 
        right operand.
    */
	public void performAdd( Triple t )
		{
		L.add( t );
		R.delete( t );
		}

    /**
        Remove a triple from the difference: remove it from the left operand. [It could
        be added to the right operand instead, but somehow that feels less satisfactory.]
    */
	public void performDelete( Triple t )
		{ L.delete( t ); }

	public ExtendedIterator graphBaseFind( TripleMatch t ) 
		{ return L.find( t ). filterDrop ( ifIn( R ) ); }
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
