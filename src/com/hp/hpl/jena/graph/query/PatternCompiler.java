/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: PatternCompiler.java,v 1.5 2005-02-21 11:52:15 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    A PatternCompiler is some class that knows how to handle fixed Node values,
    binding and bound occurences of variables, and wildcards.
	@author kers
*/

public interface PatternCompiler
    {
    /**
        Method called to deliver a compiled Element constructed from a constant Node.
    */
    public Element fixed( Node value );
    
    /**
        Method called to deliver a compiled element from a bound occurance of a 
        variable Node allocated at a given index position.
    */
    public Element bound( Node n, int index );
    
    /**
        Method called to deliver a compiled element from a binding occurance of a
        variable Node allocated at a given index position.
    */
    public Element bind( Node n, int index );
    
    /**
        Method called to deliver a compiled element from a wildcard ANY.
    */
    public Element any();
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
