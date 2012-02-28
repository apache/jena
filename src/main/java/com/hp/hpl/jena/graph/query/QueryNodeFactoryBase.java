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

package com.hp.hpl.jena.graph.query ;

import com.hp.hpl.jena.graph.Node ;

/**
 * A base-level implementation of the QueryNodeFactory that uses the
 * QueryNode/QueryTriple classes directly.
 * 
 * @author kers
 */
public class QueryNodeFactoryBase implements QueryNodeFactory
{
    @Override
    public QueryNode createAny()
    {
        return new QueryNode.Any() ;
    }

    @Override
    public QueryNode createFixed(Node n)
    {
        return new QueryNode.Fixed(n) ;
    }

    @Override
    public QueryNode createBind(Node node, int i)
    {
        return new QueryNode.Bind(node, i) ;
    }

    @Override
    public QueryNode createJustBound(Node node, int i)
    {
        return new QueryNode.JustBound(node, i) ;
    }

    @Override
    public QueryNode createBound(Node node, int i)
    {
        return new QueryNode.Bound(node, i) ;
    }

    @Override
    public QueryTriple createTriple(QueryNode S, QueryNode P, QueryNode O)
    {
        return new QueryTriple(S, P, O) ;
    }

    @Override
    public QueryTriple[] createArray(int size)
    {
        return new QueryTriple[size] ;
    }
}
