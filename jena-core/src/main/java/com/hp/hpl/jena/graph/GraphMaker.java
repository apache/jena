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

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    A factory for providing instances of named graphs with appropriate storage models.
    It represents a directory, or a database, or a mapping: names map to graphs for the
    lifetime of the GraphMaker. Names can be "arbitrary" character sequences.
*/

public interface GraphMaker 
{
    /**
        Answer the default graph of this GraphMaker. The same graph is returned on
        each call. It may only be constructed on the first call of getGraph(), or at any
        previous time.
        
        @return the same default graph each time
     */
    public Graph getGraph();
    
    /**
        Answer the default graph of this GraphMaker, if it has one. If not,
        throw an exception.
    */
    public Graph openGraph();
    
    /**
        Answer a graph who's name isn't interesting. Each call delivers a different graph.
        The GraphMaker may reserve a bunch of names for this purpose, of the form
        "anon_<digits>", if it cannot support truly anonymous graphs.
        
        @return a fresh anonymous graph
    */
    public Graph createGraph();
    
    /**
        Create a new graph associated with the given name. If there is no such
        association, create one and return it. If one exists but <code>strict</code>
        is false, return the associated graph. Otherwise throw an AlreadyExistsException.
        
        @param name the name to give to the new graph
        @param strict true to cause existing bindings to throw an exception
        @exception AlreadyExistsException if that name is already bound.
    */
    public Graph createGraph( String name, boolean strict );
    
    /**
        Create a graph that does not already exist - equivalent to
        <br><code>createGraph( name, false )</code>.
    */
    public Graph createGraph( String name );
    
    /**
        Find an existing graph that this factory knows about under the given
        name. If such a graph exists, return it. Otherwise, if <code>strict</code>
        is false, create a new graph, associate it with the name, and return it.
        Otherwise throw a DoesNotExistException. 
        
        @param name the name of the graph to find and return
        @param strict false to create a new one if one doesn't already exist
        @exception DoesNotExistException if there's no such named graph
    */
    public Graph openGraph( String name, boolean strict );
    
    /**
        Equivalent to <code>openGraph( name, false )</code> 
    */
    public Graph openGraph( String name );
    
    /**
        Remove the association between the name and the graph. create
        will now be able to create a graph with that name, and open will no
        longer be able to find it. Throws an exception if there's no such graph.
        The graph itself is not touched.
        
        @param name the name to disassociate
        @exception DoesNotExistException if the name is unbound
    */
    public void removeGraph( String name );
    
    /**
        return true iff the factory has a graph with the given name
        
        @param name the name of the graph to look for
        @return true iff there's a graph with that name
    */
    public boolean hasGraph( String name );
    
    /**
        Close the factory - no more requests need be honoured, and any clean-up
        can be done.
    */
    public void close();
    
    /**
        Answer an [extended] iterator where each element is the name of a graph in
        the maker, and the complete sequence exhausts the set of names. No particular
        order is expected from the list.
     	@return an extended iterator over the names of graphs known to this Maker.
     */
    ExtendedIterator<String> listGraphs();
}
