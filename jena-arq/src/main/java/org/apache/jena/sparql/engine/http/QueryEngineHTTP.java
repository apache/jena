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

package org.apache.jena.sparql.engine.http;

import java.io.ByteArrayInputStream ;
import java.io.IOException;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.TimeUnit ;

import org.apache.http.client.HttpClient ;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext ;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.*;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ResultSetCheckCondition ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.resultset.ResultSetException;
import org.apache.jena.sparql.util.Context ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * A query execution implementation where queries are executed against a remote
 * service
 * 
 */
public class QueryEngineHTTP implements QueryExecution {
    private static Logger log = LoggerFactory.getLogger(QueryEngineHTTP.class);

    public static final String QUERY_MIME_TYPE = WebContent.contentTypeSPARQLQuery;
    private final Query query;
    private final String queryString;
    private final String service;
    private final Context context;

    // Params
    private Params params = null;

    // Protocol
    private List<String> defaultGraphURIs = new ArrayList<>();
    private List<String> namedGraphURIs = new ArrayList<>();
    private HttpClient client;
    private HttpContext httpContext;

    private boolean closed = false;

    // Timeouts
    private long connectTimeout = -1;
    private TimeUnit connectTimeoutUnit = TimeUnit.MILLISECONDS;
    private long readTimeout = -1;
    private TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;

    // Compression Support
    private boolean allowCompression = true;

    // Content Types
    private String selectContentType    = defaultSelectHeader();
    private String askContentType       = defaultAskHeader();
    private String modelContentType     = defaultConstructHeader();
    
    private String constructContentType = defaultConstructHeader() ;
    private String datasetContentType   = defaultConstructDatasetHeader() ;
    
    // If this is non-null, it overrides the ???ContentType choice. 
    private String acceptHeader         = null;
    
    // Received content type 
    private String httpResponseContentType = null ;
    /**
     * Supported content types for SELECT queries
     */
    public static String[] supportedSelectContentTypes = new String[] { WebContent.contentTypeResultsXML,
            WebContent.contentTypeResultsJSON, WebContent.contentTypeTextTSV, WebContent.contentTypeTextCSV,
            WebContent.contentTypeResultsThrift};
    /**
     * Supported content types for ASK queries
     */
    public static String[] supportedAskContentTypes = new String[] { WebContent.contentTypeResultsXML,
            WebContent.contentTypeResultsJSON, WebContent.contentTypeTextTSV, WebContent.contentTypeTextCSV };

    // Releasing HTTP input streams is important. We remember this for SELECT,
    // and will close when the engine is closed
    private InputStream retainedConnection = null;

    public QueryEngineHTTP(String serviceURI, Query query) {
        this(serviceURI, query, null, null);
    }
    
    public QueryEngineHTTP(String serviceURI, Query query, HttpClient client) {
        this(serviceURI, query, client, null);
    }

    public QueryEngineHTTP(String serviceURI, Query query, HttpClient client, HttpContext httpContext) {
        this(serviceURI, query, query.toString(), client, httpContext);
    }

    public QueryEngineHTTP(String serviceURI, String queryString) {
        this(serviceURI, queryString, null, null);
    }
    
    public QueryEngineHTTP(String serviceURI, String queryString, HttpClient client) {
        this(serviceURI, queryString, client, null);
    }

    public QueryEngineHTTP(String serviceURI, String queryString, HttpClient client, HttpContext httpContext) {
        this(serviceURI, null, queryString, client, httpContext);
    }
    
    private QueryEngineHTTP(String serviceURI, Query query, String queryString, HttpClient client, HttpContext httpContext) {
        this.query = query;
        this.queryString = queryString;
        this.service = serviceURI;
        this.context = ARQ.getContext().copy();

        // Apply service configuration if relevant
        applyServiceConfig(serviceURI, this);
        
        // Don't want to overwrite client config we may have picked up from
        // service context in the parent constructor if the specified
        // client is null
        if (client != null) setClient(client);
        if (httpContext != null) setHttpContext(httpContext);
    }

