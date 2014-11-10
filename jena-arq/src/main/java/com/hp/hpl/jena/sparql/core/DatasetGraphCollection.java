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
import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.JenaException ;

/** Base class for implementations of a DatasetGraph as a set of graphs.
 * This can be a fixed collection or a changeable collection depending
 * on the implementation of getDefaultGraph()/getGraph(Node)  
 */
public abstract class DatasetGraphCollection extends DatasetGraphBaseFind
{
    @Override
    public void add(Quad quad)
    {
        Graph g = fetchGraph(quad.getGraph()) ;
        if ( g == null )
            throw new JenaException("No such graph: "+quad.getGraph()) ;
        g.add(quad.asTriple()) ;
    }

    @Override
    public void delete(Quad quad)
    {
        Graph g = fetchGraph(quad.getGraph()) ;
        if ( g == null )
            throw new JenaException("No such graph: "+quad.getGraph()) ;
        g.delete(quad.asTriple()) ;
    }
    
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p , Node o)
    {
        return triples2quadsDftGraph(getDefaultGraph().find(s, p, o)) ;
    }
    
    @Override
    protected Iter<Quad> findInSpecificNamedGraph(Node g, Node s, Node p , Node o)
    {
        Graph graph = fetchGraph(g) ;
        if ( graph == null )
            return Iter.nullIter() ;
        return triples2quads(g, graph.find(s, p, o)) ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    {
        Iterator<Node> gnames = listGraphNodes() ;
        Iterator<Quad> iter = null ;
        // Named graphs
        for ( ; gnames.hasNext() ; )  
        {
            Node gn = gnames.next();
            Iterator<Quad> qIter = findInSpecificNamedGraph(gn, s, p, o) ;
            if ( qIter != null )
                // copes with null for iter
                iter = Iter.append(iter, qIter) ;
        }
        return iter ;
    }
    
    @Override
    public abstract Iterator<Node> listGraphNodes() ;

    @Override
    public void clear() {
        // Delete all triples in the default graph 
        getDefaultGraph().clear() ;
        // Now remove the named graphs (but don't clear them - they may be shared).
        Iterator<Node> gnIter = listGraphNodes() ;
        for ( ; gnIter.hasNext(); ) {
            Node gn = gnIter.next() ; 
            removeGraph(gn) ;
        }
    }
    
    protected Graph fetchGraph(Node gn)
    {
        if ( Quad.isDefaultGraph(gn) || Lib.equal(gn,Quad.tripleInQuad)) // Not preferred style
            return getDefaultGraph() ;
        else
            return getGraph(gn) ;
    }
}
