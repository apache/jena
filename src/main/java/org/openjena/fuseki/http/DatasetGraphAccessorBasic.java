/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.http;


import com.hp.hpl.jena.graph.Graph ;
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
    public void httpPut(Graph data) 
    {  
        putGraph(dataset.getDefaultGraph(), data) ;
    }
    
    @Override
    public void httpPut(Node graphName, Graph data)
    {
        Graph ng = dataset.getGraph(graphName) ;
        if ( ng == null )
            dataset.addGraph(graphName, ng) ;
        else
            putGraph(ng, data) ;
    }

    @Override
    public void httpDelete()
    {
        clearGraph(dataset.getDefaultGraph()) ;
    }
    
    @Override
    public void httpDelete(Node graphName)
    {
        Graph ng = dataset.getGraph(graphName) ;
        if ( ng == null )
            return ;
        dataset.removeGraph(graphName) ;
        //clearGraph(ng) ;
    }

    @Override
    public void httpPost(Graph data)
    {
        mergeGraph(dataset.getDefaultGraph(), data) ;
    }
    
    @Override
    public void httpPost(Node graphName, Graph data)
    {
        Graph ng = dataset.getGraph(graphName) ;
        if ( ng == null )
        {
            dataset.addGraph(graphName, data) ;
            return ;
        }
        mergeGraph(ng, data) ;
    }

    @Override
    public void httpPatch(Graph data) {  httpPost(data) ; }
    
    @Override
    public void httpPatch(Node graphName, Graph data) {  httpPost(graphName, data) ;}

    private void putGraph(Graph destGraph, Graph data)
    {
        clearGraph(destGraph) ;
        mergeGraph(destGraph, data) ; 
    }

    private void clearGraph(Graph graph)
    {
        if ( ! graph.isEmpty() )
            graph.getBulkUpdateHandler().removeAll() ;
    }

    private void mergeGraph(Graph graph, Graph data)
    {
        graph.getBulkUpdateHandler().add(data) ;
    }

}

/*
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