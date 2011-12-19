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

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.graph.Node ;



/** A DatasetGraph base class for triples+quads storage.     
 */
public abstract class DatasetGraphTriplesQuads extends DatasetGraphBaseFind
{
    @Override
    final
    public void add(Quad quad)
    {
        add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    final
    public void delete(Quad quad)
    {
        delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void add(Node g, Node s, Node p, Node o)
    {
        if ( Quad.isDefaultGraphGenerated(g) || Quad.isDefaultGraphExplicit(g) )
            addToDftGraph(s, p, o) ;
        else
            addToNamedGraph(g, s, p, o) ;
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o)
    {
        if ( Quad.isDefaultGraphGenerated(g) || Quad.isDefaultGraphExplicit(g) )
            deleteFromDftGraph(s, p, o) ;
        else
            deleteFromNamedGraph(g, s, p, o) ;
    }
    
    protected abstract void addToDftGraph(Node s, Node p, Node o) ;
    protected abstract void addToNamedGraph(Node g, Node s, Node p, Node o) ;
    protected abstract void deleteFromDftGraph(Node s, Node p, Node o) ;
    protected abstract void deleteFromNamedGraph(Node g, Node s, Node p, Node o) ;
}
