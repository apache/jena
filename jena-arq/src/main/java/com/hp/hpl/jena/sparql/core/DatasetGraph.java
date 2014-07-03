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

package com.hp.hpl.jena.sparql.core ;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Closeable ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.util.Context ;

/** DatasetGraph: The graph representation of an RDF Dataset. See {@link Dataset}
 * for the Model level of an RDF dataset.  
 * <p>
 * Whether a dataset contains a graph if there are no triples
 * is not defined; see the specific implementation.
 */

public interface DatasetGraph extends Closeable
{
    // ---- Graph container view

    /** Get the default graph as a Jena Graph */
    public Graph getDefaultGraph() ;

    /** Get the graph named by graphNode : returns null on no graph 
     * NB Whether a dataset contains a graph if there are no triples is not defined - see the specifc implementation.
     * Some datasets are "open" - they have all graphs even if no triples,
     * */
    public Graph getGraph(Node graphNode) ;

    public boolean containsGraph(Node graphNode) ;

    /** Set the default graph.  Set the active graph if it was null.
     *  This replaces the contents default graph, not merge data into it.
     *  Do not assume that the same object is returned by {@link #getDefaultGraph} 
     */
    public void setDefaultGraph(Graph g) ;

    /** 
     * Add the given graph to the dataset.
     * <em>Replaces</em> any existing data for the named graph; to add data, 
     * get the graph and add triples to it, or add quads to the dataset.
     * Do not assume that the same Java object is returned by {@link #getGraph}  
     */

    public void addGraph(Node graphName, Graph graph) ;

    /** Remove all data associated with the named graph */
    public void removeGraph(Node graphName) ;

    /** Iterate over all names of named graphs */
    public Iterator<Node> listGraphNodes() ;

    // ---- Quad view
    
    /** Add a quad */
    public void add(Quad quad) ;
    
    /** Delete a quad */
    public void delete(Quad quad) ;
    
    /** Add a quad */
    public void add(Node g, Node s, Node p, Node o) ;

    /** Delete a quad */
    public void delete(Node g, Node s, Node p, Node o) ;
    
    /** Delete any quads matching the pattern */
    public void deleteAny(Node g, Node s, Node p, Node o) ;

    /** Iterate over all quads in the dataset graph */
    public Iterator<Quad> find() ;
    
    /** Find matching quads in the dataset - may include wildcards, Node.ANY or null
     * @see Graph#find(TripleMatch)
     */
    public Iterator<Quad> find(Quad quad) ;
    
    /** Find matching quads in the dataset (including default graph) - may include wildcards, Node.ANY or null
     * @see Graph#find(Node,Node,Node)
     */
    public Iterator<Quad> find(Node g, Node s, Node p , Node o) ;
    
    /** Find matching quads in the dataset in named graphs only - may include wildcards, Node.ANY or null
     * @see Graph#find(Node,Node,Node)
     */
    public Iterator<Quad> findNG(Node g, Node s, Node p , Node o) ;

    /** Test whether the dataset  (including default graph) contains a quad - may include wildcards, Node.ANY or null */
    public boolean contains(Node g, Node s, Node p , Node o) ;

    /** Test whether the dataset contains a quad  (including default graph)- may include wildcards, Node.ANY or null */
    public boolean contains(Quad quad) ;

    /** Remove everything - remove all named graphs, clear the default graph */
    public void clear() ;

    /** Test whether the dataset is empty */
    public boolean isEmpty() ;
    
    /** Return a lock for the dataset to help with concurrency control
     * @see Lock
     */
    public Lock getLock() ;
    
    /** Get the context associated with this object - may be null */
    public Context getContext() ; 
    
    /** Get the size (number of named graphs) - may be -1 for unknown */ 
    public long size() ;
    
    /** Close the dataset */
    @Override
    public void close() ;
}
