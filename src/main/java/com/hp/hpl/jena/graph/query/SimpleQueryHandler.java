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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    A SimpleQueryHandler is a more-or-less straightforward implementation of QueryHandler
    suitable for use on graphs with no special query engines.
    
	@author kers
*/

public class SimpleQueryHandler implements QueryHandler
    {
    /** the Graph this handler is working for */
    protected Graph graph;
    
    /** make an instance, remember the graph */
    public SimpleQueryHandler( Graph graph )
        { this.graph = graph; }

    @Override
    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] t )
        { return new PatternStage( graph, map, constraints, t ); }
                
    @Override
    public BindingQueryPlan prepareBindings( Query q, Node [] variables )   
        { return new SimpleQueryPlan( graph, q, variables ); }
        
    @Override
    public TreeQueryPlan prepareTree( Graph pattern )
    	{ return new SimpleTreeQueryPlan( graph, pattern ); }
    	
	@Override
    public ExtendedIterator<Node> objectsFor( Node s, Node p )
		{ return objectsFor( graph, s, p ); }
		
	@Override
    public ExtendedIterator<Node> subjectsFor( Node p, Node o )
		{ return subjectsFor( graph, p, o ); }
    
    @Override
    public ExtendedIterator<Node> predicatesFor( Node s, Node o )
        { return predicatesFor( graph, s, o ); }
    
    public static ExtendedIterator<Node> objectsFor( Graph g, Node s, Node p )
        { 
        Set<Node> objects = CollectionFactory.createHashedSet();
        ClosableIterator<Triple> it = g.find( s, p, Node.ANY );
        while (it.hasNext()) objects.add( it.next().getObject() );
        return WrappedIterator.createNoRemove( objects.iterator() );
        }
        
    public static ExtendedIterator<Node> subjectsFor( Graph g, Node p, Node o )
        { 
        Set<Node> objects = CollectionFactory.createHashedSet();
        ClosableIterator<Triple> it = g.find( Node.ANY, p, o );
        while (it.hasNext()) objects.add( it.next().getSubject() );
        return WrappedIterator.createNoRemove( objects.iterator() );
        }
    
    public static ExtendedIterator<Node> predicatesFor( Graph g, Node s, Node o )
        {
        Set<Node> predicates = CollectionFactory.createHashedSet();
        ClosableIterator<Triple> it = g.find( s, Node.ANY, o );
        while (it.hasNext()) predicates.add( it.next().getPredicate() );
        return WrappedIterator.createNoRemove( predicates.iterator() );
        }
        
    /**
        this is a simple-minded implementation of containsNode that uses find
        up to three times to locate the node. Almost certainly particular graphs
        will be able to offer better query-handlers ...
    */
    @Override
    public boolean containsNode( Node n )
        {
        return 
            graph.contains( n, Node.ANY, Node.ANY )
            || graph.contains( Node.ANY, n, Node.ANY )
            || graph.contains( Node.ANY, Node.ANY, n )
            ;
        }
    }
