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
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
 @author hedgehog
*/
public class MixedGraphMem extends GraphMemBase implements Graph
    {    
    protected MixedGraphMemStore mixedStore = new MixedGraphMemStore( this );
    
    public MixedGraphMem()
        { this( ReificationStyle.Minimal ); }
    
    public MixedGraphMem( ReificationStyle style )
        { super( style ); }
    
    /**
        MixedGraphMem's don't use TripleStore's at present. 
    */
    @Override protected TripleStore createTripleStore()
        { return null; }
    
    @Override public void performAdd( Triple t )
        { if (!getReifier().handledAdd( t )) mixedStore.add( t ); }
    
    @Override public void performDelete( Triple t )
        { if (!getReifier().handledRemove( t )) mixedStore.remove( t ); }
    
    @Override public int graphBaseSize()  
        { return mixedStore.size(); }

    /**
        Answer true iff t matches some triple in the graph. If t is concrete, we
        can use a simple membership test; otherwise we resort to the generic
        method using find.
    */
    @Override public boolean graphBaseContains( Triple t ) 
        { return isSafeForEquality( t ) ? mixedStore.contains( t ) : containsByFind( t ); }
    
    @Override protected void destroy()
        { mixedStore = null; }
    
    @Override public boolean isEmpty()
        {
        checkOpen();
        return mixedStore.isEmpty();
        }
    
    @Override public void clear()
        { mixedStore.clear(); }
    
    @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) 
        {
        Triple t = m.asTriple();
        Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
        return 
        	S.isConcrete() ? mixedStore.iterator( S, t )
            : P.isConcrete() ? mixedStore.iterator( P, t )
            : O.isURI() || O.isBlank() ? mixedStore.iterator( O, t )
            : mixedStore.iterator( m.asTriple() )
            ; 
        }

    }
