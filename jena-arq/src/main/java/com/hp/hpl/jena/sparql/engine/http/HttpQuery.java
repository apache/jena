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

import java.io.InputStream ;
import java.net.MalformedURLException ;
import java.net.URL ;
import java.util.concurrent.TimeUnit ;
import java.util.regex.Pattern ;

import org.apache.http.client.HttpClient ;
import org.apache.http.conn.params.ConnManagerPNames ;
import org.apache.http.impl.client.AbstractHttpClient ;
import org.apache.http.impl.client.DecompressingHttpClient ;
import org.apache.http.impl.client.SystemDefaultHttpClient ;
import org.apache.http.params.CoreConnectionPNames ;
import org.apache.http.protocol.BasicHttpContext ;
import org.apache.http.protocol.HttpContext ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.atlas.web.auth.HttpAuthenticator ;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.riot.web.HttpOp ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.shared.JenaException ;

/**
 * Create an execution object for performing a query on a model over HTTP. This
 * is the main protocol engine for HTTP query. There are higher level classes
 * for doing a query and presenting the results in an API fashion.
 * 
 * If the query string is large, then HTTP POST is used.
 */
@SuppressWarnings("deprecation")
public class HttpQuery extends Params {
    static final Logger log = LoggerFactory.getLogger(HttpQuery.class.getName());

    /** The definition of "large" queries */
    // Not final so that other code can change it.
    static public/* final */int urlLimit = 2 * 1024;

    String serviceURL;
    String contentTypeResult = WebContent.contentTypeResultsXML;

    // An object indicate no value associated with parameter name
    final static Object noValue = new Object();

    private HttpAuthenticator authenticator = null;
    private int responseCode = 0;
    private String responseMessage = null;
    private boolean forcePOST = false;
    private String queryString = null;
    private boolean serviceParams = false;
    private final Pattern queryParamPattern = Pattern.compile(".+[&|\\?]query=.*");
    private int connectTimeout = 0, readTimeout = 0;
    private boolean allowGZip = false;
    private boolean allowDeflate = false;
    private HttpClient client;
    private boolean requireClientShutdown = true;

    // static final String ENC_UTF8 = "UTF-8" ;

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
     * Sets whether the HTTP request will include a Accept-Encoding: gzip header
     * 
     * @param allow
     *            Whether to allow GZip encoding
     */
    public void setAllowGZip(boolean allow) {
        allowGZip = allow;
    }

    /**
     * Sets whether the HTTP request will include a Accept-Encoding: deflate
     * header
     * 
     * @param allow
     *            Whether to allow Deflate encoding
     */
    public void setAllowDeflate(boolean allow) {
        allowDeflate = allow;
    }

    /**
     * Sets basic authentication. It may be preferable to use the
     * {@link #setAuthenticator(HttpAuthenticator)} method since that provides
     * more flexibility in the type of authentication supported.
     * 
     * @param user
     *            User name
     * @param password
     *            Password
     */
    public void setBasicAuthentication(String user, char[] password) {
        this.setAuthenticator(new SimpleAuthenticator(user, password));
    }

    /**
     * Sets the authenticator to use
     * @param authenticator Authenticator
     */
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    
    /**
     * Gets the HTTP client that is being used, may be null if no request has yet been made
     * @return HTTP Client or null
     */
    public HttpClient getClient() {
        return this.client;
    }
    
