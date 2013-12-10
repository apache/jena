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

package org.apache.jena.web;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** 
 * General implementation of operations for the SPARQL HTTP Update protocol
 * over a DatasetGraph.
 */
public class DatasetGraphAccessorBasic implements DatasetGraphAccessor
{
    private DatasetGraph dataset ;
    
    public DatasetGraphAccessorBasic(DatasetGraph dataset)
    {
        this.dataset = dataset ;
    }
    
    @Override
    public Graph httpGet()                      { return dataset.getDefaultGraph() ; }
    
    @Override
    public Graph httpGet(Node graphName)        { return dataset.getGraph(graphName) ; }

    @Override
    public boolean httpHead()                   { return true ; }

    @Override
    public boolean httpHead(Node graphName)     { return dataset.containsGraph(graphName) ; }

    @Override
    public void httpPut(Graph data) {
        putGraph(dataset.getDefaultGraph(), data) ;
    }

    @Override
    public void httpPut(Node graphName, Graph data) {
        Graph ng = dataset.getGraph(graphName) ;
        if ( ng == null )
            dataset.addGraph(graphName, ng) ;
        else
            putGraph(ng, data) ;
    }

    @Override
    public void httpDelete() {
        clearGraph(dataset.getDefaultGraph()) ;
    }

    @Override
    public void httpDelete(Node graphName) {
        Graph ng = dataset.getGraph(graphName) ;
        if ( ng == null )
            return ;
        dataset.removeGraph(graphName) ;
        // clearGraph(ng) ;
    }

    @Override
    public void httpPost(Graph data) {
        mergeGraph(dataset.getDefaultGraph(), data) ;
    }

    @Override
    public void httpPost(Node graphName, Graph data) {
        Graph ng = dataset.getGraph(graphName) ;
        if ( ng == null ) {
            dataset.addGraph(graphName, data) ;
            return ;
        }
        mergeGraph(ng, data) ;
    }

    @Override
    public void httpPatch(Graph data) {  httpPost(data) ; }
    
    @Override
    public void httpPatch(Node graphName, Graph data) {  httpPost(graphName, data) ;}

    private void putGraph(Graph destGraph, Graph data) {
        clearGraph(destGraph) ;
        mergeGraph(destGraph, data) ;
    }

    private void clearGraph(Graph graph) {
        if ( !graph.isEmpty() )
            graph.clear() ;
    }

    private void mergeGraph(Graph graph, Graph data) {
        GraphUtil.addInto(graph, data) ;
    }

}
