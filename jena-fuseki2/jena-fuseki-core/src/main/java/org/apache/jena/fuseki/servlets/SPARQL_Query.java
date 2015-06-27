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

package org.apache.jena.fuseki.servlets ;

import static java.lang.String.format ;
import static org.apache.jena.fuseki.server.CounterName.QueryTimeouts ;
import static org.apache.jena.riot.WebContent.ctHTMLForm ;
import static org.apache.jena.riot.WebContent.ctSPARQLQuery ;
import static org.apache.jena.riot.WebContent.isHtmlForm ;
import static org.apache.jena.riot.WebContent.matchContentType ;
import static org.apache.jena.riot.web.HttpNames.paramAccept ;
import static org.apache.jena.riot.web.HttpNames.paramCallback ;
import static org.apache.jena.riot.web.HttpNames.paramDefaultGraphURI ;
import static org.apache.jena.riot.web.HttpNames.paramForceAccept ;
import static org.apache.jena.riot.web.HttpNames.paramNamedGraphURI ;
import static org.apache.jena.riot.web.HttpNames.paramOutput1 ;
import static org.apache.jena.riot.web.HttpNames.paramOutput2 ;
import static org.apache.jena.riot.web.HttpNames.paramQuery ;
import static org.apache.jena.riot.web.HttpNames.paramQueryRef ;
import static org.apache.jena.riot.web.HttpNames.paramStyleSheet ;
import static org.apache.jena.riot.web.HttpNames.paramTimeout ;

import java.io.IOException ;
import java.io.InputStream ;
import java.util.* ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.sparql.core.Prologue ;
import org.apache.jena.sparql.resultset.SPARQLResult ;
import org.apache.jena.web.HttpSC ;

/** Handle SPARQL Query requests overt eh SPARQL Protocol. 
 * Subclasses provide this algorithm with the actual dataset to query, whether
 * a dataset hosted by this server ({@link SPARQL_QueryDataset}) or 
 * speciifed in the protocol request ({@link SPARQL_QueryGeneral}).   
 */ 
public abstract class SPARQL_Query extends SPARQL_Protocol
{
    private static final String QueryParseBase = Fuseki.BaseParserSPARQL ;
    
    public SPARQL_Query() {
        super() ;
    }

