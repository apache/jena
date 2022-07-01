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

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdfconnection.Isolation;
import org.apache.jena.rdfconnection.JenaConnectionException;
import org.apache.jena.rdfconnection.LibSec;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.system.Txn;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Interface for SPARQL operations on a dataset, whether local or remote.
 * Operations can be performed via this interface or via the various
 * interfaces for a subset of the operations.
 *
 * <ul>
 * <li>query ({@link LinkSparqlQuery})
 * <li>update ({@link LinkSparqlUpdate})
 * <li>graph store protocol ({@link LinkDatasetGraph} and read-only {@link LinkDatasetGraphAccess}).
 * </ul>
 *
 * For remote operations, the
 * <a href="http://www.w3.org/TR/sparql11-protocol/">SPARQL Protocol</a> is used
 * for query and updates and
 * <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL Graph Store
 * Protocol</a> for the graph operations and in addition, there are analogous
 * operations on datasets (fetch, load, put; but not delete).
 *
 * {@code RDFLink} provides transaction boundaries. If not in a
 * transaction, an implicit transactional wrapper is applied ("autocommit").
 *
 * Remote SPARQL operations are atomic but without additional capabilities from
 * the remote server, multiple operations are not combined into a single
 * transaction.
 *
 * Not all implementations may implement all operations.
 * See the implementation notes for details.
 *
 * @see RDFLinkFactory
 * @see RDFLinkDataset
 * @see RDFLinkHTTP
 * @see LinkSparqlQuery
 * @see LinkSparqlUpdate
 * @see LinkDatasetGraph
 * @see RDFConnection
 */

