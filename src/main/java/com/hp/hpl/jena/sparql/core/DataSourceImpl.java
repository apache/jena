/**
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

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.DataSource ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.LabelExistsException ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

/** A implementation of a DataSource, which is a mutable Dataset,
 *  a set of a single unnamed graph and a number (zero or
 *  more) named graphs with graphs as Models. 
 */

public class DataSourceImpl implements DataSource
{
    /* 
     * synchronization: The ARQ policy is MRSW so read operations here
     * that cause internal datastructure changes need protecting to ensure
     * multiple read access does not corrupt those structures.  
     * Write operations (add/remove models) do not because there
     * should be only one writer by contract.
     */

    protected DatasetGraph dsg = null ;
    private Map<Graph, Model> cache = new HashMap<Graph, Model>() ;      

    protected DataSourceImpl()
    {}
    
    
//    public DataSourceImpl(DataSourceGraph otherDSG)
//    {
//        this.dsg = otherDSG ;
//    }
    
    
    public static DataSource createMem()
    {
        // This may not be a defaultJena model - during testing, 
        // we use a graph that is not value-awar for xsd:String vs plain literals.
 
        return new DataSourceImpl(ModelFactory.createModelForGraph(GraphFactory.createDefaultGraph())) ;
    }
    
    public static DataSource wrap(DatasetGraph datasetGraph)
    {
        DataSourceImpl ds = new DataSourceImpl() ;
        ds.dsg = datasetGraph ; 
        return ds ;
    }
    public static DataSource cloneStructure(DatasetGraph datasetGraph)
    { 
        DataSourceImpl ds = new DataSourceImpl() ;
        ds.dsg = new DatasetGraphMap(datasetGraph) ;
        return ds ;
    }
    
//    public DataSourceImpl(DatasetGraph datasetGraph)
//    { 
//        dsg = new DatasetGraphMap(datasetGraph) ;
//    }
    
    public DataSourceImpl(Model model)
    {
        addToCache(model) ;
        // TODO Is this right? this sort of DatasetGraph can't auto-add graphs.
        this.dsg = DatasetGraphFactory.create(model.getGraph()) ;
    }

    public DataSourceImpl(Dataset ds)
    {
        this.dsg = DatasetGraphFactory.create(ds.asDatasetGraph()) ;
    }

    //  Does it matter if this is not the same model each time?
    @Override
    public Model getDefaultModel() 
    { 
        return graph2model(dsg.getDefaultGraph()) ;
    }

    @Override
    public Lock getLock() { return dsg.getLock() ; }

    @Override
    public DatasetGraph asDatasetGraph() { return dsg ; }

    @Override
    public Model getNamedModel(String uri)
    { 
        Node n = Node.createURI(uri) ;
        Graph g = dsg.getGraph(n) ;
        if ( g == null )
            return null ;
        return graph2model(g) ;
    }

    @Override
    public void addNamedModel(String uri, Model model) throws LabelExistsException
    { 
        addToCache(model) ;
        Node n = Node.createURI(uri) ;
        dsg.addGraph(n, model.getGraph()) ;
    }

    @Override
    public void removeNamedModel(String uri)
    { 
        Node n = Node.createURI(uri) ;
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
    }

    @Override
    public void replaceNamedModel(String uri, Model model)
    {
        Node n = Node.createURI(uri) ;
        removeFromCache(dsg.getGraph(n)) ;
        dsg.removeGraph(n) ;
        addToCache(model) ;
        dsg.addGraph(n, model.getGraph() ) ;
    }

    @Override
    public void setDefaultModel(Model model)
    { 
        removeFromCache(dsg.getDefaultGraph()) ;
        addToCache(model) ;
        dsg.setDefaultGraph(model.getGraph()) ;
    }

    @Override
    public boolean containsNamedModel(String uri)
    { 
        Node n = Node.createURI(uri) ;
        return dsg.containsGraph(n) ;
    }

    // Don't look in the cache - go direct to source
    @Override
    public Iterator<String> listNames()
    { 
        return NodeUtils.nodesToURIs(dsg.listGraphNodes()) ;
    }


//  -------
//  Cache models wrapping graph

    @Override
    public void close()
    {
        dsg.close() ;
        cache = null ;
    }

    synchronized
    private void removeFromCache(Graph graph)
    {
        if ( graph == null )
            return ;
        cache.remove(graph) ;
    }

    synchronized
    private void addToCache(Model model)
    {
        cache.put(model.getGraph(), model) ;
    }

    synchronized
    private Model graph2model(Graph graph)
    { 
        Model model = cache.get(graph) ;
        if ( model == null )
        {
            model = ModelFactory.createModelForGraph(graph) ;
            cache.put(graph, model) ;
        }
        return model ;
    }
}
