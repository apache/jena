/*
  (c) Copyright 2002, 2003, 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphMem.java,v 1.35 2004-07-09 06:36:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A memory-backed graph with S/P/O indexes. A GraphMem maintains a 
    reference count, set to one when it is created, and incremented by the method
    <code>openAgain()</code>. When the graph is closed, the count is decrememented,
    and when it reaches 0, the tables are trashed and GraphBase.close() called.
    Thus in normal use one close is enough, but GraphMakers using GraphMems
    can arrange to re-use the same named graph.
    
    @author  bwm, kers
*/
public class GraphMem extends GraphBase implements Graph 
    {

    NodeToTriplesMap subjects = new NodeToTriplesMap()
    	{ public Node getIndexNode( Triple t ) { return t.getSubject(); } };
    	
    NodeToTriplesMap predicates = new NodeToTriplesMap()
    	{ public Node getIndexNode( Triple t ) { return t.getPredicate(); } };
    	
    NodeToTriplesMap objects = new NodeToTriplesMap()
    	{ public Node getIndexNode( Triple t ) { return t.getObject(); } };

    protected int count;
    
    /**
        Initialises a GraphMem with the Minimal reification style
    */
    public GraphMem() 
        { this( ReificationStyle.Minimal ); }
    
    /**
        Initialises a GraphMem with the given reification style.
    */
    public GraphMem( ReificationStyle style )
        { 
        super( style );
        count = 1; 
        }

    public void close()
        {
        if (--count == 0)
            {
            subjects = predicates = objects = null;
            super.close();
            }
        }
        
    public GraphMem openAgain()
        { 
        count += 1; 
        return this;
        }
        
    public void performAdd( Triple t )
        {
        if (getReifier().handledAdd( t ))
            return;
        else if (subjects.add( t.getSubject(), t ))
            {
            predicates.add( t.getPredicate(), t );
            objects.add( t.getObject(), t ); 
            }
        }

    public void performDelete( Triple t )
        {
        if (getReifier().handledRemove( t ))
            return;
        else if (subjects.remove( t.getSubject(), t ))
        	{
            predicates.remove( t.getPredicate(), t );
            objects.remove( t.getObject(), t ); 
            }
        }

    public int size()  
        {
        checkOpen();
        return subjects.size();
        }

    public boolean isEmpty()
        {
        checkOpen();
        return subjects.isEmpty();
        }
        
    private QueryHandler q;
    
    public QueryHandler queryHandler()
        {
        if (q == null) q = new GraphMemQueryHandler( this );
        return q;
        }
        
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bud == null) bud = new GraphMemBulkUpdateHandler( this );
        return bud;
        }
    
    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    public boolean contains( Triple t ) {
        checkOpen();
        return containsByFind( t ); 
    }

    /**
        Answer true if there's some triple in the graph that (s, p, o) matches.
        Ensures that nulls are not present and then defers to contains(Triple). 
    */
    public boolean contains( Node s, Node p, Node o ) {
        checkOpen();
        if (s == null || p == null || o == null) throw new JenaException( "null not allowed" );
        return contains( Triple.create( s, p, o ) );
    }

    /** 
     	Answer an ExtendedIterator returning all the triples from this Graph that
     	match the pattern <code>m = (S, P, O)</code>.
     	
     	<p>Because the node-to-triples maps index on each of subject, predicate,
     	and (non-literal) object, concrete S/P/O patterns can immediately select
        an appropriate map. Because the match for literals must be by sameValueAs,
        not equality, the optimisation is not applied for literals.
    */
    public ExtendedIterator find( TripleMatch m ) 
        {
        checkOpen();
        Triple tm = m.asTriple();
        Node pm = tm.getPredicate();
        Node om = tm.getObject();
        Node sm = tm.getSubject();
        if (sm.isConcrete())
            return new Removable( subjects.iterator( sm , tm ), predicates, objects );
        else if (pm.isConcrete())
            return new Removable( predicates.iterator( pm, tm ), subjects, objects );
        else if (om.isConcrete() && !om.isLiteral())
            return new Removable( objects.iterator( om, tm ), subjects, predicates );
        else
            return new Removable( subjects.iterator( tm ), predicates, objects );
        }

    /**
         An iterator wrapper for NodeToTriplesMap iterators which ensures that
         a .remove on the base iterator is copied to the other two maps of this
         GraphMem. The current triple (the most recent result of .next) is
         tracked by the parent <code>TrackingTripleIterator</code> so that it
         can be removed from the other two maps, passed in when this Removable
         is created.
         
        @author kers
    */
    static class Removable extends TrackingTripleIterator
    	{
        protected NodeToTriplesMap A;
        protected NodeToTriplesMap B;
        
        Removable( Iterator it, NodeToTriplesMap A, NodeToTriplesMap B )
        	{ 
            super( it ); 
            this.A = A; 
            this.B = B; 
            }

        public void remove()
            {
            super.remove();
            A.remove( current );
            B.remove( current );
            }
    	}
    }

/*
	 *  (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
	 *  All rights reserved.
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