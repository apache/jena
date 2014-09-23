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
import org.apache.jena.atlas.iterator.NullIterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** 
 * DatasetGraph of a single graph as default graph. 
 * Fixed as one graph (the default) - can not add named graphs.
 */
public class DatasetGraphOne extends DatasetGraphBaseFind
{
    private final Graph graph ;
    
    public DatasetGraphOne(Graph graph) { this.graph = graph ; }
    
    @Override
    public boolean containsGraph(Node graphNode)
    {
        if ( isDefaultGraph(graphNode) )
            return true ;
        return false ;
    }
    
    @Override
    public Graph getDefaultGraph() { return graph ; }

    @Override
    public Graph getGraph(Node graphNode)
    { 
        if ( isDefaultGraph(graphNode) )
            return graph ;
        return null ;
    }

    @Override
    public Iterator<Node> listGraphNodes()
    {
        return new NullIterator<>() ;
    }

    @Override
    public long size()
    {
        return 0 ;
    }

    @Override
    public void add(Node g , Node s, Node p, Node o)
    {
        if (  Quad.isDefaultGraph(g) )
            graph.add(new Triple(s, p, o)) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.add/named graph") ;
    }
    
    @Override
    public void add(Quad quad)
    { 
        if ( isDefaultGraph(quad) )
            graph.add(quad.asTriple()) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.add/named graph") ;
    }      
    
    @Override
    public void delete(Node g, Node s, Node p, Node o)
    {
        if (  Quad.isDefaultGraph(g) )
            graph.delete(new Triple(s, p, o)) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.delete/named graph") ;
    }

    @Override
    public void delete(Quad quad)
    {
        if (  isDefaultGraph(quad) )
            graph.delete(quad.asTriple()) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.delete/named graph") ;
    }

    @Override
    public void setDefaultGraph(Graph g)    
    { throw new UnsupportedOperationException("DatasetGraphOne.setDefaultGraph") ; }

    @Override
    public void addGraph(Node graphName, Graph graph)
    { throw new UnsupportedOperationException("DatasetGraphOne.addGraph") ; }

    @Override
    public void removeGraph(Node graphName)
    { throw new UnsupportedOperationException("DatasetGraphOne.removeGraph") ; }

    // -- Not needed -- implement find(g,s,p,o) directly.
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { 
        return triples2quadsDftGraph(graph.find(s, p ,o)) ; 
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    {
        // There are no named graphs
        return Iter.nullIterator() ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    {
        // There are no named graphs
        return Iter.nullIterator() ;
    }

    protected static boolean isDefaultGraph(Quad quad)
    {
        return isDefaultGraph(quad.getGraph()) ;
    }

    protected static boolean isDefaultGraph(Node quadGraphNode)
    {
        return ( quadGraphNode == null || Quad.isDefaultGraph(quadGraphNode) ) ;
    }

    // It's just easier and more direct ...
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p , Node o)
    {
        if ( isWildcard(g) || isDefaultGraph(g) )
            return triples2quadsDftGraph(graph.find(s, p, o)) ;
        else
            return new NullIterator<>() ;
    }
    
    
    @Override
    public void close()
    { 
        graph.close();
        super.close() ;
    }
}
