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
import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.LabelExistsException ;
import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

/** A implementation of a Dataset.
 * This is the "usual" implementation based on wrapping a DatasetGraph
 * and providing an adapter layer from Model/Resource to Graph/Node
 * The characteristics of this adpter depend on the charcateristics of
 * DatasetGraph.   
 */

public class DatasetImpl2 implements Dataset /*Temp*/ , DataSource
{
    /* 
     * We are cautious - SPARQL Update can change the graphs in a store
     * so we assume DatasetGraph.getGraph is efficient and
     * here cut the overhead of model wrappers.
     */

    protected DatasetGraph dsg = null ;
    // Cache of graph -> model so that we don't churn model creation.
    private Cache<Graph, Model> cache = CacheFactory.createCache(0.75f, 20) ;
    private Object internalLock = new Object() ;

    protected DatasetImpl2()
    {}
    
//    /** Create an in-memory dataset */
//    public static DataSource createMem()
//    {
//        // This may not be a defaultJena model - during testing, 
//        // we use a graph that is not value-aware for xsd:String vs plain literals.
//        return new DatasetImpl2(ModelFactory.createModelForGraph(GraphFactory.createDefaultGraph())) ;
//    }

    /** Wrap an existing DatasetGraph */
    public static Dataset wrap(DatasetGraph datasetGraph)
    {
        DatasetImpl2 ds = new DatasetImpl2() ;
        ds.dsg = datasetGraph ; 
        return ds ;
    }
    
    /** Clone the structure of a DatasetGraph.
     * The current graph themselves are shared but new naming and new graphs are
     * only in the cloned    
     */
    public static Dataset cloneStructure(DatasetGraph datasetGraph)
    { 
        DatasetImpl2 ds = new DatasetImpl2() ;
        ds.dsg = new DatasetGraphMap(datasetGraph) ;
        return ds ;
    }

    /** Create a Dataset with the model as default model.
     *  Named models must be explicitly added to identify the storage to be used.
     */
    public DatasetImpl2(Model model)
    {
        addToCache(model) ;
        // TODO Is this right? this sort of DatasetGraph can't auto-add graphs.
        this.dsg = DatasetGraphFactory.create(model.getGraph()) ;
    }

    public DatasetImpl2(Dataset ds)
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
    
    @Override public boolean supportsTransactions() { return false ; }
    @Override public void begin(ReadWrite mode)     { throw new UnsupportedOperationException("Transactions not supported") ; }
    @Override public void commit()                  { throw new UnsupportedOperationException("Transactions not supported") ; }
    @Override public void abort()                   { throw new UnsupportedOperationException("Transactions not supported") ; }
  
    @Override
    public DatasetGraph asDatasetGraph() { return dsg ; }

    @Override
    public Model getNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        Node n = Node.createURI(uri) ;
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
        Node n = Node.createURI(uri) ;
        dsg.addGraph(n, model.getGraph()) ;
    }

    @Override
    public void removeNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        Node n = Node.createURI(uri) ;
        // Assumes single writer.
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
    }

    @Override
    public void replaceNamedModel(String uri, Model model)
    {
        // Assumes single writer.
        checkGraphName(uri) ;
        Node n = Node.createURI(uri) ;
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
        addToCache(model) ;
        dsg.addGraph(n, model.getGraph() ) ;
    }

    @Override
    public void setDefaultModel(Model model)
    { 
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
        Node n = Node.createURI(uri) ;
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

    private void removeFromCache(Graph graph)
    {
        // Assume MRSW - no synchronized needed.
        if ( graph == null )
            return ;
        cache.remove(graph) ;
    }

    private void addToCache(Model model)
    {
        // Assume MRSW - no synchronized needed.
        cache.put(model.getGraph(), model) ;
    }

    private Model graph2model(Graph graph)
    { 
        // Called from readers -- outer synchronation needed.
        Model model = cache.get(graph) ;
        if ( model == null )
        {
            model = ModelFactory.createModelForGraph(graph) ;
            cache.put(graph, model) ;
        }
        return model ;
    }
    
    private static void checkGraphName(String uri)
    {
        if ( uri == null )
            throw new ARQException("null for graph name") ; 
    }

}
