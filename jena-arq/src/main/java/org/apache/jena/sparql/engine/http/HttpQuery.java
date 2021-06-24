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

import static org.apache.jena.web.HttpSC.*;

import java.io.InputStream ;
import java.net.MalformedURLException ;
import java.net.URL ;
import java.util.Map;
import java.util.regex.Pattern ;

import org.apache.http.client.HttpClient ;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpCaptureResponse;
import org.apache.jena.riot.web.HttpOp ;
import org.apache.jena.riot.web.HttpOp.CaptureInput;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Create an execution object for performing a query on a model over HTTP. This
 * is the main protocol engine for HTTP query. There are higher level classes
 * for doing a query and presenting the results in an API fashion.
 *
 * If the query string is large, then HTTP POST is used.
 */

public class HttpQuery extends Params {
    static final Logger log = LoggerFactory.getLogger(HttpQuery.class.getName());

    /** The definition of "large" queries */
    // Not final so that other code can change it.
    static public/* final */int urlLimit = 2 * 1024;

    String serviceURL;
    String contentTypeResult = WebContent.contentTypeResultsXML;

    // An object indicate no value associated with parameter name
    final static Object noValue = new Object();

    private int responseCode = 0;
    private String responseMessage = null;
    private boolean forcePOST = false;
    private String queryString = null;
    private boolean serviceParams = false;
    private final Pattern queryParamPattern = Pattern.compile(".+[&|\\?]query=.*");
    private int connectTimeout = 0, readTimeout = 0;
    private boolean allowCompression = false;
    private HttpClient client;

    private HttpContext context;

    /**
     * Create a execution object for a whole model GET
     *
     * @param serviceURL
     *            The model
     */
    public HttpQuery(String serviceURL) {
        init(serviceURL);
    }

    /**
     * Create a execution object for a whole model GET
     *
     * @param url
     *            The model
     */
    public HttpQuery(URL url) {
        init(url.toString());
    }

    private void init(String serviceURL) {
        if (log.isTraceEnabled())
            log.trace("URL: " + serviceURL);

        if (serviceURL.indexOf('?') >= 0)
            serviceParams = true;

        if (queryParamPattern.matcher(serviceURL).matches())
            throw new QueryExecException("SERVICE URL overrides the 'query' SPARQL protocol parameter");

        this.serviceURL = serviceURL;
    }

    private String getQueryString() {
        if (queryString == null)
            queryString = super.httpString();
        return queryString;
    }

    /**
     * Set the content type (Accept header) for the results
     *
     * @param contentType
     *            Accept content type
     */
    public void setAccept(String contentType) {
        contentTypeResult = contentType;
    }

    /**
     * Gets the Content Type
     * <p>
     * If the query has been made this reflects the Content-Type header returns,
     * if it has not been made this reflects only the Accept header that will be
     * sent (as set via the {@link #setAccept(String)} method)
     * </p>
     *
     * @return Content Type
     */
    public String getContentType() {
        return contentTypeResult;
    }

    /**
     * Gets the HTTP Response Code returned by the request (returns 0 if request
     * has yet to be made)
     *
     * @return Response Code
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Gets the HTTP Response Message returned by the request (returns null if request
     * has yet to be made)
     *
     * @return Response Message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets whether the HTTP request will include compressed encoding
     * header
     *
     * @param allow
     *            Whether to allow compressed encoding
     */
    public void setAllowCompression(boolean allow) {
        allowCompression = allow;
    }

    /**
     * Sets the client to use
     * @param client Client
     */
    public void setClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Sets the context to use
     * @param context HTTP context
     */
    public void setContext(HttpContext context) {
        this.context = context;
    }

    /**
     * Gets the HTTP client that is being used, may be null if no request has yet been made
     * @return HTTP Client or null
     */
    public HttpClient getClient() {
        Context arqContext = ARQ.getContext();
        if (arqContext.isDefined(Service.serviceContext)) {
            @SuppressWarnings("unchecked")
            Map<String, Context> context = (Map<String, Context>) arqContext.get(Service.serviceContext);
            if (context.containsKey(serviceURL)) {
                Context serviceContext = context.get(serviceURL);
                if (serviceContext.isDefined(Service.queryClient)) return serviceContext.get(Service.queryClient);
            }
        }
        return client;
    }

    /**
     * Gets the HTTP context that is being used, or sets and returns a default
     * @return the {@code HttpContext} in scope
     */
    public HttpContext getContext() {
        if (context == null)
            context = new BasicHttpContext();
        return context;
    }

    /**
     * Return whether this request will go by GET or POST
     *
     * @return boolean
     */
    public boolean usesPOST() {
        if (forcePOST)
            return true;
        String s = getQueryString();

        return serviceURL.length() + s.length() >= urlLimit;
    }

    /**
     * Force the use of HTTP POST for the query operation
     */

    public void setForcePOST() {
        forcePOST = true;
    }