    /**
     * <p>
     * Helper method which applies configuration from the Context to the query
     * engine if a service context exists for the given URI
     * </p>
     * <p>
     * Based off proposed patch for JENA-405 but modified to apply all relevant
     * configuration, this is in part also based off of the private
     * {@code configureQuery()} method of the {@link Service} class though it
     * omits parameter merging since that will be done automatically whenever
     * the {@link QueryEngineHTTP} instance makes a query for remote submission.
     * </p>
     * 
     * @param serviceURI
     *            Service URI
     */
    private static void applyServiceConfig(String serviceURI, QueryEngineHTTP engine) {
        if (engine.context == null)
            return;

        @SuppressWarnings("unchecked")
        Map<String, Context> serviceContextMap = (Map<String, Context>) engine.context.get(Service.serviceContext);
        if (serviceContextMap != null && serviceContextMap.containsKey(serviceURI)) {
            Context serviceContext = serviceContextMap.get(serviceURI);
            if (log.isDebugEnabled())
                log.debug("Endpoint URI {} has SERVICE Context: {} ", serviceURI, serviceContext);

            // Apply behavioral options
            engine.setAllowCompression(serviceContext.isTrueOrUndef(Service.queryCompression));
            applyServiceTimeouts(engine, serviceContext);

            // Apply context-supplied client settings
            HttpClient client = serviceContext.get(Service.queryClient);

            if (client != null) {
                if (log.isDebugEnabled())
                    log.debug("Using context-supplied HTTP client for endpoint URI {}", serviceURI);
                engine.setClient(client);
            }
        }
    }

    /**
     * Applies context provided timeouts to the given engine
     * 
     * @param engine
     *            Engine
     * @param context
     *            Context
     */
    private static void applyServiceTimeouts(QueryEngineHTTP engine, Context context) {
        if (context.isDefined(Service.queryTimeout)) {
            Object obj = context.get(Service.queryTimeout);
            if (obj instanceof Number) {
                int x = ((Number) obj).intValue();
                engine.setTimeout(-1, x);
            } else if (obj instanceof String) {
                try {
                    String str = obj.toString();
                    if (str.contains(",")) {

                        String[] a = str.split(",");
                        int connect = Integer.parseInt(a[0]);
                        int read = Integer.parseInt(a[1]);
                        engine.setTimeout(read, connect);
                    } else {
                        int x = Integer.parseInt(str);
                        engine.setTimeout(-1, x);
                    }
                } catch (NumberFormatException ex) {
                    throw new QueryExecException("Can't interpret string for timeout: " + obj);
                }
            } else {
                throw new QueryExecException("Can't interpret timeout: " + obj);
            }
        }
    }

    // public void setParams(Params params)
    // { this.params = params ; }

    @Override
    public void setInitialBinding(QuerySolution binding) {
        throw new UnsupportedOperationException(
                "Initial bindings not supported for remote queries, consider using a ParameterizedSparqlString to prepare a query for remote execution");
    }

    public void setInitialBindings(ResultSet table) {
        throw new UnsupportedOperationException(
                "Initial bindings not supported for remote queries, consider using a ParameterizedSparqlString to prepare a query for remote execution");
    }

    /**
     * @param defaultGraphURIs
     *            The defaultGraphURIs to set.
     */
    public void setDefaultGraphURIs(List<String> defaultGraphURIs) {
        this.defaultGraphURIs = defaultGraphURIs;
    }

    /**
     * @param namedGraphURIs
     *            The namedGraphURIs to set.
     */
    public void setNamedGraphURIs(List<String> namedGraphURIs) {
        this.namedGraphURIs = namedGraphURIs;
    }

    /**
     * Sets whether the HTTP requests will permit compressed encoding
     */
    public void setAllowCompression(boolean allowed) {
        allowCompression = allowed;
    }

    public void addParam(String field, String value) {
        if (params == null)
            params = new Params();
        params.addParam(field, value);
    }

    /**
     * @param defaultGraph
     *            The defaultGraph to add.
     */
    public void addDefaultGraph(String defaultGraph) {
        if (defaultGraphURIs == null)
            defaultGraphURIs = new ArrayList<>();
        defaultGraphURIs.add(defaultGraph);
    }

    /**
     * @param name
     *            The URI to add.
     */
    public void addNamedGraph(String name) {
        if (namedGraphURIs == null)
            namedGraphURIs = new ArrayList<>();
        namedGraphURIs.add(name);
    }

