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

package org.apache.jena.riot.web;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.ServiceAuthenticator;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.engine.http.Params;
import com.hp.hpl.jena.sparql.engine.http.Params.Pair;

/**
 * Simplified HTTP operations; simplification means only supporting certain uses
 * of HTTP. The expectation is that the simplified operations in this class can
 * be used by other code to generate more application specific HTTP interactions
 * (e.g. SPARQL queries). For more complicated requirements of HTTP, then the
 * application wil need to use org.apache.http.client directly.
 * 
 * <p>
 * For HTTP GET, the application supplies a URL, the accept header string, and a
 * list of handlers to deal with different content type responses.
 * <p>
 * For HTTP POST, the application supplies a URL, content, the accept header
 * string, and a list of handlers to deal with different content type responses,
 * or no response is expected.
 * <p>
 * For HTTP PUT, the application supplies a URL, content, the accept header
 * string
 * </p>
 * 
 * @see HttpNames HttpNames, for HTTP related constants
 * @see WebContent WebContent, for content type name constants
 */
public class HttpOp {
    /*
     * Implementation notes:
     * 
     * Test are in Fuseki (need a server to test against)
     * 
     * Pattern of functions provided: 1/ The full operation (includes
     * HttpClient, HttpContext, HttpAuthenticator) any of which can be null for
     * "default" 2/ Provide common use options without those three arguments.
     * These all become the full operation. 3/ All calls go via exec for logging
     * and debugging.
     */

    // See also:
    // Fluent API in HttpClient from v4.2

    static private Logger log = LoggerFactory.getLogger(HttpOp.class);

    /** System wide HTTP operation counter for log messages */
    static private AtomicLong counter = new AtomicLong(0);

    /**
     * Default HttpClient. This is used only if there is no authentication set.
     */
    static private HttpClient defaultHttpClient = null;

    /**
     * Whether the default HttpClient is used in conjunction with authentication
     */
    static private boolean useDefaultClientWithAuthentication = false;

    /**
     * Default authenticator used for HTTP authentication
     */
    static private HttpAuthenticator defaultAuthenticator = new ServiceAuthenticator();

    /**
     * Constant for the default User-Agent header that ARQ will use
     */
    public static final String ARQ_USER_AGENT = "Apache-Jena-ARQ/" + ARQ.VERSION;

    /**
     * User-Agent header to use
     */
    static private String userAgent = ARQ_USER_AGENT;

    /**
     * "Do nothing" response handler.
     */
    static private HttpResponseHandler nullHandler = HttpResponseLib.nullResponse;

    /** Capture response as a string (UTF-8 assumed) */
    public static class CaptureString implements HttpCaptureResponse<String> {
        String result;

        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {
            HttpEntity entity = response.getEntity();
            try(InputStream instream = entity.getContent()) {
                result = IO.readWholeFileAsUTF8(instream);
            }
        }

        @Override
        public String get() {
            return result;
        }
    }

    /**
     * TypedInputStream from an HTTP response. The TypedInputStream must be
     * explicitly closed.
     */
    public static class CaptureInput implements HttpCaptureResponse<TypedInputStream> {
        private TypedInputStream stream;

        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {

            HttpEntity entity = response.getEntity();
            String ct = (entity.getContentType() == null) ? null : entity.getContentType().getValue();
            stream = new TypedInputStream(entity.getContent(), ct);
        }

        @Override
        public TypedInputStream get() {
            return stream;
        }
    }

    /**
     * Gets the default authenticator used for authenticate requests if no
     * specific authenticator is provided.
     * 
     * @return HTTP authenticator
     */
    public static HttpAuthenticator getDefaultAuthenticator() {
        return defaultAuthenticator;
    }

    /**
     * Sets the default authenticator used for authenticate requests if no
     * specific authenticator is provided. May be set to null to turn off
     * default authentication, when set to null users must manually configure
     * authentication.
     * 
     * @param authenticator
     *            Authenticator
     */
    public static void setDefaultAuthenticator(HttpAuthenticator authenticator) {
        defaultAuthenticator = authenticator;
    }

