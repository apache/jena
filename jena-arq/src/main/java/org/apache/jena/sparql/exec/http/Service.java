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

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.RegistryHttpClient;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpAsQuery ;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.http.HttpParams;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Execution of OpService */

public class Service {
    private Service() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    // [QExec] Put this in ARQ or ARQConstants
    public static final String base = ARQ.arqParamNS; //"http://jena.apache.org/ARQ/http#";
    public static final Symbol httpQueryCompression  = SystemARQ.allocSymbol("httpQueryCompression");
    public static final Symbol httpQueryClient       = SystemARQ.allocSymbol("httpQueryClient");
    // [QExec]
    public static final Symbol httpServiceContext    = SystemARQ.allocSymbol("httpServiceContext");
    public static final Symbol httpServiceAllowed    = SystemARQ.allocSymbol("httpServiceAllowed");
    // Not connection timeout which is now in HttpClient
    public static final Symbol httpQueryTimeout      = SystemARQ.allocSymbol("httpQueryTimeout");
    // ContextBuilder?

    private static Context emptyContext = Context.emptyContext();

    // Old names.
    public static final String baseOld = "http://jena.hpl.hp.com/Service#";
    public static final Symbol oldQueryClient       = SystemARQ.allocSymbol(baseOld, "queryClient");
    public static final Symbol oldServiceContext    = SystemARQ.allocSymbol(baseOld, "serviceContext");
    public static final Symbol oldServiceAllowed    = SystemARQ.allocSymbol(baseOld, "serviceAllowed");
    public static final Symbol oldQueryTimeout      = SystemARQ.allocSymbol(baseOld, "queryTimeout");
    public static final Symbol oldQueryCompression  = SystemARQ.allocSymbol(baseOld, "queryCompression");

    // Compatibility with old AHC (Aapche HttpClient)version
    public static final Symbol serviceAllowed    = SystemARQ.allocSymbol(baseOld, "serviceAllowed");

    private void oldCheckForOldParameters(Context context) {
        if ( context == null )
            return ;
        oldCheckForOldParameters(context, oldQueryClient);
        oldCheckForOldParameters(context, oldServiceContext);
        oldCheckForOldParameters(context, oldServiceAllowed);
        oldCheckForOldParameters(context, oldQueryTimeout);
        oldCheckForOldParameters(context, oldQueryCompression);
    }

    private void oldCheckForOldParameters(Context context, Symbol oldSymbol) {
        if ( context.isDefined(oldSymbol) )
            Log.warnOnce(LOGGER, "Service context parameter '"+oldSymbol.getSymbol()+"' no longer used - see ARQ constants for replacements.", oldSymbol);
    }

    public static QueryIterator exec(OpService op, Context context) {
        if ( context == null )
            context = emptyContext;

        if ( context != null && context.isFalse(httpServiceAllowed) )
            throw new QueryExecException("SERVICE execution disabled") ;

        if (!op.getService().isURI())
            throw new QueryExecException("Service URI not bound: " + op.getService());

        boolean silent = op.getSilent();
        // [QExec] Add getSubOpUnmodified();
        if (!op.getService().isURI())
            throw new QueryExecException("Service URI not bound: " + op.getService());
        String serviceURL = op.getService().getURI();

        Op opRemote = op.getSubOp();
        Query query;
        if ( false ) {
            // ***** Interacts with substitution.
            Element el = op.getServiceElement().getElement();
            if ( el instanceof ElementSubQuery )
                query = ((ElementSubQuery)el).getQuery();
            else {
                query = QueryFactory.create();
                query.setQueryPattern(el);
                query.setResultVars();
            }
        } else
            query = OpAsQuery.asQuery(opRemote);

        // -- Setup
        //boolean withCompression = context.isTrueOrUndef(httpQueryCompression);
        long timeoutMillis = timeoutFromContext(context);

        // RegistryServiceModifier is applied by QueryExecHTTP
        Params serviceParams = getServiceParamsFromContext(serviceURL, context);

        HttpClient httpClient = chooseHttpClient(serviceURL, context);
        // -- End setup

        // Build the execution
        QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .endpoint(serviceURL)
                .timeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .query(query)
                .params(serviceParams)
                .context(context)
                .httpClient(httpClient)
                .sendMode(QuerySendMode.asGetWithLimitBody)
                .build();
        try {
            // Detach from the network stream.
            RowSet rowSet = qExec.select().materialize();
            QueryIterator qIter = QueryIterPlainWrapper.create(rowSet);
            return qIter;
        } catch (HttpException ex) {
            throw QueryExceptionHTTP.rewrap(ex);
        }
    }

    private static HttpClient chooseHttpClient(String serviceURL, Context context) {
        // -- RegistryHttpClient : preferred way to set a custom HttpClient
        HttpClient httpClient = RegistryHttpClient.get().find(serviceURL);
        if ( httpClient == null && context != null ) {
            // Check for old setting.
            if ( context.isDefined(oldQueryClient) )
                LOGGER.warn("Deprecated context symbol "+oldQueryClient+". See "+httpQueryClient+".");

            Object client = context.get(httpQueryClient);
            if ( client != null ) {
                // Check for old HttpClient
                if ( client instanceof org.apache.http.client.HttpClient ) {
                    LOGGER.warn("Found Apache HttpClient for content symbol "+httpQueryClient+". Jena now uses java.net.http.HttpClient");
                    client = null;
                } else if ( client instanceof HttpClient ) {
                    httpClient = (HttpClient)client;
                } else {
                    LOGGER.warn("Not recognized "+httpQueryClient+" -> "+client);
                }
            }
        }
        // -- Default : common case.
        if (httpClient == null)
            httpClient = HttpEnv.getDftHttpClient();
        return httpClient;
    }

    // Timeout for connection is part of HttpClient (our default is 10s).
    /*package*/ static long timeoutFromContext(Context context) {
        return parseTimeout(context.get(httpQueryTimeout));
    }

    /** Find the timeout. Return -1L for no setting. */
    /*package*/ static long parseTimeout(Object obj) {
        if ( obj == null )
            return -1L;
        try {
            if ( obj instanceof Number )
                return ((Number)obj).longValue();
            if ( obj instanceof String )
                return Long.parseLong((String)obj);
            LOGGER.warn("Can't interpret timeout: " + obj);
            return -1L;
        } catch (Exception ex) {
            LOGGER.warn("Exception setting timeout (context) from: "+obj);
            return -1L;
        }
    }

    // Old way - retain but ARQ.httpRequestModifer preferred.
    // This is to allow setting additional/optional query parameters on a per
    // SERVICE level, see: JENA-195
    /*package*/ static Params getServiceParamsFromContext(String serviceURI, Context context) throws QueryExecException {
        Params params = Params.create();

        Object obj = context.get(ARQ.serviceParams);

        if ( obj == null )
            return params;

        // Old style.
        try {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, List<String>>> serviceParams = (Map<String, Map<String, List<String>>>)obj;
            if (serviceParams != null) {
                Map<String, List<String>> paramsMap = serviceParams.get(serviceURI);
                if (paramsMap != null) {
                    for (String param : paramsMap.keySet()) {
                        if (HttpParams.pQuery.equals(param))
                            throw new QueryExecException("ARQ serviceParams overrides the 'query' SPARQL protocol parameter");
                        List<String> values = paramsMap.get(param);
                        for (String value : values)
                            params.add(param, value);
                    }
                }
            }
            return params;
        } catch(Throwable ex) {
            LOGGER.warn("Failed to process "+obj+" : context value of ARQ.serviceParams");
            return null;
        }
    }
}
