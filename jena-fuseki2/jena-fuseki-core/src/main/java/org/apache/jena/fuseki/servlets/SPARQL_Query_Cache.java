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

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.cache.CacheAction;
import org.apache.jena.fuseki.cache.CacheEntry;
import org.apache.jena.fuseki.cache.CacheStore;
import org.apache.jena.query.*;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.web.HttpSC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static java.lang.String.format;
import static org.apache.jena.fuseki.server.CounterName.QueryTimeouts;
import static org.apache.jena.riot.WebContent.*;
import static org.apache.jena.riot.web.HttpNames.*;
import static org.apache.jena.riot.web.HttpNames.paramTimeout;

public class SPARQL_Query_Cache extends SPARQL_Protocol {

    private static final String QueryParseBase = Fuseki.BaseParserSPARQL ;

    public SPARQL_Query_Cache() {
        super() ;
    }

    // All the params we support
    protected static List<String> allParams = Arrays.asList(paramQuery, paramDefaultGraphURI, paramNamedGraphURI,
            paramQueryRef, paramStyleSheet, paramAccept, paramOutput1,
            paramOutput2, paramCallback, paramForceAccept, paramTimeout) ;

    // Choose REST verbs to support.

    // doMethod : Not used with UberServlet dispatch.

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response) ;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response) ;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCommonHeadersForOptions(response) ;
        response.setHeader(HttpNames.hAllow, "GET,OPTIONS,POST") ;
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }

    protected void doOptions(HttpAction action) {
        doOptions(action.request, action.response) ;
    }

    @Override
    protected void validate(HttpAction action) {
    }

    @Override
    protected void perform(HttpAction action) {

        // OPTIONS
        if ( action.request.getMethod().equals(HttpNames.METHOD_OPTIONS) ) {
            // Share with update via SPARQL_Protocol.
            doOptions(action) ;
            return ;
        }

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
                SPARQLResult result = null;
                CacheAction cacheAction = null;
                CacheStore cacheStore = CacheStore.getInstance();
                String key = generateKey(action, query, queryString);
                CacheEntry cacheEntry = (CacheEntry) cacheStore.doGet(key);
                if(cacheEntry == null || !cacheEntry.isInitialized()) {
                    log.info("Cache is null or cache data is not initialized");
                    ActionSPARQL queryServlet    = new SPARQL_QueryDataset() ;
                    queryServlet.executeLifecycle(action) ;
                }else {
                    log.info("Cache is not null so read from cache");
                    result = cacheEntry.getResult();
                    cacheAction = new CacheAction(key,CacheAction.Type.READ_CACHE);
                    sendResults(action, result, query.getPrologue(), cacheAction);
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
    /** Ship the results to the remote caller.
     * @param action
     * @param result
     * @param qPrologue
     */
    protected void sendResults(HttpAction action, SPARQLResult result, Prologue qPrologue, CacheAction cacheAction) {
        if ( result.isResultSet() )
            ResponseResultSet.doResponseResultSet(action, result.getResultSet(), qPrologue, cacheAction) ;
        else if ( result.isDataset() )
            // CONSTRUCT is processed as a extended CONSTRUCT - result is a dataset.
            ResponseDataset.doResponseDataset(action, result.getDataset());
        else if ( result.isModel() )
            // DESCRIBE results are models
            ResponseDataset.doResponseModel(action, result.getModel());
        else if ( result.isBoolean() )
            ResponseResultSet.doResponseResultSet(action, result.getBooleanResult(), cacheAction) ;
        else
            ServletOps.errorOccurred("Unknown or invalid result type") ;
    }

    private String generateKey(HttpAction action, Query query ,String queryString){
        ResponseType responseType = null;
        if(query.isAskType())
            responseType = ResponseResultSet.getResponseType(action.getRequest(), DEF.rsOfferBoolean);
        else if(query.isSelectType())
            responseType = ResponseResultSet.getResponseType(action.getRequest(), DEF.rsOfferTable);
        else if(query.isConstructType())
            responseType = ResponseDataset.getResponseType(action.getRequest());
        else if(query.isDescribeType())
            responseType = ResponseDataset.getResponseType(action.getRequest());

        return CacheStore.generateKey(action, queryString, responseType);
    }

    private String formatForLog(Query query) {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        out.setFlatMode(true) ;
        query.serialize(out) ;
        return out.asString() ;
    }
}
