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

import static java.lang.String.format ;
import static org.apache.jena.ext.com.google.common.base.MoreObjects.firstNonNull;

import java.io.IOException ;
import java.io.InputStream ;
import java.nio.charset.StandardCharsets ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.http.* ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.entity.UrlEncodedFormEntity ;
import org.apache.http.client.methods.* ;
import org.apache.http.entity.ContentType ;
import org.apache.http.entity.InputStreamEntity ;
import org.apache.http.entity.StringEntity ;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder ;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.message.BasicNameValuePair ;
import org.apache.http.protocol.HttpContext ;
import org.apache.http.util.EntityUtils ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.sparql.engine.http.Params ;
import org.apache.jena.sparql.engine.http.Params.Pair ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Simplified HTTP operations; simplification means only supporting certain uses
 * of HTTP. The expectation is that the simplified operations in this class can
 * be used by other code to generate more application specific HTTP interactions
 * (e.g. SPARQL queries). For more complicated requirements of HTTP, then the
 * application will need to use org.apache.http.client directly.
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
     * HttpClient, HttpContext) either of which can be null for
     * "default" 2/ Provide common use options without those two arguments.
     * These all become the full operation. 3/ All calls go via exec for logging
     * and debugging.
     */

    // See also:
    // Fluent API in HttpClient from v4.2

    static private Logger log = LoggerFactory.getLogger(HttpOp.class);

    /** System wide HTTP operation counter for log messages */
    static private AtomicLong counter = new AtomicLong(0);

    private static final LaxRedirectStrategy laxRedirectStrategy = new LaxRedirectStrategy();

    /**
     * Default HttpClient.
     */
    private static HttpClient defaultHttpClient = createDefaultHttpClient();

    /**
     * Used to reset {@link #defaultHttpClient} when needed
     */
    public static final HttpClient initialDefaultHttpClient = defaultHttpClient;
    
    public static HttpClient createDefaultHttpClient() {
        return createPoolingHttpClient();
    }
    
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
        private String result;

        @Override
        public void handle(String baseIRI, HttpResponse response) throws IOException {
            HttpEntity entity = response.getEntity();
            if ( entity == null ) {
                result = null ;
                return ;
            }
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
            if ( entity == null ) {
                stream = new TypedInputStream(EOFInputStream.empty, (String)null);
                return;
            }
            String ct = (entity.getContentType() == null) ? null : entity.getContentType().getValue();
            stream = new TypedInputStream(entity.getContent(), ct);
        }

        @Override
        public TypedInputStream get() {
            return stream;
        }
    }
    
    static class EOFInputStream extends InputStream {
        static InputStream empty = new EOFInputStream();
        
        @Override
        public int available() { return 0 ; }

        @Override
        public int read() { return -1 ; }
    }

    /**
     * Return the current default {@link HttpClient}. This may be null, meaning
     * a new {@link HttpClient} is created each time, if none is provided
     * in the HttpOp function call.
     * 
     * @return Default HTTP Client
     */
    public static HttpClient getDefaultHttpClient() {
        return defaultHttpClient;
    }

    /**
     * Performance can be improved by using a shared HttpClient that uses connection pooling. However, pool management
     * is complicated and can lead to starvation (the system locks-up, especially on Java6; it's JVM sensitive). See the
     * Apache HTTP Commons Client documentation for more details.
     * 
     * @param client HTTP client to use, if this is null, reset to original default instead
     */
    public static void setDefaultHttpClient(HttpClient client) {
        defaultHttpClient = firstNonNull(client, initialDefaultHttpClient);
    }
    
    /**
     * Create an HttpClient that performs connection pooling. This can be used
     * with {@link #setDefaultHttpClient} or provided in the HttpOp calls.
     */
    public static CloseableHttpClient createPoolingHttpClient() {
        return createPoolingHttpClientBuilder().build() ;
    }
    
    /**
     * Create an HttpClientBuilder that performs connection pooling.
     */
    public static HttpClientBuilder createPoolingHttpClientBuilder() {
        String s = System.getProperty("http.maxConnections", "5");
        int max = Integer.parseInt(s);
        return HttpClientBuilder.create()
            .useSystemProperties()
            .setRedirectStrategy(laxRedirectStrategy)
            .setMaxConnPerRoute(max)
            .setMaxConnTotal(2*max);
    }

    /**
     * Create an HttpClient that performs client-side caching and connection pooling. 
     * This can be used with {@link #setDefaultHttpClient} or provided in the HttpOp calls.
     * Beware that content is cached in this process, including across remote server restart. 
     */
    public static CloseableHttpClient createCachingHttpClient() {
        String s = System.getProperty("http.maxConnections", "5");
        int max = Integer.parseInt(s);
        return CachingHttpClientBuilder.create()
            .useSystemProperties()
            .setRedirectStrategy(laxRedirectStrategy)
            .setMaxConnPerRoute(max)
            .setMaxConnTotal(2*max)
            .build() ;
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
        execHttpGet(url, acceptHeader, handler, null, null);
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
     */
    public static void execHttpGet(String url, String acceptHeader, HttpResponseHandler handler, HttpClient httpClient,
            HttpContext httpContext) {
        String requestURI = determineRequestURI(url);
        HttpGet httpget = new HttpGet(requestURI);
        exec(url, httpget, acceptHeader, handler, httpClient, httpContext);
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
        execHttpGet(url, null, handler, null, null);
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
        execHttpGet(url, acceptHeader, handler, null, null);
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
     * @return TypedInputStream or null if the URL returns 404.
     */
    public static TypedInputStream execHttpGet(String url, String acceptHeader, HttpClient httpClient, HttpContext httpContext) {
        HttpCaptureResponse<TypedInputStream> handler = new CaptureInput();
        try {
            execHttpGet(url, acceptHeader, handler, httpClient, httpContext);
        } catch (HttpException ex) {
            if (ex.getResponseCode() == HttpSC.NOT_FOUND_404)
                return null;
            throw ex;
        }
        return handler.get();
    }

    /**
     * Convenience operation to execute a GET with no content negotiation and
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
        execHttpPost(url, contentType, content, null, nullHandler, null, null);
    }

    /**
     * Execute a HTTP POST and return the typed return stream.
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param content
     *            Content to POST
     * @param acceptType
     *            Accept Type
     */
    public static TypedInputStream execHttpPostStream(String url, String contentType, String content, String acceptType) {
        return execHttpPostStream(url, contentType, content, acceptType, null, null) ;
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
     */
    public static void execHttpPost(String url, String contentType, String content, HttpClient httpClient,
                                    HttpContext httpContext) {
        execHttpPost(url, contentType, content, null, nullHandler, httpClient, httpContext);
    }

    public static TypedInputStream execHttpPostStream(String url, String contentType, String content, String acceptType,
                                                      HttpClient httpClient, HttpContext httpContext) {
        CaptureInput handler = new CaptureInput();
        try {
            execHttpPost(url, contentType, content, acceptType, handler, httpClient, httpContext);
        } catch (HttpException ex) {
            if (ex.getResponseCode() == HttpSC.NOT_FOUND_404)
                return null;
            throw ex;
        }
        return handler.get();
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
     */
    public static void execHttpPost(String url, String contentType, String content, String acceptType,
                                    HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, StandardCharsets.UTF_8);
            e.setContentType(contentType);
            execHttpPost(url, e, acceptType, handler, httpClient, httpContext);
        }
        finally {
            closeEntity(e);
        }
    }

    //    
//        
//        StringEntity e = null;
//        try {
//            e = new StringEntity(content, StandardCharsets.UTF_8);
//            e.setContentType(contentType);
//            return execHttpPostStream(url, e, acceptType, null, null, null) ;
//        }
//        finally {
//            closeEntity(e);
//        }
//    }

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
        execHttpPost(url, contentType, input, length, null, nullHandler, null, null);
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
        execHttpPost(url, contentType, input, length, acceptType, handler, null, null);
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
     *
     */
    public static void execHttpPost(String url, String contentType, InputStream input, long length, String acceptType,
                                    HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext) {
        InputStreamEntity e = new InputStreamEntity(input, length);
        String ct = decideContentType(contentType);
        e.setContentType(ct);
        try {
            execHttpPost(url, e, acceptType, handler, httpClient, httpContext);
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
     * Execute a HTTP POST and return the typed return stream.
     * 
     * @param url
     *            URL
     * @param entity
     *            Entity to POST
     */
    public static TypedInputStream execHttpPostStream(String url, HttpEntity entity, String acceptHeader) {
        CaptureInput handler = new CaptureInput();
        execHttpPost(url, entity, acceptHeader, handler);
        return handler.get() ;
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
        execHttpPost(url, entity, acceptString, handler, null, null);
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
     */
    public static void execHttpPost(String url, HttpEntity entity, HttpClient httpClient, HttpContext httpContext) {

        execHttpPost(url, entity, null, nullHandler, httpClient, httpContext);
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
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     */
    public static TypedInputStream execHttpPostStream(String url, HttpEntity entity, String acceptHeader,
                                    HttpClient httpClient, HttpContext httpContext) {
        CaptureInput handler = new CaptureInput();
        execHttpPost(url, entity, acceptHeader, handler, httpClient, httpContext) ;
        return handler.get() ;
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
     */
    public static void execHttpPost(String url, HttpEntity entity, String acceptHeader, HttpResponseHandler handler,
            HttpClient httpClient, HttpContext httpContext) {
        String requestURI = determineRequestURI(url);
        HttpPost httppost = new HttpPost(requestURI);
        if (entity != null)
            httppost.setEntity(entity);
        exec(url, httppost, acceptHeader, handler, httpClient, httpContext);
    }
    
    
    
    // ---- HTTP POST as a form.

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
        return execHttpPostFormStream(url, params, acceptHeader, null, null);
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
     */
    public static TypedInputStream execHttpPostFormStream(String url, Params params, String acceptHeader, HttpClient httpClient,
            HttpContext httpContext) {
        CaptureInput handler = new CaptureInput();
        try {
            execHttpPostForm(url, params, acceptHeader, handler, httpClient, httpContext);
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
        execHttpPostForm(url, params, acceptString, handler, null, null);
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
     */
    public static void execHttpPostForm(String url, Params params, String acceptHeader, HttpResponseHandler handler,
            HttpClient httpClient, HttpContext httpContext) {
        if (handler == null)
            throw new IllegalArgumentException("A HttpResponseHandler must be provided (e.g. HttpResponseLib.nullhandler)");
        String requestURI = url;
        HttpPost httppost = new HttpPost(requestURI);
        httppost.setEntity(convertFormParams(params));
        exec(url, httppost, acceptHeader, handler, httpClient, httpContext);
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
        execHttpPut(url, contentType, content, null, null);
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
     */
    public static void execHttpPut(String url, String contentType, String content, HttpClient httpClient,
            HttpContext httpContext) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, StandardCharsets.UTF_8);
            e.setContentType(contentType);
            execHttpPut(url, e, httpClient, httpContext);
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
        execHttpPut(url, contentType, input, length, null, null);
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
     */
    public static void execHttpPut(String url, String contentType, InputStream input, long length, HttpClient httpClient,
            HttpContext httpContext) {
        InputStreamEntity e = new InputStreamEntity(input, length);
        String ct = decideContentType(contentType);
        e.setContentType(ct);
        try {
            execHttpPut(url, e, httpClient, httpContext);
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
        execHttpPut(url, entity, null, null);
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
     */
    public static void execHttpPut(String url, HttpEntity entity, HttpClient httpClient, HttpContext httpContext) {
        String requestURI = determineRequestURI(url);
        HttpPut httpput = new HttpPut(requestURI);
        httpput.setEntity(entity);
        exec(url, httpput, null, nullHandler, httpClient, httpContext);
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
        execHttpHead(url, acceptString, handler, null, null);
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
     */

    public static void execHttpHead(String url, String acceptString, HttpResponseHandler handler, HttpClient httpClient,
            HttpContext httpContext) {
        String requestURI = determineRequestURI(url);
        HttpHead httpHead = new HttpHead(requestURI);
        exec(url, httpHead, acceptString, handler, httpClient, httpContext);
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
        execHttpDelete(url, handler, null, null);
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
     */
    public static void execHttpDelete(String url, HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext) {
        HttpUriRequest httpDelete = new HttpDelete(url);
        exec(url, httpDelete, null, handler, null, httpContext);
    }

    // ---- Perform the operation!
    private static void exec(String url, HttpUriRequest request, String acceptHeader, HttpResponseHandler handler, HttpClient httpClient, HttpContext httpContext) {
        // whether we should close the client after request execution
        // only true if we built the client right here
        httpClient = firstNonNull(httpClient, getDefaultHttpClient());
        // and also only true if the handler won't close the client for us
        try {
            if (handler == null)
                // This cleans up left-behind streams
                handler = nullHandler;

            long id = counter.incrementAndGet();
            String baseURI = determineBaseIRI(url);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, request.getMethod(), request.getURI().toString()));
            // Accept
            if (acceptHeader != null)
                request.addHeader(HttpNames.hAccept, acceptHeader);
            // User-Agent
            applyUserAgent(request);

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
            if (handler != null) handler.handle(baseURI, response);
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

	public static String readPayload(HttpEntity entity) throws IOException {
        return entity == null ? null : EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset());
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

    private static HttpEntity convertFormParams(Params params) {
        List<NameValuePair> nvps = new ArrayList<>();
        for (Pair p : params.pairs())
            nvps.add(new BasicNameValuePair(p.getName(), p.getValue()));
        HttpEntity e = new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8);
        return e;
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
     * Content-Type, ensuring charset is present, defaulting to UTF-8.
     */
    private static String decideContentType(String contentType) {
        String ct = contentType;
        if ( ct != null && ! ct.contains("charset=") )
            ct = ct+"; charset=UTF-8";
        return ct;
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