    /**
     * Sets the HTTP client to use, if none is set then the default
     * client is used.
     * 
     * @param client
     *            HTTP client
     */
    public void setClient(HttpClient client) {
        this.client = client;
    }
    
    /**
     * Get the HTTP client in use, if none is set then null.
     * 
     * @return client HTTP client
     */
    public HttpClient getClient() {
        return client;
    }
    
    /**
     * Sets the HTTP context to use, if none is set then the default context is used.
     * 
     * @param context HTTP context
     */
    public void setHttpContext(HttpContext context) {
        this.httpContext = context;
    }
    
    /**
     * Get the HTTP context in use, if none is set then null.
     * 
     * @return the {@code HttpContext} in scope
     */
    public HttpContext getHttpContext() {
        return httpContext;
    }

    /** The Content-Type response header received (null before the remote operation is attempted). */
    public String getHttpResponseContentType() {
		return httpResponseContentType;
	}

	@Override
    public ResultSet execSelect() {
        checkNotClosed() ;
        ResultSet rs = execResultSetInner() ;
        return new ResultSetCheckCondition(rs, this) ;
    }
    
	private ResultSet execResultSetInner() {
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(chooseAcceptHeader(acceptHeader, selectContentType));
        InputStream in = httpQuery.exec();

        if (false) {
            byte b[] = IO.readWholeFile(in);
            String str = new String(b);
            System.out.println(str);
            in = new ByteArrayInputStream(b);
        }

        retainedConnection = in; // This will be closed on close()

        // Don't assume the endpoint actually gives back the
        // content type we asked for
        String actualContentType = httpQuery.getContentType();
        httpResponseContentType = actualContentType;

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals("")) {
            actualContentType = selectContentType;
        }

