/*
  (c) Copyright Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: MixedGraphMem.java,v 1.4 2004-11-01 16:38:27 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.util.iterator.*;

/**
 @author hedgehog
*/
public class MixedGraphMem extends GraphMemBase implements Graph
    {    
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
        
        public ExtendedIterator iterator( final Node key, Triple pattern )
            {
            Set s = (Set) map.get( key );
            if (s == null)
                return NullIterator.instance;
            else
                {
                final Iterator it = s.iterator();
                return new NiceIterator()
                	{
                    private Triple remember = null;
                    
                    public Object next()
                        { return remember = (Triple) it.next(); }
                    
                    public boolean hasNext()
                        { return it.hasNext(); }

                    public void excise( Node k, Triple triple )
                        {
                        if (k != key) Thing.this.remove( k, triple );
                        }
                    
                    public void remove()
                        {
                        it.remove();
                        size -= 1;
                        excise( remember.getSubject(), remember );
                        excise( remember.getPredicate(), remember );
                        excise( remember.getObject(), remember );
                        }
                    
                	}  .filterKeep( new TripleMatchFilter( pattern ) );
                }
            }
        
        public ExtendedIterator iterator( final Triple pattern )
            {
            return new NiceIterator()
                {
                protected Iterator keys = map.keySet().iterator();
                protected Iterator current = NullIterator.instance;
                protected Triple triple = null;
                protected Triple remember = null;
                protected Node key = null;
                protected Set seen = HashUtils.createSet();
                protected Filter filter = new TripleMatchFilter( pattern );
                
                protected Triple ANY = Triple.create( "?? ?? ??" );
                
                public Object next()
                    {
                    ensureHasNext();
                    try { return remember = triple; } finally { triple = null; }
                    }
                
                public boolean hasNext()
                    {
                    if (triple == null)
                        {
                        while (current.hasNext())
                            { 
                            triple = (Triple) current.next(); 
                            if (!pattern.matches( triple ) || seen.contains( triple ))
                                {
                                triple = null;
                                }
                            else
                                {
                                seen.add( triple );
                                return true; 
                                }
                            }
                        if (keys.hasNext())
                            {
                            key = (Node) keys.next();
                            Set s = (Set) map.get( key );
                            if (s == null) return hasNext();
                            current = s.iterator();
                            return hasNext();
                            }
                        return false;
                        }
                    else
                        return true;
                    }
                
                public void excise( Node key, Triple triple )
                    {
                    if (key != this.key) Thing.this.remove( key, triple );
                    }
                
                public void remove()
                    { 
                    current.remove(); 
                    size -= 1;
                    excise( remember.getSubject(), remember );
                    excise( remember.getPredicate(), remember );
                    excise( remember.getObject(), remember );
                    }
                }; 
            }
        
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
        { if (!getReifier().handledAdd( t )) thing.add( t ); }
    
    public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) thing.remove( t ); }
    
    public int size()  
        {
        checkOpen();
        return thing.size();
        }

    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    public boolean contains( Triple t ) 
        {
        checkOpen();
        return t.isConcrete() ? thing.contains( t ) : containsByFind( t ); 
        }
    
    protected void destroy()
        { thing = null; }
    
    public boolean isEmpty()
        {
        checkOpen();
        return thing.isEmpty();
        }
    
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemBulkUpdateHandler( this )
        	{
            protected void clearComponents()
        	    {
        	    MixedGraphMem g = (MixedGraphMem) graph;
        	    g.thing.clear();
        	    }
        	};
        return bulkHandler;
        }
    
    public ExtendedIterator graphBaseFind( TripleMatch m ) 
        {
        Triple t = m.asTriple();
        Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
        return 
        	S.isConcrete() ? thing.iterator( S, t )
            : P.isConcrete() ? thing.iterator( P, t )
            : O.isURI() || O.isBlank() ? thing.iterator( O, t )
            : thing.iterator( m.asTriple() )
            ; 
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