    /**
     * Return the current default {@link HttpClient}. This may be null, meaning
     * a new {@linkplain HttpClient} is created each time, if none is provided
     * in the HttpOp function call.
     * 
     * @return Default HTTP Client
     */
    public static HttpClient getDefaultHttpClient() {
        return defaultHttpClient;
    }

    /**
     * <p>
     * Performance can be improved by using a shared HttpClient that uses
     * connection pooling. However, pool management is complicated and can lead
     * to starvation (the system locks-up, especially on Java6; it's JVM
     * sensitive).
     * </p>
     * <p>
     * The default HttpClient is not used if an HttpAuthenticator is provided
     * since this can potentially leak authentication credentials to be leaked
     * between requests to different services. However in some cases it may be
     * valuable to share the client regardless in which case you can also set
     * the
     * </p>
     * <p>
     * Set to "null" to create a new HttpClient for each call (default
     * behaviour, more reliable, but slower when many HTTP operation are
     * attempted).
     * <p>
     * See the Apache Http Client documentation for more details.
     * 
     * @param httpClient
     *            HTTP Client
     */
    public static void setDefaultHttpClient(HttpClient httpClient) {
        defaultHttpClient = httpClient;
    }

    /**
     * Gets whether the configured default HttpClient will be used in
     * conjunction with authentication.
     * <p>
     * This defaults to false because this can potentially leak authentication
     * credentials between requests to different services. However in some cases
     * it may be valuable to share the client regardless e.g. when you have a
     * specially configured HttpClient for your environment.
     * </p>
     * 
     * @return True if the configured default client will be used with
     *         authentication, false otherwise
     */
    public static boolean getUseDefaultClientWithAuthentication() {
        return useDefaultClientWithAuthentication;
    }

    /**
     * Sets whether the configured default HttpClient will be used in
     * conjunction with authentication.
     * <p>
     * This defaults to false because this can potentially leak authentication
     * credentials between requests to different services. However in some cases
     * it may be valuable to share the client regardless e.g. when you have a
     * specially configured HttpClient for your environment.
     * </p>
     * 
     * @param useWithAuth
     *            True if the configured default client should be used with
     *            authentication, false if it should not
     */
    public static void setUseDefaultClientWithAuthentication(boolean useWithAuth) {
        useDefaultClientWithAuthentication = useWithAuth;
    }

    /**
     * Create an HttpClient that performs connection pooling. This can be used
     * with {@link #setDefaultHttpClient} or provided in the HttpOp calls.
     */
    public static HttpClient createCachingHttpClient() {
        return new SystemDefaultHttpClient() {
            /**
             * See SystemDefaultHttpClient (4.2). This version always sets the
             * connection cache
             */
            @Override
            protected ClientConnectionManager createClientConnectionManager() {
                PoolingClientConnectionManager connmgr = new PoolingClientConnectionManager(
                        SchemeRegistryFactory.createSystemDefault());
                String s = System.getProperty("http.maxConnections", "5");
                int max = Integer.parseInt(s);
                connmgr.setDefaultMaxPerRoute(max);
                connmgr.setMaxTotal(2 * max);
                return connmgr;
            }
        };
    }

    /**
     * Gets the User-Agent string that ARQ is applying to all HTTP requests
     * 
     * @return User-Agent string
     */
    public static String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the User-Agent string that ARQ will apply to all HTTP requests
     * 
     * @param userAgent
     *            User-Agent string
     */
    public static void setUserAgent(String userAgent) {
        HttpOp.userAgent = userAgent;
    }

    // ---- HTTP GET
    /**
     * Executes a HTTP Get request, handling the response with given handler.
     * <p>
     * HTTP responses 400 and 500 become exceptions.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param handler
     *            Response Handler
     */
    public static void execHttpGet(String url, String acceptHeader, HttpResponseHandler handler) {
        execHttpGet(url, acceptHeader, handler, null, null, null);
    }

