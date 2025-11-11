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

package org.apache.jena.sparql.exec.http;

import static org.apache.jena.http.HttpLib.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ClosedInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.AsyncHttpRDF;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.query.*;
import org.apache.jena.riot.*;
import org.apache.jena.riot.lang.IteratorParsers;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.resultset.ResultSetReaderRegistry;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.HttpParams;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;

/**
 * A {@link QueryExec} implementation where queries are executed against a remote
 * service over HTTP.
 */
public class QueryExecHTTP implements QueryExec {

    public static QueryExecHTTPBuilder newBuilder() { return QueryExecHTTPBuilder.create(); }

    public static QueryExecHTTPBuilder service(String serviceURL) {
        return QueryExecHTTP.newBuilder().endpoint(serviceURL);
    }

    // Blazegraph has a bug : it impacts wikidata.
    // Unless the charset is set, wikidata interprets a POST as ISO-8859-??? (c.f. POST as form).
    // https://github.com/blazegraph/database/issues/224
    // Only applies to SendMode.asPost of a SPARQL query.
    public static final String QUERY_MIME_TYPE = WebContent.contentTypeSPARQLQuery+";charset="+WebContent.charsetUTF8;
    private final Query query;
    private final String queryString;
    private final String service;
    private final Context context;

    // Params
    private Params params = null;

    private final QuerySendMode sendMode;
    private int urlLimit = HttpEnv.urlLimit;

    // Protocol
    private List<String> defaultGraphURIs = new ArrayList<>();
    private List<String> namedGraphURIs = new ArrayList<>();

    private boolean closed = false;

    // Timeout of query execution.
    private long readTimeout = -1;
    private TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;

    private final String selectAcceptHeader;
    private final String askAcceptHeader;
    private final String graphAcceptHeader;
    private final String datasetAcceptHeader;

    // If this is non-null, it overrides the use of any Content-Type above.
    @Deprecated(forRemoval = true) // Deprecated in favor of setting the other header fields.
    private String overrideAcceptHeader         = null;

    // Received content type
    private String httpResponseContentType = null;

    private HttpClient httpClient = HttpEnv.getDftHttpClient();
    private Map<String, String> httpHeaders;

    // ----- Cancellation -----

    private volatile boolean isAborted = false;
    private final Object abortLock = new Object();
    private volatile CompletableFuture<HttpResponse<InputStream>> future = null;

    // Releasing HTTP input streams is important. We remember this for SELECT result
    // set streaming, and will close it when the execution is closed
    // This is the physical InputStream of the HTTP request which will only be closed by close().
    private InputStream retainedConnection = null;

    // This is a wrapped view of retainedConnection that will be closed by abort().
    private volatile InputStream retainedConnectionView = null;

    // Whether abort cancels an async HTTP request's future immediately.
    private boolean cancelFutureOnAbort = true;

    /**
     * This constructor is superseded by the other one which has more parameters.
     * The recommended way to create instances of this class is via {@link QueryExecHTTPBuilder}.
     */
    @Deprecated(forRemoval = true)
    public QueryExecHTTP(String serviceURL, Query query, String queryString, int urlLimit,
            HttpClient httpClient, Map<String, String> httpHeaders, Params params, Context context,
            List<String> defaultGraphURIs, List<String> namedGraphURIs,
            QuerySendMode sendMode, String overrideAcceptHeader,
            long timeout, TimeUnit timeoutUnit) {
        // Content Types: these list the standard formats and also include */*
        this(serviceURL, query, queryString, urlLimit,
                httpClient, httpHeaders, params, context,
                defaultGraphURIs, namedGraphURIs,
                sendMode,
                dft(overrideAcceptHeader, WebContent.defaultSparqlResultsHeader),
                dft(overrideAcceptHeader, WebContent.defaultSparqlAskHeader),
                dft(overrideAcceptHeader, WebContent.defaultGraphAcceptHeader),
                dft(overrideAcceptHeader, WebContent.defaultDatasetAcceptHeader),
                timeout, timeoutUnit);

        // Handling of legacy overrideAcceptHeader.
        this.overrideAcceptHeader = overrideAcceptHeader;
        // Important - handled as special case because the defaults vary by query type.
        if ( httpHeaders.containsKey(HttpNames.hAccept) ) {
            if ( this.overrideAcceptHeader != null ) {
                String acceptHeader = httpHeaders.get(HttpNames.hAccept);
                this.overrideAcceptHeader = acceptHeader;
            }
            this.httpHeaders.remove(HttpNames.hAccept);
        }
    }

