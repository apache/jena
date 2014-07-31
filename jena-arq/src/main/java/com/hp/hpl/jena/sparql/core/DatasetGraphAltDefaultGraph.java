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

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;

/** DatasetGraph that switches the default graph to another graph.
 *  This different graph can be one of the underlying dataset or a completely
 *  unconnected graph.   
 *  This wrapper should used with care at scale because it masks the
 *  query execution from storage, so generic query execution happens,
 *  not, for example, TDB quad execution.     
 */

public class DatasetGraphAltDefaultGraph extends DatasetGraphCollection {
    private Graph defaultGraph ;
    private final DatasetGraph dsg ;

    public DatasetGraphAltDefaultGraph(DatasetGraph dsg, Graph defaultGraph) {  
        this.defaultGraph = defaultGraph ;
        this.dsg = dsg ;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return dsg.listGraphNodes() ;
    }

    @Override
    public Graph getDefaultGraph() {
        return defaultGraph ;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return dsg.getGraph(graphNode) ;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) { dsg.addGraph(graphName, graph); }

    @Override
    public void removeGraph(Node graphName)           { dsg.removeGraph(graphName); }
}    


