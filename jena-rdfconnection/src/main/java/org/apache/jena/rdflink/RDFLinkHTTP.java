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

import java.net.http.HttpClient;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.JenaConnectionException;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.*;
import org.apache.jena.sparql.exec.http.DSP;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateExecHTTPBuilder;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Implementation of the {@link RDFLink} interface using remote SPARQL operations.
 */
public class RDFLinkHTTP implements RDFLink {
    // Adds a Builder to help with HTTP details.

//    private static final String fusekiDftSrvQuery   = "sparql";
//    private static final String fusekiDftSrvUpdate  = "update";
//    private static final String fusekiDftSrvGSP     = "data";

    private boolean isOpen = true;
    protected final String destination;
    protected final String svcQuery;
    protected final String svcUpdate;
    protected final String svcGraphStore;

    protected final Transactional txnLifecycle;
    protected final HttpClient httpClient;

    // On-the-wire settings.
    protected final RDFFormat outputQuads;
    protected final RDFFormat outputTriples;
    protected final String acceptGraph;
    protected final String acceptDataset;
    protected final String acceptSelectResult;
    protected final String acceptAskResult;
    // All purpose SPARQL results header used if above specific cases do not apply.
    protected final String acceptSparqlResults;

    // Whether to check SPARQL queries given as strings by parsing them.
    protected final boolean parseCheckQueries;
    // Whether to check SPARQL updates given as strings by parsing them.
    protected final boolean parseCheckUpdates;

    /** Create a {@link RDFLinkHTTPBuilder}. */
    public static RDFLinkHTTPBuilder newBuilder() {
        return new RDFLinkHTTPBuilder();
    }

    /** Create a {@link RDFLinkHTTPBuilder}. */
    public static RDFLinkHTTPBuilder service(String destinationURL) {
        return new RDFLinkHTTPBuilder().destination(destinationURL);
    }

    /**
     * Create a {@link RDFLinkHTTPBuilder} initialized with the
     * settings of another {@code RDFLinkRemote}.
     */
    public static RDFLinkHTTPBuilder from(RDFLinkHTTP base) {
        return new RDFLinkHTTPBuilder(base);
    }

    // Used by the builder.
    protected RDFLinkHTTP(Transactional txnLifecycle, HttpClient httpClient, String destination,
                            String queryURL, String updateURL, String gspURL, RDFFormat outputQuads, RDFFormat outputTriples,
                            String acceptDataset, String acceptGraph,
                            String acceptSparqlResults,
                            String acceptSelectResult, String acceptAskResult,
                            boolean parseCheckQueries, boolean parseCheckUpdates) {
        // Any defaults.
        HttpClient hc =  httpClient!=null ? httpClient : HttpEnv.getDftHttpClient();
        if ( txnLifecycle == null )
            txnLifecycle  = TransactionalLock.createMRPlusSW();

        this.httpClient = hc;
        this.destination = destination;
        this.svcQuery = queryURL;
        this.svcUpdate = updateURL;
        this.svcGraphStore = gspURL;
        this.txnLifecycle = txnLifecycle;
        this.outputQuads = outputQuads;
        this.outputTriples = outputTriples;
        this.acceptDataset = acceptDataset;
        this.acceptGraph = acceptGraph;
        this.acceptSparqlResults = acceptSparqlResults;
        this.acceptSelectResult = acceptSelectResult;
        this.acceptAskResult = acceptAskResult;
        this.parseCheckQueries = parseCheckQueries;
        this.parseCheckUpdates = parseCheckUpdates;
    }

    @Override
    public boolean isRemote() { return true; }

    /** Return the {@link HttpClient} in-use. */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /** Return the destination URL for the connection. */
    public String getDestination() {
        return destination;
    }

    public String getQueryEndpoint() {
        return svcQuery;
    }

    public String getUpdateEndpoint() {
        return svcUpdate;
    }

    public String getGraphStoreEndpoint() {
        return svcGraphStore;
    }

    // This class overrides each of these to pass down the query type as well.
    // Then we can derive the accept header if customized without needing to parse
    // the query. This allows an arbitrary string for a query and allows the remote
    // server to have custom syntax extensions or interpretations of comments.