        // Map to lang, with pragmatic alternatives. 
        Lang lang = WebContent.contentTypeToLangResultSet(actualContentType);
        if ( lang == null )
            throw new QueryException("Endpoint returned Content-Type: " + actualContentType + " which is not rcognized for SELECT queries");
        if ( !ResultSetReaderRegistry.isRegistered(lang) )
            throw new QueryException("Endpoint returned Content-Type: " + actualContentType + " which is not supported for SELECT queries");
        // This returns a streaming result set for some formats.
        // Do not close the InputStream at this point. 
        ResultSet result = ResultSetMgr.read(in, lang);
        return result;
    }

    //  XXX Move
    private static String chooseAcceptHeader(String acceptHeader, String contentType) {
        if ( acceptHeader != null )
            return acceptHeader;
        return contentType;
    }

    @Override
    public Model execConstruct() {
        return execConstruct(GraphFactory.makeJenaDefaultModel());
    }

    @Override
    public Model execConstruct(Model model) {
        return execModel(model);
    }

    @Override
    public Iterator<Triple> execConstructTriples() {
        return execTriples();
    }
    
    @Override
    public Iterator<Quad> execConstructQuads(){
    	return execQuads();
    }
    
    @Override
    public Dataset execConstructDataset(){
        return execConstructDataset(DatasetFactory.createTxnMem());
    }

    @Override
    public Dataset execConstructDataset(Dataset dataset){
        return execDataset(dataset) ;
    }

    @Override
    public Model execDescribe() {
        return execDescribe(GraphFactory.makeJenaDefaultModel());
    }

    @Override
    public Model execDescribe(Model model) {
        return execModel(model);
    }

    @Override
    public Iterator<Triple> execDescribeTriples() {
        return execTriples();
    }

    private Model execModel(Model model) {
        Pair<InputStream, Lang> p = execConstructWorker(modelContentType) ;
        try(InputStream in = p.getLeft()) {
            Lang lang = p.getRight() ;
            RDFDataMgr.read(model, in, lang);
        } catch (IOException ex) { IO.exception(ex); }
        finally { this.close(); }
        return model;
    }

    private Dataset execDataset(Dataset dataset) {
        Pair<InputStream, Lang> p = execConstructWorker(datasetContentType);
        try(InputStream in = p.getLeft()) {
            Lang lang = p.getRight() ;
            RDFDataMgr.read(dataset, in, lang);
        } catch (IOException ex) { IO.exception(ex); }
        finally { this.close(); }
        return dataset;
    }

    private Iterator<Triple> execTriples() {
        Pair<InputStream, Lang> p = execConstructWorker(modelContentType) ;
        InputStream in = p.getLeft() ;
        Lang lang = p.getRight() ;
        // Base URI?
        return RDFDataMgr.createIteratorTriples(in, lang, null);
    }
    
    private Iterator<Quad> execQuads() {
        Pair<InputStream, Lang> p = execConstructWorker(datasetContentType) ;
        InputStream in = p.getLeft() ;
        Lang lang = p.getRight() ;
        // Base URI?
        return RDFDataMgr.createIteratorQuads(in, lang, null);
    }

    private Pair<InputStream, Lang> execConstructWorker(String contentType) {
        checkNotClosed() ;
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(chooseAcceptHeader(acceptHeader, contentType));
        InputStream in = httpQuery.exec();
        
        // Don't assume the endpoint actually gives back the content type we
        // asked for
        String actualContentType = httpQuery.getContentType();
        httpResponseContentType = actualContentType;

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals("")) {
            actualContentType = WebContent.defaultDatasetAcceptHeader;
        }
        Lang lang = RDFLanguages.contentTypeToLang(actualContentType);
        if ( ! RDFLanguages.isQuads(lang) && ! RDFLanguages.isTriples(lang) )
            throw new QueryException("Endpoint returned Content Type: "
                                     + actualContentType 
                                     + " which is not a valid RDF syntax");
        return Pair.create(in, lang) ;
    }
    
    @Override
    public boolean execAsk() {
        checkNotClosed() ;
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(chooseAcceptHeader(acceptHeader, askContentType));
        try(InputStream in = httpQuery.exec()) {
            // Don't assume the endpoint actually gives back the content type we
            // asked for
            String actualContentType = httpQuery.getContentType();
            httpResponseContentType = actualContentType;

            // If the server fails to return a Content-Type then we will assume
            // the server returned the type we asked for
            if (actualContentType == null || actualContentType.equals("")) {
                actualContentType = askContentType;
            }

            Lang lang = RDFLanguages.contentTypeToLang(actualContentType);
            if ( lang == null ) {
                // Any specials :
                // application/xml for application/sparql-results+xml
                // application/json for application/sparql-results+json
                if (actualContentType.equals(WebContent.contentTypeXML))
                    lang = ResultSetLang.SPARQLResultSetXML;
                else if ( actualContentType.equals(WebContent.contentTypeJSON))
                    lang = ResultSetLang.SPARQLResultSetJSON;
            }
            if ( lang == null )
                throw new QueryException("Endpoint returned Content-Type: " + actualContentType + " which is not supported for ASK queries");
            Boolean result = ResultSetMgr.readBoolean(in, lang);
            return result;
        } catch (ResultSetException e) {
            log.warn("Returned content is not a boolean result", e);
            throw e;
        } catch (QueryExceptionHTTP e) { 
            throw e ;
        }
        catch (java.io.IOException e) {
            log.warn("Failed to close connection", e);
            return false ;
        }
    }

    @Override
    public JsonArray execJson()
    {
        checkNotClosed();
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(WebContent.contentTypeJSON);
        JsonArray result = new JsonArray();
        try(InputStream in = httpQuery.exec()) {
            JsonValue v = JSON.parseAny(in);
            if ( ! v.isArray() )
                throw new QueryExecException("Return from a JSON query isn't an array");
            result = v.getAsArray();
        } catch (IOException e) { IO.exception(e); }
        finally { this.close(); }
        return result;
    }

    @Override
    public Iterator<JsonObject> execJsonItems()
    {
        // Non-streaming.
        // TODO Integrate with the JSON parser to stream the results. 
        JsonArray array = execJson().getAsArray();
        List<JsonObject> x = new ArrayList<>(array.size());
        array.forEach(elt->{
            if ( ! elt.isObject()) 
                throw new QueryExecException("Item in an array from a JSON query isn't an object");
            x.add(elt.getAsObject());
        });
        return x.iterator();
    }

    private void checkNotClosed() {
        if ( closed )
            throw new QueryExecException("HTTP QueryExecution has been closed") ;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Dataset getDataset() {
        return null;
    }

    // This may be null - if we were created form a query string,
    // we don't guarantee to parse it so we let through non-SPARQL
    // extensions to the far end.
    @Override
    public Query getQuery() {
        if ( query != null )
            return query;
        if ( queryString != null ) {
            // Object not created with a Query object, may be because there is forgein
            // syntax in the query or may be because the queystrign was available and the app
            // didn't want the overhead of parsing it everytime. 
            // Try to parse it else return null;
            try { return QueryFactory.create(queryString, Syntax.syntaxARQ); }
            catch (QueryParseException ex) {}
            return null ;
        }
        return null;
    }

    /**
     * Return the query string. If this was supplied in a constructor, there is no
     * guaranttee this is legal SPARQL syntax.
     */
    public String getQueryString() {
        return queryString;
    }

    @Override
    public void setTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = TimeUnit.MILLISECONDS;
    }

    @Override
    public void setTimeout(long readTimeout, long connectTimeout) {
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = TimeUnit.MILLISECONDS;
        this.connectTimeout = connectTimeout;
        this.connectTimeoutUnit = TimeUnit.MILLISECONDS;
    }

    @Override
    public void setTimeout(long readTimeout, TimeUnit timeoutUnits) {
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = timeoutUnits;
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {
        this.readTimeout = timeout1;
        this.readTimeoutUnit = timeUnit1;
        this.connectTimeout = timeout2;
        this.connectTimeoutUnit = timeUnit2;
    }

    @Override
    public long getTimeout1() {
        return asMillis(readTimeout, readTimeoutUnit);
    }

    @Override
    public long getTimeout2() {
        return asMillis(connectTimeout, connectTimeoutUnit);
    }

    /**
     * Gets whether HTTP requests will indicate to the remote server that
     * compressed encoding of responses is accepted
     * 
     * @return True if compressed encoding will be accepted
     */
    public boolean getAllowCompression() {
        return allowCompression;
    }

    private static long asMillis(long duration, TimeUnit timeUnit) {
        return (duration < 0) ? duration : timeUnit.toMillis(duration);
    }

    private HttpQuery makeHttpQuery() {
        if (closed)
            throw new ARQException("HTTP execution already closed");

        HttpQuery httpQuery = new HttpQuery(service);
        httpQuery.merge(getServiceParams(service, context));
        httpQuery.addParam(HttpParams.pQuery, queryString);

        for ( String dft : defaultGraphURIs )
        {
            httpQuery.addParam( HttpParams.pDefaultGraph, dft );
        }
        for ( String name : namedGraphURIs )
        {
            httpQuery.addParam( HttpParams.pNamedGraph, name );
        }

        if (params != null) httpQuery.merge(params);

        httpQuery.setAllowCompression(allowCompression);
        
        // check for service context overrides
        if (context.isDefined(Service.serviceContext)) {
            Map<String, Context> servicesContext = context.get(Service.serviceContext);
            if (servicesContext.containsKey(service)) {
                Context serviceContext = servicesContext.get(service);
                if (serviceContext.isDefined(Service.queryClient)) client = serviceContext.get(Service.queryClient);
            }
        }
        httpQuery.setClient(client);
        HttpClientContext hcc = ( httpContext == null ) ? null : HttpClientContext.adapt(httpContext);
        httpQuery.setContext(hcc);
        
        // Apply timeouts
        if (connectTimeout > 0) httpQuery.setConnectTimeout((int) connectTimeoutUnit.toMillis(connectTimeout));

        if (readTimeout > 0) httpQuery.setReadTimeout((int) readTimeoutUnit.toMillis(readTimeout));

        return httpQuery;
    }

    // This is to allow setting additional/optional query parameters on a per
    // SERVICE level, see: JENA-195
    protected static Params getServiceParams(String serviceURI, Context context) throws QueryExecException {
        Params params = new Params();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<String>>> serviceParams = (Map<String, Map<String, List<String>>>) context
                .get(ARQ.serviceParams);
        if (serviceParams != null) {
            Map<String, List<String>> paramsMap = serviceParams.get(serviceURI);
            if (paramsMap != null) {
                for (String param : paramsMap.keySet()) {
                    if (HttpParams.pQuery.equals(param))
                        throw new QueryExecException("ARQ serviceParams overrides the 'query' SPARQL protocol parameter");

                    List<String> values = paramsMap.get(param);
                    for (String value : values)
                        params.addParam(param, value);
                }
            }
        }
        return params;
    }

    /**
     * Cancel query evaluation
     */
    public void cancel() {
        closed = true;
    }

    @Override
    public void abort() {
        try {
            close();
        } catch (Exception ex) {
            log.warn("Error during abort", ex);
        }
    }

    @Override
    public void close() {
        closed = true;
        if (retainedConnection != null) {
            try {
                // JENA-1063 - WARNING
                // This call may take a long time if the response has not been consumed
                // as HTTP client will consume the remaining response so it can re-use the
                // connection
                // If we're closing when we're not at the end of the stream then issue a
                // warning to the logs
                if (retainedConnection.read() != -1)
                    log.warn("HTTP response not fully consumed, if HTTP Client is reusing connections (its default behaviour) then it will consume the remaining response data which may take a long time and cause this application to become unresponsive");
                retainedConnection.close();
            } catch (RuntimeIOException e) {
                // If we are closing early and the underlying stream is chunk encoded
                // the close() can result in a IOException.  Unfortunately our TypedInputStream
                // catches and re-wraps that and we want to suppress it when we are cleaning up
                // and so we catch the wrapped exception and log it instead
                log.debug("Failed to close connection", e);
            } catch (java.io.IOException e) {
                log.debug("Failed to close connection", e);
            } finally {
                retainedConnection = null;
            }
        }
    }

    @Override
    public boolean isClosed() { return closed ; }

    @Override
    public String toString() {
        HttpQuery httpQuery = makeHttpQuery();
        return "GET " + httpQuery.toString();
    }

    /**
     * Sets the Content Type for SELECT queries provided that the format is
     * supported
     * 
     * @param contentType
     */
    public void setSelectContentType(String contentType) {
        boolean ok = false;
        for (String supportedType : supportedSelectContentTypes) {
            if (supportedType.equals(contentType)) {
                ok = true;
                break;
            }
        }
        if (!ok)
            throw new IllegalArgumentException("Given Content Type '" + contentType
                    + "' is not a supported SELECT results format");
        selectContentType = contentType;
    }

    /**
     * Sets the Content Type for ASK queries provided that the format is
     * supported
     * 
     * @param contentType
     */
    public void setAskContentType(String contentType) {
        boolean ok = false;
        for (String supportedType : supportedAskContentTypes) {
            if (supportedType.equals(contentType)) {
                ok = true;
                break;
            }
        }
        if (!ok)
            throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not a supported ASK results format");
        askContentType = contentType;
    }

    /**
     * Sets the Content Type for CONSTRUCT/DESCRIBE queries provided that the
     * format is supported
     * 
     * @param contentType
     */
    public void setModelContentType(String contentType) {
        // Check that this is a valid setting
        Lang lang = RDFLanguages.contentTypeToLang(contentType);
        if (lang == null)
            throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not supported by RIOT");
        if (!RDFLanguages.isTriples(lang))
            throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not a RDF Graph format");
        modelContentType = contentType;
    }
    
    public void setDatasetContentType(String contentType) {
        // Check that this is a valid setting
        Lang lang = RDFLanguages.contentTypeToLang(contentType);
        if (lang == null)
            throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not supported by RIOT");
        if (!RDFLanguages.isQuads(lang))
            throw new IllegalArgumentException("Given Content Type '" + contentType + "' is not a RDF Dataset format");
        datasetContentType = contentType;
    }
    
    private static final String dftSelectContentTypeHeader = initSelectContentTypes() ;

    public static String defaultSelectHeader() {
        return dftSelectContentTypeHeader ;
    }

    private static String initSelectContentTypes() {
        StringBuilder sBuff = new StringBuilder() ;
        accumulateContentTypeString(sBuff, WebContent.contentTypeResultsJSON,  1.0);
        accumulateContentTypeString(sBuff, WebContent.contentTypeResultsXML,   0.9);     // Less efficient
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeTextTSV,      0.7);
        accumulateContentTypeString(sBuff, WebContent.contentTypeTextCSV,      0.5);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeJSON,         0.2);     // We try to parse these in  
        accumulateContentTypeString(sBuff, WebContent.contentTypeXML,          0.2) ;    // the hope they are right.
        accumulateContentTypeString(sBuff, "*/*",                              0.1) ;    // Get something!
        return sBuff.toString() ;
    }

    private static final String askContentTypeHeader = initAskContentTypes() ;

    public static String defaultAskHeader() {
        return dftSelectContentTypeHeader ;
    }

    // These happen to be the same.
    private static String initAskContentTypes() { return initSelectContentTypes(); }

    private static final String dftConstructContentTypeHeader = initConstructContentTypes() ;

    public static String defaultConstructHeader() {
        return dftConstructContentTypeHeader ;
    }
    
    private static String initConstructContentTypes() {
        // Or use WebContent.defaultGraphAcceptHeader which is slightly
        // narrower. Here, we have a tuned setting for SPARQL operations.
        StringBuilder sBuff = new StringBuilder() ;
        accumulateContentTypeString(sBuff, WebContent.contentTypeTurtle,       1.0);
        accumulateContentTypeString(sBuff, WebContent.contentTypeNTriples,     1.0);
        accumulateContentTypeString(sBuff, WebContent.contentTypeRDFXML,       0.9);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeTurtleAlt1,   0.8);
        accumulateContentTypeString(sBuff, WebContent.contentTypeTurtleAlt2,   0.8);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeN3,           0.7);
        accumulateContentTypeString(sBuff, WebContent.contentTypeN3Alt1,       0.6);
        accumulateContentTypeString(sBuff, WebContent.contentTypeN3Alt2,       0.6);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeNTriplesAlt,  0.5);
        accumulateContentTypeString(sBuff, "*/*",                              0.1) ;

        return sBuff.toString();
    }

    private static final String dftConstructDatasetContentTypeHeader = initConstructDatasetContentTypes() ;

    public static String defaultConstructDatasetHeader() {
        return dftConstructDatasetContentTypeHeader ; 
    }
    
    private static String initConstructDatasetContentTypes() {
        // Or use WebContent.defaultDatasetAcceptHeader which is slightly
        // narrower. Here, we have a tuned setting for SPARQL operations.
        StringBuilder sBuff = new StringBuilder() ;
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeTriG,         1.0) ;
        accumulateContentTypeString(sBuff, WebContent.contentTypeTriGAlt1,     1.0) ;
        accumulateContentTypeString(sBuff, WebContent.contentTypeTriGAlt2,     1.0) ;

        accumulateContentTypeString(sBuff, WebContent.contentTypeNQuads,       1.0) ;
        accumulateContentTypeString(sBuff, WebContent.contentTypeNQuadsAlt1,   1.0) ;
        accumulateContentTypeString(sBuff, WebContent.contentTypeNQuadsAlt2,   1.0) ;
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeJSONLD,       0.9) ;

        // And triple formats (the case of execConstructDatasets but a regular triples CONSTRUCT). 
        accumulateContentTypeString(sBuff, WebContent.contentTypeTurtle,       0.8);
        accumulateContentTypeString(sBuff, WebContent.contentTypeNTriples,     0.8);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeRDFXML,       0.7);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeTurtleAlt1,   0.6);
        accumulateContentTypeString(sBuff, WebContent.contentTypeTurtleAlt2,   0.6);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeN3,           0.5);
        accumulateContentTypeString(sBuff, WebContent.contentTypeN3Alt1,       0.5);
        accumulateContentTypeString(sBuff, WebContent.contentTypeN3Alt2,       0.5);
        
        accumulateContentTypeString(sBuff, WebContent.contentTypeNTriplesAlt,  0.4);
        
        accumulateContentTypeString(sBuff, "*/*",                              0.1) ;

        return sBuff.toString();
    }
    
    private static void accumulateContentTypeString(StringBuilder sBuff, String str, double v) {
        if ( sBuff.length() != 0 )
            sBuff.append(", ") ;
        sBuff.append(str) ;
        if ( v < 1 )
            sBuff.append(";q=").append(v) ;
    }

    /** Get the HTTP Accept header for the request. */ 
    public String getAcceptHeader() {
        return this.acceptHeader;
    }
    
    /** Set the HTTP Accept header for the request.
     * Unlike the {@code set??ContentType} operations, this is not checked 
     * for validity.
     */ 
    public void setAcceptHeader(String acceptHeader) {
        this.acceptHeader = acceptHeader;
    }

}