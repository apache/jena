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

package org.apache.jena.rdfconnection;

import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphReadOnly;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.sys.Txn;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Implement of {@link RDFConnection} over a {@link Dataset} in the same JVM.
 * <p>
 * Multiple levels of {@link Isolation} are provided, The default {@code COPY} level makes a local
 * {@link RDFConnection} behave like a remote conenction. This should be the normal use in
 * testing.
 * <ul>
 * <li>{@code COPY} &ndash; {@code Model}s and {@code Dataset}s are copied. 
 *     This is most like a remote connection.
 * <li>{@code READONLY} &ndash; Read-only wrappers are added but changes to
 *     the underlying model or dataset will be seen.
 * <li>{@code NONE} (default) &ndash; Changes to the returned {@code Model}s or {@code Dataset}s act on the original object.
 * </ul>
 */

public class RDFConnectionLocal implements RDFConnection {
    private ThreadLocal<Boolean> transactionActive = ThreadLocal.withInitial(()->false);
    
    private Dataset dataset;
    private final Isolation isolation;
    
    public RDFConnectionLocal(Dataset dataset) {
        this(dataset, Isolation.NONE);
    }
    
    public RDFConnectionLocal(Dataset dataset, Isolation isolation) {
        this.dataset = dataset;
        this.isolation = isolation;
    }

    @Override
    public QueryExecution query(Query query) {
        checkOpen();
        // There is no point doing this in a transaction because the QueryExecution is passed out. 
        return QueryExecutionFactory.create(query, dataset);
    }

    @Override
    public void update(UpdateRequest update) {
        checkOpen();
        Txn.executeWrite(dataset, ()->UpdateExecutionFactory.create(update, dataset).execute() ); 
    }

    @Override
    public void load(String graph, String file) {
        checkOpen();
        doPutPost(graph, file, false);
    }

    @Override
    public void load(String file) {
        checkOpen();
        doPutPost(null, file, false);
    }

    @Override
    public void load(String graphName, Model model) {
        checkOpen();
        Txn.executeWrite(dataset, ()-> {
            Model modelDst = modelFor(graphName); 
            modelDst.add(model);
        });
    }

    @Override
    public void load(Model model) { 
        load(null, model);
    }

    /**
     * There may be differences between local and remote behaviour. A local
     * connection may return direct references to a dataset so updates on
     * returned
     */

    @Override
    public Model fetch(String graph) {
        return Txn.calculateRead(dataset, ()-> {
            Model model = modelFor(graph); 
            return isolate(model); 
        });
    }

    @Override
    public Model fetch() {
        checkOpen();
        return fetch(null);
    }

    @Override
    public void put(String file) {
        checkOpen();
        doPutPost(null, file, true);
    }

    @Override
    public void put(String graph, String file) {
        checkOpen();
        doPutPost(graph, file, true);
    }

    @Override
    public void put(Model model) {
        put(null, model); 
    }

    @Override
    public void put(String graphName, Model model) {
        checkOpen();
        Txn.executeWrite(dataset, ()-> {
            Model modelDst = modelFor(graphName); 
            modelDst.removeAll();
            modelDst.add(model);
        });
    }

    @Override
    public void delete(String graph) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            if ( LibRDFConn.isDefault(graph) ) 
                dataset.getDefaultModel().removeAll();
            else 
                dataset.removeNamedModel(graph);
        });
    }

    @Override
    public void delete() {
        checkOpen();
        delete(null);
    }

    private void doPutPost(String graph, String file, boolean replace) {
        Objects.requireNonNull(file);
        Lang lang = RDFLanguages.filenameToLang(file);
        
        Txn.executeWrite(dataset,() ->{
            if ( RDFLanguages.isTriples(lang) ) {
                Model model = LibRDFConn.isDefault(graph) ? dataset.getDefaultModel() : dataset.getNamedModel(graph);
                if ( replace )
                    model.removeAll();
                RDFDataMgr.read(model, file); 
            }
            else if ( RDFLanguages.isQuads(lang) ) {
                if ( replace )
                    dataset.asDatasetGraph().clear(); 
                // Try to POST to the dataset.
                RDFDataMgr.read(dataset, file); 
            }
            else
                throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")");
        });
    }

    /**
     * Called to isolate a model from it's storage.
     * Must be inside a transaction.
     */
    private Model isolate(Model model) {
        switch(isolation) {
            case COPY: {
                // Copy - the model is completely isolated from the original. 
                Model m2 = ModelFactory.createDefaultModel();
                m2.add(model);
                return m2;
            }
            case READONLY : {
                Graph g = new GraphReadOnly(model.getGraph());
                return ModelFactory.createModelForGraph(g);
            }
            case NONE :
                return model;
        }
        throw new InternalErrorException();
    }

    /**
     * Called to isolate a dataset from it's storage.
     * Must be inside a transaction.
     */
    private Dataset isolate(Dataset dataset) {
        switch(isolation) {
            case COPY: {
                DatasetGraph dsg2 = DatasetGraphFactory.create();
                dataset.asDatasetGraph().find().forEachRemaining(q -> dsg2.add(q) );
                return DatasetFactory.wrap(dsg2);
            }
            case READONLY : {
                DatasetGraph dsg = new DatasetGraphReadOnly(dataset.asDatasetGraph());
                return DatasetFactory.wrap(dsg);
            }
            case NONE :
                return dataset;
        }
        throw new InternalErrorException();
    }

    private Model modelFor(String graph) {
        if ( LibRDFConn.isDefault(graph)) 
            return dataset.getDefaultModel();
        return dataset.getNamedModel(graph);
    }

    @Override
    public Dataset fetchDataset() {
        checkOpen();
        return Txn.calculateRead(dataset,() -> isolate(dataset));   
    }

    @Override
    public void loadDataset(String file) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            RDFDataMgr.read(dataset, file);
        });
    }

    @Override
    public void loadDataset(Dataset dataset) {
        Txn.executeWrite(dataset,() ->{
            dataset.asDatasetGraph().find().forEachRemaining((q)->this.dataset.asDatasetGraph().add(q));
        });
    }

    @Override
    public void putDataset(String file) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            dataset.asDatasetGraph().clear();
            RDFDataMgr.read(dataset, file);
        });
    }

    @Override
    public void putDataset(Dataset dataset) {
        Txn.executeWrite(dataset,() ->{
            this.dataset = isolate(dataset);
        });
    }

    @Override
    public void close() {
        dataset = null;
    }
    
    @Override
    public boolean isClosed() {
        return dataset == null;
    }

    private void checkOpen() {
        if ( dataset == null )
            throw new ARQException("closed");
    }

    @Override public void begin()                       { dataset.begin(); }
    @Override public void begin(TxnType txnType)        { dataset.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { dataset.begin(mode); }
    @Override public boolean promote(Promote promote)   { return dataset.promote(promote); }
    @Override public void commit()                      { dataset.commit(); }
    @Override public void abort()                       { dataset.abort(); }
    @Override public boolean isInTransaction()          { return dataset.isInTransaction(); }
    @Override public void end()                         { dataset.end(); }
    @Override public ReadWrite transactionMode()        { return dataset.transactionMode(); }
    @Override public TxnType transactionType()          { return dataset.transactionType(); }
}

