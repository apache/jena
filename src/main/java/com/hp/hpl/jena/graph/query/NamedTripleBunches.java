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

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.SimpleQueryEngine.Cons;
import com.hp.hpl.jena.util.CollectionFactory;

import java.util.*;

/**
    A NamedTripleBunches maps a [graph] name to a bunch of triples associated
    with that name. 
    
 	@author hedgehog
*/
public class NamedTripleBunches
    {
    private Map<String, Cons> triples = CollectionFactory.createHashedMap();

    /**
        A more-or-less internal object for referring to the "default" graph in a query.
    */
    public static final String anon = "<this>";   
    
    /**
        Initialise an empty set of named bunches.
    */
    public NamedTripleBunches() 
        {}
    
    /**
        Associate another triple with the given name.
    	@param name the [graph] name for the buinch to add this triple to
    	@param pattern the triple to add to the bunch
    */
    public void add( String name, Triple pattern )
        { triples.put( name, SimpleQueryEngine.cons( pattern, triples.get( name ) ) ); }    
    
    /**
        Answer an iterator over the entry set of the associated map: this will be
        cleaned up as we refactor.
     */    
    public Iterator<Map.Entry<String, Cons>> entrySetIterator()
        { return triples.entrySet().iterator(); }
    }