    /**
     * Executes a HTTP Get request handling the response with the given handler.
     * <p>
     * HTTP responses 400 and 500 become exceptions.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param handler
     *            Response Handler
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpGet(String url, String acceptHeader, HttpResponseHandler handler, HttpAuthenticator authenticator) {
        execHttpGet(url, acceptHeader, handler, null, null, authenticator);
    }

    /**
     * Executes a HTTP Get request handling the response with one of the given
     * handlers
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * <p>
     * The handlers are the set of content types (without charset), used to
     * dispatch the response body for handling.
     * <p>
     * HTTP responses 400 and 500 become exceptions.
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param handler
     *            Response handler called to process the response
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpGet(String url, String acceptHeader, HttpResponseHandler handler, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        String requestURI = determineRequestURI(url);
        HttpGet httpget = new HttpGet(requestURI);
        exec(url, httpget, acceptHeader, handler, httpClient, httpContext, authenticator);
    }

    /**
     * Executes a HTTP GET and return a TypedInputStream. The stream must be
     * closed after use.
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * </p>
     * 
     * @param url
     *            URL
     * @return TypedInputStream
     */
    public static TypedInputStream execHttpGet(String url) {
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        execHttpGet(url, null, handler, null, null, null);
        return handler.get();
    }

    /**
     * Executes a HTTP GET and return a TypedInputStream. The stream must be
     * closed after use.
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @return TypedInputStream or null if the URL returns 404.
     */
    public static TypedInputStream execHttpGet(String url, String acceptHeader) {
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        execHttpGet(url, acceptHeader, handler, null, null, null);
        return handler.get();
    }

    /**
     * Executes a HTTP GET and returns a TypedInputStream
     * <p>
     * A 404 will result in a null stream being returned, any other error code
     * results in an exception.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     * @return TypedInputStream or null if the URL returns 404.
     */
    public static TypedInputStream execHttpGet(String url, String acceptHeader, HttpClient httpClient, HttpContext httpContext,
            HttpAuthenticator authenticator) {
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        try {
            execHttpGet(url, acceptHeader, handler, httpClient, httpContext, authenticator);
        } catch (HttpException ex) {
            if (ex.getResponseCode() == HttpSC.NOT_FOUND_404)
                return null;
            throw ex;
        }
        return handler.get();
    }

    /**
     * Convenience operation to execute a GET with no content negtotiation and
     * return the response as a string.
     * 
     * @param url
     *            URL
     * @return Response as a string
     */
    public static String execHttpGetString(String url) {
        return execHttpGetString(url, null);
    }

    /**
     * Convenience operation to execute a GET and return the response as a
     * string
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept header.
     * @return Response as a string
     */
    public static String execHttpGetString(String url, String acceptHeader) {
        CaptureString handler = new CaptureString();
        try {
            execHttpGet(url, acceptHeader, handler);
        } catch (HttpException ex) {
            if (ex.getResponseCode() == HttpSC.NOT_FOUND_404)
                return null;
            throw ex;
        }
        return handler.get();
    }

    // ---- HTTP POST
    /**
     * Executes a HTTP POST with the given contentype/string as the request body
     * and throws away success responses, failure responses will throw an error.
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param content
     *            Content to POST
     */
    public static void execHttpPost(String url, String contentType, String content) {
        execHttpPost(url, contentType, content, null, nullHandler, null, null, defaultAuthenticator);
    }

    /**
     * Executes a HTTP POST with a string as the request body and response
     * handling
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param content
     *            Content to POST
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, String content, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        execHttpPost(url, contentType, content, null, nullHandler, httpClient, httpContext, authenticator);
    }

    /**
     * Executes a HTTP POST with a string as the request body and response
     * handling
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param content
     *            Content to POST
     * @param acceptType
     *            Accept Type
     * @param handler
     *            Response handler called to process the response
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, String content, String acceptType,
            HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, "UTF-8");
            e.setContentType(contentType);
            execHttpPost(url, e, acceptType, handler, httpClient, httpContext, authenticator);
        } catch (UnsupportedCharsetException | UnsupportedEncodingException e1) {
            throw new InternalErrorException("Platform does not support required UTF-8");
        }
        finally {
            closeEntity(e);
        }
    }

    /**
     * Executes a HTTP POST with a request body from an input stream without
     * response body with no response handling
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param input
     *            Input Stream to POST from
     * @param length
     *            Amount of content to POST
     * 
     */
    public static void execHttpPost(String url, String contentType, InputStream input, long length) {
        execHttpPost(url, contentType, input, length, null, nullHandler, null, null, defaultAuthenticator);
    }

