/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
[See end of file]
$Id: GraphMemQueryHandler.java,v 1.5 2004-12-02 16:13:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;
import com.hp.hpl.jena.util.iterator.*;

/**
   A GraphMemQueryHandler is an extension of the SimpleQueryHandler which
   implements some of the query code more efficiently by exploiting the
   GraphMem's indexes.
   
 	@author hedgehog
*/
public class GraphMemQueryHandler extends SimpleQueryHandler
	{
	GraphMemQueryHandler( GraphMem graph ) 
	    { 
	    super( graph );
	    }
	
	public ExtendedIterator objectsFor( Node p, Node o )
	    { return bothANY( p, o ) ? findObjects() : super.objectsFor( p, o ); }

    public ExtendedIterator predicatesFor( Node s, Node o )
        { return bothANY( s, o ) ? findPredicates() : super.predicatesFor( s, o ); }
    
    public ExtendedIterator subjectsFor( Node p, Node o )
	    { return bothANY( p, o ) ? findSubjects() : super.subjectsFor( p, o ); }   

    /**
         Answer true iff both <code>a</code> and <code>b</code> are ANY wildcards
         or are null (legacy). 
    */
    private boolean bothANY( Node a, Node b )
        { return (a == null || a.equals( Node.ANY )) && (b == null || b.equals( Node.ANY )); }

    public ExtendedIterator findPredicates()
        { return ((GraphMem) graph).store.listPredicates(); }

	public ExtendedIterator findObjects()
	    { return ((GraphMem) graph).store.listObjects(); }
	
	public ExtendedIterator findSubjects()
	    { return ((GraphMem) graph).store.listSubjects(); }
	}

/*
	(c) Copyright 2004 Hewlett-Packard Development Company, LP
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