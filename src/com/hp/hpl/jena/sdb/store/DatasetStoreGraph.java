/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.Lock;

import com.hp.hpl.jena.sparql.core.DatasetGraph;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sdb.layout1.StoreRDB;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;


public class DatasetStoreGraph implements DatasetGraph
{
    Store store ;
    Graph graph = null ;       // used for graph level access via standard query engine
    List<String> names = new ArrayList<String>() ;
    
    public DatasetStoreGraph(Store store)
    {
        this.store = store ; 
        this.graph = null ;
    }
    
    public DatasetStoreGraph(Store store, GraphSDB graph)
    {
        this.store = store ; 
        this.graph = graph ;
    }

    /** Use only with existing Jena RDB models */ 
    
    public DatasetStoreGraph(StoreRDB store)
    {
        this.store = store ; 
        this.graph = SDBFactory.connectGraph(store) ;
    }
    
    public Store getStore() { return store ; }
    

    public Iterator listNames()
    {
        return names.iterator() ;
    }

    public Lock getLock()
    {
        throw new SDBNotImplemented("DatasetStore") ;
    }

    public boolean containsNamedGraph(String uri)
    {
        return false ;
    }

    public Graph getDefaultGraph()
    {
        if ( graph == null )
            graph = SDBFactory.connectGraph(store) ;
        return graph ;
    }

    public Graph getNamedGraph(String uri)
    {
        return null ;
    }

    public DatasetGraph asDatasetGraph()
    {
        return null ;
    }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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