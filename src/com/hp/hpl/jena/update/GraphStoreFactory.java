/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.update;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.modify.GraphStoreBasic ;

/** Operations to create a GraphStore
 */
public class GraphStoreFactory
{
    /** Create an empty GraphStore with an empty default graph (in-memory) */
    public static GraphStore create() { return new GraphStoreBasic(DatasetGraphFactory.createMem()) ; }
    
    /** Create a GraphStore from a Model
     * @param model
     * @return GraphStore
     */
    public static GraphStore create(Model model) { return create(model.getGraph()) ; }

    /** Create a GraphStore from a Graph
     * @param graph
     * @return GraphStore
     */
    public static GraphStore create(Graph graph) { return new GraphStoreBasic(DatasetGraphFactory.create(graph)) ; }

    /** Create a GraphStore from a dataset so that updates apply to the graphs in the dataset.
     *  Throws UpdateException (an ARQException) if the GraphStore can not be created.
     *  This is not the way to get a GraphStore for SDB or TDB - an SDB Store object is a GraphStore
     *  no conversion necessary.
     *  @param dataset
     *  @throws UpdateException
     */
    public static GraphStore create(Dataset dataset)
    { 
        if ( dataset.asDatasetGraph() instanceof GraphStore )
            return (GraphStore)(dataset.asDatasetGraph()) ;
//        if ( ( dataset instanceof DatasetImpl ) || (dataset instanceof DataSourceImpl ) )
//            return new GraphStoreBasic(dataset) ; 
        return new GraphStoreBasic(dataset.asDatasetGraph()) ;
        //throw new UpdateException("Can't create a GraphStore for dataset: "+dataset) ;
    }
    
    /** Create a GraphStore from a dataset (graph-level) so that updates apply to the graphs in the dataset.
     *  @param datasetGraph
     *  @throws UpdateException
     */
    public static GraphStore create(DatasetGraph datasetGraph)
    { 
        if ( datasetGraph instanceof GraphStore )
            return (GraphStore)datasetGraph ;
        return new GraphStoreBasic(datasetGraph) ; 
    }
}

 

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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