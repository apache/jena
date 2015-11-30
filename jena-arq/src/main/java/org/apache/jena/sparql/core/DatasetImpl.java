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

package org.apache.jena.sparql.core;

import java.util.Iterator ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.shared.Lock ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.NodeUtils ;

/** A implementation of a Dataset.
 * This is the "usual" implementation based on wrapping a DatasetGraph
 * and providing an adapter layer from Model/Resource to Graph/Node
 * The characteristics of this adapter depend on the characteristics of
 * DatasetGraph.   
 */

public class DatasetImpl implements Dataset
{
    protected DatasetGraph dsg = null ;
    private Transactional transactional = null ;
    // Preserve ancient behaviour.
    private Graph seenDftGraph = null ;
    private Model dftModel = null ;

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

    protected DatasetImpl(DatasetGraph dsg)
    {
        this.dsg = dsg ;
        if ( dsg instanceof Transactional )
            this.transactional = (Transactional)dsg ; 
    }
    
    /** Create a Dataset with the model as default model.
     *  Named models must be explicitly added to identify the storage to be used.
     */
    public DatasetImpl(Model model)
    {
        seenDftGraph = model.getGraph() ;
        dftModel = model ;
        this.dsg = DatasetGraphFactory.create(model.getGraph()) ;
        if ( dsg instanceof Transactional )
            this.transactional = (Transactional)dsg ; 
    }

    /** Create a Dataset with a copy of the structure of another one,
     * while sharing the graphs themselves.  
     */
    @SuppressWarnings("deprecation")
    public DatasetImpl(Dataset ds)
    {
        this(DatasetGraphFactory.create(ds.asDatasetGraph())) ;
    }

    @Override
    public Model getDefaultModel() 
    { 
        setDefaultModelSlots() ;
        return dftModel ;
    }

    private synchronized void setDefaultModelSlots() {
        Graph g = dsg.getDefaultGraph() ;
        if ( g != seenDftGraph ) {
            seenDftGraph = g ;
            dftModel = ModelFactory.createModelForGraph(g) ;
        }
    }
    
    private synchronized void clearDefaultModelSlots() {
        seenDftGraph = null ;
        dftModel = null ;
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
        checkTransactional() ;
        transactional.begin(mode) ;
    }
    
    /** Say whether a transaction is active */ 
    @Override
    public boolean isInTransaction()
    {
        checkTransactional() ;
        return transactional.isInTransaction() ;
    }

    @Override
    public void commit()
    {
        checkTransactional() ;
        transactional.commit() ;
    }

    @Override
    public void abort()
    {
        checkTransactional() ;
        transactional.abort() ;
    }

    @Override
    public void end()
    {
        checkTransactional() ;
        transactional.end() ;
        
        seenDftGraph = null ;
        dftModel = null ;
    }

    private void checkTransactional() {
        if ( ! supportsTransactions() )
            throw new UnsupportedOperationException("Transactions not supported") ;
    }
    
    @Override
    public DatasetGraph asDatasetGraph() { return dsg ; }

    @Override
    public Model getNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        return graph2model(dsg.getGraph(n)) ;
    }

    @Override
    public void addNamedModel(String uri, Model model)
    { 
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        dsg.addGraph(n, model.getGraph()) ;
    }

    @Override
    public void removeNamedModel(String uri)
    { 
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        dsg.removeGraph(n) ;
    }

    @Override
    public void replaceNamedModel(String uri, Model model)
    {
        // Assumes single writer.
        checkGraphName(uri) ;
        Node n = NodeFactory.createURI(uri) ;
        dsg.removeGraph(n) ;
        dsg.addGraph(n, model.getGraph() ) ;
    }

    @Override
    public void setDefaultModel(Model model)
    { 
        if ( model == null )
            model = ModelFactory.createDefaultModel() ;
        dsg.setDefaultGraph(model.getGraph()) ;
        clearDefaultModelSlots();
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

    @Override
    public void close()
    {
        dsg.close() ;
        seenDftGraph = null ;
        dftModel = null ;
    }
    
    protected Model graph2model(final Graph graph)
    { 
        return ModelFactory.createModelForGraph(graph) ;
    }
    
    protected static void checkGraphName(String uri)
    {
        if ( uri == null )
            throw new ARQException("null for graph name") ; 
    }
}
