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

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.UpdateRequest;

/**
 * Implementation of {@link RDFConnection} where the query, update and graph store
 * operations are given by specific implementations of the respective interfaces.
 */
public class RDFConnectionModular implements RDFConnection {
    
    private final SparqlQueryConnection queryConnection;
    private final SparqlUpdateConnection updateConnection;
    private final RDFDatasetConnection datasetConnection;
    private final Transactional transactional;
    
    @Override public void begin()                       { transactional.begin(); }
    @Override public void begin(TxnType txnType)        { transactional.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { transactional.begin(mode); }
    @Override public boolean promote()                  { return transactional.promote(); }
    @Override public void commit()                      { transactional.commit(); }
    @Override public void abort()                       { transactional.abort(); }
    @Override public boolean isInTransaction()          { return transactional.isInTransaction(); }
    @Override public void end()                         { transactional.end(); }
    @Override public ReadWrite transactionMode()        { return transactional.transactionMode(); }
    @Override public TxnType transactionType()          { return transactional.transactionType(); }
    
    public RDFConnectionModular(SparqlQueryConnection queryConnection ,
                                SparqlUpdateConnection updateConnection ,
                                RDFDatasetConnection datasetConnection ) {
        this.queryConnection = queryConnection;
        this.updateConnection = updateConnection;
        this.datasetConnection = datasetConnection;
        this.transactional = 
            updateConnection  != null ? updateConnection :
            datasetConnection != null ? datasetConnection :
            queryConnection   != null ? queryConnection :
            null;
    }
    
    public RDFConnectionModular(RDFConnection connection) {
        this.queryConnection = connection;
        this.updateConnection = connection;
        this.datasetConnection = connection;
        this.transactional = connection;
    }

    private SparqlQueryConnection queryConnection() {
        if ( queryConnection == null )
            throw new UnsupportedOperationException("No SparqlQueryConnection");
        return queryConnection;
    }
    
    private SparqlUpdateConnection updateConnection() {
        if ( updateConnection == null )
            throw new UnsupportedOperationException("No SparqlUpdateConnection");
        return updateConnection;
    }

    private RDFDatasetConnection datasetConnection() {
        if ( datasetConnection == null )
            throw new UnsupportedOperationException("No RDFDatasetConnection");
        return datasetConnection;
    }

    @Override
    public QueryExecution query(Query query) { return queryConnection().query(query); }

    @Override
    public void update(UpdateRequest update) {
        updateConnection().update(update);
    }

    @Override
    public void load(String graphName, String file) {
        datasetConnection().load(graphName, file);
    }

    @Override
    public void load(String file) {
        datasetConnection().load(file);
    }

    @Override
    public void load(String graphName, Model model) {
        datasetConnection().load(graphName, model);
    }

    @Override
    public void load(Model model) {
        datasetConnection().load(model);
    }

    @Override
    public void put(String graphName, String file) {
        datasetConnection().put(graphName, file);
    }

    @Override
    public void put(String file) {
        datasetConnection().put(file);
    }

    @Override
    public void put(String graphName, Model model) {
        datasetConnection().put(graphName, model);
    }

    @Override
    public void put(Model model) {
        datasetConnection().put(model);
    }

    @Override
    public void delete(String graphName) {
        datasetConnection().delete(graphName);
    }

    @Override
    public void delete() {
        datasetConnection().delete();
    }

    @Override
    public void loadDataset(String file) {
        datasetConnection().loadDataset(file);
    }

    @Override
    public void loadDataset(Dataset dataset) {
        datasetConnection().loadDataset(dataset);
    }

    @Override
    public void putDataset(String file) {
        datasetConnection().putDataset(file);
    }

    @Override
    public void putDataset(Dataset dataset) {
        datasetConnection().putDataset(dataset);
    }

    @Override
    public Model fetch(String graphName) {
        return datasetConnection.fetch(graphName);
    }
    @Override
    public Model fetch() {
        return datasetConnection().fetch();
    }
    @Override
    public Dataset fetchDataset() {
        return datasetConnection().fetchDataset();
    }
    @Override
    public boolean isClosed() { return false; }
    
    /** Close this connection.  Use with try-resource. */ 
    @Override 
    public void close() {
        if ( queryConnection != null )
            queryConnection.close(); 
        if ( updateConnection != null )
            updateConnection.close();
        if ( datasetConnection != null )
            datasetConnection.close();
    }
}

