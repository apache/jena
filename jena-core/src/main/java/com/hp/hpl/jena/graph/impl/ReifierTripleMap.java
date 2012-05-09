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
     ReifierTripleMap - an interface that describes how SimpleReifier manages
     complete reified statements.
     @author kers
*/
public interface ReifierTripleMap
    {
    /**
         Answer the triple (ie reified statement) that is bound to this node, or
         null if there's no such triple.
    */
    public abstract Triple getTriple( Node tag );

    /**
         Answer true iff we have a reified triple <code>t</code> -- ie, getTriple would
         not return <code>null</code>.
    */
    public abstract boolean hasTriple( Triple t );

    /**
         Bind the triple <code>value</code> to the node <code>key</code> and
         answer that triple. An implementation may assume that <code>key</code> 
         is not already bound.
    */
    public abstract Triple putTriple( Node key, Triple value );

    /**
         Unbind <code>key</code> from any triple already bound to it.
    */
    public abstract void removeTriple( Node key );

    /**
         <code>key</code> should already be bound to <code>triple</code>; that
         binding is removed.
    */
    public abstract void removeTriple( Node key, Triple value );

    /**
         Remove every binding tag -> <code>triple</code>.
    */
    public abstract void removeTriple( Triple triple );
    
    /**
         Answer an iterator over all the quadlets that match <code>m</code> that
         correspond to complete reified triples held in this map.
    */
    public ExtendedIterator<Triple> find( TripleMatch m );
    
    /**
         Answer the number of quadlets in this map.
    */
    public int size();

    /**
         Answer an iterator over all the bound tags in this map.
    */
    public abstract ExtendedIterator<Node> tagIterator();

    /**
         Answer an iterator over all the tags in this map that are bound to
         <code>t</code>.
    */
    public abstract ExtendedIterator<Node> tagIterator( Triple t );
    
    /**
        Clear away all the triples.
    */
    public void clear();    
    }
