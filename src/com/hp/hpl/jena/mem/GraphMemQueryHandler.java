/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
[See end of file]
$Id: GraphMemQueryHandler.java,v 1.3 2004-09-10 09:47:56 chris-dollin Exp $
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
	    {
	    return p == null && o == null ? findObjects() : super.objectsFor( p, o );
	    }
	
	public ExtendedIterator subjectsFor( Node p, Node o )
	    {
	    return p == null && o == null ? findSubjects() : super.subjectsFor( p, o );
	    }   
	
	public ExtendedIterator findObjects()
	    {
	    return ((GraphMem) graph).store.listObjects();
	    }
	
	public ExtendedIterator findSubjects()
	    {
	    return ((GraphMem) graph).store.listSubjects();
	    }
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