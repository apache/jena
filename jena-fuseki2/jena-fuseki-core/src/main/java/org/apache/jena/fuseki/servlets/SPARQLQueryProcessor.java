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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format;
import static org.apache.jena.fuseki.server.CounterName.QueryTimeouts;
import static org.apache.jena.fuseki.servlets.ActionExecLib.incCounter;
import static org.apache.jena.riot.WebContent.ctHTMLForm;
import static org.apache.jena.riot.WebContent.ctSPARQLQuery;
import static org.apache.jena.riot.WebContent.isHtmlForm;
import static org.apache.jena.riot.WebContent.matchContentType;
import static org.apache.jena.riot.web.HttpNames.*;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.web.HttpSC;

/**
 * Handle SPARQL Query requests over the SPARQL Protocol. Subclasses provide this
 * algorithm with the actual dataset to query, whether a dataset hosted by this server
 * ({@link SPARQL_QueryDataset}) or specified in the protocol request
 * ({@link SPARQL_QueryGeneral}).
 * <p>
 * When data-level access control is in use, the ActionService's are
 * {@code AccessCtl_SPARQL_QueryDataset} etc.
 */
public abstract class SPARQLQueryProcessor extends ActionService
{
    private static final String QueryParseBase = Fuseki.BaseParserSPARQL;

    public SPARQLQueryProcessor() { }

    @Override
    public void execOptions(HttpAction action) {
        ActionLib.doOptionsGetPost(action);
        ServletOps.success(action);
    }

    // Not supported - depends on query and body.
    @Override public void execHead(HttpAction action) { super.execHead(action); }

    @Override public void execGet(HttpAction action) {
        executeLifecycle(action);
    }

    @Override public void execPost(HttpAction action) {
        executeLifecycle(action);
    }

    /**
     * All the query parameters that are acceptable in a given request.
     * This is comprised of, by default,
     * <ul>
     * <li>SPARQL Protocol for query ({@link #stdParams()}) as mentioned in the spec.
     * <li>Fuseki parameters ({@link #fusekiParams()}) e.g. timeout and formatting
     * <li>Any custom parameter for this particular servlet ({@link #customParams()}, usually none.
     * </ul>
     * The default implementation calculates this list of parameters once (on first use).
     */
    private volatile Set<String> acceptedParams_ = null;
    protected Collection<String> acceptedParams(HttpAction action) {
        if ( acceptedParams_ == null ) {
            synchronized(this) {
                if ( acceptedParams_ == null )
                    acceptedParams_ = generateAcceptedParams();
            }
        }
        return acceptedParams_;
    }

    /**
     * Validate the request, checking HTTP method and HTTP Parameters.
     * @param action HTTP Action
     */
    @Override
    public void validate(HttpAction action) {
        String method = action.getRequestMethod().toUpperCase(Locale.ROOT);

        if ( HttpNames.METHOD_OPTIONS.equals(method) )
            return;

        if ( !HttpNames.METHOD_POST.equals(method) && !HttpNames.METHOD_GET.equals(method) )
            ServletOps.errorMethodNotAllowed("Not a GET or POST request");

        if ( HttpNames.METHOD_GET.equals(method) && action.getRequestQueryString() == null ) {
            ServletOps.warning(action, "Service Description / SPARQL Query / " + action.getRequestRequestURI());
            ServletOps.errorNotFound("Service Description: " + action.getRequestRequestURI());
        }

        // Use of the dataset describing parameters is checked later.
        try {
            Collection<String> x = acceptedParams(action);
            validateParams(action, x);
            validateRequest(action);
        } catch (ActionErrorException ex) {
            throw ex;
        }
        // Query not yet parsed.
    }

    /**
     * Validate the request after checking HTTP method and HTTP Parameters.
     * @param action HTTP Action
     */
    protected abstract void validateRequest(HttpAction action);

