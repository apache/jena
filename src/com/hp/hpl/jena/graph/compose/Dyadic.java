/*
  (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Dyadic.java,v 1.7 2003-08-27 13:01:00 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.*;

/**
    Base class for the two-operand composition operations; has two graphs L and R
    @author kers
    @author Ian Dickinson - refactored most of the content to {@link CompositionBase}.
*/

public abstract class Dyadic extends CompositionBase
	{
	protected Graph L;
	protected Graph R;
	
    /**
        When the graph is constructed, copy the prefix mappings of both components
        into this prefix mapping. The prefix mapping doesn't change afterwards with the
        components, which might be regarded as a bug.
    */
	public Dyadic( Graph L, Graph R )
		{
		this.L = L;
		this.R = R;
        getPrefixMapping()
            .setNsPrefixes( L.getPrefixMapping() )
            .setNsPrefixes( R.getPrefixMapping() )
            ;
		}

    public void close()
    	{
    	L.close();
    	R.close();
        }
        
    /**
        Generic dependsOn, true iff it depends on either of the subgraphs.
    */
    public boolean dependsOn( Graph other )
        { return other == this || L.dependsOn( other ) || R.dependsOn( other ); }
 				
    public Union union( Graph X )
        { return new Union( this, X ); }    
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
