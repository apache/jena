/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.util.Context ;

public class DatasetGraphWrapper implements DatasetGraph
{
    private final DatasetGraph dsg ;
    protected final DatasetGraph getWrapped() { return dsg ; }

    public DatasetGraphWrapper(DatasetGraph dsg)
    {
        this.dsg = dsg ;
    }

    //@Override
    public boolean containsGraph(Node graphNode)
    { return dsg.containsGraph(graphNode) ; }

    //@Override
    public Graph getDefaultGraph()
    { return dsg.getDefaultGraph(); }

    //@Override
    public Graph getGraph(Node graphNode)
    { return dsg.getGraph(graphNode) ; }

    public void addGraph(Node graphName, Graph graph)
    { dsg.addGraph(graphName, graph) ; }

    public void removeGraph(Node graphName)
    { dsg.removeGraph(graphName) ; }

    public void setDefaultGraph(Graph g)
    { dsg.setDefaultGraph(g) ; }

    //@Override
    public Lock getLock()
    { return dsg.getLock() ; }

    //@Override
    public Iterator<Node> listGraphNodes()
    { return dsg.listGraphNodes() ; }

    //@Override
    public void add(Quad quad)
    { dsg.add(quad) ; }

    //@Override
    public void delete(Quad quad)
    { dsg.delete(quad) ; }

    //@Override
    public void add(Node g, Node s, Node p, Node o)
    { dsg.add(g, s, p, o) ; }

    //@Override
    public void delete(Node g, Node s, Node p, Node o)
    { dsg.delete(g, s, p, o) ; }
    
    public void deleteAny(Node g, Node s, Node p, Node o)
    { dsg.deleteAny(g, s, p, o) ; }

    //@Override
    public boolean isEmpty()
    { return dsg.isEmpty() ; }
    
    //@Override
    public Iterator<Quad> find()
    { return dsg.find() ; }

    //@Override
    public Iterator<Quad> find(Quad quad)
    { return dsg.find(quad) ; }

    //@Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    { return dsg.find(g, s, p, o) ; }

    //@Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o)
    { return dsg.findNG(g, s, p, o) ; }

    //@Override
    public boolean contains(Quad quad)
    { return dsg.contains(quad) ; }

    //@Override
    public boolean contains(Node g, Node s, Node p, Node o)
    { return dsg.contains(g, s, p, o) ; }

    //@Override
    public Context getContext()
    { return dsg.getContext() ; }

    //@Override
    public long size()
    { return dsg.size() ; }

    //@Override
    public void close()
    { dsg.close() ; }
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