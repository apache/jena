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

package com.hp.hpl.jena.sparql.modify;

import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateException ;

public class GraphStoreUtils
{
    public static void sendToAll(GraphStore graphStore, final Object object)
    {
        actionAll(graphStore, new GraphStoreAction()
        {
            @Override
            public void exec(Graph graph){ graph.getEventManager().notifyEvent(graph, object) ; }
        }) ; 
    }
    
    public static void actionAll(GraphStore graphStore, GraphStoreAction action)
    {
        action.exec(graphStore.getDefaultGraph()) ;
        for ( Iterator<Node> iter = graphStore.listGraphNodes() ; iter.hasNext() ; )
        {
            Node gn = iter.next() ;
            Graph g = graphStore.getGraph(gn) ;
            if ( g == null )
                throw new UpdateException("No such graph: "+gn) ; 
            action.exec(g) ;
        }
    }
    
    // Choose graph and dispatch.
    public static void action(GraphStore graphStore, Node graphName, GraphStoreAction action)
    {
        Graph g = null ;
        if ( graphName != null )
        {
            g = graphStore.getGraph(graphName) ;
            if ( g == null )
                throw new UpdateException("No such graph: "+graphName) ;
        }
        else
            g = graphStore.getDefaultGraph() ;
        action.exec(g) ;
    }

    public static void action(GraphStore graphStore, List<Node> graphNodes, GraphStoreAction action)
    {
        if ( graphNodes.isEmpty() )
        {
            Graph g = graphStore.getDefaultGraph() ;
            action.exec(g) ;
        }
        else
        {
            for (Node gn : graphNodes)
            {
                Graph g = graphStore.getGraph(gn) ;
                if ( g == null )
                    throw new UpdateException("No such graph: "+gn) ; 
                action.exec(g) ;
            }
        }
    }
    

}