    // Choose REST verbs to support.

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response) ;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response) ;
    }

    // HEAD

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCommonHeadersForOptions(response) ;
        response.setHeader(HttpNames.hAllow, "GET,OPTIONS,POST") ;
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }

    @Override
    protected final void perform(HttpAction action) {
        // GET
        if ( action.request.getMethod().equals(HttpNames.METHOD_GET) ) {
            executeWithParameter(action) ;
            return ;
        }

        ContentType ct = FusekiLib.getContentType(action) ;

        // POST application/x-www-form-url
        // POST ?query= and no Content-Type
        if ( ct == null || isHtmlForm(ct) ) {
            // validation checked that if no Content-type, then its a POST with ?query=
            executeWithParameter(action) ;
            return ;
        }

        // POST application/sparql-query
        if ( matchContentType(ct, ctSPARQLQuery) ) {
            executeBody(action) ;
            return ;
        }

        ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Bad content type: " + ct.getContentType()) ;
    }

    // All the params we support

    protected static List<String> allParams = Arrays.asList(paramQuery, paramDefaultGraphURI, paramNamedGraphURI,
                                                            paramQueryRef, paramStyleSheet, paramAccept, paramOutput1,
                                                            paramOutput2, paramCallback, paramForceAccept, paramTimeout) ;

    /**
     * Validate the request, checking HTTP method and HTTP Parameters.
     * @param action HTTP Action
     */
    @Override
    protected void validate(HttpAction action) {
        String method = action.request.getMethod().toUpperCase(Locale.ROOT) ;

        if ( !HttpNames.METHOD_POST.equals(method) && !HttpNames.METHOD_GET.equals(method) )
            ServletOps.errorMethodNotAllowed("Not a GET or POST request") ;

        if ( HttpNames.METHOD_GET.equals(method) && action.request.getQueryString() == null ) {
            ServletOps.warning(action, "Service Description / SPARQL Query / " + action.request.getRequestURI()) ;
            ServletOps.errorNotFound("Service Description: " + action.request.getRequestURI()) ;
        }

        // Use of the dataset describing parameters is check later.
        try {
            validateParams(action, allParams) ;
            validateRequest(action) ;
        } catch (ActionErrorException ex) {
            throw ex ;
        }
        // Query not yet parsed.
    }

    /**
     * Validate the request after checking HTTP method and HTTP Parameters.
     * @param action HTTP Action
     */
    protected abstract void validateRequest(HttpAction action) ;

    /**
     * Helper method for validating request.
     * @param request HTTP request
     * @param params parameters in a collection of Strings
     */
    protected void validateParams(HttpAction action, Collection<String> params) {
        HttpServletRequest request = action.request ;
        ContentType ct = FusekiLib.getContentType(request) ;
        boolean mustHaveQueryParam = true ;
        if ( ct != null ) {
            String incoming = ct.getContentType() ;

            if ( matchContentType(ctSPARQLQuery, ct) ) {
                mustHaveQueryParam = false ;
                // Drop through.
            } else if ( matchContentType(ctHTMLForm, ct)) {
                // Nothing specific to do
            } 
            else
                ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "Unsupported: " + incoming) ;
        }

        // GET/POST of a form at this point.

        if ( mustHaveQueryParam ) {
            int N = countParamOccurences(request, paramQuery) ;

            if ( N == 0 )
                ServletOps.errorBadRequest("SPARQL Query: No 'query=' parameter") ;
            if ( N > 1 )
                ServletOps.errorBadRequest("SPARQL Query: Multiple 'query=' parameters") ;

            // application/sparql-query does not use a query param.
            String queryStr = request.getParameter(HttpNames.paramQuery) ;

            if ( queryStr == null )
                ServletOps.errorBadRequest("SPARQL Query: No query specified (no 'query=' found)") ;
            if ( queryStr.isEmpty() )
                ServletOps.errorBadRequest("SPARQL Query: Empty query string") ;
        }

        if ( params != null ) {
            Enumeration<String> en = request.getParameterNames() ;
            for (; en.hasMoreElements();) {
                String name = en.nextElement() ;
                if ( !params.contains(name) )
                    ServletOps.warning(action, "SPARQL Query: Unrecognize request parameter (ignored): " + name) ;
            }
        }
    }

    private void executeWithParameter(HttpAction action) {
        String queryString = action.request.getParameter(paramQuery) ;
        execute(queryString, action) ;
    }

    private void executeBody(HttpAction action) {
        String queryString = null ;
        try {
            InputStream input = action.request.getInputStream() ;
            queryString = IO.readWholeFileAsUTF8(input) ;
        } catch (IOException ex) {
            ServletOps.errorOccurred(ex) ;
        }
        execute(queryString, action) ;
    }

    private void execute(String queryString, HttpAction action) {
        String queryStringLog = ServletOps.formatForLog(queryString) ;
        if ( action.verbose )
            action.log.info(format("[%d] Query = \n%s", action.id, queryString)) ;
        else
            action.log.info(format("[%d] Query = %s", action.id, queryStringLog)) ;

        Query query = null ;
        try {
            // NB syntax is ARQ (a superset of SPARQL)
            query = QueryFactory.create(queryString, QueryParseBase, Syntax.syntaxARQ) ;
            queryStringLog = formatForLog(query) ;
            validateQuery(action, query) ;
        } catch (ActionErrorException ex) {
            throw ex ;
        } catch (QueryParseException ex) {
            ServletOps.errorBadRequest("Parse error: \n" + queryString + "\n\r" + messageForQueryException(ex)) ;
        }
        // Should not happen.
        catch (QueryException ex) {
            ServletOps.errorBadRequest("Error: \n" + queryString + "\n\r" + ex.getMessage()) ;
        }

        // Assumes finished whole thing by end of sendResult.
        try {
            action.beginRead() ;
            Dataset dataset = decideDataset(action, query, queryStringLog) ;
            try ( QueryExecution qExec = createQueryExecution(query, dataset) ; ) {
                SPARQLResult result = executeQuery(action, qExec, query, queryStringLog) ;
                // Deals with exceptions itself.
                sendResults(action, result, query.getPrologue()) ;
            }
        } 
        catch (QueryParseException ex) {
            // Late stage static error (e.g. bad fixed Lucene query string). 
            ServletOps.errorBadRequest("Query parse error: \n" + queryString + "\n\r" + messageForQueryException(ex)) ;
        }
        catch (QueryCancelledException ex) {
            // Additional counter information.
            incCounter(action.getEndpoint().getCounters(), QueryTimeouts) ;
            throw ex ;
        } finally { action.endRead() ; }
    }

    /**
     * Check the query - if unacceptable, throw ActionErrorException or call
     * super.error
     * @param action HTTP Action
     * @param query  SPARQL Query
     */
    protected abstract void validateQuery(HttpAction action, Query query) ;

    /** Create the {@link QueryExecution} for this operation.
     * @param query
     * @param dataset
     * @return QueryExecution
     */
    protected QueryExecution createQueryExecution(Query query, Dataset dataset) {
        return QueryExecutionFactory.create(query, dataset) ;
    }

    /** Perform the {@link QueryExecution} once.
     * @param action
     * @param queryExecution
     * @param query
     * @param queryStringLog Informational string created from the initial query. 
     * @return
     */
    protected SPARQLResult executeQuery(HttpAction action, QueryExecution queryExecution, Query query, String queryStringLog) {
        setAnyTimeouts(queryExecution, action) ;

        if ( query.isSelectType() ) {
            ResultSet rs = queryExecution.execSelect() ;

            // Force some query execution now.
            //
            // If the timeout-first-row goes off, the output stream has not
            // been started so the HTTP error code is sent.

            rs.hasNext() ;

            // If we wanted perfect query time cancellation, we could consume
            // the result now
            // to see if the timeout-end-of-query goes off.

            // rs = ResultSetFactory.copyResults(rs) ;

            action.log.info(format("[%d] exec/select", action.id)) ;
            return new SPARQLResult(rs) ;
        }

        if ( query.isConstructType() ) {
            Model model = queryExecution.execConstruct() ;
            action.log.info(format("[%d] exec/construct", action.id)) ;
            return new SPARQLResult(model) ;
        }

        if ( query.isDescribeType() ) {
            Model model = queryExecution.execDescribe() ;
            action.log.info(format("[%d] exec/describe", action.id)) ;
            return new SPARQLResult(model) ;
        }

        if ( query.isAskType() ) {
            boolean b = queryExecution.execAsk() ;
            action.log.info(format("[%d] exec/ask", action.id)) ;
            return new SPARQLResult(b) ;
        }

        ServletOps.errorBadRequest("Unknown query type - " + queryStringLog) ;
        return null ;
    }

    private void setAnyTimeouts(QueryExecution qexec, HttpAction action) {
//        if ( !(action.getDataService().allowTimeoutOverride) )
//            return ;

        long desiredTimeout = Long.MAX_VALUE ;
        String timeoutHeader = action.request.getHeader("Timeout") ;
        String timeoutParameter = action.request.getParameter("timeout") ;
        if ( timeoutHeader != null ) {
            try {
                desiredTimeout = (int)(Float.parseFloat(timeoutHeader) * 1000) ;
            } catch (NumberFormatException e) {
                throw new FusekiException("Timeout header must be a number", e) ;
            }
        } else if ( timeoutParameter != null ) {
            try {
                desiredTimeout = (int)(Float.parseFloat(timeoutParameter) * 1000) ;
            } catch (NumberFormatException e) {
                throw new FusekiException("timeout parameter must be a number", e) ;
            }
        }

//        desiredTimeout = Math.min(action.getDataService().maximumTimeoutOverride, desiredTimeout) ;
        if ( desiredTimeout != Long.MAX_VALUE )
            qexec.setTimeout(desiredTimeout) ;
    }

    /** Choose the dataset for this SPARQL Query request. 
     * @param action
     * @param query
     * @param queryStringLog 
     * @return {@link Dataset}
     */
    protected abstract Dataset decideDataset(HttpAction action, Query query, String queryStringLog) ;

    /** Ship the results to the remote caller.
     * @param action
     * @param result
     * @param qPrologue
     */
    protected void sendResults(HttpAction action, SPARQLResult result, Prologue qPrologue) {
        if ( result.isResultSet() )
            ResponseResultSet.doResponseResultSet(action, result.getResultSet(), qPrologue) ;
        else if ( result.isGraph() )
            ResponseModel.doResponseModel(action, result.getModel()) ;
        else if ( result.isBoolean() )
            ResponseResultSet.doResponseResultSet(action, result.getBooleanResult()) ;
        else
            ServletOps.errorOccurred("Unknown or invalid result type") ;
    }

    private String formatForLog(Query query) {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        out.setFlatMode(true) ;
        query.serialize(out) ;
        return out.asString() ;
    }

    private String getRemoteString(String queryURI) {
        return HttpOp.execHttpGetString(queryURI) ;
    }

}
