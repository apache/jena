/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Stage.java,v 1.4 2003-07-17 14:56:40 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

/**
	a processing stage in the query pipeline. Each stage
	gets connected to its predecessor in the pipeline, and
	mangles the contents before handing them on to the next
	stage.
<br>
	@author hedgehog
*/

public abstract class Stage
	{
	/** the previous stage of the pipeline, once connected */
	protected Stage previous;
    
    protected volatile boolean stillOpen = true;
    
	/** construct a new initial stage for the pipeline */    
	public static Stage initial( int count )
		{ return new InitialStage( count ); }
        
    /** connect this stage to its supplier; return this for chaining. */
	public Stage connectFrom( Stage s )
        { previous = s; return this; }
        
    public boolean isClosed()
        { return !stillOpen; }
        
    protected final void markClosed()
        { stillOpen = false; }
        
    public void close()
        { 
        previous.close(); 
        markClosed();
        }

	/**
		execute the pipeline and pump the results into _sink_; this is asynchronous.
		deliver that same _sink_ as our result. (This allows the sink to be created
		as the argument to _deliver_.) 
	*/		
	public abstract Pipe deliver( Pipe sink );
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
