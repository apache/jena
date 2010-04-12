/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;

/** Implementation of a DatasetGraph as an extensible set of graphs.
 */
public class DatasetGraphMap extends DatasetGraphCollection
{
    private Map<Node, Graph> graphs = new HashMap<Node, Graph>() ;

    private Graph defaultGraph ;

    public DatasetGraphMap(Graph initialDefaultGraph)
    { 
        this.defaultGraph = initialDefaultGraph ;
    }
    
    DatasetGraphMap()
    { }
    
    public DatasetGraphMap(DatasetGraph dsg)
    {
        defaultGraph = dsg.getDefaultGraph() ;
        for ( Iterator<Node> names = dsg.listGraphNodes() ; names.hasNext() ; )
        {
            Node gn = names.next() ;
            dsg.addGraph(gn, dsg.getGraph(gn)) ;
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
        return graphs.get(graphNode) ;
    }

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

/*
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