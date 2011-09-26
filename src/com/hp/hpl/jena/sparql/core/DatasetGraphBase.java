/**
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
import java.util.List ;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.shared.LockMRSW ;
import com.hp.hpl.jena.sparql.sse.writers.WriterGraph ;
import com.hp.hpl.jena.sparql.util.Context ;

/** 
 * <p>DatasetGraph framework : readonly dataset need only provide find(g,s,p,o), getGraph() and getDefaultGraph()
 * although it may wish to override other operations and do better.</p>
 * 
 * <p>Other implementations include:</p>
 * <ul>
 * <li>{@link DatasetGraphBase} that adds an implementation of find based on default / named graphs.</li>
 * <li>{@link DatasetGraphCollection} that adds mutating quad operations mapped to a collection of graphs.</li>
 * <li>{@link DatasetGraphQuad} that maps graph operations to a quad view.
 * </ul> 
 */
abstract public class DatasetGraphBase implements DatasetGraph
{
    private final Lock lock = new LockMRSW() ;
    private Context context = new Context() ;
    
    protected DatasetGraphBase() {}
    
    //@Override
    public boolean containsGraph(Node graphNode)
    { return contains(graphNode, Node.ANY, Node.ANY, Node.ANY) ; }
    
    //@Override
    public abstract Graph getDefaultGraph() ;

    //@Override
    public abstract Graph getGraph(Node graphNode) ;

    //@Override
    public void addGraph(Node graphName, Graph graph)
    { throw new UnsupportedOperationException("DatasetGraph.addGraph") ; }

    //@Override
    public void removeGraph(Node graphName)
    { throw new UnsupportedOperationException("DatasetGraph.removeGraph") ; }

    //@Override
    public void setDefaultGraph(Graph g)
    { throw new UnsupportedOperationException("DatasetGraph.setDefaultGraph") ; }
    
    //@Override
    public void add(Quad quad) { throw new UnsupportedOperationException("DatasetGraph.add(Quad)") ; } 
    
    //@Override
    public void delete(Quad quad) { throw new UnsupportedOperationException("DatasetGraph.delete(Quad)") ; }
    
//  //@Override
//  public void add(Node g, Node s, Node p, Node o) { throw new UnsupportedOperationException("DatasetGraph.add(Node, Node, Node, Node)") ; } 
//  
//  //@Override
//  public void delete(Node g, Node s, Node p, Node o) { throw new UnsupportedOperationException("DatasetGraph.delete(Node, Node, Node, Node)") ; }

    //@Override
    public void add(Node g, Node s, Node p, Node o)     { add(new Quad(g,s,p,o)) ; }  
    //@Override
    public void delete(Node g, Node s, Node p, Node o)  { delete(new Quad(g,s,p,o)) ; }
    
    //@Override
    /** Simple implementation */
    public void deleteAny(Node g, Node s, Node p, Node o)
    { 
        Iterator<Quad> iter = find(g, s, p, o) ;
        List<Quad> list = Iter.toList(iter) ;
        for ( Quad q : list )
            delete(q) ;
    }
    
    //@Override
    public Iterator<Quad> find()
    { return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY) ; }

    
    //@Override
    public Iterator<Quad> find(Quad quad)
    { 
        if ( quad == null )
            return find() ;
        return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }
    
    //@Override
    public boolean contains(Quad quad) { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ; }

    //@Override
    public boolean contains(Node g, Node s, Node p , Node o)
    {
        Iterator<Quad> iter = find(g, s, p, o) ;
        boolean b = iter.hasNext() ;
        Iter.close(iter) ;
        return b ;
    }
    
    protected static boolean isWildcard(Node g)
    {
        return g == null || g == Node.ANY ;
    }
    
    //@Override
    public boolean isEmpty()
    {
        return ! contains(Node.ANY, Node.ANY, Node.ANY, Node.ANY) ;
    }

    //@Override
    public long size() { return -1 ; } 
    
    //@Override
    public Lock getLock()
    {
        return lock ;
    }
    
    public Context getContext()
    {
        return context ;
    }
    
    //@Override
    public void close()
    { }
    
    @Override
    public String toString()
    {
        // Using the size of the graphs would be better.
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        WriterGraph.output(out, this, null) ;
        return out.asString() ;
    }

    // Helpers
    
    protected static Iterator<Quad> triples2quadsDftGraph(Iterator<Triple> iter)
    {
        //return triples2quads(Quad.defaultGraphIRI, iter) ;
        return triples2quads(Quad.defaultGraphNodeGenerated, iter) ;
        //return triples2quads(Quad.tripleInQuad, iter) ;
    }

    protected static Iter<Quad> triples2quads(final Node graphNode, Iterator<Triple> iter)
    {
        Transform<Triple, Quad> transformNamedGraph = new Transform<Triple, Quad> () {
            public Quad convert(Triple triple)
            {
                return new Quad(graphNode, triple) ;
            }
        } ;

        return Iter.iter(iter).map(transformNamedGraph) ;
    }

    

}