    protected QueryExecHTTP(String serviceURL, Query query, String queryString, int urlLimit,
                         HttpClient httpClient, Map<String, String> httpHeaders, Params params, Context context,
                         List<String> defaultGraphURIs, List<String> namedGraphURIs,
                         QuerySendMode sendMode,
                         String selectAcceptHeader, String askAcceptHeader,
                         String graphAcceptHeader, String datasetAcceptHeader,
                         long timeout, TimeUnit timeoutUnit) {
        this.context = ( context == null ) ? ARQ.getContext().copy() : context.copy();
        this.service = serviceURL;
        this.query = query;
        this.queryString = queryString;
        this.urlLimit = urlLimit;
        this.httpHeaders = httpHeaders;
        this.defaultGraphURIs = defaultGraphURIs;
        this.namedGraphURIs = namedGraphURIs;
        this.sendMode = Objects.requireNonNull(sendMode);
        this.selectAcceptHeader = selectAcceptHeader;
        this.askAcceptHeader = askAcceptHeader;
        this.graphAcceptHeader = graphAcceptHeader;
        this.datasetAcceptHeader = datasetAcceptHeader;
        this.httpHeaders = httpHeaders;
        this.params = params;
        this.readTimeout = timeout;
        this.readTimeoutUnit = timeoutUnit;
        this.httpClient = HttpLib.dft(httpClient, HttpEnv.getDftHttpClient());
    }

    public String getAcceptHeaderSelect() {
        return selectAcceptHeader;
    }

    public String getAcceptHeaderAsk() {
        return askAcceptHeader;
    }

    public String getAcceptHeaderDescribe() {
        return graphAcceptHeader;
    }

    public String getAcceptHeaderConstructGraph() {
        return graphAcceptHeader;
    }

    public String getAcceptHeaderConstructDataset() {
        return datasetAcceptHeader;
    }

    /** Getter for the override accept header. Only used for testing. */
    @Deprecated(forRemoval = true)
    public String getAppProvidedAcceptHeader() {
        return overrideAcceptHeader;
    }

    /** The Content-Type response header received (null before the remote operation is attempted). */
    public String getHttpResponseContentType() {
        return httpResponseContentType;
    }

    @Override
    public RowSet select() {
        checkNotClosed();
        check(QueryType.SELECT);
        RowSet rs = execRowSet();
        return rs;
    }

    private RowSet execRowSet() {
        HttpRequest request = effectiveHttpRequest(selectAcceptHeader);
        HttpResponse<InputStream> response = executeQuery(request);
        InputStream in = registerInputStream(response);
        // Don't assume the endpoint actually gives back the content type we asked for
        String actualContentType = responseHeader(response, HttpNames.hContentType);

        // Remember the response.
        httpResponseContentType = actualContentType;

        // More reliable to use the format-defined charsets e.g. JSON -> UTF-8
        actualContentType = removeCharset(actualContentType);

        if (false) {
            byte b[] = IO.readWholeFile(in);
            String str = new String(b);
            System.out.println(str);
            in = new ByteArrayInputStream(b);
        }

        if (actualContentType == null || actualContentType.equals(""))
            actualContentType = WebContent.contentTypeResultsXML;

        // Map to lang, with pragmatic alternatives.
        Lang lang = WebContent.contentTypeToLangResultSet(actualContentType);
        boolean unknownLang = lang == null;
        boolean unsupportedFormat = !unknownLang && !ResultSetReaderRegistry.isRegistered(lang);
        if ( unknownLang || unsupportedFormat ) {
            String errorTerm = unknownLang ? "recognized" : "supported";
            String errorMsg = String.format("Endpoint returned Content-Type: %s which is not %s for SELECT queries",
                    actualContentType, errorTerm);
            raiseException(errorMsg, request, response, in);
        }

        // This returns a streaming result set for some formats.
        // Do not close the InputStream at this point.
        ResultSet result = ResultSetMgr.read(in, lang);
        return RowSet.adapt(result);
    }

