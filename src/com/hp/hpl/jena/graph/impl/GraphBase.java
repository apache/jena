/*
  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: GraphBase.java,v 1.38 2005-02-21 11:52:10 andy_seaborne Exp $
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
    graphBaseFind(TripleMatch,TripleAction), and graphBaseSize(). GraphBase 
    provides default implementations of the other methods, including the other finds 
    (on top of that one), a simple-minded prepare, and contains. GraphBase also 
    handles the event-listening and registration interfaces.
<p>
    When a GraphBase is closed, future operations on it may throw an exception.
    
	@author kers
*/

public abstract class GraphBase implements GraphWithPerform 
	{
    /**
         The reification style of this graph, used when the reifier is created (and
         nowhere else, as it happens, which is good).
    */
    protected final ReificationStyle style;
    
    /**
         Whether or not this graph has been closed - used to report ClosedExceptions
         when an operation is attempted on a closed graph.
    */
    protected boolean closed = false;

    /**
         Initialise this graph as one with reification style Minimal.
    */
    public GraphBase()
        { this( ReificationStyle.Minimal ); }
    
    /**
         Initialise this graph with the given reification style (which will be supplied to
         the reifier when it is created).
    */
    public GraphBase( ReificationStyle style )
        { this.style = style; }
        
    /**
         Utility method: throw a ClosedException if this graph has been closed.
    */
    protected void checkOpen()
        { if (closed) throw new ClosedException( "already closed", this ); }

    /**
         Close this graph. Subgraphs may extend to discard resources.
    */
    public void close() 
        { closed = true;
        if (reifier != null) reifier.close(); }
            
    /**
         Default implemenentation answers <code>true</code> iff this graph is the
         same graph as the argument graph.
    */
	public boolean dependsOn( Graph other ) 
        { return this == other; }

	/**
		Answer a QueryHandler bound to this graph. The default implementation
        returns the same SimpleQueryHandler each time ti is called; sub-classes
        may override if they need specialed query handlers.
	*/
	public QueryHandler queryHandler() 
        { 
        if (queryHandler == null) queryHandler = new SimpleQueryHandler(this);
        return queryHandler;
        }
    
    /**
         The query handler for this graph, or null if queryHandler() has not been
         called yet. 
    */
    protected QueryHandler queryHandler;
    
    /**
        Answer the event manager for this graph; allocate a new one if required.
        Subclasses may override if they have a more specialed event handler.
        The default is a SimpleEventManager.
    */
    public GraphEventManager getEventManager()
        { 
        if (gem == null) gem = new SimpleEventManager( this ); 
        return gem;
        }
    
    /**
        The event manager that this Graph uses to, well, manage events; allocated on
        demand.
    */
    protected GraphEventManager gem;

        
    /**
        Tell the event manager that the triple <code>t</code> has been added to the graph.
    */
    public void notifyAdd( Triple t )
        { getEventManager().notifyAddTriple( this, t ); }
        
    /**
        Tell the event manager that the triple <code>t</code> has been deleted from the
        graph.
    */
    public void notifyDelete( Triple t )
        { getEventManager().notifyDeleteTriple( this, t ); }
        
    /**
         Answer a transaction handler bound to this graph. The default is
         SimpleTransactionHandler, which handles <i>no</i> transactions.
    */
    public TransactionHandler getTransactionHandler()
        { return new SimpleTransactionHandler(); }
        
    /**
         Answer a BulkUpdateHandler bound to this graph. The default is a
         SimpleBulkUpdateHandler, which does bulk update by repeated simple
         (add/delete) updates; the same handler is returned on each call. Subclasses
         may override if they have specialised implementations.
    */
    public BulkUpdateHandler getBulkUpdateHandler()
        { 
        if (bulkHandler == null) bulkHandler = new SimpleBulkUpdateHandler( this ); 
        return bulkHandler;
        }

    /**
         The allocated BulkUpdateHandler, or null if no handler has been allocated yet.
    */
    protected BulkUpdateHandler bulkHandler;
    
    /**
         Answer the capabilities of this graph; the default is an AllCapabilities object
         (the same one each time, not that it matters - Capabilities should be 
         immutable).
    */
    public Capabilities getCapabilities()
        { 
        if (capabilities == null) capabilities = new AllCapabilities();
        return capabilities;
        }

    /**
         The allocated Capabilities object, or null if unallocated.
    */
    protected Capabilities capabilities = null;
    
    /**
         Answer the PrefixMapping object for this graph, the same one each time.
         Subclasses are unlikely to want to modify this.
    */
    public PrefixMapping getPrefixMapping()
        { return pm; }

    protected PrefixMapping pm = new PrefixMappingImpl();
    
	/**
	   Add a triple, and notify the event manager. Subclasses should not need to
       override this - we might make it final. The triple is added using performAdd,
       and notification done by notifyAdd.
	*/
	public void add( Triple t ) 
        {
        checkOpen();
        performAdd( t );
        notifyAdd( t );
        }
    
    /**
         Add a triple to the triple store. The default implementation throws an
         AddDeniedException; subclasses must override if they want to be able to
         add triples.
    */
    public void performAdd( Triple t )
        { throw new AddDeniedException( "GraphBase::performAdd" ); }

	/**
       Delete a triple, and notify the event manager. Subclasses should not need to
       override this - we might make it final. The triple is added using performDelete,
       and notification done by notifyDelete.
	 */
    
    public final void delete( Triple t )
        {
        checkOpen();
        performDelete( t );
        notifyDelete( t );
        }
        
    /**
         Remove a triple from the triple store. The default implementation throws
         a DeleteDeniedException; subclasses must override if they want to be able
         to remove triples.
    */
	public void performDelete( Triple t ) 
        { throw new DeleteDeniedException( "GraphBase::delete" ); }

	/**
	     Answer an (extended) iterator over all the triples in this Graph matching
         <code>m</code>. Subclasses cannot over-ride this, because it implements
         the appending of reification quadlets; instead they must implement
         graphBaseFind(TripleMatch).
	*/
	public final ExtendedIterator find( TripleMatch m )
        { checkOpen(); 
        return reifierTriples( m ) .andThen( graphBaseFind( m ) ); }

    /**
        Answer an iterator over all the triples held in this graph's non-reified triple store
        that match <code>m</code>. Subclasses <i>must</i> override; it is the core
        implementation for <code>find(TripleMatch)</code>.
    */
    protected abstract ExtendedIterator graphBaseFind( TripleMatch m );

    /**
         
    */
    public final ExtendedIterator find( Node s, Node p, Node o ) 
        { checkOpen();
        return graphBaseFind( s, p, o ); }
    
    protected ExtendedIterator graphBaseFind( Node s, Node p, Node o )
        { return find( Triple.createMatch( s, p, o ) ); }

    /**
		Answer <code>true</code> iff <code>t</code> is in the graph as revealed by 
        <code>find(t)</code> being non-empty. <code>t</code> may contain ANY
        wildcards. Sub-classes may over-ride reifierContains and graphBaseContains
        for efficiency.
	*/
	public final boolean contains( Triple t ) 
        { checkOpen();
		return reifierContains( t ) || graphBaseContains( t );	}
    
    /**
         Answer true if the reifier contains a quad matching <code>t</code>. The
         default implementation uses the reifier's <code>findExposed</code> method.
         Subclasses probably don't need to override (if they're interested, they
         probably have specialised reifiers).
    */
    protected boolean reifierContains( Triple t )
        { return getReifier().findExposed( t ).hasNext(); }

	/**
         Answer true if the graph contains any triple matching <code>t</code>.
         The default implementation uses <code>find</code> and checks to see
         if the iterator is non-empty.
    */
    protected boolean graphBaseContains( Triple t )
        { return containsByFind( t ); }

    /**
         Answer <code>true</code> if this graph contains <code>(s, p, o)</code>;
         this canonical implementation cannot be over-ridden. 
	*/
	public final boolean contains( Node s, Node p, Node o ) {
        checkOpen();
		return contains( Triple.create( s, p, o ) );
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
         Answer an iterator over all the triples exposed in this graph's reifier that 
        match <code>m</code>. The default implementation delegates this to
        the reifier; subclasses probably don't need to override this.
    */
    protected ExtendedIterator reifierTriples( TripleMatch m )
        { return getReifier().findExposed( m ); }

    /**
         Answer this graph's reifier. The reifier may be lazily constructed, and it
         must be the same reifier on each call. The default implementation is a
         SimpleReifier.
    */
	public Reifier getReifier() 
        {
		if (reifier == null) reifier = new SimpleReifier( this, style );
		return reifier;
	    }
    
    /**
         The cache variable for the allocated Reifier.
    */
    protected Reifier reifier = null;
    
	/**
	     Answer the size of this graph (ie the number of exposed triples). Defined as
         the size of the triple store plus the size of the reification store. Subclasses
         must override graphBaseSize() to reimplement (and reifierSize if they have
         some special reason for redefined that).
	*/
	public final int size() 
        { checkOpen();
        return graphBaseSize() + reifierSize(); }
    
    /**
         Answer the number of visible reification quads. Subclasses will not normally
         need to override this, since it just invokes the reifier's size() method, and
         they can implement their own reifier.
    */
    protected int reifierSize()
        { return getReifier().size(); }

    /**
         Answer the number of triples in this graph. Default implementation counts its
         way through the results of a findAll. Subclasses must override if they want
         size() to be efficient.
    */
    protected int graphBaseSize()
        {
		ExtendedIterator it = GraphUtil.findAll( this );
        int tripleCount = 0;
        while (it.hasNext()) { it.next(); tripleCount += 1; }
        return tripleCount; 
        }

    /** 
     	Answer true iff this graph contains no triples (hidden reification quads do
        not count). The default implementation is <code>size() == 0</code>, which is
        fine if <code>size</code> is reasonable efficient. Subclasses may override
        if necessary. This method may become final and defined in terms of other
        methods.
    */
    public boolean isEmpty()
        { return size() == 0; }

    /**
         Answer true iff this graph is isomorphic to <code>g</code> according to
         the algorithm (indeed, method) in <code>GraphMatcher</code>.
    */
	public boolean isIsomorphicWith( Graph g )
        { checkOpen();
		return g != null && GraphMatcher.equals( this, g ); }

	/**
	     Answer a human-consumable representation of this graph. Not advised for
         big graphs, as it generates a big string: intended for debugging purposes.
    */

	public String toString() 
        { return toString( (closed ? "closed " : ""), this ); }
        
    /**
         Answer a human-consumable representation of <code>that</code>. The 
         string <code>prefix</code> will appear near the beginning of the string. Nodes
         may be prefix-compressed using <code>that</code>'s prefix-mapping. This
         default implementation will display all the triples exposed by the graph (ie
         including reification triples if it is Standard).
    */
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
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
