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

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory.GraphMaker ;

/** Implementation of a DatasetGraph as an open set of graphs where all graphs "exist".
 *  New graphs are created (via the policy of a GraphMaker) when a getGraph call is 
 *  made to a graph that has not been allocated.
 */
public class DatasetGraphMaker extends DatasetGraphMap
{
    private GraphMaker graphMaker ;

    public DatasetGraphMaker(GraphMaker graphMaker)
    {
        super(graphMaker.create()) ;
        this.graphMaker = graphMaker ;
    }

    public DatasetGraphMaker(Graph graph)
    {
        super(graph) ;
        this.graphMaker = DatasetGraphFactory.graphMakerNull ;
    }

    @Override
    protected Graph getGraphCreate()
    {
        return graphMaker.create() ;
    }
}
