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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;
import org.apache.jena.riot.web.HttpCaptureResponse;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpOp.CaptureInput;
import org.apache.jena.riot.web.HttpResponseLib;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.web.HttpSC;

/**
 * Implementation of the {@link RDFConnection} interface using remote SPARQL operations.
 */
public class RDFConnectionRemote implements RDFConnection {
    // Adds a Builder to help with HTTP details.

    private static final String fusekiDftSrvQuery   = "sparql";
    private static final String fusekiDftSrvUpdate  = "update";
    private static final String fusekiDftSrvGSP     = "data";

    private boolean isOpen = true;
    protected final String destination;
    protected final String svcQuery;
    protected final String svcUpdate;
    protected final String svcGraphStore;

    protected final Transactional txnLifecycle;
    protected final HttpClient httpClient;
    protected final HttpContext httpContext;

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

    /** Create a {@link RDFConnectionRemoteBuilder}. */
    public static RDFConnectionRemoteBuilder create() {
        return new RDFConnectionRemoteBuilder();
    }

    /**
     * Create a {@link RDFConnectionRemoteBuilder} initialized with the
     * settings of another {@code RDFConnectionRemote}.
     */
    public static RDFConnectionRemoteBuilder create(RDFConnectionRemote base) {
        return new RDFConnectionRemoteBuilder(base);
    }

