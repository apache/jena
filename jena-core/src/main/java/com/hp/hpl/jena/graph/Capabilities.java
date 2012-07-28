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

/**
    Interface for expressing capabilities.
 */
public interface Capabilities
    {
    /**
        Answer true iff Graph::size() is accurate.
     */
    boolean sizeAccurate();
    
    /**
        Answer true if Graph::add() can be used to add at least some triples to
        the graph.
    */
    boolean addAllowed();
    
    /**
        Answer true if Graph::add() can be used to add at least some triples to the
        graph. If everyTriple is true, answer true iff *any* triple can be added (ie the
        graph places no special restrictions on triples).
    */
    boolean addAllowed( boolean everyTriple );
    
    /**
        Answer true iff Graph::delete() can be used to remove at least some triples
        from the graph.
    */
    boolean deleteAllowed();
    
    /**
        Answer true if Graph::delete() can be used to remove at least some triples 
        from the graph. If everyTriple is true, any such triple may be removed.
    */
    boolean deleteAllowed( boolean everyTriple );
    
    /**
    	Answer true iff the iterators returned from <b>find</b> support the .remove()
        operation. 
    */
    boolean iteratorRemoveAllowed();
    
    /**
        Answer true iff the graph can be completely empty.
     */
    boolean canBeEmpty();

    /**
         Answer true if the find() contract on the associated graph is "safe", ie,
         can be used safely by the pretty-printer (we'll tighten up that definition).
     */
    boolean findContractSafe();

    /**
        Answer true iff this graph compares literals for equality by value
        rather than by lexical form (the memory-based graphs do; 
        TDB model do mostly (because of canonicalization), SDB models don't).
    */
    boolean handlesLiteralTyping();
    }
