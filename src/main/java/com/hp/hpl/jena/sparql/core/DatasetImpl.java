/*
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

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

/** Wrapper around a DatasetGraph. See also DataSourceImpl. */

public class DatasetImpl implements Dataset
{
    protected DatasetGraph dsg = null ;
    private Object lock = new Object() ;
    
    // A small cache so that calls getDefaultModel()/getNamedModel() are
    // cheap when used repeatedly in code.  This is not an excuse for
    // DatasetGraph not to cache if appropriate for the storage technology.
    private Model defaultModel = null ;
    // read synchronised in this class, not need for a sync wrapper.
    private Cache<String, Model> cache = CacheFactory.createCache(0.75f, 20) ;      

    public DatasetImpl(Model model)
    {
        defaultModel = model ;
        this.dsg = DatasetGraphFactory.create(model.getGraph()) ;
    }
    
    public DatasetImpl(DatasetGraph dsg)
    {
        this.dsg = dsg ;
    }

    /** Return the default model */
    @Override
    public Model getDefaultModel() 
    { 
        synchronized(lock)
        {
            if ( defaultModel == null )
                defaultModel = graph2model(dsg.getDefaultGraph()) ;
            return defaultModel ;
        }
    }

    @Override
    public Lock getLock() { return dsg.getLock() ; }
    
    @Override public boolean supportsTransactions() { return false ; }
    @Override public void begin(ReadWrite mode)     { throw new UnsupportedOperationException("Transactions not supported") ; }
    @Override public void commit()                  { throw new UnsupportedOperationException("Transactions not supported") ; }
    @Override public void abort()                   { throw new UnsupportedOperationException("Transactions not supported") ; }
 
    @Override
    public DatasetGraph asDatasetGraph() { return dsg ; }

    /** Return a model for the named graph - repeated calls so not guarantee to return the same Java object */
    @Override
    public Model getNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        
        // synchronized because we need to read and possible update the cache atomically 
        synchronized(lock)
        {
            Model m = cache.get(uri) ;
            if ( m == null )
            {
                m = graph2model(dsg.getGraph(Node.createURI(uri))) ;
                cache.put(uri, m) ;
            }
            return m ;
        }
    }

    private static void checkGraphName(String uri)
    {
        if ( uri == null )
            throw new ARQException("null for graph name") ; 
    }

    @Override
    public boolean containsNamedModel(String uri)
    {
        checkGraphName(uri) ;

        // Don't look in the cache - just ask the DSG which either caches graphs
        // or asks the storage as needed. The significant case is whether an
        // empty graph is contained in a dataset.  If it's pure quad storage,
        // the answer is usually better as "no"; if it's an in-memory
        // dataset the answer is "yes". 
        return dsg.containsGraph(Node.createURI(uri)) ;
    }

    @Override
    public void close()
    {
        cache = null ;
        dsg.close();
    }

    @Override
    public Iterator<String> listNames()
    { 
        return NodeUtils.nodesToURIs(dsg.listGraphNodes()) ;
    }

    private Model graph2model(Graph graph)
    { 
        return ModelFactory.createModelForGraph(graph) ;
    }
}