    /**
     * Helper method for validating request.
     * @param request HTTP request
     * @param params parameters in a collection of Strings
     */
    protected void validateParams(HttpAction action, Collection<String> params) {
        HttpServletRequest request = action.getRequest();
        ContentType ct = FusekiNetLib.getContentType(request);
        boolean mustHaveQueryParam = true;
        if ( ct != null ) {
            String incoming = ct.getContentTypeStr();

            if ( matchContentType(ctSPARQLQuery, ct) ) {
                mustHaveQueryParam = false;
                // Drop through.
            } else if ( matchContentType(ctHTMLForm, ct)) {
                // Nothing specific to do
            }
            else
                ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Unsupported: " + incoming);
        }

        // GET/POST of a form at this point.

        if ( mustHaveQueryParam ) {
            int N = SPARQLProtocol.countParamOccurences(request, paramQuery);

            if ( N == 0 )
                ServletOps.errorBadRequest("SPARQL Query: No 'query=' parameter");
            if ( N > 1 )
                ServletOps.errorBadRequest("SPARQL Query: Multiple 'query=' parameters");

            // application/sparql-query does not use a query param.
            String queryStr = request.getParameter(HttpNames.paramQuery);

            if ( queryStr == null )
                ServletOps.errorBadRequest("SPARQL Query: No query specified (no 'query=' found)");
            if ( queryStr.isEmpty() )
                ServletOps.errorBadRequest("SPARQL Query: Empty query string");
        }

        if ( params != null ) {
            Enumeration<String> en = request.getParameterNames();
            for (; en.hasMoreElements();) {
                String name = en.nextElement();
                if ( !params.contains(name) )
                    ServletOps.warning(action, "SPARQL Query: Unrecognized request parameter (ignored): " + name);
            }
        }
    }

    @Override
    public final void execute(HttpAction action) {
        // GET
        if ( action.getRequestMethod().equals(HttpNames.METHOD_GET) ) {
            executeWithParameter(action);
            return;
        }

        ContentType ct = ActionLib.getContentType(action);

        // POST application/x-www-form-url
        // POST ?query= and no Content-Type
        if ( ct == null || isHtmlForm(ct) ) {
            // validation checked that if no Content-type, then its a POST with ?query=
            executeWithParameter(action);
            return;
        }

        // POST application/sparql-query
        if ( matchContentType(ct, ctSPARQLQuery) ) {
            executeBody(action);
            return;
        }

        ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: " + ct.getContentTypeStr());
    }

    protected void executeWithParameter(HttpAction action) {
        String queryString = action.getRequestParameter(paramQuery);
        execute(queryString, action);
    }

    protected void executeBody(HttpAction action) {
        String queryString = null;
        try {
            InputStream input = action.getRequestInputStream();
            queryString = IO.readWholeFileAsUTF8(input);
        } catch (Throwable ex) {
            ActionLib.consumeBody(action);
            ServletOps.errorOccurred(ex);
        }
        execute(queryString, action);
    }

    protected void execute(String queryString, HttpAction action) {
        String queryStringLog = ServletOps.formatForLog(queryString);
        if ( action.verbose ) {
            String str = queryString;
            if ( str.endsWith("\n") )
                str = str.substring(0, str.length()-1);
            action.log.info(format("[%d] Query = \n%s", action.id, str));
        }
        else
            action.log.info(format("[%d] Query = %s", action.id, queryStringLog));

        Query query = null;
        try {
            // NB syntax is ARQ (a superset of SPARQL)
            query = QueryFactory.create(queryString, QueryParseBase, Syntax.syntaxARQ);
            queryStringLog = formatForLog(query);
            validateQuery(action, query);
        } catch (ActionErrorException ex) {
            throw ex;
        } catch (QueryParseException ex) {
            String msg = SPARQLProtocol.messageForParseException(ex);
            action.log.warn(format("[%d] %s", action.id, msg));
            ServletOps.errorBadRequest(msg);
        } catch (QueryException ex) {
            String msg = SPARQLProtocol.messageForException(ex);
            // Should not happen.
            action.log.warn(format("[%d] %s", action.id, msg));
            ServletOps.errorBadRequest("Error: \n" + queryString + "\n" + msg);
        }

        // Assumes finished whole thing by end of sendResult.
        try {
            action.beginRead();
            Pair<DatasetGraph, Query> p = decideDataset(action, query, queryStringLog);
            DatasetGraph dataset = p.getLeft();
            Query q = p.getRight();
            if ( q == null )
                q = query;

            try ( QueryExecution qExec = createQueryExecution(action, q, dataset); ) {
                SPARQLResult result = executeQuery(action, qExec, query, queryStringLog);
                // Deals with exceptions itself.
                sendResults(action, result, query.getPrologue());
            }
        }
        catch (QueryParseException ex) {
            // Late stage static error (e.g. bad fixed Lucene query string).
            ServletOps.errorBadRequest("Query parse error: \n" + queryString + "\n" + SPARQLProtocol.messageForException(ex));
        }
        catch (QueryCancelledException ex) {
            // Additional counter information.
            incCounter(action.getEndpoint().getCounters(), QueryTimeouts);
            throw ex;
        } finally { action.endRead(); }
    }

