/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: SmallGraphMem.java,v 1.6 2004-12-03 12:11:34 chris-dollin Exp $
*/
package com.hp.hpl.jena.mem;

import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     A SmallGraphMem is a memory-based Graph suitable only for Small models
     (a few triples, perhaps a few tens of triples), because it does no indexing,
     but it stores onlya single flat set of triples and so is memory-cheap.
     
    @author kers
*/

public class SmallGraphMem extends GraphMemBase
    {
    protected Set triples = CollectionFactory.createHashedSet();
    
    public SmallGraphMem()
        { this( ReificationStyle.Minimal ); }
    
    public SmallGraphMem( ReificationStyle style )
        { super( style ); }
    
    public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) triples.add( t ); }
    
    public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) triples.remove( t ); }
    
    public int graphBaseSize()  
        { return triples.size(); }

    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    public boolean graphBaseContains( Triple t ) 
        { return t.isConcrete() ? triples.contains( t ) : containsByFind( t ); }
    
    protected void destroy()
        { triples = null; }
    
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemBulkUpdateHandler( this )
        	{
            protected void clearComponents()
        	    {
        	    SmallGraphMem g = (SmallGraphMem) graph;
        	    g.triples.clear();
        	    }
        	};
        return bulkHandler;
        }
    
    public ExtendedIterator graphBaseFind( TripleMatch m ) 
        {
        return new TrackingTripleIterator( triples.iterator() ) 
            {
            final Graph parent = SmallGraphMem.this;
            final GraphEventManager man = parent.getEventManager();
            
            public void remove() 
                {
                super.remove();
                man.notifyDeleteTriple( parent, current );
                }
            } .filterKeep ( new TripleMatchFilter( m.asTriple() ) );
        }
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