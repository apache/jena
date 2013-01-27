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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

/**
    Adapter class that converts a ModelChangedListener into a GraphListener.
    The only tricky bit is that we have to implement equality as equality of the
    underlying ModelChangedListeners/ModelCom pairs.
<p>
    This implementation only works for <code>ModelCom</code> models,
    because it relies on various service methods; this gives the model the
    opportunity to cache various mappings for efficiency.
*/
public class ModelListenerAdapter implements GraphListener
    {
    protected ModelCom m;
    protected ModelChangedListener L;

    public ModelListenerAdapter( ModelCom m, ModelChangedListener L )
        { this.m = m; this.L = L; }

    @Override
    public void notifyAddArray( Graph graph, Triple [] triples )
        { L.addedStatements( m.asStatements( triples ) ); }
        
    @Override
    public void notifyDeleteArray( Graph g, Triple [] triples )
        { L.removedStatements( m.asStatements( triples ) ); }
        
    @Override
    public void notifyAddTriple( Graph g, Triple t )
        { L.addedStatement( m.asStatement( t ) ); }

    @Override
    public void notifyAddList( Graph g, List<Triple> triples )
        { L.addedStatements( m.asStatements( triples ) ); }  
              
    @Override
    public void notifyAddIterator( Graph g, Iterator<Triple> it )
        { L.addedStatements( m.asStatements( it ) ); }
        
    @Override
    public void notifyAddGraph( Graph g, Graph added )
        { L.addedStatements( m.asModel( added ) ); }
        
    @Override
    public void notifyDeleteIterator( Graph g, Iterator<Triple> it )
        { L.removedStatements( m.asStatements( it ) ); }
        
    @Override
    public void notifyDeleteTriple( Graph g, Triple t )
        { L.removedStatement( m.asStatement( t ) ); }
        
    public void notifyAddIterator( Graph g, List<Triple> triples )
        { L.addedStatements( m.asStatements( triples ) ); }
        
    @Override
    public void notifyDeleteList( Graph g, List<Triple> triples )
        { L.removedStatements( m.asStatements( triples ) ); }

    @Override
    public void notifyDeleteGraph( Graph g, Graph removed )
        { L.removedStatements( m.asModel( removed ) ); }
    
    @Override
    public void notifyEvent( Graph g, Object event )
        { L.notifyEvent( m, event ); }
        
    @Override
    public boolean equals( Object other )
        { 
        return 
            other instanceof ModelListenerAdapter 
            && ((ModelListenerAdapter) other).sameAs( this )
            ; 
        }
        
    public boolean sameAs( ModelListenerAdapter other )
        { return this.L.equals( other.L ) && this.m.equals( other.m ); }
    }
