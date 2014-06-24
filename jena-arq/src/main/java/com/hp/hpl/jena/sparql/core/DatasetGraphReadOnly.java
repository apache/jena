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
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.graph.GraphReadOnly ;

/** Read-only view of a DatasetGraph.  Assumes the dataset underneath isn't changing.
 */
public class DatasetGraphReadOnly extends DatasetGraphWrapper
{
    public DatasetGraphReadOnly(DatasetGraph dsg) { super(dsg) ; }
    
    private Graph dftGraph = null ;
    
    @Override
    public Graph getDefaultGraph()
    {
        if ( dftGraph == null )
            dftGraph = new GraphReadOnly(super.getDefaultGraph()) ;
        return dftGraph ;
    }

    private Map<Node, Graph> namedGraphs = new HashMap<>() ;
    
    @Override
    public Graph getGraph(Node graphNode)
    {
        if ( namedGraphs.containsKey(graphNode) )
        {
            if ( ! super.containsGraph(graphNode) )
            {
                namedGraphs.remove(graphNode) ;
                return null ;
            }
            return namedGraphs.get(graphNode) ;
        }
        
        Graph g = super.getGraph(graphNode) ;
        if ( g == null ) return null ;
        g = new GraphReadOnly(g) ;
        namedGraphs.put(graphNode, g) ;
        return g ;
    }

    @Override
    public void setDefaultGraph(Graph g)
    { throw new UnsupportedOperationException("read-only dataset") ; }

    @Override
    public void addGraph(Node graphName, Graph graph)
    { throw new UnsupportedOperationException("read-only dataset") ; }

    @Override
    public void removeGraph(Node graphName)
    { throw new UnsupportedOperationException("read-only dataset") ; }

    @Override
    public void add(Quad quad)
    { throw new UnsupportedOperationException("read-only dataset") ; }

    @Override
    public void delete(Quad quad)
    { throw new UnsupportedOperationException("read-only dataset") ; }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o)
    { throw new UnsupportedOperationException("read-only dataset") ; }
}