    /**
     * Sets HTTP Connection timeout, any value {@literal <=} 0 is taken to mean no timeout
     *
     * @param timeout
     *            Connection Timeout
     */
    public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
    }

    /**
     * Gets the HTTP Connection timeout
     *
     * @return Connection Timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets HTTP Read timeout, any value {@literal <=} 0 is taken to mean no timeout
     *
     * @param timeout
     *            Read Timeout
     */
    public void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }

    /**
     * Gets the HTTP Read timeout
     *
     * @return Read Timeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Execute the operation
     *
     * @return Model The resulting model
     * @throws QueryExceptionHTTP
     */
    public InputStream exec() throws QueryExceptionHTTP {
        // Select the appropriate HttpClient to use
        HttpClientContext hcc = HttpClientContext.adapt(getContext());
        RequestConfig.Builder builder = RequestConfig.copy(hcc.getRequestConfig());
        contextualizeCompressionSettings(builder);
        contextualizeTimeoutSettings(builder);
        hcc.setRequestConfig(builder.build());
        try {
            if (usesPOST())
                return execPost();
            return execGet();
        } catch (QueryExceptionHTTP httpEx) {
            log.trace("Exception in exec", httpEx);
            throw httpEx;
        } catch (JenaException jEx) {
            log.trace("JenaException in exec", jEx);
            throw jEx;
        }
    }

    private void contextualizeCompressionSettings(RequestConfig.Builder builder) {
        builder.setContentCompressionEnabled(allowCompression);
    }

    private void contextualizeTimeoutSettings(RequestConfig.Builder builder) {
        if (connectTimeout > 0) builder.setConnectTimeout(connectTimeout);
        if (readTimeout > 0) builder.setSocketTimeout(readTimeout);
    }

    private InputStream execGet() throws QueryExceptionHTTP {
        URL target = null;
        String qs = getQueryString();

        ARQ.getHttpRequestLogger().trace(qs);

        try {
            if (count() == 0)
                target = new URL(serviceURL);
            else
                target = new URL(serviceURL + (serviceParams ? "&" : "?") + qs);
        } catch (MalformedURLException malEx) {
            throw new QueryExceptionHTTP(0, "Malformed URL: " + malEx);
        }
        log.trace("GET " + target.toExternalForm());
        try {
            try {
                // Get the actual response stream
                TypedInputStream stream = execHttpGet(target.toString(), contentTypeResult, client, getContext());
                if (stream == null)
                    throw new QueryExceptionHTTP(NOT_FOUND_404, HttpSC.getMessage(NOT_FOUND_404));
                return execCommon(stream);
            } catch (HttpException httpEx) {
                // Back-off and try POST if something complain about long URIs
                if (httpEx.getStatusCode() == REQUEST_URI_TOO_LONG_414)
                    return execPost();
                throw httpEx;
            }
        } catch (HttpException httpEx) {
            throw QueryExceptionHTTP.rewrap(httpEx);
        }
    }

    // With exception.
    private static TypedInputStream execHttpGet(String url, String acceptHeader, HttpClient httpClient, HttpContext httpContext) {
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        HttpOp.execHttpGet(url, acceptHeader, handler, httpClient, httpContext);
        return handler.get();
    }

    private InputStream execPost() throws QueryExceptionHTTP {
        URL target = null;
        try {
            target = new URL(serviceURL);
        } catch (MalformedURLException malEx) {
            throw new QueryExceptionHTTP(0, "Malformed URL: " + malEx);
        }
        log.trace("POST " + target.toExternalForm());

        ARQ.getHttpRequestLogger().trace(target.toExternalForm());

        try {
            // Get the actual response stream
            TypedInputStream stream = HttpOp.execHttpPostFormStream(serviceURL, this, contentTypeResult, client, getContext());
            if (stream == null)
                throw new QueryExceptionHTTP(404);
            return execCommon(stream);
        } catch (HttpException httpEx) {
        	throw QueryExceptionHTTP.rewrap(httpEx);
        }
    }

    private InputStream execCommon(TypedInputStream stream) throws QueryExceptionHTTP {
        // Assume response code must be 200 if we got here
        responseCode = 200;
        responseMessage = "OK" ;

        // Get the returned content type so we can expose this later via the
        // getContentType() method
        // We strip any parameters off the returned content type e.g.
        // ;charset=UTF-8 since code that
        // consumes our getContentType() method will expect a bare MIME type
        contentTypeResult = stream.getContentType();
        if (contentTypeResult != null && contentTypeResult.contains(";")) {
            contentTypeResult = contentTypeResult.substring(0, contentTypeResult.indexOf(';'));
        }

        // NB - Content Encoding is now handled at a higher level
        // so we don't have to worry about wrapping the stream at all

        return stream;
    }

    @Override
    public String toString() {
        String s = httpString();
        if (s != null && s.length() > 0)
            return serviceURL + "?" + s;
        return serviceURL;
    }
}
