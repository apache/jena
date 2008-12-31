package com.hp.hpl.jena.sdb.modify ;
/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.Lock;

import com.hp.hpl.jena.query.Dataset;

import com.hp.hpl.jena.update.GraphStore;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;

public class GraphStoreSDB implements GraphStore
{
    private Store store ;

    public GraphStoreSDB(Store store) { this.store = store ; }

    public Store getStore() { return store ; }

    
    public void startRequest()
    {}

    public void finishRequest()
    {}

    public Dataset toDataset()
    {
        return SDBFactory.connectDataset(store) ;
    }

    public void addGraph(Node graphName, Graph graph)
    { /* No-op in SDB until there is explicit graph management */ }

    public Graph removeGraph(Node graphName)
    {
        return null ;
    }

    public void setDefaultGraph(Graph g)
    { throw new UnsupportedOperationException("Can't change the default graph in an existing store") ; }

    public boolean containsGraph(Node graphNode)
    {
        return StoreUtils.containsGraph(store, graphNode) ;
    }

    public Graph getDefaultGraph()
    {
        return SDBFactory.connectDefaultGraph(store) ;
    }

    public Graph getGraph(Node graphNode)
    {
        return SDBFactory.connectNamedGraph(store, graphNode) ;
    }

    public Lock getLock()
    {
        throw new UnsupportedOperationException("Locking must be over all stores on this connection") ;
    }

    public Iterator<Node> listGraphNodes()
    {
        return StoreUtils.storeGraphNames(store) ;
    }

    public int size()
    {   //?? "SELECT COUNT(DISTINCT ?g) { GRAPH ?g { ?s ?p ?o}}"
        return -1 ;
    }
    
    public void clear(Node n)
    {
        Graph g ;
        if ( n != null )
            g = SDBFactory.connectNamedGraph(store, n) ;
        else
            g = SDBFactory.connectDefaultGraph(store) ;
        // Delete all triples.
        g.getBulkUpdateHandler().removeAll() ;
    }
    
    public void close()
    { store.close(); }

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