/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * 
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core ;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.util.Context ;

/** DatasetGraph
 * Whether a dataset contains a graph if there are no triples is not defined;
 * see the specifc implementation.
 * Some datasets are "open" - they have all graphs even if no triples,
 * Some datasets are "closed" - fixed set of graphs 
 * */
public interface DatasetGraph extends Closeable
{
    /** Get the default graph as a Jena Graph */
    public Graph getDefaultGraph() ;

    /** Get the graph named by graphNode : returns null on no graph 
     * NB Whether a dataset contains a graph if there are no triples is not defined - see the specifc implementation.
     * Some datasets are "open" - they have all graphs even if no triples,
     * */
    public Graph getGraph(Node graphNode) ;

    public boolean containsGraph(Node graphNode) ;

    public Iterator<Node> listGraphNodes() ;

    /** Add a quad - may throw UnsupportedOperationException */
    public void add(Quad quad) ;
    
    /** Delete a quad - may throw UnsupportedOperationException */
    public void delete(Quad quad) ;
    
    /** Find matching quads in the datset - may include wildcards, Node.ANY or null
     * @see Graph#find(TripleMatch)
     */
    public Iterator<Quad> find(Quad quad) ;
    
    /** Find matching quads in the datset - may include wildcards, Node.ANY or null
     * @see Graph#find(Node,Node,Node)
     */
    public Iterator<Quad> find(Node g, Node s, Node p , Node o) ;
    
    /** Test whether the dataset is empty */
    public boolean isEmpty() ;
    
    /** Test whether the dataset contains a quad - may include wildcards, Node.ANY or null */
    public boolean contains(Node g, Node s, Node p , Node o) ;

    /** Test whether the dataset contains a quad - may include wildcards, Node.ANY or null */
    public boolean contains(Quad quad) ;
    
    /** Return a lock for the dataset to help with concurrency control
     * @see Lock
     */
    public Lock getLock() ;
    
    /** Get the context associated with this object - may be null */
    public Context getContext() ; 
    
    /** Get the size (number of graphs) - may be -1 for unknown */ 
    public int size() ;
    
    /** Close the dataset */
    public void close() ;
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */