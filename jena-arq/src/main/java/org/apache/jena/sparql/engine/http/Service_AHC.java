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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.query.ResultSet ;
import org.apache.jena.query.ResultSetFactory ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpAsQuery ;
import org.apache.jena.sparql.algebra.OpVars ;
import org.apache.jena.sparql.algebra.op.OpService ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.Rename ;
import org.apache.jena.sparql.engine.iterator.QueryIter ;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet ;
import org.apache.jena.sparql.mgt.Explain ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;

/** Execution of OpService */
@SuppressWarnings("deprecation")
public class Service_AHC {
    /* define the symbols that Service will use to set the HttpQuery parameters */
    public static final String base = "http://jena.hpl.hp.com/Service#";

    /**
     * Use to set the HttpQuery.allowCompression flag.
     */
    public static final Symbol queryCompression = SystemARQ.allocSymbol(base, "queryCompression");

    /**
     * Use to set the HTTP client for a service.
     */
    public static final Symbol queryClient = SystemARQ.allocSymbol(base, "queryClient");

    /**
     * Use this Symbol to allow passing additional service context variables
     * {@literal SERVICE <IRI>} call. Parameters need to be grouped by {@literal SERVICE <IRI>}, a
     * {@literal Map<String, Context>} is assumed. The key of the first map is the SERVICE
     * IRI, the value is a Context who's values will override any defaults in
     * the original context.
     */
    public static final Symbol serviceContext = SystemARQ.allocSymbol(base, "serviceContext");

    /**
     * Control whether SERVICE processing is allowed.
     * If the context contains this, and it is set to "false",
     * then SERVICE is not allowed.
     */

    public static final Symbol serviceAllowed = SystemARQ.allocSymbol(base, "serviceAllowed");

    /**
     * Set timeout. The value of this symbol gives the value of the timeout in
     * milliseconds
     * <ul>
     * <li>A Number; the long value is used</li>
     * <li>A string, e.g. "1000", parsed as a number</li>
     * <li>A string, as two numbers separated by a comma, e.g. "500,10000"
     * parsed as two numbers</li>
     * </ul>
     * The first value is passed to HttpQuery.setConnectTimeout() the second, if
     * it exists, is passed to HttpQuery.setReadTimeout()
     */
    public static final Symbol queryTimeout = SystemARQ.allocSymbol(base, "queryTimeout");

    /**
     * Executes a service operator
     *
     * @param op
     *            Service
     * @param context
     *            Context
     * @return Query iterator of service results
     */
    public static QueryIterator exec(OpService op, Context context) {
        if ( context != null && context.isFalse(serviceAllowed) )
            throw new QueryExecException("SERVICE execution disabled") ;

        if (!op.getService().isURI())
            throw new QueryExecException("Service URI not bound: " + op.getService());

        // This relies on the observation that the query was originally correct,
        // so reversing the scope renaming is safe (it merely restores the
        // algebra expression).
        // Any variables that reappear should be internal ones that were hidden
        // by renaming in the first place.
        // Any substitution is also safe because it replaced variables by
        // values.
        Op opRemote = Rename.reverseVarRename(op.getSubOp(), true);

        // JENA-494 There is a bug here that the renaming means that if this is
        // deeply nested and joined to other things at the same level of you end
        // up with the variables being disjoint and the same results
        // The naive fix for this is to map the variables visible in the inner
        // operator to those visible in the rewritten operator
        // There may be some cases where the re-mapping is incorrect due to
        // deeply nested SERVICE clauses
        Map<Var, Var> varMapping = new HashMap<>();
        Set<Var> originalVars = OpVars.visibleVars(op);
        Set<Var> remoteVars = OpVars.visibleVars(opRemote);

        boolean requiresRemapping = false;
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

        // Explain.explain("HTTP", opRemote, context) ;

        Query query;

        //@formatter:off
        // Comment (for the future?)
//        if ( false )
//        {
//            // ***** Interacts with substitution.
//            Element el = op.getServiceElement().getElement() ;
//            if ( el instanceof ElementSubQuery )
//                query = ((ElementSubQuery)el).getQuery() ;
//            else
//            {
//                query = QueryFactory.create() ;
//                query.setQueryPattern(el) ;
//                query.setResultVars() ;
//            }
//        }
//        else
        //@formatter:on
        query = OpAsQuery.asQuery(opRemote);

        Explain.explain("HTTP", query, context);
        String uri = op.getService().getURI();
        HttpQuery httpQuery = configureQuery(uri, context, query);
        QueryIterator qIter;
        try (InputStream in = httpQuery.exec()) {
            // Read the whole of the results now.
            // Avoids the problems with calling back into the same system e.g.
            // Fuseki+SERVICE <http://localhost:3030/...>

            ResultSet rs = ResultSetFactory.fromXML(in);
            qIter = QueryIter.materialize(new QueryIteratorResultSet(rs));
            // And close connection now, not when qIter is closed.
        } catch (IOException e) {
            throw new QueryExecException("Could not parse result set from XML", e);
        }

        // In some cases we may need to apply a re-mapping
        // This solves JENA-494 the naive way and may be brittle for complex
        // nested SERVICE clauses
        if (requiresRemapping) {
            qIter = QueryIter.map(qIter, varMapping);
        }

        return qIter;
    }

    /**
     * Create and configure the HttpQuery object.
     *
     * The parentContext is not modified but is used to create a new context
     * copy.
     *
     * @param uri
     *            The uri of the endpoint
     * @param parentContext
     *            The initial context.
     * @param Query
     *            the Query to execute.
     * @return An HttpQuery configured as per the context.
     */
    private static HttpQuery configureQuery(String uri, Context parentContext, Query query) {
        HttpQuery httpQuery = new HttpQuery(uri);
        Context context = new Context(parentContext);

        // add the context settings from the service context
        @SuppressWarnings("unchecked")
        Map<String, Context> serviceContextMap = (Map<String, Context>) context.get(serviceContext);
        if (serviceContextMap != null) {
            Context serviceContext = serviceContextMap.get(uri);
            if (serviceContext != null)
                context.putAll(serviceContext);
        }

        // configure the query object.
        httpQuery.merge(QueryEngineHTTP.getServiceParams(uri, context));
        httpQuery.addParam(HttpParams.pQuery, query.toString());
        httpQuery.setAllowCompression(context.isTrueOrUndef(queryCompression));

        HttpClient client = context.get(queryClient);
        if (client != null) httpQuery.setClient(client);

        setAnyTimeouts(httpQuery, context);

        return httpQuery;
    }

    /**
     * Modified from QueryExecutionBase
     *
     * @see org.apache.jena.sparql.engine.QueryExecutionBase
     */
    private static void setAnyTimeouts(HttpQuery query, Context context) {
        if (context.isDefined(queryTimeout)) {
            Object obj = context.get(queryTimeout);
            if (obj instanceof Number) {
                int x = ((Number) obj).intValue();
                query.setConnectTimeout(x);
            } else if (obj instanceof String) {
                try {
                    String str = obj.toString();
                    if (str.contains(",")) {

                        String[] a = str.split(",");
                        int x1 = Integer.parseInt(a[0]);
                        int x2 = Integer.parseInt(a[1]);
                        query.setConnectTimeout(x1);
                        query.setReadTimeout(x2);
                    } else {
                        int x = Integer.parseInt(str);
                        query.setConnectTimeout(x);
                    }
                } catch (NumberFormatException ex) {
                    throw new QueryExecException("Can't interpret string for timeout: " + obj);
                }
            } else {
                throw new QueryExecException("Can't interpret timeout: " + obj);
            }
        }
    }
}
