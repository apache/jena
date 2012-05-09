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

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.util.CollectionFactory;

/**
    a mapping from from names to Graphs.
*/
public class NamedGraphMap
    {
    NamedGraphMap() {}      
    
    private Map<String, Graph> map = CollectionFactory.createHashedMap();    
    
    /**
        Add a named graph to the map and return this map.
    	@param name the name to give this graph. Must not already be bound.
    	@param g the graph to name
    	@return this NamedGraphMap
    */
    public NamedGraphMap put( String name, Graph g ) 
        { map.put( name, g ); 
        return this; }       
    
    /**
        Answer the GRaph with the given name, or null if there isn't one.
    	@param name the name of the graph
    	@return the named graph, or null
    */
    public Graph get( String name ) 
        { return map.get( name ); } 
    }
