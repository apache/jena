/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: WrappedGraph.java,v 1.2 2003-08-22 14:34:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A  wrapper class which simply defers all operations to its base.
 	@author kers
*/
public class WrappedGraph implements Graph
    {
    protected Graph base;
    
    public WrappedGraph( Graph base )
        { this.base = base; }

    public boolean dependsOn( Graph other )
        { return base.dependsOn( other ); }

    public QueryHandler queryHandler()
        { return base.queryHandler(); }

    public TransactionHandler getTransactionHandler()
        { return base.getTransactionHandler(); }

    public BulkUpdateHandler getBulkUpdateHandler()
        { return base.getBulkUpdateHandler(); }

    public Capabilities getCapabilities()
        { return base.getCapabilities(); }

    public GraphEventManager getEventManager()
        { return base.getEventManager(); }

    public Reifier getReifier()
        {return base.getReifier(); }

    public PrefixMapping getPrefixMapping()
        { return base.getPrefixMapping(); }

    public void add( Triple t ) throws AddDeniedException
        { base.add( t ); }

    public void delete( Triple t ) throws DeleteDeniedException
        { base.delete( t ); }

    public ExtendedIterator find( TripleMatch m )
        { return base.find( m ); }

    public ExtendedIterator find( Node s, Node p, Node o )
        { return base.find( s, p, o ); }

    public boolean isIsomorphicWith( Graph g )
        { return base.isIsomorphicWith( g ); }

    public boolean contains( Node s, Node p, Node o )
        { return base.contains( s, p, o ); }

    public boolean contains( Triple t )
        { return base.contains( t ); }

    public void close()
        { base.close(); }

    public boolean isEmpty()
        { return base.isEmpty(); }
        
    public int size() throws UnsupportedOperationException
        { return base.size(); }

    public int capabilities()
        { return base.capabilities(); }

    }


/*
    (c) Copyright Hewlett-Packard Company 2003
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