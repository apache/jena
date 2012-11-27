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


import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Node ;

/** 
 * DatasetGraph framework.  
 * This class contains a convenience implementation of find that maps to a split between 
 * defaultGraph/named graphs.
 * @see DatasetGraphTriplesQuads
 * @see DatasetGraphCollection
 * @see DatasetGraphOne
 * 
 */
abstract public class DatasetGraphBaseFind extends DatasetGraphBase 
{
    protected DatasetGraphBaseFind() {}
    
    /** Implementation of find based on splitting into triples (default graph) and quads (named graph) */
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p , Node o)
    {
        if ( Quad.isDefaultGraph(g))
            return findInDftGraph(s, p, o) ;
        if ( ! isWildcard(g) )
            return findNG(g, s, p, o) ;
        return findAny(s, p, o) ;
    }
    
    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p , Node o)
    {
        Iterator<Quad> qIter ;
        if ( Quad.isUnionGraph(g))
            qIter = findInAnyNamedGraphs(s, p, o) ;
        else if ( ! isWildcard(g) )
            qIter = findInSpecificNamedGraph(g, s, p, o) ;
        else
            qIter = findInAnyNamedGraphs(s, p, o) ;
        if ( qIter == null )
            return Iter.nullIterator() ;
        return qIter ;
    }

    protected Iterator<Quad> findAny(Node s, Node p , Node o) 
    {
        // Default graph
        Iterator<Quad> iter1 = findInDftGraph(s, p, o) ;
        Iterator<Quad> iter2 = findInAnyNamedGraphs(s, p, o) ;

        if ( iter1 == null && iter2 == null )
            return Iter.nullIterator() ;
        // Copes with null in either position.
        return Iter.append(iter1, iter2) ;
    }

    protected abstract Iterator<Quad> findInDftGraph(Node s, Node p , Node o) ;
    protected abstract Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p , Node o) ;
    protected abstract Iterator<Quad> findInAnyNamedGraphs(Node s, Node p , Node o) ;
}
