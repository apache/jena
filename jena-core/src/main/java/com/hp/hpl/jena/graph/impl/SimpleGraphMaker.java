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
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A SimpleGraphFactory produces memory-based graphs and records them
    in a local map.
 */

public class SimpleGraphMaker extends BaseGraphMaker
{

    /**
        Initialise a SimpleGraphMaker with reification style Minimal
     */ 
    public SimpleGraphMaker()
    { super() ; }

    /**
        The mapping from the names of graphs to the Graphs themselves.
     */    
    private Map<String, Graph> graphs = new HashMap<>();

    public Graph create()
    { return Factory.createGraphMem(); }

    /**
        Create a graph and record it with the given name in the local map.
     */
    @Override
    public Graph createGraph( String name, boolean strict )
    {
        GraphMemBase already = (GraphMemBase) graphs.get( name );
        if (already == null)
        {
            Graph result = Factory.createGraphMem( );
            graphs.put( name, result );
            return result;            
        }
        else if (strict)
            throw new AlreadyExistsException( name );
        else
            return already.openAgain();
    }

    /**
        Open (aka find) a graph with the given name in the local map.
     */
    @Override
    public Graph openGraph( String name, boolean strict )
    {
        GraphMemBase already = (GraphMemBase) graphs.get( name );
        if (already == null) 
            if (strict) throw new DoesNotExistException( name );
            else return createGraph( name, true );
        else
            return already.openAgain();
    }

    @Override
    public Graph openGraph()
    { return getGraph(); }

    /**
        Remove the mapping from name to any graph from the local map.
     */
    @Override
    public void removeGraph( String name )
    {
        if (!graphs.containsKey( name )) throw new DoesNotExistException( name );
        graphs.remove( name );
    }

    /**
        Return true iff we have a graph with the given name
     */
    @Override
    public boolean hasGraph( String name )
    { return graphs.containsKey( name ); }

    /**
        Close this factory - we choose to do nothing.
     */
    @Override
    public void close()
    { /* nothing to do */ }

    @Override
    public ExtendedIterator<String> listGraphs()
    { return WrappedIterator.create( graphs.keySet().iterator() ); }
}
