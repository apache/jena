/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphBase.java,v 1.25 2003-09-08 11:28:03 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/**
    GraphBase is an implementation of Graph that provides some convenient
    base functionality for Graph implementations.
<p>
    Subtypes of GraphBase must provide performAdd(Triple), performDelete(Triple), 
    find(TripleMatch,TripleAction), and size(). GraphBase provides default 
    implementations of the other methods, including the other finds (on top of that one), 
    a simple-minded prepare, and contains. GraphBase also handles the event-listening
    and registration interfaces.
<p>
    When a GraphBase is closed, future operations on it may throw an exception.
    
	@author kers
*/

public abstract class GraphBase implements Graph {

    protected final ReificationStyle style;
    
    protected boolean closed = false;
    
    public GraphBase( ReificationStyle style )
        { this.style = style; }
        
    public GraphBase()
        { this( ReificationStyle.Minimal ); }
        
    protected void checkOpen()
        { if (closed) throw new ClosedException( "already closed", this ); }

    /**
     * @see com.hp.hpl.jena.graph.Graph#close()
     */
    public void close() 
        { closed = true; }
            
	public boolean dependsOn(Graph other) {
		return this == other;
	}

	/**
		@see com.hp.hpl.jena.graph.Graph#queryHandler
	*/

	public QueryHandler queryHandler() 
        { return new SimpleQueryHandler(this); }
    
    /**
        The event manager that this Graph uses to, well, manage events; allocated on
        demand.
    */
    protected GraphEventManager gem;
    
    /**
        Answer the event manager for this graph; allocated a new one if required.
        @return the graph's event manager.
    */
    public GraphEventManager getEventManager()
        { 
        if (gem == null) gem = new SimpleEventManager( this ); 
        return gem;
        }
        
    /**
        Tell the event manager that the triple <code>t</code> has been added to the graph.
    */
    public void notifyAdd( Triple t )
        { getEventManager().notifyAddTriple( t ); }
        
    /**
        Tell the event manager that the triple <code>t</code> has been deleted from the
        graph.
    */
    public void notifyDelete( Triple t )
        { getEventManager().notifyDeleteTriple( t ); }
        
    public TransactionHandler getTransactionHandler()
        { return new SimpleTransactionHandler(); }
        
    private BulkUpdateHandler bud;
    
    public BulkUpdateHandler getBulkUpdateHandler()
        { 
        if (bud == null) bud = new SimpleBulkUpdateHandler( this ); 
        return bud;
        }
        
    protected Capabilities capabilities = null;
    
    public Capabilities getCapabilities()
        { 
        if (capabilities == null) capabilities = new AllCapabilities();
        return capabilities;
        }
        
    private PrefixMapping pm = new PrefixMappingImpl();
    
    public PrefixMapping getPrefixMapping()
        { return pm; }

	/**
	   Add a triple, and notify the event manager. 
	*/
	public void add( Triple t ) 
        {
        checkOpen();
        performAdd( t );
        notifyAdd( t );
        }
    
    public void performAdd( Triple t )
        { throw new AddDeniedException( "GraphBase::performAdd" ); }

	/**
	 * @see com.hp.hpl.jena.graph.Graph#delete(Triple)
	 */
    
    public void delete( Triple t )
        {
        checkOpen();
        performDelete( t );
        notifyDelete( t );
        }
        
	public void performDelete( Triple t ) {
		throw new DeleteDeniedException( "GraphBase::delete" );
	}

	/**
	 * @see com.hp.hpl.jena.graph.Graph#find(TripleMatch)
	 */
	public abstract ExtendedIterator find( TripleMatch m );

	/**
		contains( t ) - return true iff the triple t is in this graph
	*/
	public boolean contains(Triple t) {
        checkOpen();
		return contains( t.getSubject(), t.getPredicate(), t.getObject() );
	}

	/**
		contains( s, p, o ) - returns true iff the triple (s, p, o) is in this graph. 
	    s/p/o may be concrete or fluid. default implementation used the
        containsByFind utility.
	*/
	public boolean contains( Node s, Node p, Node o ) {
        checkOpen();
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
        checkOpen();
		return find( Triple.createMatch( s, p, o ) );
	}

	protected Reifier reifier = null;
	
	public Reifier getReifier() {
		if (reifier == null) reifier = new SimpleReifier( this, style );
		return reifier;
	}
    
	/**
	 * @see com.hp.hpl.jena.graph.Graph#size()
	 */
	public int size() {
        checkOpen();
		ExtendedIterator it = GraphUtil.findAll( this );
        int result = 0;
        while (it.hasNext()) { it.next(); result += 1; }
        return result;    
        }

    /** 
     	@see com.hp.hpl.jena.graph.Graph#isEmpty()
    */
    public boolean isEmpty()
        { return size() == 0; }

	public boolean isIsomorphicWith( Graph g ) {
        checkOpen();
		return g != null && GraphMatcher.equals( this, g );
	}

	/** for little graphs only ... */

	public String toString() 
        { return toString( (closed ? "closed " : ""), this ); }
        
    public static String toString( String prefix, Graph that )
        {
        PrefixMapping pm = that.getPrefixMapping();
		StringBuffer b = new StringBuffer( prefix + " {" );
		String gap = "";
		ClosableIterator it = GraphUtil.findAll( that );
		while (it.hasNext()) 
            {
			b.append( gap );
			gap = "; ";
			b.append( ((Triple) it.next()).toString( pm ) );
		    } 
		b.append( "}" );
		return b.toString();
	   }

}

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