    /**
     * Check the query - if unacceptable, throw ActionErrorException
     * or call on of the {@link ServletOps#error} operations.
     * @param action HTTP Action
     * @param query  SPARQL Query
     */
    protected abstract void validateQuery(HttpAction action, Query query);

    /** Create the {@link QueryExecution} for this operation.
     * @param action
     * @param query
     * @param dataset
     * @return QueryExecution
     */
    protected QueryExecution createQueryExecution(HttpAction action, Query query, DatasetGraph dataset) {
        QueryExecDatasetBuilder builder = QueryExec.newBuilder()
                .dataset(dataset)
                .query(query)
                .context(action.getContext())
                ;
        setTimeouts(builder, action);
        QueryExec qExec = builder.build();
        return QueryExecutionAdapter.adapt(qExec);
    }

    /**
     * Set the timeouts. The context timeout, which is the system settings, provides
     * an upper bound to setting by protocol ?timeout.
     */
    private static void setTimeouts(QueryExecDatasetBuilder builder, HttpAction action) {
        // Protocol settings.
        long protocolInitialTimeout = -1;
        long protocolOverallTimeout = -1;
        String timeoutParameter = action.getRequestParameter("timeout");
        if ( timeoutParameter != null ) {
            Pair<Long, Long> pair1 = Timeouts.parseTimeoutStr(timeoutParameter, TimeUnit.SECONDS);
            if ( pair1 != null ) {
                protocolInitialTimeout = pair1.getLeft();
                protocolOverallTimeout = pair1.getRight();
            }
        }

        // Timeout from context.
        long cxtInitialTimeout = -1;
        long cxtOverallTimeout = -1;
        String timeoutCxt = action.getContext().getAsString(ARQ.queryTimeout);
        if ( timeoutCxt != null ) {
            Pair<Long, Long> pair2 = Timeouts.parseTimeoutStr(timeoutCxt, TimeUnit.MILLISECONDS);
            cxtInitialTimeout = pair2.getLeft();
            cxtOverallTimeout = pair2.getRight();
        }
        long initialTimeout = chooseTimeout(cxtInitialTimeout, protocolInitialTimeout);
        long overallTimeout = chooseTimeout(cxtOverallTimeout, protocolOverallTimeout);

        if( initialTimeout > 0 )
            builder.initialTimeout(initialTimeout, TimeUnit.MILLISECONDS);
        if( overallTimeout > 0 )
            builder.overallTimeout(overallTimeout, TimeUnit.MILLISECONDS);
    }

    // Choose the timeout : setupTimeout (if set) limits the protocolTimeout.
    private static long chooseTimeout(long setupTimeout, long protocolTimeout) {
        if ( setupTimeout < 0 )
            return protocolTimeout;
        if (protocolTimeout > 0 )
            return Math.min(setupTimeout, protocolTimeout);
        else
            return setupTimeout;
    }

