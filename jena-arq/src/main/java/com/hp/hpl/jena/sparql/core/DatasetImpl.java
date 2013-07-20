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

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.LabelExistsException ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

/** A implementation of a Dataset.
 * This is the "usual" implementation based on wrapping a DatasetGraph
 * and providing an adapter layer from Model/Resource to Graph/Node
 * The characteristics of this adapter depend on the characteristics of
 * DatasetGraph.   
 */

public class DatasetImpl implements Dataset
{
    /* 
     * We are cautious - SPARQL Update can change the graphs in a store
     * so we assume DatasetGraph.getGraph is efficient and
     * here cut the overhead of model wrappers.
     */

    protected DatasetGraph dsg = null ;
    private Transactional transactional = null ;
    // Cache of graph -> model so that we don't churn model creation.
    private Cache<Graph, Model> cache = createCache() ;
    private Object internalLock = new Object() ;

    //private DatasetImpl() {}
    
    protected DatasetImpl(DatasetGraph dsg)
    {
        this.dsg = dsg ;
        if ( dsg instanceof Transactional )
            this.transactional = (Transactional)dsg ; 
    }
    /** Wrap an existing DatasetGraph */
    public static Dataset wrap(DatasetGraph datasetGraph)
    {
        DatasetImpl ds = new DatasetImpl(datasetGraph) ;
        return ds ;
    }
    
    /** Clone the structure of a DatasetGraph.
     * The current graph themselves are shared but new naming and new graphs are
     * only in the cloned    
     */
    public static Dataset cloneStructure(DatasetGraph datasetGraph)
    { 
        return new DatasetImpl(new DatasetGraphMap(datasetGraph)) ;
    }

    /** Create a Dataset with the model as default model.
     *  Named models must be explicitly added to identify the storage to be used.
     */
    public DatasetImpl(Model model)
    {
        addToCache(model) ;
        this.dsg = DatasetGraphFactory.create(model.getGraph()) ;
    }

    public DatasetImpl(Dataset ds)
    {
        this.dsg = DatasetGraphFactory.create(ds.asDatasetGraph()) ;
    }

    @Override
    public Model getDefaultModel() 
    { 
        synchronized(internalLock)
        {
            return graph2model(dsg.getDefaultGraph()) ;
        }
    }

    @Override
    public Lock getLock() { return dsg.getLock() ; }
    
    @Override
    public Context getContext()
    {
        return dsg.getContext() ;
    }
    @Override
    public boolean supportsTransactions()
    {
        return (transactional != null) ;
    }

    @Override public void begin(ReadWrite mode)     
    {
        if ( transactional == null )
            throw new UnsupportedOperationException("Transactions not supported") ;
        transactional.begin(mode) ;
    }
    
    /** Say whether a transaction is active */ 
    @Override
    public boolean isInTransaction()
    {
        if ( transactional == null )
            throw new UnsupportedOperationException("Transactions not supported") ;
        return transactional.isInTransaction() ;
    }

    @Override
    public void commit()
    {
        if ( transactional == null )
            throw new UnsupportedOperationException("Transactions not supported") ;
        transactional.commit() ;
    }

    @Override
    public void abort()
    {
        if ( transactional == null )
            throw new UnsupportedOperationException("Transactions not supported") ;
        transactional.abort() ;
    }

    @Override
    public void end()
    {
        if ( transactional == null )
            throw new UnsupportedOperationException("Transactions not supported") ;
        transactional.end() ;
    }

    @Override
    public DatasetGraph asDatasetGraph() { return dsg ; }

    @Override
    public Model getNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        synchronized(internalLock)
        {
            Graph g = dsg.getGraph(n) ;
            if ( g == null )
                return null ;
            return graph2model(g) ;
        }
    }

    @Override
    public void addNamedModel(String uri, Model model) throws LabelExistsException
    { 
        checkGraphName(uri) ;
        // Assumes single writer.
        addToCache(model) ;
        Node n = NodeFactory.createURI(uri) ;
        dsg.addGraph(n, model.getGraph()) ;
    }

    @Override
    public void removeNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        // Assumes single writer.
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
    }

    @Override
    public void replaceNamedModel(String uri, Model model)
    {
        // Assumes single writer.
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
        addToCache(model) ;
        dsg.addGraph(n, model.getGraph() ) ;
    }

    @Override
    public void setDefaultModel(Model model)
    { 
        if ( model == null )
            model = ModelFactory.createDefaultModel() ;
        // Assumes single writer.
        removeFromCache(dsg.getDefaultGraph()) ;
        addToCache(model) ;
        dsg.setDefaultGraph(model.getGraph()) ;
    }

    @Override
    public boolean containsNamedModel(String uri)
    { 
        // Does not touch the cache.
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        return dsg.containsGraph(n) ;
    }

    @Override
    public Iterator<String> listNames()
    { 
        return NodeUtils.nodesToURIs(dsg.listGraphNodes()) ;
    }


    //  -------
    //  Cache models wrapping graphs
    // Assumes outser syncrhonization of necessary (multiple readers possible).
    // Assume MRSW (Multiple Reader OR Single Writer)

    @Override
    public void close()
    {
        dsg.close() ;
        cache = null ;
    }

    protected Cache<Graph, Model> createCache() { return CacheFactory.createCache(0.75f, 20) ; }
    
    protected void removeFromCache(Graph graph)
    {
        // Assume MRSW - no synchronized needed.
        if ( graph == null )
            return ;
        cache.remove(graph) ;
    }

    protected void addToCache(Model model)
    {
        // Assume MRSW - no synchronized needed.
        cache.put(model.getGraph(), model) ;
    }

    protected Model graph2model(Graph graph)
    { 
        // Outer synchronization needed.
        Model model = cache.get(graph) ;
        if ( model == null )
        {
            model = ModelFactory.createModelForGraph(graph) ;
            cache.put(graph, model) ;
        }
        return model ;
    }
    
    protected static void checkGraphName(String uri)
    {
        if ( uri == null )
            throw new ARQException("null for graph name") ; 
    }

}