    @Override
    public boolean ask() {
        checkNotClosed();
        check(QueryType.ASK);
        HttpRequest request = effectiveHttpRequest(askAcceptHeader);
        HttpResponse<InputStream> response = executeQuery(request);
        InputStream in = registerInputStream(response);

        String actualContentType = responseHeader(response, HttpNames.hContentType);
        httpResponseContentType = actualContentType;
        actualContentType = removeCharset(actualContentType);

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals(""))
            actualContentType = askAcceptHeader;

        Lang lang = RDFLanguages.contentTypeToLang(actualContentType);
        if ( lang == null ) {
            // Any specials :
            // application/xml for application/sparql-results+xml
            // application/json for application/sparql-results+json
            if (actualContentType.equals(WebContent.contentTypeXML))
                lang = ResultSetLang.RS_XML;
            else if ( actualContentType.equals(WebContent.contentTypeJSON))
                lang = ResultSetLang.RS_JSON;
        }
        if (lang == null) {
            raiseException("Endpoint returned Content-Type: " + actualContentType + " which is not supported for ASK queries", request, response, in);
        }
        try {
            boolean result = ResultSetMgr.readBoolean(in, lang);
            return result;
        } finally {
            finishInputStream(in);
        }
   }

    private String removeCharset(String contentType) {
        if ( contentType == null )
            return contentType;
        int idx = contentType.indexOf(';');
        if ( idx < 0 )
            return contentType;
        return contentType.substring(0,idx);
    }

    @Override
    public Graph construct(Graph graph) {
        checkNotClosed();
        check(QueryType.CONSTRUCT);
        return execGraph(graph, graphAcceptHeader);
    }

    @Override
    public Iterator<Triple> constructTriples() {
        checkNotClosed();
        check(QueryType.CONSTRUCT);
        return execTriples(graphAcceptHeader);
    }

    @Override
    public Iterator<Quad> constructQuads(){
        checkNotClosed();
        return execQuads();
    }

    @Override
    public DatasetGraph constructDataset(){
        checkNotClosed();
        return constructDataset(DatasetGraphFactory.createTxnMem());
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset){
        checkNotClosed();
        check(QueryType.CONSTRUCT);
        return execDataset(dataset);
    }

    @Override
    public Graph describe(Graph graph) {
        checkNotClosed();
        check(QueryType.DESCRIBE);
        return execGraph(graph, graphAcceptHeader);
    }

    @Override
    public Iterator<Triple> describeTriples() {
        checkNotClosed();
        return execTriples(graphAcceptHeader);
    }

    private Graph execGraph(Graph graph, String acceptHeader) {
        Pair<InputStream, Lang> p = execRdfWorker(acceptHeader, WebContent.contentTypeRDFXML);
        InputStream in = p.getLeft();
        Lang lang = p.getRight();
        try {
            RDFDataMgr.read(graph, in, lang);
        } finally {
            finishInputStream(in);
        }
        return graph;
    }

    private DatasetGraph execDataset(DatasetGraph dataset) {
        Pair<InputStream, Lang> p = execRdfWorker(datasetAcceptHeader, WebContent.contentTypeNQuads);
        InputStream in = p.getLeft();
        Lang lang = p.getRight();
        try {
            RDFDataMgr.read(dataset, in, lang);
        } finally {
            finishInputStream(in);
        }
        return dataset;
    }

    private Iterator<Triple> execTriples(String acceptHeader) {
        Pair<InputStream, Lang> p = execRdfWorker(acceptHeader, WebContent.contentTypeRDFXML);
        InputStream input = p.getLeft();
        Lang lang = p.getRight();
        // Base URI?
        Iterator<Triple> iter = IteratorParsers.createIteratorTriples(input, lang, null);
        return Iter.onCloseIO(iter, input);
    }

    private Iterator<Quad> execQuads() {
        checkNotClosed();
        Pair<InputStream, Lang> p = execRdfWorker(datasetAcceptHeader, WebContent.contentTypeNQuads);
        InputStream input = p.getLeft();
        Lang lang = p.getRight();
        Iterator<Quad> iter = IteratorParsers.createIteratorQuads(input, lang, null);
        return Iter.onCloseIO(iter, input);
    }

    // Any RDF data back (CONSTRUCT, DESCRIBE, QUADS)
    // ifNoContentType - some wild guess at the content type.
    private Pair<InputStream, Lang> execRdfWorker(String contentType, String ifNoContentType) {
        checkNotClosed();
        String thisAcceptHeader = contentType;
        HttpRequest request = effectiveHttpRequest(thisAcceptHeader);
        HttpResponse<InputStream> response = executeQuery(request);
        InputStream in = registerInputStream(response);

        // Don't assume the endpoint actually gives back the content type we asked for
        String actualContentType = responseHeader(response, HttpNames.hContentType);
        httpResponseContentType = actualContentType;
        actualContentType = removeCharset(actualContentType);

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals(""))
            actualContentType = ifNoContentType;

        Lang lang = RDFLanguages.contentTypeToLang(actualContentType);
        if ( ! RDFLanguages.isQuads(lang) && ! RDFLanguages.isTriples(lang) ) {
            raiseException("Endpoint returned Content Type: "
                    + actualContentType
                    + " which is not a valid RDF syntax", request, response, in);
        }
        return Pair.create(in, lang);
    }

    @Override
    public JsonArray execJson() {
        checkNotClosed();
        check(QueryType.CONSTRUCT_JSON);
        String thisAcceptHeader = dft(overrideAcceptHeader, WebContent.contentTypeJSON);
        HttpRequest request = effectiveHttpRequest(thisAcceptHeader);
        HttpResponse<InputStream> response = executeQuery(request);
        InputStream in = registerInputStream(response);
        try {
            return JSON.parseAny(in).getAsArray();
        } finally { finishInputStream(in); }
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        JsonArray array = execJson().getAsArray();
        List<JsonObject> x = new ArrayList<>(array.size());
        array.forEach(elt->{
            if ( ! elt.isObject())
                throw new QueryExecException("Item in an array from a JSON query isn't an object");
            x.add(elt.getAsObject());
        });
        return x.iterator();
    }

    private void check(QueryType queryType) {
        if ( query == null ) {
            // Pass through the queryString.
            return;
        }
        if ( query.queryType() != queryType )
            throw new QueryExecException("Not the right form of query. Expected "+queryType+" but got "+query.queryType());
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public DatasetGraph getDataset() {
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
            // Object not created with a Query object, may be because there is foreign
            // syntax in the query or may be because the query string was available and the app
            // didn't want the overhead of parsing it every time.
            // Try to parse it else return null;
            try { return QueryFactory.create(queryString, Syntax.syntaxARQ); }
            catch (QueryParseException ex) {}
            return null;
        }
        return null;
    }

    /**
     * Return the query string. If this was supplied as a string,
     * there is no guarantee this is legal SPARQL syntax.
     */
    @Override
    public String getQueryString() {
        return queryString;
    }

    private static long asMillis(long duration, TimeUnit timeUnit) {
        return (duration < 0) ? duration : timeUnit.toMillis(duration);
    }

    private void raiseException(String errorMsg, HttpRequest request, HttpResponse<?> response, InputStream in) {
        int bodySummaryLength = 1024;
        int statusCode = response.statusCode();
        String statusCodeMsg = HttpSC.getMessage(statusCode);

        // Determine the charset for extracting an excerpt of the body
        String actualContentType = responseHeader(response, HttpNames.hContentType);
        MediaType ct = MediaType.create(actualContentType);
        String charsetName = ct == null ? null : ct.getCharset();
        Charset charset = null;
        try {
            charset = charsetName == null ? null : Charset.forName(charsetName);
        } catch (Throwable e) {
            // Silently ignore
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        String bodyStr;
        try {
            bodyStr = in == null ? "(no data supplied)" : IO.abbreviate(in, charset, bodySummaryLength, "...");
        } catch (Throwable e) {
            // No need to rethrow because we are already about to throw
            bodyStr = "(failed to retrieve HTTP body due to: " + e.getMessage() + ")";
        }

        throw new QueryException(String.format(
                "%s.\nStatus code %d %s, Method %s, Request Headers: %s\nBody (extracted with charset %s): %s",
                errorMsg, statusCode, statusCodeMsg, request.method(), request.headers().map(), charset.name(), bodyStr));
    }

    /**
     * Build the effective HTTP request ready for use with {@link #executeQuery(HttpRequest)}.
     */
    private HttpRequest effectiveHttpRequest(String reqAcceptHeader) {
        if (closed)
            throw new ARQException("HTTP execution already closed");

        //  SERVICE specials.

        Params thisParams = Params.create(params);

        if ( defaultGraphURIs != null ) {
            for ( String dft : defaultGraphURIs )
                thisParams.add( HttpParams.pDefaultGraph, dft );
        }
        if ( namedGraphURIs != null ) {
            for ( String name : namedGraphURIs )
                thisParams.add( HttpParams.pNamedGraph, name );
        }

        HttpLib.modifyByService(service, context, thisParams, httpHeaders);

        HttpRequest request = makeRequest(thisParams, reqAcceptHeader);
        return request;
    }

    private HttpRequest makeRequest(Params thisParams, String reqAcceptHeader) {
        QuerySendMode actualSendMode = actualSendMode();
        HttpRequest.Builder requestBuilder;
        switch(actualSendMode) {
            case asGetAlways :
                requestBuilder = executeQueryGet(thisParams, reqAcceptHeader);
                break;
            case asPostForm :
                requestBuilder = executeQueryPostForm(thisParams, reqAcceptHeader);
                break;
            case asPost :
                requestBuilder = executeQueryPostBody(thisParams, reqAcceptHeader);
                break;
            default :
                // Should not happen!
                throw new InternalErrorException("Invalid value for 'actualSendMode' "+actualSendMode);
        }
        return requestBuilder.build();
    }

    /**
     * Execute an HttpRequest and wait for the HttpResponse.
     * A call to {@link #abort()} interrupts the wait.
     * The response is returned after status code processing so the caller can assume the
     * query execution was successful and return 200.
     * Use {@link HttpLib#getInputStream} to access the body.
     */
    private HttpResponse<InputStream> executeQuery(HttpRequest request) {
        checkNotClosed();

        if (future != null) {
            throw new IllegalStateException("Execution was already started.");
        }

        try {
            synchronized (abortLock) {
                checkNotAborted();
                logQuery(queryString, request);
                future = HttpLib.executeAsync(httpClient, request);
            }

            HttpResponse<InputStream> response = AsyncHttpRDF.getOrElseThrow(future, request);
            HttpLib.handleHttpStatusCode(response);
            return response;
        } catch (HttpException httpEx) {
            throw QueryExceptionHTTP.rewrap(httpEx);
        }
    }

    private QuerySendMode actualSendMode() {
        int thisLengthLimit = urlLimit;
        switch(sendMode) {
            case asGetAlways :
            case asPostForm :
            case asPost :
                return sendMode;
            case asGetWithLimitBody :
            case asGetWithLimitForm :
                break;
        }

        // Only QuerySendMode.asGetWithLimitBody and QuerySendMode.asGetWithLimitForm here.
        String requestURL = service;
        // Other params (query= has not been added at this point)
        int paramsLength = params.httpString().length();
        int qEncodedLength = calcEncodeStringLength(queryString);

        // URL Length, including service (for safety)
        int length = service.length()
                + /* ?query= */        1 + HttpParams.pQuery.length()
                + /* encoded query */  qEncodedLength
                + /* &other params*/   1 + paramsLength;
        if ( length <= thisLengthLimit )
            return QuerySendMode.asGetAlways;
        return (sendMode==QuerySendMode.asGetWithLimitBody) ? QuerySendMode.asPost : QuerySendMode.asPostForm;
    }

    private static int calcEncodeStringLength(String str) {
        // Could approximate by counting non-queryString character and adding that *2 to the length of the string.
        String qs = HttpLib.urlEncodeQueryString(str);
        int encodedLength = qs.length();
        return encodedLength;
    }

    private HttpRequest.Builder executeQueryGet(Params thisParams, String acceptHeader) {
        thisParams.add(HttpParams.pQuery, queryString);
        String requestURL = requestURL(service, thisParams.httpString());
        HttpRequest.Builder builder = HttpLib.requestBuilder(requestURL, httpHeaders, readTimeout, readTimeoutUnit);
        acceptHeader(builder, acceptHeader);
        return builder.GET();
    }

    private HttpRequest.Builder executeQueryPostForm(Params thisParams, String acceptHeader) {
        thisParams.add(HttpParams.pQuery, queryString);
        String requestURL = service;
        String formBody = thisParams.httpString();
        HttpRequest.Builder builder = HttpLib.requestBuilder(requestURL, httpHeaders, readTimeout, readTimeoutUnit);
        acceptHeader(builder, acceptHeader);
        // Use an HTML form.
        contentTypeHeader(builder, WebContent.contentTypeHTMLForm);
        // Already UTF-8 encoded to ASCII.
        return builder.POST(BodyPublishers.ofString(formBody, StandardCharsets.US_ASCII));
    }

    // Use SPARQL query body and MIME type.
    private HttpRequest.Builder executeQueryPostBody(Params thisParams, String acceptHeader) {
        // Use thisParams (for default-graph-uri etc)
        String requestURL = requestURL(service, thisParams.httpString());
        HttpRequest.Builder builder = HttpLib.requestBuilder(requestURL, httpHeaders, readTimeout, readTimeoutUnit);
        contentTypeHeader(builder, QUERY_MIME_TYPE);
        acceptHeader(builder, acceptHeader);
        return builder.POST(BodyPublishers.ofString(queryString));
    }

    private static void logQuery(String queryString, HttpRequest request) {}

    /**
     * Cancel query evaluation
     */
    @Override
    public void abort() {
        // Setting abort to true causes the next read from
        // retainedConnectionView (if already created) to
        // fail with a QueryCancelledException.
        isAborted = true;
        if (cancelFutureOnAbort) {
            cancelFuture(future);
        }
    }

    private InputStream registerInputStream(HttpResponse<InputStream> httpResponse) {
        InputStream in = HttpLib.getInputStream(httpResponse);
        registerInputStream(in);
        return in;
    }

    /**
     * Set the given input stream as the 'retainedConnection' and create a corresponding
     * asynchronously abortable 'retainedConnectionView'. The latter is returned.
     * If execution was already aborted then a {@link QueryCancelledException} is raised.
     */
    private InputStream registerInputStream(InputStream input) {
        synchronized (abortLock) {
            this.retainedConnection = input;
            // Note: Used ProxyInputStream because the ctor of CloseShieldInputStream is deprecated.
            this.retainedConnectionView = new ProxyInputStream(input) {
                @Override
                protected void beforeRead(int n) throws IOException {
                    checkNotAborted();
                    super.beforeRead(n);
                }
                @Override
                public void close() {
                    this.in = ClosedInputStream.INSTANCE;
                }
            };

            // If already aborted then bail out before starting the parsers.
            checkNotAborted();
        }
        return retainedConnectionView;
    }

    @Override
    public void close() {
        closed = true;
        // No need to handle the future here, because the possible states are:
        // - Null because no execution was started -> retainedConnection is null.
        // - Cancelled by asynchronous abort       -> retainedConnection is null.
        // - Completed successfully by the same thread that now closes the retainedConnection
        //                                         -> retainedConnection is non-null.
        IOUtils.closeQuietly(retainedConnectionView);
        closeRetainedConnection();
    }

    private static void cancelFuture(CompletableFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private void closeRetainedConnection() {
        if (retainedConnection != null) {
            try {
                // This call may take a long time if the response has not been consumed
                // as HTTP client will consume the remaining response so it can re-use the
                // connection. If we're closing when we're not at the end of the stream then
                // issue a warning to the logs
                if (retainedConnection.read() != -1)
                    Log.warn(this, "HTTP response not fully consumed, if HTTP Client is reusing connections (its default behaviour) then it will consume the remaining response data which may take a long time and cause this application to become unresponsive");
                retainedConnection.close();
            } catch (RuntimeIOException | java.io.IOException e) {
                // If we are closing early and the underlying stream is chunk encoded
                // the close() can result in a IOException. TypedInputStream catches
                // and re-wraps that and we want to suppress both forms.
            } finally {
                retainedConnection = null;
            }
        }
    }

    private void checkNotClosed() {
        if ( closed )
            throw new QueryExecException("HTTP QueryExecHTTP has been closed");
    }

    protected void checkNotAborted() {
        if ( isAborted )
            throw new QueryCancelledException();
    }

    @Override
    public boolean isClosed() { return closed; }
}
