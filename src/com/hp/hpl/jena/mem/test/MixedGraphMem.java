/*
  (c) Copyright 2004, Chris Dollin
  [See end of file]
  $Id: MixedGraphMem.java,v 1.1 2004-07-19 08:31:58 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem.test;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.mem.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;


/**
 @author hedgehog
*/
public class MixedGraphMem extends GraphMemBase implements Graph
    {
    protected Set triples = HashUtils.createSet();
    
    protected Thing thing = new Thing();
    
    public static class Thing 
    	{
        protected Map map = HashUtils.createMap();

        protected int size = 0;
        
        protected boolean add( Node key, Triple t )
            {
            Set s = (Set) map.get( key );
            if (s == null) map.put( key, s = HashUtils.createSet() );
            return s.add( t );
            }
        
        protected boolean remove( Node key, Triple t )
            {
            Set s = (Set) map.get( key );
            if (s != null)
                {
                boolean removed = s.remove( t );
                if (s.isEmpty()) map.put( key, null );
                return removed;
                }
            else
                return false;
            }
        
        public void add( Triple t )
            {
            if (add( t.getSubject(), t )) size += 1;
            add( t.getPredicate(), t );
            add( t.getObject(), t );
            }
        
        public void remove( Triple t )
            {
            if (remove( t.getSubject(), t )) size -= 1;
            remove( t.getPredicate(), t );
            remove( t.getObject(), t );
            }
        
        public boolean contains( Triple t )
            {
            Set s = (Set) map.get( t.getSubject() );
            return s != null && s.contains( t );
            }
        
        public Iterator iterator()
            { return null; }
        
        public boolean isEmpty()
            { return size == 0; }
        
        public int size()
            { return size; }
        
        public void clear()
            { map.clear(); size = 0; }
    	}
    
    public MixedGraphMem()
        { this( ReificationStyle.Minimal ); }
    
    public MixedGraphMem( ReificationStyle style )
        { super( style ); }
    
    public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) { thing.add( t ); triples.add( t ); } }
    
    public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) { thing.remove( t ); triples.remove( t ); } }
    
    public int size()  
        {
        checkOpen();
        if (thing.size() != triples.size())
            throw new RuntimeException( "inconsistent: " + thing.size() + " vs " + triples.size() );
        return triples.size();
        }

    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    public boolean contains( Triple t ) 
        {
        checkOpen();
        if (t.isConcrete())
            if (triples.contains( t ) != thing.contains( t ))
                throw new RuntimeException( "inconsistent" );
        return t.isConcrete() ? triples.contains( t ) : containsByFind( t ); 
        }
    
    protected void destroy()
        { triples = null; thing = null; }
    
    public boolean isEmpty()
        {
        checkOpen();
        if (thing.isEmpty() != triples.isEmpty())
            throw new RuntimeException( "inconsistent" );
        return triples.isEmpty();
        }
    
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemBulkUpdateHandler( this )
        	{
            protected void clearComponents()
        	    {
        	    MixedGraphMem g = (MixedGraphMem) graph;
        	    g.triples.clear();
        	    g.thing.clear();
        	    }
        	};
        return bulkHandler;
        }
    
    public ExtendedIterator find( TripleMatch m ) 
        {
        checkOpen();
        thing.iterator();
        return WrappedIterator.create( triples.iterator() ) .filterKeep ( new TripleMatchFilter( m.asTriple() ) );
        }

    }


/*
    (c) Copyright 2004, Chris Dollin
    All rights reserved. Provided AS IS. Redistribution only by written consent.
    Work in progress - could easily break; that's your problem, not mine.
*/