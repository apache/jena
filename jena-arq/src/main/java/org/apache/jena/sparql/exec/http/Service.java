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

//import static org.apache.jena.query.ARQ.*;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.RegistryHttpClient;
import org.apache.jena.query.*;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpAsQuery ;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.http.HttpParams;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.engine.iterator.QueryIter;
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
    public static final String base = ARQ.arqParamNS;

    public static final Symbol serviceParams            = ARQ.serviceParams;
    public static final Symbol httpServiceAllowed       = ARQ.httpServiceAllowed;
//    //public static final Symbol httpQueryCompression    = ARQ.httpQueryCompression;
    public static final Symbol httpQueryClient          = ARQ.httpQueryClient;
    public static final Symbol httpServiceSendMode      = ARQ.httpServiceSendMode;
    @Deprecated(since = "4.8.0")
    public static final Symbol httpServiceContext    = ARQ.httpServiceContext;
//    // No connection timeout which is now in HttpClient
    public static final Symbol httpQueryTimeout         = ARQ.httpQueryTimeout;

    // ContextBuilder?
    private static Context emptyContext = Context.emptyContext();

    // Old names.
    public static final String baseOld = "http://jena.hpl.hp.com/Service#";
    public static final Symbol oldQueryClient       = SystemARQ.allocSymbol(baseOld, "queryClient");
    public static final Symbol oldServiceContext    = SystemARQ.allocSymbol(baseOld, "serviceContext");
    public static final Symbol oldServiceAllowed    = SystemARQ.allocSymbol(baseOld, "serviceAllowed");
    public static final Symbol oldQueryTimeout      = SystemARQ.allocSymbol(baseOld, "queryTimeout");
    public static final Symbol oldQueryCompression  = SystemARQ.allocSymbol(baseOld, "queryCompression");

    // Compatibility with old AHC (Apache HttpClient) version
    public static final Symbol serviceAllowed    = SystemARQ.allocSymbol(baseOld, "serviceAllowed");

    private static void checkForOldParameters(Context context) {
        if ( context == null )
            return ;
        checkForOldParameter(context, oldQueryClient);
        checkForOldParameter(context, oldServiceContext);
        checkForOldParameter(context, oldServiceAllowed);
        checkForOldParameter(context, oldQueryTimeout);
        checkForOldParameter(context, oldQueryCompression);
    }

    private static void checkForOldParameter(Context context, Symbol oldSymbol) {
        if ( context.isDefined(oldSymbol) )
            Log.warnOnce(LOGGER, "Service context parameter '"+oldSymbol.getSymbol()+"' no longer used - see ARQ constants for replacements.", oldSymbol);
    }

    /** Test whether SERVICE calls out of this JVM are allowed. */
    public static void checkServiceAllowed(Context context) {
        if ( ! ARQ.globalServiceAllowed )
            serviceDisabled();

        if ( context == null )
            context = ARQ.getContext();

        Boolean b1 = getBoolean(context, httpServiceAllowed);
        if ( b1 != null) {
            if ( b1 )
                return;
            serviceNotEnabled();
        }
        Boolean b2 = getBoolean(context, oldServiceAllowed);
        if ( b2 != null) {
            if ( b2 )
                return;
            serviceNotEnabled();
        }
        // Not set.
        if ( ! ARQ.allowServiceDefault )
            serviceNotEnabled();
    }

    private static Boolean getBoolean(Context context, Symbol symbol) {
        try {
             return context.getTrueOrFalse(httpServiceAllowed);
        } catch (Throwable ex) {
            throw new QueryException("Failed to read content setting  "+symbol.getSymbol());
        }
    }

    private static void serviceNotEnabled() {
        throw new QueryDeniedException("SERVICE execution disabled - enable with "+httpServiceAllowed) ;
    }

    private static void serviceDisabled() {
        throw new QueryDeniedException("SERVICE execution disabled") ;
    }

    /** Plain service execution. */
    public static QueryIterator exec(OpService op, Context context) {
        checkServiceAllowed(context);
        //checkForOldParameters(context);

        if ( context == null )
            context = emptyContext;

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
        }

        // This relies on the observation that the query was originally correct,
        // so reversing the scope renaming is safe (it merely restores the
        // algebra expression).
        //
        // Any variables that reappear should be internal ones that were hidden
        // by renaming in the first place.
        //
        // Any substitution is also safe because it replaces variables by
        // values.
        //
        // It is safer to rename/unrename than skipping SERVICE during rename
        // to avoid substituting hidden variables.

        Op opRestored = Rename.reverseVarRename(opRemote, true);
        query = OpAsQuery.asQuery(opRestored);
        // Transforming: Same object means "no change"
        boolean requiresRemapping = false;
        Map<Var, Var> varMapping = null;
        if ( ! opRestored.equals(opRemote) ) {
            varMapping = new HashMap<>();
            Set<Var> originalVars = OpVars.visibleVars(op);
            Set<Var> remoteVars = OpVars.visibleVars(opRestored);

            for (Var v : originalVars) {
                if (v.getName().contains("/")) {
                    // A variable which was scope renamed so has a different name
                    String origName = v.getName().substring(v.getName().lastIndexOf('/') + 1);
                    Var remoteVar = Var.alloc(origName);
                    if (remoteVars.contains(remoteVar)) {
                        varMapping.put(remoteVar, v);
                        requiresRemapping = true;
                    }
                } else {
                    // A variable which does not have a different name
                    if (remoteVars.contains(v))
                        varMapping.put(v, v);
                }
            }
        }

        // -- Setup
        //boolean withCompression = context.isTrueOrUndef(httpQueryCompression);
        long timeoutMillis = timeoutFromContext(context);

        // RegistryServiceModifier is applied by QueryExecHTTP
        Params serviceParams = getServiceParamsFromContext(serviceURL, context);

        HttpClient httpClient = chooseHttpClient(serviceURL, context);

        QuerySendMode querySendMode = chooseQuerySendMode(serviceURL, context, QuerySendMode.asGetWithLimitBody);

        // -- End setup

        // Build the execution
        QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .endpoint(serviceURL)
                .timeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .query(query)
                .params(serviceParams)
                .context(context)
                .httpClient(httpClient)
                .sendMode(querySendMode)
                .build();
        try {
            // Detach from the network stream.
            RowSet rowSet = qExec.select().materialize();
            QueryIterator qIter = QueryIterPlainWrapper.create(rowSet);
            if (requiresRemapping)
                qIter = QueryIter.map(qIter, varMapping);
            return qIter;
        } catch (HttpException ex) {
            throw QueryExceptionHTTP.rewrap(ex);
        }
    }

    private static HttpClient chooseHttpClient(String serviceURL, Context context) {
        // [QExec] Done in HttpLib?
        // -- RegistryHttpClient : preferred way to set a custom HttpClient
        HttpClient httpClient = RegistryHttpClient.get().find(serviceURL);
        if ( httpClient == null && context != null ) {
            // Check for old setting.
            if ( context.isDefined(oldQueryClient) )
                LOGGER.warn("Deprecated context symbol "+oldQueryClient+". See "+httpQueryClient+".");

            Object client = context.get(httpQueryClient);
            if ( client != null ) {
                // Check for old HttpClient
                if ( client.getClass().getName().equals("org.apache.http.client.HttpClient") ) {
                    LOGGER.warn("Found Apache HttpClient for context symbol "+httpQueryClient+". Jena now uses java.net.http.HttpClient");
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

    /**
     * Choose the QuerySendMode for execution.
     * Normally, this is the system default.
     * <p>
     * Also allow it to be set specifically for SERVICE execution by setting httpServiceSendMode
     * ("arq:httpServiceSendMode" in a Fuseki configuration file).
     * <p>
     * Acceptable values are
     * <ul>
     * <li>"POST or "GET"</li>
     * <li> a {@link QuerySendMode} object</li>
     * <li> a string with the same name as a {@link QuerySendMode}</li>
     * <ul>
     */
    private static QuerySendMode chooseQuerySendMode(String serviceURL, Context context, QuerySendMode dftValue) {
        if ( context == null )
            return dftValue;
        Object querySendMode = context.get(httpServiceSendMode, dftValue);
        if ( querySendMode == null )
            return dftValue;

        if (querySendMode instanceof QuerySendMode)
            // handle enum type from Java API
            return (QuerySendMode) querySendMode;

        if (querySendMode instanceof String) {
            String str = (String) querySendMode;
            // Specials.
            if ( "POST".equalsIgnoreCase(str) )
                return QuerySendMode.asPost;
            if ( "GET".equalsIgnoreCase(str) )
                return QuerySendMode.asGetAlways;
            try {
                // "asGetAlways", "asGetWithLimitForm", "asGetWithLimitBody", "asGetAlways", "asPostForm", "asPost"
                return QuerySendMode.valueOf((String) querySendMode);
            } catch (IllegalArgumentException ex) {
                throw new QueryExecException("Failed to interpret '"+querySendMode+"' as a query send mode");
            }
        }
        FmtLog.warn(Service.class,
                    "Unrecognized object type '%s' as a query send mode - ignored", querySendMode.getClass().getSimpleName());
        return dftValue;
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

        Object obj = context.get(serviceParams);

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
