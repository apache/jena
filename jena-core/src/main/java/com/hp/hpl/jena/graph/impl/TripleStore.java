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
     TripleStore - interface for bulk storage of triples used in composed graphs.
*/
public interface TripleStore
    {
    /**
         Destroy this triple store - discard the indexes.
    */
    public abstract void close();

    /**
         Add a triple to this triple store.
    */
    public abstract void add( Triple t );

    /**
         Remove a triple from this triple store.
    */
    public abstract void delete( Triple t );

    /**
         Answer the size (number of triples) of this triple store.
    */
    public abstract int size();

    /**
         Answer true iff this triple store is empty.
    */
    public abstract boolean isEmpty();

    /**
         Answer true iff this triple store contains the (concrete) triple <code>t</code>.
    */
    public abstract boolean contains( Triple t );

    /**
         Answer an setwise iterator over all the subjects of triples in this store.
    */
    public ExtendedIterator<Node> listSubjects();
    
    /**
         Answer an iterator over all the predicates of triples in this store.
    */
    public ExtendedIterator<Node> listPredicates();
    
    /**
         Answer an setwise iterator over all the objects of triples in this store.
    */    
    public ExtendedIterator<Node> listObjects();
    
    /** 
         Answer an ExtendedIterator returning all the triples from this store that
         match the pattern <code>m = (S, P, O)</code>.
    */
    public abstract ExtendedIterator<Triple> find( TripleMatch tm );

    /**
        Clear this store, ie remove all triples from it.
    */
    public abstract void clear();
    }
