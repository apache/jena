/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import iterator.Iter;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.tdb.store.GraphTDBBase;

public class BulkUpdateHandlerTDB extends SimpleBulkUpdateHandler implements BulkUpdateHandler
{
    GraphTDBBase graphTDB ;
    
    public BulkUpdateHandlerTDB(GraphTDBBase graph)
    {
        super(graph) ;
        this.graphTDB = graph ;
    }

//    @Override
//    public void add(Triple[] triples)
//    {}
//
//    @Override
//    public void add(List triples)
//    { }
//
//    @Override
//    public void add(Iterator it)
//    {}
//
//    @Override
//    public void add(Graph g)
//    {}
//
//    @Override
//    public void add(Graph g, boolean withReifications)
//    {}
//
//    @Override
//    public void delete(Triple[] triples)
//    {}
//
//    @Override
//    public void delete(List triples)
//    {}
//
//    @Override
//    public void delete(Iterator it)
//    {}
//
//    @Override
//    public void delete(Graph g)
//    {}
//
//    @Override
//    public void delete(Graph g, boolean withReifications)
//    {}
//
    @Override
    public void remove(Node s, Node p, Node o)
    {
        // Reliable but slow
        @SuppressWarnings("unchecked")
        Iterator<Triple> iter = graph.find(s, p, o) ;
        List<Triple> x = Iter.toList(iter) ;
        delete(x, false) ;
        manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) );
    }

    @Override
    public void removeAll()
    {
        // Reliable but slow
        // Materialize the triples - then delete them.
        @SuppressWarnings("unchecked")
        Iterator<Triple> iter = graph.find(null, null, null) ;
        List<Triple> x = Iter.toList(iter) ;
        delete(x, false) ;      // No notification on the list delete
        notifyRemoveAll();    
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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