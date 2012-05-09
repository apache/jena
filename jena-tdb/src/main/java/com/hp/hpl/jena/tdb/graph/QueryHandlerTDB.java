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

package com.hp.hpl.jena.tdb.graph;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler;

import com.hp.hpl.jena.tdb.store.GraphTDB;

public class QueryHandlerTDB extends SimpleQueryHandler implements QueryHandler
{

    public QueryHandlerTDB(GraphTDB graph)
    {
        super(graph) ;
    }

    // ----------------------
    @Override
    public boolean containsNode(Node n)
    {
        return super.containsNode(n) ;
    }

    @Override
    public ExtendedIterator<Node> subjectsFor(Node p, Node o)
    {
        return super.subjectsFor(p, o) ;
    }

    @Override
    public ExtendedIterator<Node> predicatesFor(Node s, Node o)
    {
        return super.predicatesFor(s, o) ;
    }

    @Override
    public ExtendedIterator<Node> objectsFor(Node s, Node p)
    {
        return super.objectsFor(s, p) ;
    }
    // ----------------------

//    @Override
//    public Stage patternStage(Mapping map, ExpressionSet constraints, Triple[] p)
//    {
//        return null ;
//    }
//
//    @Override
//    public BindingQueryPlan prepareBindings(Query q, Node[] variables)
//    {
//        return null ;
//    }
//
//    @Override
//    public TreeQueryPlan prepareTree(Graph pattern)
//    {
//        return null ;
//    }
}
