/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.migrate;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphWrapper ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Read-only view of a DatasetGraph.  Assumes the datset underneath isn't chnaging
 * (at least, not in its named graph contents)
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

    private Map<Node, Graph> namedGraphs = new HashMap<Node, Graph>() ;
    
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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