    /**
     * Gets whether the HTTP client used should be shutdown
     * @return True if the client should be shutdown, false otherwise
     */
    public boolean shouldShutdownClient() {
        return this.requireClientShutdown;
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
     * Sets HTTP Connection timeout, any value <= 0 is taken to mean no timeout
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
     * Sets HTTP Read timeout, any value <= 0 is taken to mean no timeout
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
                // Select the appropriate HttpClient to use
                this.selectClient();
                
                // Always apply a 10 second timeout to obtaining a connection lease from HTTP Client
                // This prevents a potential lock up
                this.client.getParams().setLongParameter(ConnManagerPNames.TIMEOUT, TimeUnit.SECONDS.toMillis(10));
                
                // If user has specified time outs apply them now
                if (this.connectTimeout > 0)
                    this.client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, this.connectTimeout);
                if (this.readTimeout > 0)
                    this.client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, this.readTimeout);
                
                // Enable compression support appropriately
                HttpContext context = new BasicHttpContext();
                if (allowGZip || allowDeflate) {
                    // Apply auth early as the decompressing client we're about
                    // to add will block this being applied later
                    HttpOp.applyAuthentication((AbstractHttpClient) client, serviceURL, context, authenticator);
                    client = new DecompressingHttpClient(client);
                }
                
                // Get the actual response stream
                TypedInputStream stream = HttpOp.execHttpGet(target.toString(), contentTypeResult, client, context,
                        this.authenticator);
                if (stream == null)
                    throw new QueryExceptionHTTP(404);
                return execCommon(stream);
            } catch (HttpException httpEx) {
                // Back-off and try POST if something complain about long URIs
                if (httpEx.getResponseCode() == 414)
                    return execPost();
                throw httpEx;
            }
        } catch (HttpException httpEx) {
            throw rewrap(httpEx);
        }
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
            // Select the appropriate HttpClient to use
            this.selectClient();
                    
            // Always apply a 10 second timeout to obtaining a connection lease from HTTP Client
            // This prevents a potential lock up
            this.client.getParams().setLongParameter(ConnManagerPNames.TIMEOUT, TimeUnit.SECONDS.toMillis(10));
            
            // If user has specified time outs apply them now
            if (this.connectTimeout > 0)
                this.client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, this.connectTimeout);
            if (this.readTimeout > 0)
                this.client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, this.readTimeout);
            
            // Enable compression support appropriately
            HttpContext context = new BasicHttpContext();
            if (allowGZip || allowDeflate) {
                // Apply auth early as the decompressing client we're about
                // to add will block this being applied later
                HttpOp.applyAuthentication((AbstractHttpClient) client, serviceURL, context, authenticator);
                this.client = new DecompressingHttpClient(client);
            }

            // Get the actual response stream
            TypedInputStream stream = HttpOp.execHttpPostFormStream(serviceURL, this, contentTypeResult, client, context, authenticator);
            if (stream == null)
                throw new QueryExceptionHTTP(404);
            return execCommon(stream);
        } catch (HttpException httpEx) {
        	throw rewrap(httpEx);
        }
    }
    
    private QueryExceptionHTTP rewrap(HttpException httpEx) {
    	// The historical contract of HTTP Queries has been to throw QueryExceptionHTTP however using the standard
    	// ARQ HttpOp machinery we use these days means the internal HTTP errors come back as HttpException
        // Therefore we need to unnwrap and re-wrap  appropriately
        responseCode = httpEx.getResponseCode();
        if (responseCode != -1) {
        	// Was an actual HTTP error
        	String responseLine = httpEx.getStatusLine() != null ? httpEx.getStatusLine() : "No Status Line";
        	return new QueryExceptionHTTP(responseCode, "HTTP " + responseCode + " error making the query: " + responseLine, httpEx.getCause());
        } else if (httpEx.getMessage() != null) {
        	// Some non-HTTP error with a valid message e.g. Socket Communications failed, IO error
        	return new QueryExceptionHTTP("Unexpected error making the query: " + httpEx.getMessage(), httpEx.getCause());
        } else if (httpEx.getCause() != null) {
        	// Some other error with a cause e.g. Socket Communications failed, IO error
        	return new QueryExceptionHTTP("Unexpected error making the query, see cause for further details", httpEx.getCause());
        } else {
        	// Some other error with no message and no further cause
        	return new QueryExceptionHTTP("Unexpected error making the query", httpEx);
        }
    }
    
    private void selectClient() {
        // May use configured default client where appropriate
        this.client = HttpOp.getDefaultHttpClient();
        if (this.client == null || (this.authenticator != null && !HttpOp.getUseDefaultClientWithAuthentication())) {
            // If no configured default or authentication is in-use and the user has not configured
            // to use authentication with the default client use a fresh SystemDefaultHttpClient instance
            this.client = new SystemDefaultHttpClient();
        } else {
            // When using the configured default client we don't want to shut it down at the end of a request
            this.requireClientShutdown = false;
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
