/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: GraphMemFaster.java,v 1.6 2005-07-12 15:57:42 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.faster;

import java.util.Iterator;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class GraphMemFaster extends GraphMemBase
    {
    public GraphMemFaster()
        { this( ReificationStyle.Minimal ); }
    
    public GraphMemFaster( ReificationStyle style )
        { super( style ); }

    protected FasterTripleStore store = new FasterTripleStore( this );

    protected void destroy()
        { store.close(); }

    public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) store.add( t ); }

    public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) store.delete( t ); }

    public int graphBaseSize()  
        { return store.size(); }
    
    public QueryHandler queryHandler()
        { 
        if (queryHandler == null) queryHandler = new GraphMemFasterQueryHandler( this );
        return queryHandler;
        }
        
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemFasterBulkUpdateHandler( this );
        return bulkHandler;
        }

    /**
         Answer an ExtendedIterator over all the triples in this graph that match the
         triple-pattern <code>m</code>. Delegated to the store.
     */
    public ExtendedIterator graphBaseFind( TripleMatch m ) 
        { return store.find( m.asTriple() ); }
    
    public Iterator findFaster( Node S, Node P, Node O )
        { return store.findFaster( S, P, O ); }
    
    public ProcessedTriple.PreindexedFind findFasterFixedS( final Node node )
        { return store.findFasterFixedS( node ); }
    
    public ProcessedTriple.PreindexedFind findFasterFixedO( Node node )
        { return store.findFasterFixedO( node ); }
    
    /**
         Answer true iff this graph contains <code>t</code>. If <code>t</code>
         happens to be concrete, then we hand responsibility over to the store.
         Otherwise we use the default implementation.
    */
    public boolean graphBaseContains( Triple t )
        { return t.isConcrete() ? store.contains( t ) : super.graphBaseContains( t ); }
    
    /**
        Clear this GraphMem, ie remove all its triples (delegated to the store).
    */
    public void clear()
        { store.clear(); }

    public ProcessedTriple.HalfindexedFind findFasterBoundS()
        { return store.findFasterBoundS(); }

    public ProcessedTriple.HalfindexedFind findFasterBoundO()
        { return store.findFasterBoundO(); }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/