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

import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
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
    protected Set<Triple> triples = CollectionFactory.createHashedSet();
    
    public SmallGraphMem()
        { this( ReificationStyle.Minimal ); }
    
    public SmallGraphMem( ReificationStyle style )
        { super( style ); }
    
    /**
        SmallGraphMem's don't use TripleStore's at present. 
    */
    @Override protected TripleStore createTripleStore()
        { return null; }
    
    @Override public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) triples.add( t ); }
    
    @Override public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) triples.remove( t ); }
    
    @Override public int graphBaseSize()  
        { return triples.size(); }

    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    @Override public boolean graphBaseContains( Triple t ) 
        { return isSafeForEquality( t ) ? triples.contains( t ) : containsByFind( t ); }

    @Override protected void destroy()
        { triples = null; }
    
    @Override public void clear()
        { 
        triples.clear(); 
        ((SimpleReifier) getReifier()).clear();
        }
    
    @Override public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new GraphMemBulkUpdateHandler( this );
        return bulkHandler;
        }
    
    @Override public ExtendedIterator <Triple>graphBaseFind( TripleMatch m ) 
        {
        return 
            SimpleEventManager.notifyingRemove( this, triples.iterator() ) 
            .filterKeep ( new TripleMatchFilter( m.asTriple() ) );
        }    
    }