    // Used by the builder.
    protected RDFConnectionRemote(Transactional txnLifecycle, HttpClient httpClient, HttpContext httpContext, String destination,
                                   String queryURL, String updateURL, String gspURL, RDFFormat outputQuads, RDFFormat outputTriples,
                                   String acceptDataset, String acceptGraph,
                                   String acceptSparqlResults,
                                   String acceptSelectResult, String acceptAskResult,
                                   boolean parseCheckQueries, boolean parseCheckUpdates) {
        this.httpClient = httpClient;
        this.httpContext = httpContext;
        this.destination = destination;
        this.svcQuery = queryURL;
        this.svcUpdate = updateURL;
        this.svcGraphStore = gspURL;
        if ( txnLifecycle == null )
            txnLifecycle  = TransactionalLock.createMRPlusSW();
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

    /** Return the {@link HttpClient} in-use. */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /** Return the {@link HttpContext} in-use. */
    public HttpContext getHttpContext() {
        return httpContext;
    }

    /** Return the destination URL for the connection. */
    public String getDestination() {
        return destination;
    }

    // For custom content negotiation.

    // This class overrides each of these to pass down the query type as well.
    // Then we can derive the accept header if customized without needing to parse
    // the query. This allows an arbitrary string for a query and allows the remote
    // server to have custom syntax extensions or interpretations of comments.

    /**
     * Execute a SELECT query and process the ResultSet with the handler code.
     * @param queryString
     * @param resultSetAction
     */
    @Override
    public void queryResultSet(String queryString, Consumer<ResultSet> resultSetAction) {
        Txn.executeRead(this, ()->{
            try ( QueryExecution qExec = query(queryString, QueryType.SELECT) ) {
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
    public void querySelect(String queryString, Consumer<QuerySolution> rowAction) {
        Txn.executeRead(this, ()->{
            try ( QueryExecution qExec = query(queryString, QueryType.SELECT) ) {
                qExec.execSelect().forEachRemaining(rowAction);
            }
        } );
    }

    /** Execute a CONSTRUCT query and return as a Model */
    @Override
    public Model queryConstruct(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExecution qExec = query(queryString, QueryType.CONSTRUCT) ) {
                    return qExec.execConstruct();
                }
            } );
    }

    /** Execute a DESCRIBE query and return as a Model */
    @Override
    public Model queryDescribe(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExecution qExec = query(queryString, QueryType.DESCRIBE) ) {
                    return qExec.execDescribe();
                }
            } );
    }

    /** Execute a ASK query and return a boolean */
    @Override
    public boolean queryAsk(String queryString) {
        return
            Txn.calculateRead(this, ()->{
                try ( QueryExecution qExec = query(queryString, QueryType.ASK) ) {
                    return qExec.execAsk();
                }
            } );
    }

    /**
     * Operation that passed down the query type so the accept header can be set without parsing the query string.
     * @param queryString
     * @param queryType
     * @return QueryExecution
     */
    protected QueryExecution query(String queryString, QueryType queryType) {
        Objects.requireNonNull(queryString);
        return queryExec(null, queryString, queryType);
    }

    @Override
    public QueryExecution query(String queryString) {
        Objects.requireNonNull(queryString);
        return queryExec(null, queryString, null);
    }

    @Override
    public QueryExecution query(Query query) {
        Objects.requireNonNull(query);
        return queryExec(query, null, null);
    }

    private QueryExecution queryExec(Query query, String queryString, QueryType queryType) {
        checkQuery();
        if ( query == null && queryString == null )
            throw new InternalErrorException("Both query and query string are null");
        if ( query == null ) {
            if ( parseCheckQueries )
                QueryFactory.create(queryString);
        }

        // Use the query string as provided if possible, otherwise serialize the query.
        String queryStringToSend = ( queryString != null ) ? queryString : query.toString();
        return exec(()-> createQueryExecution(query, queryStringToSend, queryType));
    }

    // Create the QueryExecution
    private QueryExecution createQueryExecution(Query query, String queryStringToSend, QueryType queryType) {
        QueryExecution qExec = new QueryEngineHTTP(svcQuery, queryStringToSend, httpClient, httpContext);
        QueryEngineHTTP qEngine = (QueryEngineHTTP)qExec;
        QueryType qt = queryType;
        if ( query != null && qt == null )
            qt = query.queryType();
        if ( qt == null )
            qt = QueryType.UNKNOWN;
        // Set the accept header - use the most specific method.
        switch(qt) {
            case SELECT :
                if ( acceptSelectResult != null )
                    qEngine.setAcceptHeader(acceptSelectResult);
                break;
            case ASK :
                if ( acceptAskResult != null )
                    qEngine.setAcceptHeader(acceptAskResult);
                break;
            case DESCRIBE :
            case CONSTRUCT :
                if ( acceptGraph != null )
                    qEngine.setAcceptHeader(acceptGraph);
                break;
            case UNKNOWN:
                // All-purpose content type.
                if ( acceptSparqlResults != null )
                    qEngine.setAcceptHeader(acceptSparqlResults);
                else
                    // No idea! Set an "anything" and hope.
                    // (Reasonable chance this is going to end up as HTML though.)
                    qEngine.setAcceptHeader("*/*");
            default :
                break;
        }
        // Make sure it was set somehow.
        if ( qEngine.getAcceptHeader() == null )
            throw new JenaConnectionException("No Accept header");
        return qExec ;
    }

    private void acc(StringBuilder sBuff, String acceptString) {
        if ( acceptString == null )
            return;
        if ( sBuff.length() != 0 )
            sBuff.append(", ");
        sBuff.append(acceptString);
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
        if ( update == null ) {
            if ( parseCheckUpdates )
                UpdateFactory.create(updateString);
        }
        // Use the update string as provided if possible, otherwise serialize the update.
        String updateStringToSend = ( updateString != null ) ? updateString  : update.toString();
        exec(()->HttpOp.execHttpPost(svcUpdate, WebContent.contentTypeSPARQLUpdate, updateStringToSend, this.httpClient, this.httpContext));
    }

    @Override
    public Model fetch(String graphName) {
        checkGSP();
        String url = LibRDFConn.urlForGraph(svcGraphStore, graphName);
        Graph graph = fetch$(url);
        return ModelFactory.createModelForGraph(graph);
    }

    @Override
    public Model fetch() {
        checkGSP();
        return fetch(null);
    }

    private Graph fetch$(String url) {
        HttpCaptureResponse<Graph> graph = HttpResponseLib.graphHandler();
        exec(()->HttpOp.execHttpGet(url, acceptGraph, graph, this.httpClient, this.httpContext));
        return graph.get();
    }

    @Override
    public void load(String graph, String file) {
        checkGSP();
        upload(graph, file, false);
    }

    @Override
    public void load(String file) {
        checkGSP();
        upload(null, file, false);
    }

    @Override
    public void load(Model model) {
        doPutPost(model, null, false);
    }

    @Override
    public void load(String graphName, Model model) {
        doPutPost(model, graphName, false);
    }

    @Override
    public void put(String graph, String file) {
        checkGSP();
        upload(graph, file, true);
    }

    @Override
    public void put(String file) {
        checkGSP();
        upload(null, file, true);
    }

    @Override
    public void put(String graphName, Model model) {
        checkGSP();
        doPutPost(model, graphName, true);
    }

    @Override
    public void put(Model model) {
        checkGSP();
        doPutPost(model, null, true);
    }

    /** Send a file to named graph (or "default" or null for the default graph).
     * <p>
     * The Content-Type is inferred from the file extension.
     * <p>
     * "Replace" means overwrite existing data, othewise the date is added to the target.
     */
    protected void upload(String graph, String file, boolean replace) {
        // if triples
        Lang lang = RDFLanguages.filenameToLang(file);
        if ( RDFLanguages.isQuads(lang) )
            throw new ARQException("Can't load quads into a graph");
        if ( ! RDFLanguages.isTriples(lang) )
            throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")");
        String url = LibRDFConn.urlForGraph(svcGraphStore, graph);
        doPutPost(url, file, lang, replace);
    }

    /** Send a file to named graph (or "default" or null for the defaultl graph).
     * <p>
     * The Content-Type is taken from the given {@code Lang}.
     * <p>
     * "Replace" means overwrite existing data, otherwise the date is added to the target.
     */
    protected void doPutPost(String url, String file, Lang lang, boolean replace) {
        File f = new File(file);
        long length = f.length();

        // Leave RDF/XML to the XML parse, else it's UTF-8.
        String charset = (lang.equals(Lang.RDFXML) ? null : WebContent.charsetUTF8);
        // HttpClient Content type.
        ContentType ct = ContentType.create(lang.getContentType().getContentType(), charset);

        exec(()->{
            HttpEntity entity = fileToHttpEntity(file, lang);
            if ( replace )
                HttpOp.execHttpPut(url, entity, httpClient, httpContext);
            else
                HttpOp.execHttpPost(url, entity, httpClient, httpContext);
        });

        // This is non-repeatable so does not work with authentication.
//        InputStream source = IO.openFile(file);
//        exec(()->{
//            HttpOp.execHttpPost(url, null);
//
//            if ( replace )
//                HttpOp.execHttpPut(url, lang.getContentType().getContentType(), source, length, httpClient, this.httpContext);
//            else
//                HttpOp.execHttpPost(url, lang.getContentType().getContentType(), source, length, null, null, httpClient, this.httpContext);
//        });
    }

    /** Send a model to named graph (or "default" or null for the defaultl graph).
     * <p>
     * The Content-Type is taken from the given {@code Lang}.
     * <p>
     * "Replace" means overwrite existing data, othewise the date is added to the target.
     */
    protected void doPutPost(Model model, String name, boolean replace) {
        String url = LibRDFConn.urlForGraph(svcGraphStore, name);
        exec(()->{
            Graph graph = model.getGraph();
            if ( replace )
                HttpOp.execHttpPut(url, graphToHttpEntity(graph), httpClient, httpContext);
            else
                HttpOp.execHttpPost(url, graphToHttpEntity(graph), null, null, httpClient, httpContext);
        });
    }

    @Override
    public void delete(String graph) {
        checkGSP();
        String url = LibRDFConn.urlForGraph(svcGraphStore, graph);
        exec(()->HttpOp.execHttpDelete(url));
    }

    @Override
    public void delete() {
        checkGSP();
        delete(null);
    }

    @Override
    public Dataset fetchDataset() {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided");
        Dataset ds = DatasetFactory.createTxnMem();
        Txn.executeWrite(ds, ()->{
            HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
            exec(()->HttpOp.execHttpGet(destination, acceptDataset, handler, this.httpClient, this.httpContext));
            TypedInputStream s = handler.get();
            Lang lang = RDFLanguages.contentTypeToLang(s.getContentType());
            RDFDataMgr.read(ds, s, lang);
        });
        return ds;
    }

    @Override
    public void loadDataset(String file) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided");
        doPutPostDataset(file, false);
    }

    @Override
    public void loadDataset(Dataset dataset) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided");
        doPutPostDataset(dataset, false);
    }

    @Override
    public void putDataset(String file) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided");
        doPutPostDataset(file, true);
    }

    @Override
    public void putDataset(Dataset dataset) {
        if ( destination == null )
            throw new ARQException("Dataset operations not available - no dataset URL provided");
        doPutPostDataset(dataset, true);
    }

    /** Do a PUT or POST to a dataset, sending the contents of the file.
     * <p>
     * The Content-Type is inferred from the file extension.
     * <p>
     * "Replace" implies PUT, otherwise a POST is used.
     */
    protected void doPutPostDataset(String file, boolean replace) {
        Lang lang = RDFLanguages.filenameToLang(file);
        File f = new File(file);
        long length = f.length();
        exec(()->{
            HttpEntity entity = fileToHttpEntity(file, lang);
            if ( replace )
                HttpOp.execHttpPut(destination, entity, httpClient, httpContext);
            else
                HttpOp.execHttpPost(destination, entity, httpClient, httpContext);
        });
    }

    /** Do a PUT or POST to a dataset, sending the contents of a dataset.
     * The Content-Type is {@code application/n-quads}.
     * <p>
     * "Replace" implies PUT, otherwise a POST is used.
     */
    protected void doPutPostDataset(Dataset dataset, boolean replace) {
        exec(()->{
            DatasetGraph dsg = dataset.asDatasetGraph();
            if ( replace )
                HttpOp.execHttpPut(destination, datasetToHttpEntity(dsg), httpClient, null);
            else
                HttpOp.execHttpPost(destination, datasetToHttpEntity(dsg), httpClient, null);
        });
    }

    protected void checkQuery() {
        checkOpen();
        if ( svcQuery == null )
            throw new ARQException("No query service defined for this RDFConnection");
    }

    protected void checkUpdate() {
        checkOpen();
        if ( svcUpdate == null )
            throw new ARQException("No update service defined for this RDFConnection");
    }

    protected void checkGSP() {
        checkOpen();
        if ( svcGraphStore == null )
            throw new ARQException("No SPARQL Graph Store service defined for this RDFConnection");
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

    protected HttpEntity fileToHttpEntity(String filename, Lang lang) {
        // Leave RDF/XML to the XML parse, else it's UTF-8.
        String charset = (lang.equals(Lang.RDFXML) ? null : WebContent.charsetUTF8);
        // HttpClient Content type.
        ContentType ct = ContentType.create(lang.getContentType().getContentType(), charset);
        // Repeatable.
        return new FileEntity(new File(filename), ct);
    }

    /** Create an HttpEntity for the graph */
    protected HttpEntity graphToHttpEntity(Graph graph) {
        return graphToHttpEntity(graph, outputTriples);
    }

    /** Create an HttpEntity for the graph. */
    protected HttpEntity graphToHttpEntity(Graph graph, RDFFormat syntax) {
        // Length - leaves connection reusable.
        return graphToHttpEntityWithLength(graph, syntax);
    }

    /**
     * Create an HttpEntity for the graph. The HTTP entity will have the length but this
     * requires serialising the graph at the point when this function is called.
     */
    private HttpEntity graphToHttpEntityWithLength(Graph graph, RDFFormat syntax) {
        String ct = syntax.getLang().getContentType().getContentType();
        ByteArrayOutputStream out = new ByteArrayOutputStream(128*1024);
        RDFDataMgr.write(out, graph, syntax);
        IO.close(out);
        ByteArrayEntity entity = new ByteArrayEntity(out.toByteArray());
        entity.setContentType(ct);
        return entity;
    }

    /**
     * Create an HttpEntity for the graph. The bytes for the graph are written
     * directly the HTTP stream but the length of the entity will be -1 (unknown).
     * This does not work over cached connections which need to know when
     * a request body is finished.
     */
    private HttpEntity graphToHttpEntityStream(Graph graph, RDFFormat syntax) {
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, graph, syntax));
        String ct = syntax.getLang().getContentType().getContentType();
        entity.setContentType(ct);
        return entity;
    }

    /** Create an HttpEntity for the dataset */
    protected HttpEntity datasetToHttpEntity(DatasetGraph dataset) {
        return datasetToHttpEntity(dataset, outputQuads);
    }

    /** Create an HttpEntity for the dataset */
    protected HttpEntity datasetToHttpEntity(DatasetGraph dataset, RDFFormat syntax) {
        // Length - leaves connection reusable.
        return datasetToHttpEntityWithLength(dataset, syntax);
    }

    private HttpEntity datasetToHttpEntityWithLength(DatasetGraph dataset, RDFFormat syntax) {
        String ct = syntax.getLang().getContentType().getContentType();
        ByteArrayOutputStream out = new ByteArrayOutputStream(128*1024);
        RDFDataMgr.write(out, dataset, syntax);
        IO.close(out);
        ByteArrayEntity entity = new ByteArrayEntity(out.toByteArray());
        entity.setContentType(ct);
        return entity;
    }

    private HttpEntity datasetToHttpEntityStream(DatasetGraph dataset, RDFFormat syntax) {
        EntityTemplate entity = new EntityTemplate((out)->RDFDataMgr.write(out, dataset, syntax));
        String ct = syntax.getLang().getContentType().getContentType();
        entity.setContentType(ct);
        return entity;
    }

    /** Convert HTTP status codes to exceptions */
    static protected void exec(Runnable action)  {
        try { action.run(); }
        catch (HttpException ex) { handleHttpException(ex, false); }
    }

    /** Convert HTTP status codes to exceptions */
    static protected <X> X exec(Supplier<X> action)  {
        try { return action.get(); }
        catch (HttpException ex) { handleHttpException(ex, true); return null;}
    }

    private static void handleHttpException(HttpException ex, boolean ignore404) {
        if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 && ignore404 )
            return ;
        throw ex;
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
