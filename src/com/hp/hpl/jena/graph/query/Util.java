/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: Util.java,v 1.2 2003-10-15 10:56:30 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
	Util: some utility code used by graph query that doesn't seem to belong 
    anywhere else that it can be put.

	@author kers
*/
public class Util 
    {
	/**
         Answer a new set which is the union of the two argument sets.
	*/
	public static Set union( Set x, Set y )
    	{
    	Set result = new HashSet( x );
    	result.addAll( y );
    	return result;
    	}

    /**
    	Answer a new set which contains exactly the names of the variable[ node]s
        in the triple.
    */
	public static Set variablesOf( Triple t )
    	{
    	Set result = new HashSet();
        addIfVariable( result, t.getSubject() );
        addIfVariable( result, t.getPredicate() );
        addIfVariable( result, t.getObject() );
    	return result;
    	}
    
    private static void addIfVariable( Set result, Node n )
        { if (n.isVariable()) result.add( n.getName() ); }
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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