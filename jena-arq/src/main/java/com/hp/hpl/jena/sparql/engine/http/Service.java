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

package com.hp.hpl.jena.sparql.engine.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.riot.WebContent;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.Rename;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorResultSet;
import com.hp.hpl.jena.sparql.mgt.Explain;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

/** Execution of OpService */

public class Service {
    /* define the symbols that Service will use to set the HttpQuery parameters */
    public static final String base = "http://jena.hpl.hp.com/Service#";

    /**
     * Use to set the HttpQuery.allowDeflate flag.
     */
    public static final Symbol queryDeflate = ARQConstants.allocSymbol(base, "queryDeflate");

    /**
     * Use to set the HttpQuery.allowGZip flag.
     */
    public static final Symbol queryGzip = ARQConstants.allocSymbol(base, "queryGzip");

    /**
     * Use to set the user id for basic auth.
     */
    public static final Symbol queryAuthUser = ARQConstants.allocSymbol(base, "queryAuthUser");

    /**
     * Use to set the user password for basic auth.
     */
    public static final Symbol queryAuthPwd = ARQConstants.allocSymbol(base, "queryAuthPwd");

    /**
     * Use this Symbol to allow passing additional service context variables
     * SERVICE <IRI> call. Parameters need to be grouped by SERVICE <IRI>, a
     * Map<String, Context> is assumed. The key of the first map is the SERVICE
     * IRI, the value is a Context who's values will override any defaults in
     * the original context.
     * 
     * @see com.hp.hpl.jena.sparql.engine.http.Service
     */
    public static final Symbol serviceContext = ARQConstants.allocSymbol(base, "serviceContext");

    /**
     * Control whether SERVICE processing is allowed.
     * If the context contains this, and it is set to "false",
     * then SERVICE is not allowed.
     */
    
    public static final Symbol serviceAllowed = ARQConstants.allocSymbol(base, "serviceAllowed");
    
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
    public static final Symbol queryTimeout = ARQConstants.allocSymbol(base, "queryTimeout");

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
        InputStream in = httpQuery.exec();

        // Read the whole of the results now.
        // Avoids the problems with calling back into the same system e.g.
        // Fuseki+SERVICE <http://localhost:3030/...>

        ResultSet rs = ResultSetFactory.fromXML(in);
        QueryIterator qIter = QueryIter.materialize(new QueryIteratorResultSet(rs));
        // And close connection now, not when qIter is closed.
        IO.close(in);

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
        httpQuery.setAccept(WebContent.contentTypeResultsXML);
        httpQuery.setAllowGZip(context.isTrueOrUndef(queryGzip));
        httpQuery.setAllowDeflate(context.isTrueOrUndef(queryDeflate));

        String user = context.getAsString(queryAuthUser);
        String pwd = context.getAsString(queryAuthPwd);

        if (user != null || pwd != null) {
            user = user == null ? "" : user;
            pwd = pwd == null ? "" : pwd;
            httpQuery.setBasicAuthentication(user, pwd.toCharArray());
        }

        setAnyTimeouts(httpQuery, context);

        return httpQuery;
    }

    /**
     * Modified from QueryExecutionBase
     * 
     * @see com.hp.hpl.jena.sparql.engine.QueryExecutionBase
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