    /**
     * Execute a SELECT query and process the RowSet with the handler code.
     * @param queryString
     * @param rowSetAction
     */
    @Override
    public void queryRowSet(String queryString, Consumer<RowSet> rowSetAction) {
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(queryString, QueryType.SELECT) ) {
                RowSet rs = qExec.select();
                rowSetAction.accept(rs);
            }
        } );
    }

    /**
     * Execute a SELECT query and process the rows of the results with the handler code.
     * @param queryString
     * @param rowAction
     */
    @Override
    public void querySelect(String queryString, Consumer<Binding> rowAction) {
        Txn.executeRead(this, ()->{
            try ( QueryExec qExec = query(queryString, QueryType.SELECT) ) {
                qExec.select().forEachRemaining(rowAction);
            }
        } );
    }

    /** Execute a CONSTRUCT query and return as a Graph */
    @Override
    public Graph queryConstruct(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString, QueryType.CONSTRUCT) ) {
                    return qExec.construct();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Graph */
    @Override
    public Graph queryDescribe(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString, QueryType.DESCRIBE) ) {
                    return qExec.describe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public boolean queryAsk(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExec qExec = query(queryString, QueryType.ASK) ) {
                    return qExec.ask();
                }
            } );
    }

    /**
     * Operation that passed down the query type so the accept header can be set without parsing the query string.
     * @param queryString
     * @param queryType
     * @return QueryExecution
     */
    protected QueryExec query(String queryString, QueryType queryType) {
        Objects.requireNonNull(queryString);
        return queryExec(null, queryString, queryType);
    }

    @Override
    public QueryExec query(String queryString) {
        Objects.requireNonNull(queryString);
        return queryExec(null, queryString, null);
    }

    @Override
    public QueryExec query(Query query) {
        Objects.requireNonNull(query);
        return queryExec(query, null, null);
    }

    @Override
    public QueryExecBuilder newQuery() {
        return createQExecBuilder();
    }

    // Create the QExec

    private QueryExec queryExec(Query query, String queryString, QueryType queryType) {
        checkQuery();
        if ( query == null && queryString == null )
            throw new InternalErrorException("Both query and query string are null");
        if ( query == null ) {
            if ( parseCheckQueries )
                // Don't retain the query.
                QueryFactory.create(queryString);
        }

        // Use the query string as provided if possible, otherwise serialize the query.
        String queryStringToSend = ( queryString != null ) ? queryString : query.toString();
        return createQExec(query, queryStringToSend, queryType);
    }

    // Create the QExec

    /** Create a builder, configured with the link setup. */
    private QueryExecHTTPBuilder createQExecBuilder() {
        return QueryExecHTTPBuilder.create().endpoint(svcQuery).httpClient(httpClient);
    }

    private QueryExec createQExec(Query query, String queryStringToSend, QueryType queryType) {
        QueryExecHTTPBuilder builder = createQExecBuilder().queryString(queryStringToSend);
        QueryType qt = queryType;
        if ( query != null && qt == null )
            qt = query.queryType();
        if ( qt == null )
            qt = QueryType.UNKNOWN;
        // Set the accept header - use the most specific method.
        String requestAcceptHeader = null;
        switch(qt) {
            case SELECT :
                if ( acceptSelectResult != null )
                    requestAcceptHeader = acceptSelectResult;
                break;
            case ASK :
                if ( acceptAskResult != null )
                    requestAcceptHeader = acceptAskResult;
                break;
            case DESCRIBE :
            case CONSTRUCT :
                if ( acceptGraph != null )
                    requestAcceptHeader = acceptGraph;
                break;
            case UNKNOWN:
                // All-purpose content type.
                if ( acceptSparqlResults != null )
                    requestAcceptHeader = acceptSparqlResults;
                else
                    // No idea! Set an "anything" and hope.
                    // (Reasonable chance this is going to end up as HTML though.)
                    requestAcceptHeader = "*/*";
            default :
                break;
        }

        // Make sure it was set somehow.
        if ( requestAcceptHeader == null )
            throw new JenaConnectionException("No Accept header");
        if ( requestAcceptHeader != null )
            builder.acceptHeader(requestAcceptHeader);
        // Delayed creation so QueryExecution.setTimeout works.

        builder.queryString(queryStringToSend);
        return QueryExecApp.create(builder, null, query, queryStringToSend);
    }

    private void acc(StringBuilder sBuff, String acceptString) {
        if ( acceptString == null )
            return;
        if ( sBuff.length() != 0 )
            sBuff.append(", ");
        sBuff.append(acceptString);
    }


    /**
     * Return a {@link UpdateExecBuilder} that is initially configured for this link
     * setup and type. The update built will be set to go to the same dataset/remote
     * endpoint as the other RDFLink operations.
     *
     * @return UpdateExecBuilder
     */
    @Override
    public UpdateExecBuilder newUpdate() {
        return createUExecBuilder();
    }

    /** Create a builder, configured with the link setup. */
    private UpdateExecHTTPBuilder createUExecBuilder() {
        return UpdateExecHTTPBuilder.create().endpoint(svcUpdate).httpClient(httpClient);
    }

    @Override
    public void update(String updateString) {
        Objects.requireNonNull(updateString);
        updateExec(null, updateString);
    }

    @Override
    public void update(UpdateRequest update) {
        Objects.requireNonNull(update);
        updateExec(update, null);
    }

    private void updateExec(UpdateRequest update, String updateString ) {
        checkUpdate();
        if ( update == null && updateString == null )
            throw new InternalErrorException("Both update request and update string are null");
        UpdateRequest actual = null;
        if ( update == null ) {
            if ( parseCheckUpdates )
                actual = UpdateFactory.create(updateString);
        }
        // Use the update string as provided if possible, otherwise serialize the update.
        String updateStringToSend = ( updateString != null ) ? updateString  : update.toString();
        createUExecBuilder()
            .updateString(updateStringToSend)
            .build()
            .execute();
    }

