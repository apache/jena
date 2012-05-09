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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
     ReifierFragmentsMap: how a SimpleReifier manages its incomplete reifications.
     Most of the active operations are deferred to FragmentHandler.
     
     @author kers
*/
public interface ReifierFragmentsMap
    {
    /**
         Answer an iterator over all the fragments that match <code>m</code>.
    */
    public ExtendedIterator<Triple> find( TripleMatch m );
    
    /**
         Answer the number of fragments in this map.
    */
    public int size();

    /**
         Answer a FragmentHandler which can handle this fragment, or null if it isn't a
         reification fragment.
    */
    public abstract ReifierFragmentHandler getFragmentHandler( Triple fragment );

    /**
         Answer true iff this map has fragments associated with <code>tag</code>.
    */
    public abstract boolean hasFragments( Node tag );
    
    /**
        Clear away all the fragments.
    */
    public void clear();
    }