    /**
     * Executes a HTTP POST with request body from an input stream and response
     * handling.
     * <p>
     * The input stream is assumed to be UTF-8.
     * </p>
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param input
     *            Input Stream to POST content from
     * @param length
     *            Length of content to POST
     * @param acceptType
     *            Accept Type
     * @param handler
     *            Response handler called to process the response
     */
    public static void execHttpPost(String url, String contentType, InputStream input, long length, String acceptType,
            HttpResponseHandler handler) {
        execHttpPost(url, contentType, input, length, acceptType, handler, null, null, null);
    }

    /**
     * Executes a HTTP POST with request body from an input stream and response
     * handling.
     * <p>
     * The input stream is assumed to be UTF-8.
     * </p>
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param input
     *            Input Stream to POST content from
     * @param length
     *            Length of content to POST
     * @param acceptType
     *            Accept Type
     * @param handler
     *            Response handler called to process the response
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, InputStream input, long length, String acceptType,
            HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
        InputStreamEntity e = new InputStreamEntity(input, length);
        e.setContentType(contentType);
        e.setContentEncoding("UTF-8");
        try {
            execHttpPost(url, e, acceptType, handler, httpClient, httpContext, authenticator);
        } finally {
            closeEntity(e);
        }
    }

    /**
     * Executes a HTTP POST of the given entity
     * 
     * @param url
     *            URL
     * @param entity
     *            Entity to POST
     */
    public static void execHttpPost(String url, HttpEntity entity) {
        execHttpPost(url, entity, null, nullHandler);
    }

    /**
     * Executes a HTTP Post
     * 
     * @param url
     *            URL
     * @param entity
     *            Entity to POST
     * @param acceptString
     *            Accept Header
     * @param handler
     *            Response Handler
     */
    public static void execHttpPost(String url, HttpEntity entity, String acceptString, HttpResponseHandler handler) {
        execHttpPost(url, entity, acceptString, handler, null, null, null);
    }

    /**
     * POST with response body.
     * <p>
     * The content for the POST body comes from the HttpEntity.
     * <p>
     * Additional headers e.g. for authentication can be injected through an
     * {@link HttpContext}
     * 
     * @param url
     *            URL
     * @param entity
     *            Entity to POST
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, HttpEntity entity, HttpClient httpClient, HttpContext httpContext,
            HttpAuthenticator authenticator) {

        execHttpPost(url, entity, null, nullHandler, httpClient, httpContext, authenticator);
    }

    /**
     * POST with response body.
     * <p>
     * The content for the POST body comes from the HttpEntity.
     * <p>
     * Additional headers e.g. for authentication can be injected through an
     * {@link HttpContext}
     * 
     * @param url
     *            URL
     * @param entity
     *            Entity to POST
     * @param acceptHeader
     *            Accept Header
     * @param handler
     *            Response handler called to process the response
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, HttpEntity entity, String acceptHeader, HttpResponseHandler handler,
            HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
        String requestURI = determineRequestURI(url);
        HttpPost httppost = new HttpPost(requestURI);
        if (entity != null)
            httppost.setEntity(entity);
        exec(url, httppost, acceptHeader, handler, httpClient, httpContext, authenticator);
    }

    // ---- HTTP POST as a form.

    /**
     * Executes a HTTP POST and returns a TypedInputStream, The TypedInputStream
     * must be closed.
     * 
     * @param url
     *            URL
     * @param params
     *            Parameters to POST
     * @param acceptHeader
     */
    public static TypedInputStream execHttpPostFormStream(String url, Params params, String acceptHeader) {
        return execHttpPostFormStream(url, params, acceptHeader, null, null, null);
    }

    /**
     * Executes a HTTP POST.
     * 
     * @param url
     *            URL
     * @param params
     *            Parameters to POST
     */
    public static void execHttpPostForm(String url, Params params) {
        execHttpPostForm(url, params, null, nullHandler);
    }

