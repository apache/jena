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

import java.util.function.Consumer;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Interface for SPARQL operations on a datasets, whether local or remote.
 * Operations can performed via this interface or via the various
 * interfaces for a subset of the operations.
 *
 * <ul>
 * <li>query ({@link SparqlQueryConnection})
 * <li>update ({@link SparqlUpdateConnection})
 * <li>graph store protocol ({@link RDFDatasetConnection}).
 * </ul>
 *
 * For remote operations, the
 * <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL Protocol</a> is used
 * for query and updates and
 * <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL Graph Store
 * Protocol</a> for the graph operations and in addition, there are analogous
 * operations on datasets (fetch, load, put; but not delete).
 *
 * {@code RDFConnection} provides transaction boundaries. If not in a
 * transaction, an implicit transactional wrapper is applied ("autocommit").
 *
 * Remote SPARQL operations are atomic but without additional capabilities from
 * the remote server, multiple operations are not combined into a single
 * transaction.
 *
 * Not all implementations may implement all operations.
 * See the implementation notes for details.
 *
 * @see RDFConnectionFactory
 * @see RDFConnectionLocal
 * @see RDFConnectionRemote
 * @see SparqlQueryConnection
 * @see SparqlUpdateConnection
 * @see RDFDatasetConnection
 */

