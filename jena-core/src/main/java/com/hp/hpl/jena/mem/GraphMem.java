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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A memory-backed graph with S/P/O indexes. 
    @author  bwm, kers
*/
@Deprecated
public class GraphMem extends GraphMemBase implements Graph 
    {    
    public GraphTripleStore forTestingOnly_getStore() 
        { return (GraphTripleStore) store; }
    
    /**
        Initialises a GraphMem with the Minimal reification style. Use the
        factory if possible; this method is public to allow certain reflective
        tests.
    */
    public GraphMem() 
        { this( ReificationStyle.Minimal ); }
    
    /**
        Initialises a GraphMem with the given reification style. Use the
        factory if possible; this method is public to allow certain reflective
        tests.
    */
    public GraphMem( ReificationStyle style )
        { super( style ); }
    
    @Override protected TripleStore createTripleStore()
        { return new GraphTripleStore( this ); }

    @Override protected void destroy()
        { store.close(); }

    @Override public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) store.add( t ); }

    @Override public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) store.delete( t ); }

    @Override public int graphBaseSize()  
        { return store.size(); }
    
    @Override public QueryHandler queryHandler()
        {
        if (queryHandler == null) queryHandler = new GraphMemBaseQueryHandler( this );
        return queryHandler;
        }
        
    /**
         Answer an ExtendedIterator over all the triples in this graph that match the
         triple-pattern <code>m</code>. Delegated to the store.
     */
    @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) 
        { return store.find( m.asTriple() ); }
    
    /**
         Answer true iff this graph contains <code>t</code>. If <code>t</code>
         happens to be concrete, then we hand responsibility over to the store.
         Otherwise we use the default implementation.
    */
    @Override public boolean graphBaseContains( Triple t )
        { return isSafeForEquality( t ) ? store.contains( t ) : super.graphBaseContains( t ); }
    
    /**
        Clear this GraphMem, ie remove all its triples (delegated to the store).
    */
    @Override public void clear()
        { 
        store.clear(); 
        ((SimpleReifier) getReifier()).clear();
        }
    }
