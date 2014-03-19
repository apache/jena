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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.*;
import org.apache.jena.riot.web.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.sparql.resultset.CSVInput;
import com.hp.hpl.jena.sparql.resultset.JSONInput;
import com.hp.hpl.jena.sparql.resultset.TSVInput;
import com.hp.hpl.jena.sparql.resultset.XMLInput;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileManager;

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
    private List<String> defaultGraphURIs = new ArrayList<String>();
    private List<String> namedGraphURIs = new ArrayList<String>();
    private HttpAuthenticator authenticator;

    private boolean finished = false;

    // Timeouts
    private long connectTimeout = -1;
    private TimeUnit connectTimeoutUnit = TimeUnit.MILLISECONDS;
    private long readTimeout = -1;
    private TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;

    // Compression Support
    private boolean allowGZip = true;
    private boolean allowDeflate = true;

    // Content Types
    private String selectContentType = getSelectContentTypes();
    private String askContentType = getAskContentTypes();
    private String modelContentType = getConstructContentTypes();
    /**
     * Supported content types for SELECT queries
     */
    public static String[] supportedSelectContentTypes = new String[] { WebContent.contentTypeResultsXML,
            WebContent.contentTypeResultsJSON, WebContent.contentTypeTextTSV, WebContent.contentTypeTextCSV };
    /**
     * Supported content types for ASK queries
     */
    public static String[] supportedAskContentTypes = new String[] { WebContent.contentTypeResultsXML,
            WebContent.contentTypeJSON, WebContent.contentTypeTextTSV, WebContent.contentTypeTextCSV };

    // Releasing HTTP input streams is important. We remember this for SELECT,
    // and will close when the engine is closed
    private InputStream retainedConnection = null;

    private HttpClient retainedClient;

    public QueryEngineHTTP(String serviceURI, Query query) {
        this(serviceURI, query, query.toString());
    }
    
    public QueryEngineHTTP(String serviceURI, Query query, HttpAuthenticator authenticator) {
        this(serviceURI, query, query.toString(), authenticator);
    }

    public QueryEngineHTTP(String serviceURI, String queryString) {
        this(serviceURI, null, queryString);
    }
    
    public QueryEngineHTTP(String serviceURI, String queryString, HttpAuthenticator authenticator) {
        this(serviceURI, null, queryString, authenticator);
    }
    
    private QueryEngineHTTP(String serviceURI, Query query, String queryString) {
        this(serviceURI, query, queryString, null);
    }

    private QueryEngineHTTP(String serviceURI, Query query, String queryString, HttpAuthenticator authenticator) {
        this.query = query;
        this.queryString = queryString;
        this.service = serviceURI;
        // Copy the global context to freeze it.
        this.context = new Context(ARQ.getContext());

        // Apply service configuration if relevant
        QueryEngineHTTP.applyServiceConfig(serviceURI, this);
        
        // Don't want to overwrite credentials we may have picked up from
        // service context in the parent constructor if the specified
        // authenticator is null
        if (authenticator != null)
            this.setAuthenticator(authenticator);
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
            engine.setAllowGZip(serviceContext.isTrueOrUndef(Service.queryGzip));
            engine.setAllowDeflate(serviceContext.isTrueOrUndef(Service.queryDeflate));
            applyServiceTimeouts(engine, serviceContext);

            // Apply authentication settings
            String user = serviceContext.getAsString(Service.queryAuthUser);
            String pwd = serviceContext.getAsString(Service.queryAuthPwd);

            if (user != null || pwd != null) {
                user = user == null ? "" : user;
                pwd = pwd == null ? "" : pwd;
                if (log.isDebugEnabled())
                    log.debug("Setting basic HTTP authentication for endpoint URI {} with username: {} ", serviceURI, user);
                engine.setBasicAuthentication(user, pwd.toCharArray());
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

    // Meaning-less
    @Deprecated
    @Override
    public void setFileManager(FileManager fm) {
        throw new UnsupportedOperationException("FileManagers do not apply to remote query execution");
    }

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
     * Sets whether the HTTP request will specify Accept-Encoding: gzip
     */
    public void setAllowGZip(boolean allowed) {
        allowGZip = allowed;
    }

    /**
     * Sets whether the HTTP requests will specify Accept-Encoding: deflate
     */
    public void setAllowDeflate(boolean allowed) {
        allowDeflate = allowed;
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
            defaultGraphURIs = new ArrayList<String>();
        defaultGraphURIs.add(defaultGraph);
    }

    /**
     * @param name
     *            The URI to add.
     */
    public void addNamedGraph(String name) {
        if (namedGraphURIs == null)
            namedGraphURIs = new ArrayList<String>();
        namedGraphURIs.add(name);
    }

    /**
     * Gets whether an authentication mechanism has been provided.
     * <p>
     * Even if this returns false authentication may still be used if the
     * default authenticator applies, this is controlled via the
     * {@link HttpOp#setDefaultAuthenticator(HttpAuthenticator)} method
     * </p>
     * 
     * @return True if an authenticator has been provided
     */
    public boolean isUsingBasicAuthentication() {
        return this.authenticator != null;
    }

    /**
     * Set user and password for basic authentication. After the request is made
     * (one of the exec calls), the application can overwrite the password array
     * to remove details of the secret.
     * <p>
     * Note that it may be more flexible to
     * </p>
     * 
     * @param user
     * @param password
     */
    public void setBasicAuthentication(String user, char[] password) {
        this.authenticator = new SimpleAuthenticator(user, password);
    }

    /**
     * Sets the HTTP authenticator to use, if none is set then the default
     * authenticator is used. This may be configured via the
     * {@link HttpOp#setDefaultAuthenticator(HttpAuthenticator)} method.
     * 
     * @param authenticator
     *            HTTP authenticator
     */
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public ResultSet execSelect() {
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(selectContentType);
        InputStream in = httpQuery.exec();

        if (false) {
            byte b[] = IO.readWholeFile(in);
            String str = new String(b);
            System.out.println(str);
            in = new ByteArrayInputStream(b);
        }

        retainedConnection = in; // This will be closed on close()
        retainedClient = httpQuery.shouldShutdownClient() ? httpQuery.getClient() : null;

        // TODO: Find a way to auto-detect how to create the ResultSet based on
        // the content type in use

        // Don't assume the endpoint actually gives back the content type we
        // asked for
        String actualContentType = httpQuery.getContentType();

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals("")) {
            actualContentType = selectContentType;
        }

        if (actualContentType.equals(WebContent.contentTypeResultsXML) || actualContentType.equals(WebContent.contentTypeXML))
            return ResultSetFactory.fromXML(in);
        if (actualContentType.equals(WebContent.contentTypeResultsJSON) || actualContentType.equals(WebContent.contentTypeJSON))
            return ResultSetFactory.fromJSON(in);
        if (actualContentType.equals(WebContent.contentTypeTextTSV))
            return ResultSetFactory.fromTSV(in);
        if (actualContentType.equals(WebContent.contentTypeTextCSV))
            return CSVInput.fromCSV(in);
        throw new QueryException("Endpoint returned Content-Type: " + actualContentType
                + " which is not currently supported for SELECT queries");
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
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(modelContentType);
        InputStream in = httpQuery.exec();

        // Don't assume the endpoint actually gives back the content type we
        // asked for
        String actualContentType = httpQuery.getContentType();

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals("")) {
            actualContentType = modelContentType;
        }

        // Try to select language appropriately here based on the model content
        // type
        Lang lang = RDFLanguages.contentTypeToLang(actualContentType);
        if (!RDFLanguages.isTriples(lang))
            throw new QueryException("Endpoint returned Content Type: " + actualContentType
                    + " which is not a valid RDF Graph syntax");
        RDFDataMgr.read(model, in, lang);
        this.close();
        return model;
    }

    private Iterator<Triple> execTriples() {
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(modelContentType);
        InputStream in = httpQuery.exec();

        // Don't assume the endpoint actually gives back the content type we
        // asked for
        String actualContentType = httpQuery.getContentType();

        // If the server fails to return a Content-Type then we will assume
        // the server returned the type we asked for
        if (actualContentType == null || actualContentType.equals("")) {
            actualContentType = modelContentType;
        }

        // Try to select language appropriately here based on the model content
        // type
        Lang lang = RDFLanguages.contentTypeToLang(actualContentType);
        if (!RDFLanguages.isTriples(lang))
            throw new QueryException("Endpoint returned Content Type: " + actualContentType
                    + " which is not a valid RDF Graph syntax");

        return RiotReader.createIteratorTriples(in, lang, null);
    }

    @Override
    public boolean execAsk() {
        HttpQuery httpQuery = makeHttpQuery();
        httpQuery.setAccept(askContentType);
        InputStream in = httpQuery.exec();

        try {
            // Don't assume the endpoint actually gives back the content type we
            // asked for
            String actualContentType = httpQuery.getContentType();

            // If the server fails to return a Content-Type then we will assume
            // the server returned the type we asked for
            if (actualContentType == null || actualContentType.equals("")) {
                actualContentType = askContentType;
            }

            // Parse the result appropriately depending on the
            // selected content type.
            if (actualContentType.equals(WebContent.contentTypeResultsXML) || actualContentType.equals(WebContent.contentTypeXML))
                return XMLInput.booleanFromXML(in);
            if (actualContentType.equals(WebContent.contentTypeResultsJSON) || actualContentType.equals(WebContent.contentTypeJSON))
                return JSONInput.booleanFromJSON(in);
            if (actualContentType.equals(WebContent.contentTypeTextTSV))
                return TSVInput.booleanFromTSV(in);
            if (actualContentType.equals(WebContent.contentTypeTextCSV))
                return CSVInput.booleanFromCSV(in);
            throw new QueryException("Endpoint returned Content-Type: " + actualContentType
                    + " which is not currently supported for ASK queries");
        } finally {
            // Ensure connection is released
            try {
                in.close();
            } catch (java.io.IOException e) {
                log.warn("Failed to close connection", e);
            }
        }
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
        return query;
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
     * Gets whether HTTP requests will indicate to the remote server that GZip
     * encoding of responses is accepted
     * 
     * @return True if GZip encoding will be accepted
     */
    public boolean getAllowGZip() {
        return allowGZip;
    }

    /**
     * Gets whether HTTP requests will indicate to the remote server that
     * Deflate encoding of responses is accepted
     * 
     * @return True if Deflate encoding will be accepted
     */
    public boolean getAllowDeflate() {
        return allowDeflate;
    }

    private static long asMillis(long duration, TimeUnit timeUnit) {
        return (duration < 0) ? duration : timeUnit.toMillis(duration);
    }

    private HttpQuery makeHttpQuery() {
        if (finished)
            throw new ARQException("HTTP execution already closed");

        HttpQuery httpQuery = new HttpQuery(service);
        httpQuery.merge(getServiceParams(service, context));
        httpQuery.addParam(HttpParams.pQuery, queryString);

        for (Iterator<String> iter = defaultGraphURIs.iterator(); iter.hasNext();) {
            String dft = iter.next();
            httpQuery.addParam(HttpParams.pDefaultGraph, dft);
        }
        for (Iterator<String> iter = namedGraphURIs.iterator(); iter.hasNext();) {
            String name = iter.next();
            httpQuery.addParam(HttpParams.pNamedGraph, name);
        }

        if (params != null)
            httpQuery.merge(params);

        if (allowGZip)
            httpQuery.setAllowGZip(true);

        if (allowDeflate)
            httpQuery.setAllowDeflate(true);

        httpQuery.setAuthenticator(this.authenticator);

        // Apply timeouts
        if (connectTimeout > 0) {
            httpQuery.setConnectTimeout((int) connectTimeoutUnit.toMillis(connectTimeout));
        }
        if (readTimeout > 0) {
            httpQuery.setReadTimeout((int) readTimeoutUnit.toMillis(readTimeout));
        }

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
        finished = true;
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
        finished = true;
        if (retainedConnection != null) {
            try {
                retainedConnection.close();
            } catch (java.io.IOException e) {
                log.warn("Failed to close connection", e);
            } finally {
                retainedConnection = null;
            }
        }
        if (retainedClient != null) {
            try {
                retainedClient.getConnectionManager().shutdown();
            } catch (RuntimeException e) {
                log.warn("Failed to shutdown HTTP client", e);
            } finally {
                retainedClient = null;
            }
        }
    }

    // public boolean isActive() { return false ; }

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

    public static String getSelectContentTypes() {
        Map<String, Double> datatypes = new HashMap<String, Double>();
        datatypes.put(WebContent.contentTypeResultsXML, 1.0);
        datatypes.put(WebContent.contentTypeResultsJSON, 1.0);
        datatypes.put(WebContent.contentTypeJSON, 0.5);
        datatypes.put(WebContent.contentTypeTextTSV, 0.5);
        datatypes.put(WebContent.contentTypeTextCSV, 0.5);
        datatypes.put(WebContent.contentTypeXML, 0.5);
        return datatypesHashmapToString(datatypes);
    }

    public static String getAskContentTypes() {
        return getSelectContentTypes();
    }

    public static String getConstructContentTypes() {
        Map<String, Double> datatypes = new HashMap<String, Double>();
        datatypes.put(WebContent.contentTypeTurtle, 1.0);
        datatypes.put(WebContent.contentTypeTurtleAlt1, 1.0);
        datatypes.put(WebContent.contentTypeTurtleAlt2, 1.0);
        datatypes.put(WebContent.contentTypeRDFXML, 1.0);
        datatypes.put(WebContent.contentTypeN3, 1.0);
        datatypes.put(WebContent.contentTypeN3Alt1, 1.0);
        datatypes.put(WebContent.contentTypeN3Alt2, 1.0);
        datatypes.put(WebContent.contentTypeNTriples, 1.0);
        datatypes.put(WebContent.contentTypeNTriplesAlt, 0.5);
        return datatypesHashmapToString(datatypes);
    }

    private static String datatypesHashmapToString(Map<String, Double> datatypes) {
        Iterator<Entry<String, Double>> it = datatypes.entrySet().iterator();
        StringBuilder datatypeString = new StringBuilder();
        while (it.hasNext()) {
            Entry<String, Double> curr = it.next();
            if ( curr.getValue() < 1e0 )
                datatypeString.append(curr.getKey() + ";q=" + curr.getValue());
            if (it.hasNext()) {
                datatypeString.append(", ");
            }
        }
        return datatypeString.toString();
    }
}
