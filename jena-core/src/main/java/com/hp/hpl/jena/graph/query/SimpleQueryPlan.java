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

import java.util.ArrayList;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;

/**
    SimpleQueryPlan is an implementation of QueryPlan which uses the engine defined
    in Query to do the work.
    
	@author kers
*/
public class SimpleQueryPlan implements BindingQueryPlan
    {
    private Graph graph;
    private GraphQuery query;
    private Node [] variables;
    
    public SimpleQueryPlan( Graph graph, GraphQuery query, Node [] variables )
        {
        this.graph = graph;
        this.query = query;
        this.variables = variables;
        }
        
    @Override
    public ExtendedIterator<Domain> executeBindings()
        // { return query.executeBindings( graph, variables ); }
        {
        return new SimpleQueryEngine( query.getPattern(), query.getSorter(), query.getConstraints() )
            .executeBindings( new ArrayList<Stage>(), query.args().put( NamedTripleBunches.anon, graph ), variables );   
        }
    }
