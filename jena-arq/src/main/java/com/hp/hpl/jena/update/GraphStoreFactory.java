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
        return create(dataset.asDatasetGraph()) ;
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
