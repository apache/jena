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

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** An Aggregator is the processor for the whole result stream.
 *  BindingKeys identify which section of a group we're in. */ 

public interface Aggregator
{
    //-- Aggregator - per query (strictly, one per SELECT level), unique even if mentioned several times.
    //-- Accumulator - per group per key section processors (from AggregatorBase)

    /** Create an accumulator for this aggregator */ 
    public Accumulator createAccumulator() ;
    
    /** Value if there are no elements in any group : return null for no result */
    public Node getValueEmpty() ;
    
    public String toPrefixString()  ;
    // Key to identify an aggregator as syntax for duplicate use in a query.
    public String key() ;           
    
    /** Get the expression - may be null (e.g COUNT(*)) ; */ 
    public Expr getExpr() ;
    public Aggregator copy(Expr expr) ;
    public Aggregator copyTransform(NodeTransform transform) ;
    
    @Override
    public int hashCode() ;
    @Override
    public boolean equals(Object other) ;
}