    // @formatter:off
//    /**
//     * Executes a HTTP POST Form.
//     * @param url
//     *            URL
//     * @param acceptHeader
//     *            Accept Header
//     * @param params
//     *            Parameters to POST
//     * @param httpClient
//     *            HTTP Client
//     * @param httpContext
//     *            HTTP Context
//     * @param authenticator
//     *            HTTP Authenticator
//     */
//    public static void execHttpPostForm(String url, Params params, 
//                                        String acceptHeader,  
//                                        HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
//        try {
//            execHttpPostForm(url, params, acceptHeader, HttpResponseLib.nullResponse, httpClient, httpContext, authenticator);
//        } catch (HttpException ex) {
//            if (ex.getResponseCode() == HttpSC.NOT_FOUND_404)
//                return ;
//            throw ex;
//        }
//        return ;
//    }
    // @formatter:on

    /**
     * Executes a HTTP POST Form and returns a TypedInputStream
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * </p>
     * <p>
     * A 404 will result in a null stream being returned, any other error code
     * results in an exception.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param params
     *            Parameters to POST
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static TypedInputStream execHttpPostFormStream(String url, Params params, String acceptHeader, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        CaptureInput handler = new CaptureInput();
        try {
            execHttpPostForm(url, params, acceptHeader, handler, httpClient, httpContext, authenticator);
        } catch (HttpException ex) {
            if (ex.getResponseCode() == HttpSC.NOT_FOUND_404)
                return null;
            throw ex;
        }
        return handler.get();
    }

    /**
     * Executes a HTTP POST form operation
     * 
     * @param url
     *            URL
     * @param params
     *            Form parameters to POST
     * @param acceptString
     *            Accept Header
     * @param handler
     *            Response handler called to process the response
     */
    public static void execHttpPostForm(String url, Params params, String acceptString, HttpResponseHandler handler) {
        execHttpPostForm(url, params, acceptString, handler, null, null, null);
    }

