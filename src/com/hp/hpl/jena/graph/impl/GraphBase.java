/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: GraphBase.java,v 1.7 2003-07-09 10:15:54 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import java.util.*;

/**
    GraphBase is an implementation of Graph that provides some convenient
    base functionality for Graph implementations.
<p>
    Subtypes of GraphBase must provide add(Triple), delete(Triple), 
    find(TripleMatch,TripleAction), and size(). GraphBase provides
    default implementations of the other methods, including the other
    finds (on top of that one), a simple-minded prepare, and contains.
<p>    
	@author kers
*/

public abstract class GraphBase implements Graph {

    private Reifier.Style style;
    
    public GraphBase( Reifier.Style style )
        { this.style = style; }
        
    public GraphBase()
        { this( Reifier.Minimal ); }
        
	public boolean dependsOn(Graph other) {
		return this == other;
	}

	/**
		@see com.hp.hpl.jena.graph.Graph#queryHandler
	*/

	public QueryHandler queryHandler() 
        { return new SimpleQueryHandler(this); }
    
    protected GraphEventManager gem;
    
    public GraphEventManager getEventManager()
        { 
        if (gem == null) gem = new SimpleManager( this ); 
        return gem;
        }
        
    static class SimpleManager implements GraphEventManager
        {
        protected Graph graph;
        protected List  listeners;
        
        SimpleManager( Graph graph ) 
            { 
            this.graph = graph;
            this.listeners = new ArrayList(); 
            }
        
        public GraphEventManager register( GraphListener listener ) 
            { 
            listeners.add( listener );
            return this; 
            }
            
        public void unregister( GraphListener listener ) 
            { listeners.remove( listener ); }
        
        public void notifyAdd( Triple t ) 
            {
            for (int i = 0; i < listeners.size(); i += 1) 
                ((GraphListener) listeners.get(i)).notifyAdd( t ); 
            }
        
        public void notifyDelete( Triple t ) 
            { 
            for (int i = 0; i < listeners.size(); i += 1) 
                ((GraphListener) listeners.get(i)).notifyDelete( t ); 
            }
        }
        
    public void notifyAdd( Triple t )
        { getEventManager().notifyAdd( t ); }
        
    public void notifyDelete( Triple t )
        { getEventManager().notifyDelete( t ); }
        
    public TransactionHandler getTransactionHandler()
        { return new SimpleTransactionHandler(); }
        
    public BulkUpdateHandler getBulkUpdateHandler()
        { return new SimpleBulkUpdateHandler( this ); }
        
    public Capabilities getCapabilities()
        { return null; }
        
    private PrefixMapping pm = new PrefixMappingImpl();
    
    public PrefixMapping getPrefixMapping()
        { return pm; }

	/**
	 * @see com.hp.hpl.jena.graph.Graph#add(Triple)
	 */
	public void add( Triple t ) 
        {
        performAdd( t );
        notifyAdd( t );
        }
    
    public void performAdd( Triple t )
        { throw new JenaAddDeniedException( "GraphBase::performAdd" ); }

	/**
	 * @see com.hp.hpl.jena.graph.Graph#delete(Triple)
	 */
    
    public void delete( Triple t )
        {
        performDelete( t );
        notifyDelete( t );
        }
        
	public void performDelete( Triple t ) {
		throw new JenaDeleteDeniedException( "GraphBase::delete" );
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#find(TripleMatch)
	 */
	public abstract ExtendedIterator find( TripleMatch m );

	/**
		contains( t ) - return true iff the triple t is in this graph
	*/
	public boolean contains(Triple t) {
		return contains( t.getSubject(), t.getPredicate(), t.getObject() );
	}

	/**
		contains( s, p, o ) - returns true iff the triple (s, p, o) is in this graph. 
	    s/p/o may be concrete or fluid. default implementation used the
        containsByFind utility.
	*/
	public boolean contains( Node s, Node p, Node o ) {
		return containsByFind( Triple.create( s, p, o ) );
	}
    
    /**
        Utility method: answer true iff we can find at least one instantiation of
        the triple in this graph using find(TripleMatch).
        
        @param t Triple that is the pattern to match
        @return true iff find(t) returns at least one result
    */
    final protected boolean containsByFind( Triple t )
        {
        ClosableIterator it = find( t );
        try { return it.hasNext(); } finally { it.close(); }
        }

	/**
	 * @see com.hp.hpl.jena.graph.Graph#find(Node, Node, Node)
	 */
	public ExtendedIterator find(Node s, Node p, Node o) {
		return find( Triple.createMatch( s, p, o ) );
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#close()
	 */
	public void close() {
	}
	
	protected Reifier reifier = null;
	
	public Reifier getReifier() {
		if (reifier == null) reifier = new SimpleReifier( this, style.intercepts() );
		return reifier;
	}
    
	/**
	 * @see com.hp.hpl.jena.graph.Graph#size()
	 */
	public int size() {
		throw new UnsupportedOperationException("GraphBase::size");
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#capabilities()
	 */
	public int capabilities() {
		return 0;
	}

	public boolean isIsomorphicWith( Graph g ) {
		return g != null && GraphMatcher.equals( this, g );
	}

	/** for little graphs only ... */

	public String toString() {
		StringBuffer b = new StringBuffer("{");
		String gap = "";
		ClosableIterator it = find(null, null, null);
		while (it.hasNext()) {
			b.append(gap);
			gap = "; ";
			b.append(it.next());
		}
		b.append("}");
		return b.toString();
	}
    
    /**
        return a dynamic copy of G with full reification (ie captures
        inbound reification triples)
    */
    public static Graph withReification( Graph g )
        { return new ReifyingCaptureGraph( g ); }
        
}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