//    /** Convert HTTP status codes to exceptions */
//    static protected void exec(Runnable action)  {
//        try { action.run(); }
//        catch (HttpException ex) { handleHttpException(ex, false); }
//    }
//
//    /** Convert HTTP status codes to exceptions */
//    static protected <X> X exec(Supplier<X> action)  {
//        try { return action.get(); }
//        catch (HttpException ex) { handleHttpException(ex, true); return null;}
//    }
//
//    private static void handleHttpException(HttpException ex, boolean ignore404) {
//        if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 && ignore404 )
//            return ;
//        throw ex;
//    }

    /** {@inheritDoc} */
    @Override
    public Graph get(Node graphName) {
        checkGSP();
        return gsp(graphName).acceptHeader(acceptGraph).GET();
    }

    @Override
    public Graph get() {
        checkGSP();
        return gsp().acceptHeader(acceptGraph).GET();
    }

    // "load" => POST

    // When sending a file (POST or PUT), we are going to send it raw (no syntax
    // checking). The content type comes from the filename, not the link setting.

    @Override
    public void load(String file) {
        checkGSP();
        // Use file extension for the ContentType
        gsp().POST(file);
    }

    @Override
    public void load(Node graphName, String file) {
        checkGSP();
        gsp(graphName).POST(file);
    }

    @Override
    public void load(Graph graph) {
        gsp().contentType(outputTriples).POST(graph);
    }

    @Override
    public void load(Node graphName, Graph graph) {
        gsp(graphName).contentType(outputTriples).POST(graph);
    }

    @Override
    public void put(String file) {
        checkGSP();
        // Use file extension for the ContentType
        gsp().PUT(file);
    }

    @Override
    public void put(Node graphName, String file) {
        checkGSP();
        gsp(graphName).PUT(file);
    }

    @Override
    public void put(Graph graph) {
        checkGSP();
        gsp().contentType(outputTriples).PUT(graph);
    }

    @Override
    public void put(Node graphName, Graph graph) {
        checkGSP();
        gsp(graphName).contentType(outputTriples).PUT(graph);
    }

    // ---- GSP requests
    private String ct(RDFFormat format) { return format.getLang().getHeaderString(); }

    private GSP gsp() {
        return gspRequest().defaultGraph();
    }

    private GSP gsp(Node graphName) {
        if ( LibRDFLink.isDefault(graphName) )
            return gspRequest().defaultGraph();
        else
            return gspRequest().graphName(graphName);
    }

    private GSP gspRequest() {
        return GSP.service(svcGraphStore).httpClient(httpClient);
    }

    private DSP dspRequest() {
        return DSP.service(svcGraphStore).httpClient(httpClient);
    }

    @Override
    public void delete(Node graphName) {
        checkGSP();
        gsp(graphName).DELETE();
    }

    @Override
    public void delete() {
        checkGSP();
        gsp().DELETE();
    }

    @Override
    public DatasetGraph getDataset() {
        checkDataset();
        return dspRequest().acceptHeader(acceptDataset).GET();
    }

    @Override
    public void loadDataset(String file) {
        checkDataset();
        dspRequest().POST(file);
    }

    @Override
    public void loadDataset(DatasetGraph dataset) {
        checkDataset();
        dspRequest().POST(dataset);
    }

    @Override
    public void putDataset(String file) {
        checkDataset();
        dspRequest().PUT(file);
    }

    @Override
    public void putDataset(DatasetGraph dataset) {
        checkDataset();
        dspRequest().PUT(dataset);
    }

    // -- Internal.

    @Override
    public void clearDataset() {
        checkOpen();
        update("CLEAR ALL");
    }

    protected void checkQuery() {
        checkOpen();
        if ( svcQuery == null )
            throw new ARQException("No query service defined for this RDFLink");
    }

    protected void checkUpdate() {
        checkOpen();
        if ( svcUpdate == null )
            throw new ARQException("No update service defined for this RDFLink");
    }

    protected void checkGSP() {
        checkOpen();
        if ( svcGraphStore == null )
            throw new ARQException("No SPARQL Graph Store service defined for this RDFLink");
    }

    protected void checkDataset() {
        checkOpen();
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided");
    }

    protected void checkOpen() {
        if ( ! isOpen )
            throw new ARQException("closed");
    }

    @Override
    public void close() {
        isOpen = false;
    }

    @Override
    public boolean isClosed() {
        return ! isOpen;
    }

    @Override public void begin()                       { txnLifecycle.begin(); }
    @Override public void begin(TxnType txnType)        { txnLifecycle.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txnLifecycle.begin(mode); }
    @Override public boolean promote(Promote promote)   { return txnLifecycle.promote(promote); }
    @Override public void commit()                      { txnLifecycle.commit(); }
    @Override public void abort()                       { txnLifecycle.abort(); }
    @Override public boolean isInTransaction()          { return txnLifecycle.isInTransaction(); }
    @Override public void end()                         { txnLifecycle.end(); }
    @Override public ReadWrite transactionMode()        { return txnLifecycle.transactionMode(); }
    @Override public TxnType transactionType()          { return txnLifecycle.transactionType(); }
}