    /**
     * Executes a HTTP POST form operation
     * 
     * @param url
     *            URL
     * @param params
     *            Form parameters to POST
     * @param acceptHeader
     *            Accept Header
     * @param handler
     *            Response handler called to process the response
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPostForm(String url, Params params, String acceptHeader, HttpResponseHandler handler,
            HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
        if (handler == null)
            throw new IllegalArgumentException("A HttpResponseHandler must be provided (e.g. HttpResponseLib.nullhandler)");
        String requestURI = url;
        HttpPost httppost = new HttpPost(requestURI);
        httppost.setEntity(convertFormParams(params));
        exec(url, httppost, acceptHeader, handler, httpClient, httpContext, authenticator);
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type for the PUT
     * @param content
     *            Content for the PUT
     */
    public static void execHttpPut(String url, String contentType, String content) {
        execHttpPut(url, contentType, content, null, null, defaultAuthenticator);
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type for the PUT
     * @param content
     *            Content for the PUT
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPut(String url, String contentType, String content, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, "UTF-8");
            e.setContentType(contentType);
            execHttpPut(url, e, httpClient, httpContext, authenticator);
        } catch (UnsupportedCharsetException | UnsupportedEncodingException e1) {
            throw new InternalErrorException("Platform does not support required UTF-8");
        }
        finally {
            closeEntity(e);
        }
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type for the PUT
     * @param input
     *            Input Stream to read PUT content from
     * @param length
     *            Amount of content to PUT
     */
    public static void execHttpPut(String url, String contentType, InputStream input, long length) {
        execHttpPut(url, contentType, input, length, null, null, null);
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type for the PUT
     * @param input
     *            Input Stream to read PUT content from
     * @param length
     *            Amount of content to PUT
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPut(String url, String contentType, InputStream input, long length, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        InputStreamEntity e = new InputStreamEntity(input, length);
        e.setContentType(contentType);
        e.setContentEncoding("UTF-8");
        try {
            execHttpPut(url, e, httpClient, httpContext, authenticator);
        } finally {
            closeEntity(e);
        }
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param entity
     *            HTTP Entity to PUT
     */
    public static void execHttpPut(String url, HttpEntity entity) {
        execHttpPut(url, entity, null, null, null);
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param entity
     *            HTTP Entity to PUT
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPut(String url, HttpEntity entity, HttpClient httpClient, HttpContext httpContext,
            HttpAuthenticator authenticator) {
        String requestURI = determineRequestURI(url);
        HttpPut httpput = new HttpPut(requestURI);
        httpput.setEntity(entity);
        exec(url, httpput, null, nullHandler, httpClient, httpContext, authenticator);
    }

    /**
     * Executes a HTTP HEAD operation
     * 
     * @param url
     *            URL
     */
    public static void execHttpHead(String url) {
        execHttpHead(url, null, nullHandler);
    }

    /**
     * Executes a HTTP HEAD operation
     * 
     * @param url
     *            URL
     * @param acceptString
     *            Accept Header
     * @param handler
     *            Response Handler
     */
    public static void execHttpHead(String url, String acceptString, HttpResponseHandler handler) {
        execHttpHead(url, acceptString, handler, null, null, null);
    }

    /**
     * Executes a HTTP HEAD operation
     * 
     * @param url
     *            URL
     * @param acceptString
     *            Accept Header
     * @param handler
     *            Response Handler
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */

    public static void execHttpHead(String url, String acceptString, HttpResponseHandler handler, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        String requestURI = determineRequestURI(url);
        HttpHead httpHead = new HttpHead(requestURI);
        exec(url, httpHead, acceptString, handler, httpClient, httpContext, authenticator);
    }

    /**
     * Executes a HTTP DELETE operation
     * 
     * @param url
     *            URL
     */
    public static void execHttpDelete(String url) {
        execHttpDelete(url, nullHandler);
    }

    /**
     * Executes a HTTP DELETE operation
     * 
     * @param url
     *            URL
     * @param handler
     *            Response Handler
     */
    public static void execHttpDelete(String url, HttpResponseHandler handler) {
        execHttpDelete(url, handler, null, null, null);
    }

    /**
     * Executes a HTTP DELETE operation
     * 
     * @param url
     *            URL
     * @param handler
     *            Response Handler
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpDelete(String url, HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext,
            HttpAuthenticator authenticator) {
        HttpUriRequest httpDelete = new HttpDelete(url);
        exec(url, httpDelete, null, handler, null, httpContext, authenticator);
    }

    // ---- Perform the operation!
    // With logging.

    private static void exec(String url, HttpUriRequest request, String acceptHeader, HttpResponseHandler handler,
            HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            if (handler == null)
                // This cleans up.
                handler = nullHandler;

            long id = counter.incrementAndGet();
            String requestURI = determineRequestURI(url);
            String baseURI = determineBaseIRI(url);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, request.getMethod(), request.getURI().toString()));
            // Accept
            if (acceptHeader != null)
                request.addHeader(HttpNames.hAccept, acceptHeader);
            // User-Agent
            applyUserAgent(request);

            // Prepare and execute
            httpClient = ensureClient(httpClient, authenticator);
            httpContext = ensureContext(httpContext);
            applyAuthentication(asAbstractClient(httpClient), url, httpContext, authenticator);
            HttpResponse response = httpClient.execute(request, httpContext);

            // Response
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (HttpSC.isClientError(statusCode) || HttpSC.isServerError(statusCode)) {
                log.debug(format("[%d] %s %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                // Error responses can have bodies so it is important to clear
                // up.
				final String contentPayload = readPayload(response.getEntity());
				throw new HttpException(statusLine.getStatusCode(), statusLine.getReasonPhrase(), contentPayload);
            }
            // Redirects are followed by HttpClient.
            if (handler != null)
                handler.handle(baseURI, response);
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }


	public static String readPayload(HttpEntity entity) throws IOException {
		if (entity == null) {
			return null;
		}
		return EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
	}

    /**
     * Ensures that a HTTP Client is non-null
     * <p>
     * Prefers the {@link HttpClient} provided for the request if available.
     * Then it tries to use a Jena-wide user configurable
     * {@link HttpClient} if available. This is used only when no
     * authentication is required unless the user has indicated they want to use
     * the default client with authentication regardless.
     * </p>
     * <p>
     * In all other cases it creates a fresh instance of a
     * {@link SystemDefaultHttpClient} each time.
     * </p>
     * 
     * @param client
     *            HTTP Client
     * @return HTTP Client
     */
    private static HttpClient ensureClient(HttpClient client, HttpAuthenticator auth) {
        // Use user provided client if available
        if (client != null)
            return client;

        // Use configured default client if no authentication involved or
        // configured to use it with authentication
        if (defaultHttpClient != null && (auth == null || useDefaultClientWithAuthentication))
            return defaultHttpClient;

        // Otherwise use a fresh client each time
        return new SystemDefaultHttpClient();
    }

    private static AbstractHttpClient asAbstractClient(HttpClient client) {
        if (AbstractHttpClient.class.isAssignableFrom(client.getClass())) {
            return (AbstractHttpClient) client;
        }
        return null;
    }

    /**
     * Ensures that a context is non-null, uses a new {@link BasicHttpContext}
     * if none is provided
     * 
     * @param context
     *            HTTP Context
     * @return Non-null HTTP Context
     */
    private static HttpContext ensureContext(HttpContext context) {
        return context != null ? context : new BasicHttpContext();
    }

    /**
     * Applies the configured User-Agent string to the HTTP request
     * 
     * @param message
     *            HTTP request
     */
    public static void applyUserAgent(HttpMessage message) {
        if (userAgent != null) {
            message.setHeader("User-Agent", userAgent);
        }
    }

    /**
     * Applies authentication to the given client as appropriate
     * <p>
     * If a null authenticator is provided this method tries to use the
     * registered default authenticator which may be set via the
     * {@link HttpOp#setDefaultAuthenticator(HttpAuthenticator)} method.
     * </p>
     * 
     * @param client
     *            HTTP Client
     * @param target
     *            Target URI
     * @param context
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void applyAuthentication(AbstractHttpClient client, String target, HttpContext context,
            HttpAuthenticator authenticator) {
        // Cannot apply to null client
        if (client == null)
            return;

        // Fallback to default authenticator if null authenticator provided
        if (authenticator == null)
            authenticator = defaultAuthenticator;

        // Authenticator could still be null even if we fell back to default
        if (authenticator == null)
            return;

        try {
            // Apply the authenticator
            URI uri = new URI(target);
            authenticator.apply(client, context, uri);
        } catch (URISyntaxException e) {
            throw new RiotException("Invalid request URI", e);
        } catch (NullPointerException e) {
            throw new RiotException("Null request URI", e);
        }
    }

    private static HttpEntity convertFormParams(Params params) {
        try {
            List<NameValuePair> nvps = new ArrayList<>();
            for (Pair p : params.pairs())
                nvps.add(new BasicNameValuePair(p.getName(), p.getValue()));
            HttpEntity e = new UrlEncodedFormEntity(nvps, "UTF-8");
            return e;
        } catch (UnsupportedEncodingException e) {
            throw new InternalErrorException("Platform does not support required UTF-8");
        }
    }

    private static void closeEntity(HttpEntity entity) {
        if (entity == null)
            return;
        try {
            entity.getContent().close();
        } catch (Exception e) {
        }
    }

    /**
     * Calculate the request URI from a general URI. This means remove any
     * fragment.
     */
    private static String determineRequestURI(String uri) {
        String requestURI = uri;
        if (requestURI.contains("#")) {
            // No frag ids.
            int i = requestURI.indexOf('#');
            requestURI = requestURI.substring(0, i);
        }
        return requestURI;
    }

    /**
     * Calculate the base IRI to use from a URI. The base is without fragement
     * and without query string.
     */
    private static String determineBaseIRI(String uri) {
        // Defrag
        String baseIRI = determineRequestURI(uri);
        // Technically wrong, but including the query string is "unhelpful"
        if (baseIRI.contains("?")) {
            int i = baseIRI.indexOf('?');
            baseIRI = baseIRI.substring(0, i);
        }
        return baseIRI;
    }
}
