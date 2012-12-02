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

package com.hp.hpl.jena.sparql.graph;


import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.shared.ReificationStyle ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** A Reifier that only supports one style Standard (intercept, no conceal 
 *  -- and intercept is a no-op anyway because all triples 
 *  appear in the underlying graph for storing all triples).
 *  Adapter tot he code library that provided "Standard" reificiation.  
 */

public class Reifier2 implements Reifier
{
    private final Graph graph ;

    public Reifier2(Graph graph)
    {
        this.graph = graph ;
    }
    
    @Override
    public ExtendedIterator<Node> allNodes()
    {
        return ReifierStd.allNodes(graph, null) ;
    }

    @Override
    public void close()
    {}

    @Override
    public ExtendedIterator<Triple> find(TripleMatch match)
    {
        return graph.find(match) ; 
    }

    @Override
    public ExtendedIterator<Triple> findEither(TripleMatch match, boolean showHidden)
    {
        return ReifierStd.findEither(graph, match, showHidden) ;
    }

    @Override
    public ExtendedIterator<Triple> findExposed(TripleMatch match)
    {
        return ReifierStd.findExposed(graph, match) ;
    }

    @Override
    public Graph getParentGraph()
    {
        return graph ;
    }

    @Override
    public ReificationStyle getStyle()
    {
        return ReificationStyle.Standard ;
    }
    
    @Override
    public Triple getTriple(Node n)
    {
        return ReifierStd.getTriple(graph, n) ; 
    }
    
    @Override
    public boolean hasTriple(Triple t)
    {
        return ReifierStd.hasTriple(graph, t) ;
    }

    @Override
    public boolean hasTriple(Node node)
    {
        return ReifierStd.hasTriple(graph, node) ;
    }

    @Override
    public ExtendedIterator<Node> allNodes(Triple t)
    {
        return ReifierStd.allNodes(graph, t) ;
    }

    @Override
    public boolean handledAdd(Triple triple)
    {
        graph.add(triple) ;
        return true ;
    }

    @Override
    public boolean handledRemove(Triple triple)
    {
        graph.delete(triple) ;
        return true ;
    }

    @Override
    public Node reifyAs(Node node, Triple triple)
    {
        return ReifierStd.reifyAs(graph, node, triple) ;
    }

    @Override
    public void remove(Triple triple)
    {
        ReifierStd.remove(graph, triple) ;
    }

    @Override
    public void remove(Node node, Triple triple)
    {
        ReifierStd.remove(graph, node, triple) ;
    }
    
    @Override
    public int size()
    {
        return 0 ;
    }
}
