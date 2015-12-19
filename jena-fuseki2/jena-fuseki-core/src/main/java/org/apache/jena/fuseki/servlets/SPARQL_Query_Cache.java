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
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.cache.CacheAction;
import org.apache.jena.fuseki.cache.CacheEntry;
import org.apache.jena.query.*;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.resultset.SPARQLResult;
import org.apache.jena.web.HttpSC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static org.apache.jena.fuseki.server.CounterName.QueryTimeouts;
import static org.apache.jena.riot.WebContent.*;
import static org.apache.jena.riot.web.HttpNames.*;
import static org.apache.jena.riot.web.HttpNames.paramTimeout;

public class SPARQL_Query_Cache extends SPARQL_QueryDataset {

    private static final String QueryParseBase = Fuseki.BaseParserSPARQL ;

    private static final int CACHE_SIZE = 10000;

    private static ConcurrentHashMap<String, Cache> cacheDatasetMap = new ConcurrentHashMap<>();

    @Override
    protected void execute(String queryString, HttpAction action) {
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
                Cache cache = getCache(SPARQL_Query_Cache.getDatasetUri(action));
                String key = generateKey(action, query);
                CacheEntry cacheEntry = (CacheEntry) cache.getIfPresent(key);

                if(cacheEntry == null || !cacheEntry.isInitialized()) {
                    log.debug("Cache is null or cache data is not initialized");
                    super.execute(queryString, action);
                }else {
                    log.debug("Cache is not null so read from cache");
                    SPARQLResult result = cacheEntry.getResult();
                    CacheAction cacheAction = new CacheAction(key,CacheAction.Type.READ_CACHE);
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

    public static String generateKey(HttpAction action, Query query){
        ResponseType responseType = null;
        if(query.isAskType())
            responseType = ResponseResultSet.getResponseType(action.getRequest(), DEF.rsOfferBoolean);
        else if(query.isSelectType())
            responseType = ResponseResultSet.getResponseType(action.getRequest(), DEF.rsOfferTable);
        else if(query.isConstructType())
            responseType = ResponseDataset.getResponseType(action.getRequest());
        else if(query.isDescribeType())
            responseType = ResponseDataset.getResponseType(action.getRequest());

        return getKey(query, responseType);
    }

    private String formatForLog(Query query) {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        out.setFlatMode(true) ;
        query.serialize(out) ;
        return out.asString() ;
    }

    private static String getKey(Query query, ResponseType responseType) {
        return query + responseType.getContentType();
    }

    public static Cache getCache(String uri) {
        if(cacheDatasetMap.containsKey(uri)){
            return cacheDatasetMap.get(uri);
        }else{
            Cache cache = CacheFactory.createCache(CACHE_SIZE);
            cacheDatasetMap.putIfAbsent(uri,cache);
            return cacheDatasetMap.get(uri);
        }
    }

    public static String getDatasetUri(HttpAction action){
        HttpServletRequest req = action.getRequest();
        String uri = ActionLib.actionURI(req);
        return ActionLib.mapActionRequestToDataset(uri);
    }

}
