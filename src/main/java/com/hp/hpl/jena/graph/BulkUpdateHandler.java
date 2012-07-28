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

package com.hp.hpl.jena.graph;

import java.util.*;

/**
    Defines how bulk update may be done on Graphs.
<p>
    Bulk updates are not necessarily transactions; that is, a bulk update may
    fail part-way through, leaving some but not all triples added or deleted.
    However, if a bulk update does not fail (ie throw an exception) then the
    addition or removal of triples must have been successfully completed
    in accordance with the operation of the owning graph.    
    
 */
public interface BulkUpdateHandler
    {
    /**
        Add all the triples into the graph this is handler for.
        @param triples an array of triples to add
    */
    void add( Triple [] triples );
    
    /**
        Add all the triples in the list into the graph this is handler for.
        Each element of the List must be a Triple.
        @param triples a list of Triple objects to add
    */
    void add( List<Triple> triples );
    
    /**
        Add all the elements from the iterator into the graph this is handler for.
        Each element of the iterator must be a Triple.  WARNING. An implementation may
        have to expand the iterator into a data structure containing all the component
        elements; hence long iterators may be expensive on store. 
        @param it an Iterator delivering Triples
    */
    void add( Iterator<Triple> it );
    
    /**
        Add all the triples of the given graph into the graph this is handler for.
        Optionally add g's reified triples.
        @param g a Graph whose triples are to be added
        @param withReifications if true, the reified triples of g are added as well
    */
    void add( Graph g, boolean withReifications );
    
    /**
        Add all the triples of the given graph into the graph this is handler for.
        Leave this graph's reifications unchanged.
        @param g a Graph whose triples are to be added
    */
    void add( Graph g );
    
    /**
        Remove all the triples from the graph this is handler for.
        @param triples an array of triples to remove
    */
    void delete( Triple [] triples );
    
    /**
        Remove all the triples in the list from the graph this is handler for.
        Each element of the List must be a Triple.
        @param triples a list of triples to remove
    */
    void delete( List<Triple> triples );
    
    /**
        Remove all the triples in the iterator from the graph this is handler for.
        Each element from the iterator must be a Triple. WARNING. An implementation may
        have to expand the iterator into a data structure containing all the component
        elements; hence long iterators may be expensive on store. 
        
        @param it an iterator over Triple
    */
    void delete( Iterator<Triple> it );
    
    /**
        Remove all the triples of the given graph from the graph this is handler for.
        Do not change the reifications.
        @param g a graph whose triples are to be removed
    */
    void delete( Graph g );
    
    /**
        Remove all the triples of the given graph from the graph this is handler for.
        Reified triples may optionally be removed.
        @param g a graph whose triples are to be removed
        @param withReifications if true, remove g's reifications from this graph
    */
    void delete( Graph g, boolean withReifications );

    /**
    	Remove all the statements from a graph.
    */
    void removeAll();
    
    /**
       Remove all triples that would be delivered by find(s, p, o)
    */
    void remove( Node s, Node p, Node o );
    }
