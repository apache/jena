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
    
    protected Map<Node, Set<Triple>> map = CollectionFactory.createHashedMap();
    
    protected int size = 0;
    
    protected boolean add( Node key, Triple t )
        {
        Set<Triple> s = map.get( key );
        if (s == null) map.put( key, s = CollectionFactory.createHashedSet() );
        return s.add( t );
        }
    
    protected boolean remove( Node key, Triple t )
        {
        Set<Triple> s = map.get( key );
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
        { Set<Triple> s = map.get( t.getSubject() );
        return s != null && s.contains( t ); }
    
    public ExtendedIterator<Triple> iterator( final Node key, Triple pattern )
        {
        Set<Triple> s = map.get( key );
        if (s == null)
            return NullIterator.instance();
        else
            {
            final Iterator<Triple> it = s.iterator();
            return new NiceIterator<Triple>()
            	{
                private Triple remember = null;
                
                @Override public Triple next()
                    { return remember = it.next(); }
                
                @Override public boolean hasNext()
                    { return it.hasNext(); }
    
                public void excise( Node k, Triple triple )
                    {
                    if (k != key) MixedGraphMemStore.this.remove( k, triple );
                    }
                
                @Override public void remove()
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
    
    public ExtendedIterator<Triple> iterator( final Triple pattern )
        {
        return new NiceIterator<Triple>()
            {
            protected Iterator<Node> keys = map.keySet().iterator();
            protected Iterator<Triple> current = NullIterator.instance();
            protected Triple triple = null;
            protected Triple remember = null;
            protected Node key = null;
            protected Set<Triple> seen = CollectionFactory.createHashedSet();
                        
            @Override public Triple next()
                {
                ensureHasNext();
                try { return remember = triple; } finally { triple = null; }
                }
            
            @Override public boolean hasNext()
                {
                if (triple == null)
                    {
                    while (current.hasNext())
                        { 
                        triple = current.next(); 
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
                        key = keys.next();
                        Set<Triple> s = map.get( key );
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
            
            @Override public void remove()
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