public interface RDFLink extends
        LinkSparqlQuery, LinkSparqlUpdate, LinkDatasetGraph,
        Transactional, AutoCloseable {
    /**
     * Connect to a local (same JVM) dataset.
     * The default isolation is {@code NONE}.
     * See {@link #connect(DatasetGraph, Isolation)} to select an isolation mode.
     *
     * @param dataset
     * @return RDFLink
     * @see RDFLinkDataset
     */
    public static RDFLink connect(DatasetGraph dataset) {
        return RDFLinkDatasetBuilder.newBuilder().dataset(dataset).build();
    }

    /**
     * Connect to a local (same JVM) dataset.
     * <p>
     * Multiple levels of {@link Isolation} are provided, The default {@code COPY} level makes a local
     * {@link RDFLink} behave like a remote connection.
     * See <a href="https://jena.apache.org/documentation/rdfconnection/">the documentation for more details.</a>
     * <ul>
     * <li>{@code COPY} &ndash; {@code Model}s and {@code Dataset}s are copied.
     *     This is most like a remote connection.
     * <li>{@code READONLY} &ndash; Read-only wrappers are added but changes to
     *     the underlying model or dataset will be seen.
     * <li>{@code NONE} (default) &ndash; Changes to the returned {@code Model}s or {@code Dataset}s act on the original object.
     * </ul>
     *
     * @param dataset
     * @param isolation
     * @return RDFConnection
     */
    public static RDFLink connect(DatasetGraph dataset, Isolation isolation) {
        return RDFLinkDatasetBuilder.newBuilder().dataset(dataset).isolation(isolation).build();
    }

    /**
     * Create a connection to a remote location for SPARQL query requests
     *
     * @param queryServiceURL
     * @return RDFConnection
     */
    public static RDFLink queryConnect(String queryServiceURL) {
        return RDFLinkHTTP.newBuilder().queryEndpoint(queryServiceURL).queryOnly().build();
    }

    /** Create a connection to a remote location by URL.
     * This is the URL for the dataset.
     * <p>
     * This is the URL for the dataset.
     * Other names can be specified using {@link RDFLinkHTTP#newBuilder()} and setting the endpoint URLs.
     * </p>
     * <pre>
     * RDFConnectionRemote.newBuilder()
     *       .queryEndpoint(queryServiceEndpoint)
     *       .updateEndpoint(updateServiceEndpoint)
     *       .gspEndpoint(graphStoreProtocolEndpoint)
     *       .build();
     * </pre>
     *
     * @param serviceURL
     * @return RDFConnection
     */
    public static RDFLink connect(String serviceURL) {
        return RDFLinkHTTP.service(serviceURL).build();
    }

    /** Make a remote RDFConnection to the URL, with user and password for the client access using basic auth.
     *  Use with care &ndash; basic auth over plain HTTP reveals the password on the network.
     * @param URL
     * @param user
     * @param password
     * @return RDFConnection
     */
    public static RDFLink connectPW(String URL, String user, String password) {
        Objects.requireNonNull(URL);
        Objects.requireNonNull(user);
        Objects.requireNonNull(password);

        // Authenticator to hold user and password.
        Authenticator authenticator = LibSec.authenticator(user, password);
        HttpClient client = HttpEnv.httpClientBuilder()
                .authenticator(authenticator)
                .build();
        return RDFLinkHTTP.newBuilder()
            .destination(URL)
            .httpClient(client)
            .build();
    }

    // Default implementations could be pushed up but then they can't be mentioned here
    // and the javadoc for RDFLink is not in one place.
    // Inheriting interfaces and re-mentioning gets the javadoc in one place.

    // ---- SparqlQueryConnection
    // Where the argument is a query string, this code avoids simply parsing it and calling
    // the Query object form. This allows RDFLinkRemote to pass the query string
    // untouched to the connection depending in the internal setting to parse/check
    // queries.
    // Java9 introduces private methods for interfaces which could clear the duplication up by passing in a Creator<QueryExecution>.
    // (Alternatively, add RDFLinkBase with protected query(String, Query)
    // See RDFLinkRemote.

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param queryString
     * @param rowSetAction
     */
    @Override
    public default void queryRowSet(String queryString, Consumer<RowSet> rowSetAction) {
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(queryString) ) {
                RowSet rs = qExec.select();
                rowSetAction.accept(rs);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param query
     * @param rowSetAction
     */
    @Override
    public default void queryRowSet(Query query, Consumer<RowSet> rowSetAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(query) ) {
                RowSet rs = qExec.select();
                rowSetAction.accept(rs);
            }
        } );
    }

    private static void forEachRow(RowSet rowSet, Consumer<Binding> rowAction) {
        rowSet.forEachRemaining(rowAction);
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param queryString
     * @param rowAction
     */
    @Override
    public default void querySelect(String queryString, Consumer<Binding> rowAction) {
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(queryString) ) {
                forEachRow(qExec.select(), rowAction);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param query
     * @param rowAction
     */
    @Override
    public default void querySelect(Query query, Consumer<Binding> rowAction) {
        if ( ! query.isSelectType() )
            throw new JenaConnectionException("Query is not a SELECT query");
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(query) ) {
                forEachRow(qExec.select(), rowAction);
            }
        } );
    }

    /** Execute a CONSTRUCT query and return as a Graph */
    @Override
    public default Graph queryConstruct(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString) ) {
                    return qExec.construct();
                }
            } );
    }

    /** Execute a CONSTRUCT query and return as a DatasetGraph */
    //@Override
    public default DatasetGraph queryConstructDataset(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.constructDataset();
                }
            } );
    }

    /** Execute a CONSTRUCT query and return as a Graph */
    //@Override
    public default DatasetGraph queryConstructDataset(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString) ) {
                    return qExec.constructDataset();
                }
            } );
    }

    /** Execute a CONSTRUCT query and return as a Graph */
    @Override
    public default Graph queryConstruct(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.construct();
                }
            } );
    }



    /** Execute a DESCRIBE query and return as a Graph */
    @Override
    public default Graph queryDescribe(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString) ) {
                    return qExec.describe();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Graph */
    @Override
    public default Graph queryDescribe(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.describe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString) ) {
                    return qExec.ask();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public default boolean queryAsk(Query query) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(query) ) {
                    return qExec.ask();
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
    public QueryExec query(Query query);

    /**
     * Setup a SPARQL query execution.
     * <p>
     * This is a low-level operation.
     * Handling the {@link QueryExecution} should be done with try-resource.
     * Some {@link QueryExecution QueryExecutions}, such as ones connecting to a remote server,
     * need to be properly closed to release system resources.
     * <p>
     * See also {@link #querySelect(String, Consumer)}, {@link #queryConstruct(String)},
     * {@link #queryDescribe(String)}, {@link #queryAsk(String)}
     * for ways to execute queries of a specific form.
     *
     * @param queryString
     * @return QueryExecution
     */
    @Override
    public default QueryExec query(String queryString) {
        return query(QueryFactory.create(queryString));
    }

    /**
     * Return a {@link QueryExecBuilder} that is initially configured for this link
     * setup and type. The query built will be set to go to the same dataset/remote
     * endpoint as the other RDFLink operations.
     *
     * @return QueryExecBuilder
     */
    @Override
    public QueryExecBuilder newQuery();

    /**
     * Return a {@link UpdateExecBuilder} that is initially configured for this link
     * setup and type. The update built will be set to go to the same dataset/remote
     * endpoint as the other RDFLink operations.
     *
     * @return UpdateExecBuilder
     */
    @Override
    public UpdateExecBuilder newUpdate();

    // ---- SparqlUpdateConnection

    /** Execute a SPARQL Update.
     *
     * @param update
     */
    @Override
    public default void update(Update update) {
        update(new UpdateRequest(update));
    }

    /**
     * Execute a SPARQL Update.
     * @param update
     */
    @Override
    public void update(UpdateRequest update);

    /**
     * Execute a SPARQL Update.
     * @param updateString
     */
    @Override
    public default void update(String updateString) {
        update(UpdateFactory.create(updateString));
    }

    /** Fetch the default graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent.
     * @return Graph
     */
    @Override
    public Graph get();

    /** Fetch a named graph.
     * This is SPARQL Graph Store Protocol HTTP GET or equivalent.
     *
     * @param graphName URI string for the graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @return Graph
     */
    @Override
    public Graph get(Node graphName);


    /** Send file - this merges the file RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     * <p>
     * If this is a remote connection:
     * <ul>
     * <li> The file is sent as-is and not parsed in the RDFLink
     * <li> The Content-Type is determined by the filename
     * </ul>
     *
     * @param file File of the data.
     */
    @Override
    public void load(String file);

    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     * <p>
     * If this is a remote connection:
     * <ul>
     * <li> The file is sent as-is and not parsed in the RDFLink
     * <li> The Content-Type is determined by the filename
     * </ul>
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param file File of the data.
     */
    @Override
    public void load(Node graphName, String file);

    /** Load (add, append) RDF into the default graph of a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graph Data.
     */
    @Override
    public void load(Graph graph);

    /** Load (add, append) RDF into a named graph in a dataset.
     * This is SPARQL Graph Store Protocol HTTP POST or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param graph Data.
     */
    @Override
    public void load(Node graphName, Graph graph);

    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     * <p>
     * If this is a remote connection:
     * <ul>
     * <li> The file is sent as-is and not parsed in the RDFLink
     * <li> The Content-Type is determined by the filename
     * </ul>
     *
     * @param file File of the data.
     */
    @Override
    public void put(String file);

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param file File of the data.
     */
    @Override
    public void put(Node graphName, String file);

    /** Set the contents of the default graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     * <p>
     * If this is a remote connection:
     * <ul>
     * <li> The file is sent as-is and not parsed in the RDFLink
     * <li> The Content-Type is determined by the filename
     * </ul>
     *
     * @param graph Data.
     */
    @Override
    public void put(Graph graph);

    /** Set the contents of a named graph of a dataset.
     * Any existing data is lost.
     * This is SPARQL Graph Store Protocol HTTP PUT or equivalent.
     *
     * @param graphName Graph name (null or {@link Quad#defaultGraphIRI} for the default graph)
     * @param graph Data.
     */
    @Override
    public void put(Node graphName, Graph graph);

    /**
     * Delete a graph from the dataset.
     * Null or {@link Quad#defaultGraphIRI} means the default graph, which is cleared, not removed.
     *
     * @param graphName
     */
    @Override
    public void delete(Node graphName);

    /**
     * Remove all data from the default graph.
     */
    @Override
    public void delete();

    /* Load (add, append) RDF triple or quad data into a dataset. Triples will go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    @Override
    public void loadDataset(String file);

    /* Load (add, append) RDF triple or quad data into a dataset. Triples will go into the default graph.
     * This is not a SPARQL Graph Store Protocol operation.
     * It is an HTTP POST equivalent to the dataset.
     */
    @Override
    public void loadDataset(DatasetGraph dataset);

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
    public void putDataset(DatasetGraph dataset);

    /** Clear the dataset - remove all named graphs, clear the default graph. */
    @Override
    public void clearDataset();

    /** Test whether this connection is closed or not */
    @Override
    public boolean isClosed();

    /** Whether this RDFLink is to a remote server or not. */
    public default boolean isRemote() { return false; }

    /** Close this connection.  Use with try-resource. */
    @Override
    public void close();
}

