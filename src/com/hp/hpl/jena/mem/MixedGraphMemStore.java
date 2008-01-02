/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: MixedGraphMemStore.java,v 1.5 2008-01-02 12:09:51 andy_seaborne Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

public class MixedGraphMemStore 
    {
    protected final Graph parent;
    
    public MixedGraphMemStore( Graph parent )
        { this.parent = parent; }
    
    protected Map map = CollectionFactory.createHashedMap();
    
    protected int size = 0;
    
    protected boolean add( Node key, Triple t )
        {
        Set s = (Set) map.get( key );
        if (s == null) map.put( key, s = CollectionFactory.createHashedSet() );
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
        { if (add( t.getSubject(), t )) 
            { size += 1;
            add( t.getPredicate(), t );
            add( t.getObject(), t ); } }
    
    public void remove( Triple t )
        { if (remove( t.getSubject(), t )) 
            { size -= 1;
            remove( t.getPredicate(), t );
            remove( t.getObject(), t ); } }
    
    public boolean contains( Triple t )
        { Set s = (Set) map.get( t.getSubject() );
        return s != null && s.contains( t ); }
    
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
                    if (k != key) MixedGraphMemStore.this.remove( k, triple );
                    }
                
                public void remove()
                    {
                    it.remove();
                    size -= 1;
                    excise( remember.getSubject(), remember );
                    excise( remember.getPredicate(), remember );
                    excise( remember.getObject(), remember );
                    parent.getEventManager().notifyDeleteTriple( parent, remember );
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
            protected Set seen = CollectionFactory.createHashedSet();
                        
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
                if (key != this.key) MixedGraphMemStore.this.remove( key, triple );
                }
            
            public void remove()
                { 
                current.remove(); 
                size -= 1;
                excise( remember.getSubject(), remember );
                excise( remember.getPredicate(), remember );
                excise( remember.getObject(), remember );
                parent.getEventManager().notifyDeleteTriple( parent, remember );
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

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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