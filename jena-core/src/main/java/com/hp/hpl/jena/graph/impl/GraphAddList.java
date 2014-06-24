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

import java.util.*;

import com.hp.hpl.jena.graph.*;

/**
    A List that implements the GraphAdd interface, so that it can be passed
    to things that want to add triples to Graphs. The triples are filtered.
    @depecated This will be removed. 
*/
@Deprecated
public class GraphAddList implements GraphAdd
    {
    protected Triple match;
    protected final ArrayList<Triple> triples = new ArrayList<>();
    
    /**
         Initialise a GraphAddList with a triple [pattern] that specifies what triples
         will be accepted into the list. 
    @depecated This will be removed. 
    */
    @Deprecated   
    public GraphAddList( Triple match ) { this.match = match; }
    
    /**
         Add the triple <code>t</code> to this list if it is matched by the pattern.
    */
    @Override
    public void add( Triple t ) { if (match.matches( t )) triples.add( t ); }
    
    /**
        The number of triples held.
    */
    public int size() { return triples.size(); }
    
    /**
        Answer the last triple, and remove it.
    */
    public Triple removeLast() { return triples.remove( triples.size() - 1 ); }

    /**
        Answer an iterator over all the triples in this add-list.
    */
    public Iterator<Triple> iterator()
        { return triples.iterator(); }
    }
