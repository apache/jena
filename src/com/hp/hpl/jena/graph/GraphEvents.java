/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: GraphEvents.java,v 1.2 2004-06-29 08:46:31 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph;

/**
    GraphEvents is the base class for Jena general graph events. Each graph event
    has a title and some content.
    
    @author kers
 */
public class GraphEvents
	{
	public static final GraphEvents removeAll = new GraphEvents( "removeAll", "" );
	
    private String title;
    private Object content;
    
    public GraphEvents( String title, Object content )
        { this.title = title;
        this.content = content; }
    
    public boolean equals( Object o )
        { return o instanceof GraphEvents && same( (GraphEvents) o ); }
    
    public boolean same( GraphEvents o )
        { return title.equals( o.title ) && content.equals( o.content ); }
   
	public static Object remove( Node s, Node p, Node o )
	    { return new GraphEvents( "remove", Triple.create( s, p, o ) ); }
	}

/*
(c) Copyright 2004, Hewlett-Packard Development Company, LP
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