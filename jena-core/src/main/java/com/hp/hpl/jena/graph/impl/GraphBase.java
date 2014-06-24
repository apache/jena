/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.shared.AddDeniedException ;
import com.hp.hpl.jena.shared.ClosedException ;
import com.hp.hpl.jena.shared.DeleteDeniedException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

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
    
*/

public abstract class GraphBase implements GraphWithPerform 
	{
    /**
         Whether or not this graph has been closed - used to report ClosedExceptions
         when an operation is attempted on a closed graph.
    */
    protected boolean closed = false;

    /**
         Initialise this graph as one with reification style Minimal.
    */
    public GraphBase()  {}
    
    /**
         Initialise this graph with the given reification style (which will be supplied to
         the reifier when it is created).
    */
    
    /**
         Utility method: throw a ClosedException if this graph has been closed.
    */
    protected void checkOpen()
        { if (closed) throw new ClosedException( "already closed", this ); }

    /**
         Close this graph. Subgraphs may extend to discard resources.
    */
    @Override
    public void close()
    {
        closed = true ;
    }
    
    @Override
    public boolean isClosed()
        { return closed; }
            
    /**
         Default implementation answers <code>true</code> iff this graph is the
         same graph as the argument graph.
    */
	@Override
    public boolean dependsOn( Graph other ) 
        { return this == other; }

    @Override
    public GraphStatisticsHandler getStatisticsHandler()
        {
        if (statisticsHandler == null) statisticsHandler = createStatisticsHandler();
        return statisticsHandler;
        }
    
    protected GraphStatisticsHandler statisticsHandler;
    
    protected GraphStatisticsHandler createStatisticsHandler()
        { return null; }
    
    /**
        Answer the event manager for this graph; allocate a new one if required.
        Subclasses may override if they have a more specialised event handler.
        The default is a SimpleEventManager.
    */
    @Override
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
    @Override
    public TransactionHandler getTransactionHandler()
        { return new SimpleTransactionHandler(); }
        
    /**
         Answer a BulkUpdateHandler bound to this graph. The default is a
         SimpleBulkUpdateHandler, which does bulk update by repeated simple
         (add/delete) updates; the same handler is returned on each call. Subclasses
         may override if they have specialised implementations.
         @deprecated Bulk update operations are going to be removed.  
         @see GraphUtil for convenience helpers.
    */
    @Override
    @Deprecated
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
    @Override
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
     */
    @Override
    public PrefixMapping getPrefixMapping()
    { 
        if ( pm == null )
            pm = createPrefixMapping() ;
        return pm;
    }

    protected PrefixMapping pm = null ;
    protected PrefixMapping createPrefixMapping() { return new PrefixMappingImpl() ; }
    
	/**
	   Add a triple, and notify the event manager. Subclasses should not need to
       override this - we might make it final. The triple is added using performAdd,
       and notification done by notifyAdd.
	*/
	@Override
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
    @Override
    public void performAdd( Triple t )
        { throw new AddDeniedException( "GraphBase::performAdd" ); }

	/**
       Delete a triple, and notify the event manager. Subclasses should not need to
       override this - we might make it final. The triple is added using performDelete,
       and notification done by notifyDelete.
	 */
    
    @Override
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
	@Override
    public void performDelete( Triple t ) 
        { throw new DeleteDeniedException( "GraphBase::delete" ); }

    /**
        Remove all the statements from this graph.
     */
	@Override
    public void clear()
	{
	    GraphUtil.remove(this, Node.ANY, Node.ANY, Node.ANY) ;
        getEventManager().notifyEvent(this, GraphEvents.removeAll ) ;	
	}

	/**
       Remove all triples that match by find(s, p, o)
	 */
	@Override
    public void remove( Node s, Node p, Node o )
	{
	    GraphUtil.remove(this, s, p, o) ;
	    getEventManager().notifyEvent(this, GraphEvents.remove(s, p, o) ) ;
	}
	
	/**
	     Answer an (extended) iterator over all the triples in this Graph matching
         <code>m</code>. Subclasses cannot over-ride this, because it implements
         the appending of reification quadlets; instead they must implement
         graphBaseFind(TripleMatch).
	*/
    @Override
    public final ExtendedIterator<Triple> find(TripleMatch m)
    {
        checkOpen() ;
        return graphBaseFind(m) ;
    }

    /**
        Answer an iterator over all the triples held in this graph's non-reified triple store
        that match <code>m</code>. Subclasses <i>must</i> override; it is the core
        implementation for <code>find(TripleMatch)</code>.
    */
    protected abstract ExtendedIterator<Triple> graphBaseFind( TripleMatch m );

    public ExtendedIterator<Triple> forTestingOnly_graphBaseFind( TripleMatch tm )
        { return graphBaseFind( tm ); }
    
    /**
         
    */
    @Override
    public final ExtendedIterator<Triple> find( Node s, Node p, Node o ) 
        { checkOpen();
        return graphBaseFind( s, p, o ); }
    
    protected ExtendedIterator<Triple> graphBaseFind( Node s, Node p, Node o )
        { return find( Triple.createMatch( s, p, o ) ); }

    /**
		Answer <code>true</code> iff <code>t</code> is in the graph as revealed by 
        <code>find(t)</code> being non-empty. <code>t</code> may contain ANY
        wildcards. Sub-classes may over-ride reifierContains and graphBaseContains
        for efficiency.
	*/
	@Override
    public final boolean contains( Triple t ) 
        { checkOpen();
		return graphBaseContains( t );	}

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
	@Override
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
        ClosableIterator<Triple> it = find( t );
        try { return it.hasNext(); } finally { it.close(); }
        }
    
	/**
	     Answer the size of this graph (ie the number of exposed triples). Defined as
         the size of the triple store plus the size of the reification store. Subclasses
         must override graphBaseSize() to reimplement (and reifierSize if they have
         some special reason for redefined that).
	*/
    @Override
    public final int size()
    {
        checkOpen() ;
        return graphBaseSize() ;
    }
    
    /**
         Answer the number of triples in this graph. Default implementation counts its
         way through the results of a findAll. Subclasses must override if they want
         size() to be efficient.
    */
    protected int graphBaseSize()
        {
		ExtendedIterator<Triple> it = GraphUtil.findAll( this );
        try 
            {
            int tripleCount = 0;
            while (it.hasNext()) { it.next(); tripleCount += 1; }
            return tripleCount;
            }
        finally
            { it.close(); }
        }

    /** 
     	Answer true iff this graph contains no triples (hidden reification quads do
        not count). The default implementation is <code>size() == 0</code>, which is
        fine if <code>size</code> is reasonable efficient. Subclasses may override
        if necessary. This method may become final and defined in terms of other
        methods.
    */
    @Override
    public boolean isEmpty()
        { return size() == 0; }

    /**
         Answer true iff this graph is isomorphic to <code>g</code> according to
         the algorithm (indeed, method) in <code>GraphMatcher</code>.
    */
	@Override
    public boolean isIsomorphicWith( Graph g )
        { checkOpen();
		return g != null && GraphMatcher.equals( this, g ); }

	/**
	     Answer a human-consumable representation of this graph. Not advised for
         big graphs, as it generates a big string: intended for debugging purposes.
    */

	@Override public String toString() 
        { return toString( (closed ? "closed " : ""), this ); }
        
	/**
	    toString will not cut off up to this number of triples.
	*/
	public static final int TOSTRING_TRIPLE_BASE = 10;
    
	/**
	    toString will not output more than this number of triples.
	*/
	public static final int TOSTRING_TRIPLE_LIMIT = 17;
	
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
		StringBuilder b = new StringBuilder( prefix + " {" );
		int count = 0;
		String gap = "";
		ClosableIterator<Triple> it = GraphUtil.findAll( that );
		while (it.hasNext() && count < TOSTRING_TRIPLE_LIMIT) 
            {
			b.append( gap );
			gap = "; ";
			count += 1;
			b.append( it.next().toString( pm ) );
		    } 
		if (it.hasNext()) b.append( "..." );
		it.close();
		b.append( "}" );
		return b.toString();
	   }

}
