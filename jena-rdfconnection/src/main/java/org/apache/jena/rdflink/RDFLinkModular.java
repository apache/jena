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

package org.apache.jena.rdflink;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.UpdateRequest;

/**
 * Implementation of {@link RDFLink} where the query, update and graph store
 * operations are given by specific implementations of the respective interfaces.
 */
public class RDFLinkModular implements RDFLink {

    private final LinkSparqlQuery queryConnection;
    private final LinkSparqlUpdate updateConnection;
    private final LinkDatasetGraph datasetConnection;
    private final Transactional transactional;

    @Override public void begin()                       { transactional.begin(); }
    @Override public void begin(TxnType txnType)        { transactional.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { transactional.begin(mode); }
    @Override public boolean promote(Promote promote)   { return transactional.promote(promote); }
    @Override public void commit()                      { transactional.commit(); }
    @Override public void abort()                       { transactional.abort(); }
    @Override public boolean isInTransaction()          { return transactional.isInTransaction(); }
    @Override public void end()                         { transactional.end(); }
    @Override public ReadWrite transactionMode()        { return transactional.transactionMode(); }
    @Override public TxnType transactionType()          { return transactional.transactionType(); }

    public RDFLinkModular(LinkSparqlQuery queryConnection ,
                          LinkSparqlUpdate updateConnection ,
                          LinkDatasetGraph datasetConnection ) {
        this.queryConnection = queryConnection;
        this.updateConnection = updateConnection;
        this.datasetConnection = datasetConnection;
        this.transactional =
            updateConnection  != null ? updateConnection :
            datasetConnection != null ? datasetConnection :
            queryConnection   != null ? queryConnection :
            null;
    }

    private RDFLinkModular(RDFLink connection) {
        this.queryConnection = connection;
        this.updateConnection = connection;
        this.datasetConnection = connection;
        this.transactional = connection;
    }

    // Accessors - not for internal use.
    public LinkSparqlQuery queryLink()     { return queryConnection; }
    public LinkSparqlUpdate updateLink()   { return updateConnection; }
    public LinkDatasetGraph datasetLink()  { return datasetConnection; }

    // For use in a query / update/data operation. Must be non-null.
    private LinkSparqlQuery queryConnection() {
        if ( queryConnection == null )
            throw new UnsupportedOperationException("No LinkSparqlQuery");
        return queryConnection;
    }

    private LinkSparqlUpdate updateConnection() {
        if ( updateConnection == null )
            throw new UnsupportedOperationException("No LinkSparqlUpdate");
        return updateConnection;
    }

    private LinkDatasetGraph datasetConnection() {
        if ( datasetConnection == null )
            throw new UnsupportedOperationException("No LinkDatasetGraph");
        return datasetConnection;
    }

    @Override
    public QueryExec query(Query query) {
        return queryConnection().query(query);
    }

    @Override
    public QueryExecBuilder newQuery() {
        return queryConnection().newQuery();
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        return updateConnection().newUpdate();
    }

    @Override
    public void update(UpdateRequest update) {
        updateConnection().update(update);
    }

    @Override
    public void load(Node graphName, String file) {
        datasetConnection().load(graphName, file);
    }

    @Override
    public void load(String file) {
        datasetConnection().load(file);
    }

    @Override
    public void load(Node graphName, Graph Graph) {
        datasetConnection().load(graphName, Graph);
    }

    @Override
    public void load(Graph Graph) {
        datasetConnection().load(Graph);
    }

    @Override
    public void put(Node graphName, String file) {
        datasetConnection().put(graphName, file);
    }

    @Override
    public void put(String file) {
        datasetConnection().put(file);
    }

    @Override
    public void put(Node graphName, Graph Graph) {
        datasetConnection().put(graphName, Graph);
    }

    @Override
    public void put(Graph Graph) {
        datasetConnection().put(Graph);
    }

    @Override
    public void delete(Node graphName) {
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
    public void loadDataset(DatasetGraph dataset) {
        datasetConnection().loadDataset(dataset);
    }

    @Override
    public void putDataset(String file) {
        datasetConnection().putDataset(file);
    }

    @Override
    public void putDataset(DatasetGraph dataset) {
        datasetConnection().putDataset(dataset);
    }

    @Override
    public Graph get(Node graphName) {
        return datasetConnection.get(graphName);
    }
    @Override
    public Graph get() {
        return datasetConnection().get();
    }
    @Override
    public DatasetGraph getDataset() {
        return datasetConnection().getDataset();
    }

    @Override
    public void clearDataset() {
        datasetConnection().clearDataset();
    }

    @Override
    public boolean isClosed() { return false; }

    /** Close this connection. */
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

