/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.util.Iterator ;

import org.openjena.atlas.lib.Closeable ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.graph.GraphSDB ;
import com.hp.hpl.jena.sdb.util.StoreUtils ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.shared.LockMRSW ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphCaching ;
import com.hp.hpl.jena.sparql.core.DatasetImpl ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;

public class DatasetStoreGraph extends DatasetGraphCaching
    implements DatasetGraph, Closeable, GraphStore 
{
    final Store store ;
    Graph defaultGraph = null ;
    Lock lock = new LockMRSW() ;
    final Context context ;
    
    public DatasetStoreGraph(Store store, Context context)
    {
        this(store, null, context) ;
    }
    
    public DatasetStoreGraph(Store store, GraphSDB graph, Context context)
    {
        this.store = store ;
        // Force the "default" graph
        this.defaultGraph = graph ;
        this.context = context ;
    }
    
    public Store getStore() { return store ; }
    
    public Iterator<Node> listGraphNodes()
    {
        return StoreUtils.storeGraphNames(store) ;
    }

    //---- Update
    public void startRequest()
    {}

    public void finishRequest()
    {}

    public Dataset toDataset()
    { return new DatasetImpl(this) ; }
    
    @Override
    protected boolean _containsGraph(Node graphNode)
    {
        return StoreUtils.containsGraph(store, graphNode) ;
    }

    @Override
    protected Graph _createDefaultGraph()
    {
        return new GraphSDB(store) ;
    }

    @Override
    protected Graph _createNamedGraph(Node graphNode)
    {
        return new GraphSDB(store, graphNode) ;
    }

    // Use unsubtle helper versions (the bulk loader copes with large additions).
    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    { Helper.addToDftGraph(this, s, p, o) ; }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    { Helper.addToNamedGraph(this, g, s, p, o) ; }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    { Helper.deleteFromDftGraph(this, s, p, o) ; }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    { Helper.deleteFromNamedGraph(this, g, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { return Helper.findInDftGraph(this, s, p, o) ; }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    { return Helper.findInAnyNamedGraphs(this, s, p, o) ; } 

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    { return Helper.findInSpecificNamedGraph(this, g, s, p, o) ; }

    @Override
    protected void _close()
    { store.close() ; }

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