    /** Perform the {@link QueryExecution} once.
     * @param action
     * @param queryExecution
     * @param requestQuery Original query; queryExecution query may have been modified.
     * @param queryStringLog Informational string created from the initial query.
     * @return
     */
    protected SPARQLResult executeQuery(HttpAction action, QueryExecution queryExecution, Query requestQuery, String queryStringLog) {
        if ( requestQuery.isSelectType() ) {
            ResultSet rs = queryExecution.execSelect();

            // Force some query execution now.
            // If the timeout-first-row goes off, the output stream has not
            // been started so the HTTP error code is sent.

            rs.hasNext();

            // If we wanted perfect query time cancellation, we could consume
            // the result now to see if the timeout-end-of-query goes off.
            // rs = ResultSetFactory.copyResults(rs);

            //action.log.info(format("[%d] exec/select", action.id));
            return new SPARQLResult(rs);
        }

        if ( requestQuery.isConstructType() ) {
            Dataset dataset = queryExecution.execConstructDataset();
            //action.log.info(format("[%d] exec/construct", action.id));
            return new SPARQLResult(dataset);
        }

        if ( requestQuery.isDescribeType() ) {
            Model model = queryExecution.execDescribe();
            //action.log.info(format("[%d] exec/describe", action.id));
            return new SPARQLResult(model);
        }

        if ( requestQuery.isAskType() ) {
            boolean b = queryExecution.execAsk();
            //action.log.info(format("[%d] exec/ask", action.id));
            return new SPARQLResult(b);
        }

        if ( requestQuery.isJsonType() ) {
            Iterator<JsonObject> jsonIterator = queryExecution.execJsonItems();
            //JsonArray jsonArray = queryExecution.execJson();
            action.log.info(format("[%d] exec/json", action.id));
            return new SPARQLResult(jsonIterator);
        }

        ServletOps.errorBadRequest("Unknown query type - " + queryStringLog);
        return null;
    }

    /** Choose the dataset for this SPARQL Query request.
     * @param action
     * @param query  Query - this may be modified to remove a DatasetDescription.
     * @param queryStringLog
     * @return Pair of {@link Dataset} and {@link Query}.
     */
    protected abstract Pair<DatasetGraph, Query> decideDataset(HttpAction action, Query query, String queryStringLog);

    /** Ship the results to the remote caller.
     * @param action
     * @param result
     * @param qPrologue
     */
    protected void sendResults(HttpAction action, SPARQLResult result, Prologue qPrologue) {
        if ( result.isResultSet() )
            ResponseResultSet.doResponseResultSet(action, result.getResultSet(), qPrologue);
        else if ( result.isDataset() )
            // CONSTRUCT is processed as a extended CONSTRUCT - result is a dataset.
            ResponseDataset.doResponseDataset(action, result.getDataset());
        else if ( result.isModel() )
            // DESCRIBE results are models
            ResponseDataset.doResponseModel(action, result.getModel());
        else if ( result.isBoolean() )
            ResponseResultSet.doResponseResultSet(action, result.getBooleanResult());
        else if ( result.isJson() )
            ResponseJson.doResponseJson(action, result.getJsonItems());
        else
            ServletOps.errorOccurred("Unknown or invalid result type");
    }

    private String formatForLog(Query query) {
        IndentedLineBuffer out = new IndentedLineBuffer();
        out.setFlatMode(true);
        query.serialize(out);
        return out.asString();
    }

    // ---- Query parameters for validation
    /**
     * Create the set of all parameters passed by validation.
     * This is called once only.
     * Override {@link acceptedParams} for a full dynamic choice.
     */
    protected Set<String> generateAcceptedParams() {
        Set<String> x  = new HashSet<>();
        x.addAll(stdParams());
        x.addAll(fusekiParams());
        x.addAll(customParams());
        return x;
    }

    private static Collection<String> customParams_ = Collections.emptyList();
    /** Extension parameters : called once during parameter collection setup. */
    protected Collection<String> customParams() {
        return customParams_;
    }

    /** The parameters in the SPARQL Protocol for query */
    private static Collection<String> stdParams_ = Arrays.asList(paramQuery, paramDefaultGraphURI, paramNamedGraphURI);

    protected Collection<String> stdParams() { return stdParams_; }

    /** The parameters Fuseki also provides */
    private static Collection<String> fusekiParams_ = Arrays.asList(paramQueryRef, paramStyleSheet, paramAccept,
                                                                    paramOutput1, paramOutput2, paramOutput3,
                                                                    paramCallback, paramForceAccept, paramTimeout);

    protected Collection<String> fusekiParams() { return fusekiParams_; }
}
