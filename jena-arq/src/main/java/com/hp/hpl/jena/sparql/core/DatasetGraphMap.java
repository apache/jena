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

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;

/** Implementation of a DatasetGraph as an extensible set of graphs.
 *  Subclasses need to manage any implicit graph creation.
 */
public class DatasetGraphMap extends DatasetGraphCollection
{
    private Map<Node, Graph> graphs = new HashMap<>() ;

    private Graph defaultGraph ;

    public DatasetGraphMap(Graph defaultGraph)
    { this.defaultGraph = defaultGraph ; }
    
    /** Create a new DatasetGraph that initially shares the graphs of the
     * givem DatasetGraph.  Adding/removing graphs will only affect this
     * object, not the argument DatasetGraph but changed to shared
     * graphs are seenby both objects.
     */
     
    public DatasetGraphMap(DatasetGraph dsg)
    {
        this(dsg.getDefaultGraph()) ;
        for ( Iterator<Node> names = dsg.listGraphNodes() ; names.hasNext() ; )
        {
            Node gn = names.next() ;
            addGraph(gn, dsg.getGraph(gn)) ;
        }
    }

    @Override
    public boolean containsGraph(Node graphNode)
    {
        return graphs.containsKey(graphNode) ;
    }

    @Override
    public Graph getDefaultGraph()
    {
        return defaultGraph ;
    }

    @Override
    public Graph getGraph(Node graphNode)
    {
        Graph g = graphs.get(graphNode) ;
        if ( g == null )
        {
            g = getGraphCreate() ;
            if ( g != null )
                addGraph(graphNode, g) ;
        }
        return g ;
    }

    /** Called from getGraph when a nonexistent graph is asked for.
     * Return null for "nothing created as a graph"
     */
    protected Graph getGraphCreate() { return null ; }
    

    @Override
    public void addGraph(Node graphName, Graph graph)
    { 
        graphs.put(graphName, graph) ;
    }

    @Override
    public void removeGraph(Node graphName)
    {
        graphs.remove(graphName) ;
    }

    @Override
    public void setDefaultGraph(Graph g)
    {
        defaultGraph = g ;
    }

    @Override
    public Iterator<Node> listGraphNodes()
    {
        return graphs.keySet().iterator() ;
    }

    @Override
    public long size()
    {
        return graphs.size() ;
    }

    @Override
    public void close()
    { 
        defaultGraph.close();
        for ( Graph graph : graphs.values() )
            graph.close();
        super.close() ;
    }
}
