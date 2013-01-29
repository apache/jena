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

package com.hp.hpl.jena.graph.compose;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     DisjointUnion - a version of Union that assumes the graphs are disjoint, and
     hence that <code>find</code> need not do duplicate-removal. Adding things
     to the graph adds them to the left component, and does <i>not</i> add
     triples that are already in the right component.
     
*/
public class DisjointUnion extends Dyadic
    {
    public DisjointUnion( Graph L, Graph R )
        { super( L, R ); }

    @Override protected ExtendedIterator<Triple> _graphBaseFind( TripleMatch m )
        { return L.find( m ) .andThen( R.find( m ) ); }
    
    @Override public boolean graphBaseContains( Triple t )
        { return L.contains( t ) || R.contains( t ); }
    
    @Override public void performDelete( Triple t )
        { L.delete( t ); R.delete( t ); }
    
    @Override public void performAdd( Triple t )
        { if (!R.contains( t )) L.add( t ); }
    }
