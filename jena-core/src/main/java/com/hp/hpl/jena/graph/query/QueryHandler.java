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
import com.hp.hpl.jena.util.iterator.*;

/**
    a QueryHandler handles queries on behalf of a graph. It's primary purpose
    is to isolate changes to the query interface away from the Graph; multiple
    different Graph implementations can use the same QueryHandler class, such
    as the built-in SimpleQueryHandler.

	@author kers
*/

public interface QueryHandler
    {
    /**
        prepare a plan for generating bindings given the query _q_ and the result
        variables _variables_.
    */
    public BindingQueryPlan prepareBindings( Query q, Node [] variables );

    /**
        produce a single Stage which will probe the underlying graph for triples
        matching p and inject all the resulting bindings into the processing stream
        (see Stage for details)
    <p>
        _map_ is the variable binding map to use and update. _constraints_ is
        the current constraint expression: if this Stage can absorb some of the 
        ANDed constraints, it may do so, and remove them from the ExpressionSet.
    */
    public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] p );
    
    /**
    	deliver a plan for executing the tree-match query defined by _pattern_.
    */
    public TreeQueryPlan prepareTree( Graph pattern );
    
    /**
    	deliver an iterator over all the objects _o_ such that _(s, p, o)_ is in the
    	underlying graph; nulls count as wildcards.  .remove() is not defined 
        on this iterator.
    */
    public ExtendedIterator<Node> objectsFor( Node s, Node p );

    /**
    	deliver an iterator over all the subjects _s_ such that _(s, p, o)_ is in the
    	underlying graph; nulls count as wildcards.  .remove() is not defined 
        on this iterator.
    */
    public ExtendedIterator<Node> subjectsFor( Node p, Node o );

    /**
         Answer an iterator over all the predicates <code>p</code> such that
         <code>(s, p, o)</code> is in the underlying graph.  .remove() is not 
         defined on this iterator.
    */
    public ExtendedIterator<Node> predicatesFor( Node s, Node o );
    
    /**
        true iff the graph contains a triple in which n appears somewhere.
        if n is a fluid node, it is not defined whether true or false is returned,
        so don't do that.
    */
    public boolean containsNode( Node n );

    }