public interface RDFConnection extends
        SparqlQueryConnection, SparqlUpdateConnection, RDFDatasetConnection,
        Transactional, AutoCloseable
 {
    // Default implementations could be pushed up but then they can't be mentioned here
    // and the javadoc for RDFConnection is not in one place.
    // Inheriting interfaces and re-mentioning gets the javadoc in one place.

    // ---- SparqlQueryConnection
    // Where the argument is a query string, this code avoids simply parsing it and calling
    // the Query object form. This allows RDFConnectionRemote to pass the query string
    // untouched to the connection depending in the internal setting to parse/check
    // queries.
    // Java9 introduces private methods for interfaces which could clear the duplication up by passing in a Creator<QueryExecution>.
    // (Alternatively, add RDFConnectionBase with protected query(String, Query)
    // See RDFConnectionRemote.

    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param queryString
     * @param resultSetAction
     */
    @Override
    public default void queryResultSet(String queryString, Consumer<ResultSet> resultSetAction) {
        executeRead(()->{
            try ( QueryExecution qExec = query(queryString) ) {
                ResultSet rs = qExec.execSelect();
                resultSetAction.accept(rs);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param query
     * @param resultSetAction
     */
    @Override
    public default void queryResultSet(Query query, Consumer<ResultSet> resultSetAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");
        executeRead(()->{
            try ( QueryExecution qExec = query(query) ) {
                ResultSet rs = qExec.execSelect();
                resultSetAction.accept(rs);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param queryString
     * @param rowAction
     */
    @Override
    public default void querySelect(String queryString, Consumer<QuerySolution> rowAction) {
        executeRead(()->{
            try ( QueryExecution qExec = query(queryString) ) {
                qExec.execSelect().forEachRemaining(rowAction);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    public default void querySelect(Query query, Consumer<QuerySolution> rowAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");
        executeRead(()->{
            try ( QueryExecution qExec = query(query) ) {
                qExec.execSelect().forEachRemaining(rowAction);
            }
        } );
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public default Model queryConstruct(String queryString) {
        return
            calculateRead(()->{
                try ( QueryExecution qExec = query(queryString) ) {
                    return qExec.execConstruct();
                }
            } );
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public default Model queryConstruct(Query query) {
        return
            calculateRead(()->{
                try ( QueryExecution qExec = query(query) ) {
                    return qExec.execConstruct();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public default Model queryDescribe(String queryString) {
        return
            calculateRead(()->{
                try ( QueryExecution qExec = query(queryString) ) {
                    return qExec.execDescribe();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public default Model queryDescribe(Query query) {
        return
            calculateRead(()->{
                try ( QueryExecution qExec = query(query) ) {
                    return qExec.execDescribe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(String queryString) {
        return
            calculateRead(()->{
                try ( QueryExecution qExec = query(queryString) ) {
                    return qExec.execAsk();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(Query query) {
        return
            calculateRead(()->{
                try ( QueryExecution qExec = query(query) ) {
                    return qExec.execAsk();
                }
            } );
    }

    /** Setup a SPARQL query execution.
     *
     *  See also {@link #querySelect(Query, Consumer)}, {@link #queryConstruct(Query)},
     *  {@link #queryDescribe(Query)}, {@link #queryAsk(Query)}
     *  for ways to execute queries for of a specific form.
     *
     * @param query
     * @return QueryExecution
     */
    @Override
    public QueryExecution query(Query query);

    /** Setup a SPARQL query execution.
     * This is a low-level operation.
     * Handling the {@link QueryExecution} should be done with try-resource.
     * Some {@link QueryExecution QueryExecutions}, such as ones connecting to a remote server,
     * need to be properly closed to release system resources.
     *
     *  See also {@link #querySelect(String, Consumer)}, {@link #queryConstruct(String)},
     *  {@link #queryDescribe(String)}, {@link #queryAsk(String)}
     *  for ways to execute queries of a specific form.
     *
     * @param queryString
     * @return QueryExecution
     */
    @Override
    public default QueryExecution query(String queryString) {
        return query(QueryFactory.create(queryString));
    }

    // ---- SparqlUpdateConnection

    /** Execute a SPARQL Update.
     *
     * @param update
     */
    @Override
    public default void update(Update update) {
        update(new UpdateRequest(update));
    }

    /** Execute a SPARQL Update.
     *
     * @param update
     */
    @Override
    public void update(UpdateRequest update);

    /** Execute a SPARQL Update.
     *
     * @param updateString
     */
    @Override
    public default void update(String updateString) {
        update(UpdateFactory.create(updateString));
    }

    // ---- RDFDatasetConnection

    /** Fetch a named graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent.
     *
     * @param graphName URI string for the graph name (null or "default" for the default graph)
     * @return Model
     */
    @Override
    public Model fetch(String graphName);

    /** Fetch the default graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent.
     * @return Model
     */
    @Override
    public Model fetch();

    /** Fetch the contents of the dataset */
    @Override
    public Dataset fetchDataset();

    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graphName Graph name (null or "default" for the default graph)
     * @param file File of the data.
     */
    @Override
    public void load(String graphName, String file);

    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param file File of the data.
     */
    @Override
    public void load(String file);

    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graphName Graph name (null or "default" for the default graph)
     * @param model Data.
     */
    @Override
    public void load(String graphName, Model model);

    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param model Data.
     */
    @Override
    public void load(Model model);

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graphName Graph name (null or "default" for the default graph)
     * @param file File of the data.
     */
    @Override
    public void put(String graphName, String file);

    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param file File of the data.
     */
    @Override
    public void put(String file);

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graphName Graph name (null or "default" for the default graph)
     * @param model Data.
     */
    @Override
    public void put(String graphName, Model model);

    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param model Data.
     */
    @Override
    public void put( Model model);

    /**
     * Delete a graph from the dataset.
     * Null or "default" means the default graph, which is cleared, not removed.
     *
     * @param graphName
     */
    @Override
    public void delete(String graphName);

    /**
     * Remove all data from the default graph.
     */
    @Override
    public void delete();

    /* Load (add, append) RDF triple or quad data into a dataset. Triples wil go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    @Override
    public void loadDataset(String file);

    /* Load (add, append) RDF triple or quad data into a dataset. Triples wil go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    @Override
    public void loadDataset(Dataset dataset);

    /* Set RDF triple or quad data as the dataset contents.
     * Triples will go into the default graph, quads in named graphs.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP PUT equivalent to the dataset.
     */
    @Override
    public void putDataset(String file);

    /* Set RDF triple or quad data as the dataset contents.
     * Triples will go into the default graph, quads in named graphs.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP PUT equivalent to the dataset.
     */
    @Override
    public void putDataset(Dataset dataset);

    //    /** Clear the dataset - remove all named graphs, clear the default graph. */
    //    public void clearDataset();

    /** Test whether this connection is closed or not */
    @Override
    public boolean isClosed();

    /** Close this connection.  Use with try-resource. */
    @Override
    public void close();
}

