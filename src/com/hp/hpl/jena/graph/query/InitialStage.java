/*
  (c) Copyright 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: InitialStage.java,v 1.10 2005-02-21 11:52:15 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

/**
    The initial stage of a query, responsible for dropping the no-variables-bound seed
    binding domain into the remaining stages of the query pipeline.
    
	@author kers
*/
public class InitialStage extends Stage
    {
    /**
        The value passed in is the computed width of the result array(s); this
        is used to allocate the seeding node array.
        
     	@param count the width of the result binding array
     */
    public InitialStage( int count )
        { this.count = count; }
        
    private int count = -1;
    
    public void close()
        { markClosed(); }

    /**
        To deliver value into the Pipe result, we drop in a binding array of the correct
        width in which all the elements are null, then we close the pipe. Everything else
        is spawned by the following stages.
    */
    public Pipe deliver( Pipe result )
        {
        result.put( new Domain( count ) );
        result.close();
        return result;
        }
    }

/*
    (c) Copyright 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
