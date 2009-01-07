/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateException;

public class GraphStoreUtils
{
    public static void sendToAll(GraphStore graphStore, final Object object)
    {
        actionAll(graphStore, new GraphStoreAction()
        {
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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