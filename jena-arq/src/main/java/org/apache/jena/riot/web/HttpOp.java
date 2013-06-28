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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.ServiceAuthenticator;
import org.apache.jena.riot.WebContent;
import org.apache.jena.web.JenaHttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.engine.http.Params;
import com.hp.hpl.jena.sparql.engine.http.Params.Pair;

/**
 * Simplified HTTP operations; simplification means only supporting certain uses
 * of HTTP. The expectation is that the simplified operations in this class can
 * be used by other code to generate more application specific HTTP interactions
 * (e.g. SPARQL queries). For more complictaed requirments of HTTP, then the
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
    // See also:
    // Fluent API in HttpClient from v4.2
    static private Logger log = LoggerFactory.getLogger(HttpOp.class);

    static private AtomicLong counter = new AtomicLong(0);

    static Map<String, HttpResponseHandler> noActionHandlers = new HashMap<String, HttpResponseHandler>();
    static {
        noActionHandlers.put("*", HttpResponseLib.nullResponse);
    }

    /**
     * Default authenticator used for HTTP authentication
     */
    static private HttpAuthenticator defaultAuthenticator = new ServiceAuthenticator();

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
     * Executes a HTTP Get request handling the response with one of the given
     * handlers
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * </p>
     * <p>
     * The handlers are the set of content types (without charset), used to
     * dispatch the response body for handling.
     * </p>
     * <p>
     * A Map entry of ("*",....) is used "no handler found".
     * </p>
     * <p>
     * HTTP responses 400 and 500 become exceptions.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param handlers
     *            Response Handlers
     * @param httpContext
     *            HTTP Context
     */
    public static void execHttpGet(String url, String acceptHeader, Map<String, HttpResponseHandler> handlers,
            HttpContext httpContext) {
        execHttpGet(url, acceptHeader, handlers, null, httpContext, defaultAuthenticator);
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
     * A Map entry of ("*",....) is used "no handler found".
     * <p>
     * HTTP responses 400 and 500 become exceptions.
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param handlers
     *            Response Handlers
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpGet(String url, String acceptHeader, Map<String, HttpResponseHandler> handlers,
            HttpClient httpClient, HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            long id = counter.incrementAndGet();
            String requestURI = determineRequestURI(url);
            String baseIRI = determineBaseIRI(requestURI);

            HttpGet httpget = new HttpGet(requestURI);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, httpget.getMethod(), httpget.getURI().toString()));
            // Accept
            if (acceptHeader != null)
                httpget.addHeader(HttpNames.hAccept, acceptHeader);

            // Prepare and execute
            httpClient = ensureClient(httpClient);
            httpContext = ensureContext(httpContext);
            applyAuthentication(asAbstractClient(httpClient), url, httpContext, authenticator);
            HttpResponse response = httpClient.execute(httpget, httpContext);

            // Handle response
            httpResponse(id, response, baseIRI, handlers);
            httpClient.getConnectionManager().shutdown();
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    /**
     * Executes a HTTP GET and returns a typed input stream
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param httpContext
     *            HTTP Context
     * @return Typed Input Stream
     */
    public static TypedInputStreamHttp execHttpGet(String url, String acceptHeader, HttpContext httpContext) {
        return execHttpGet(url, acceptHeader, null, httpContext, defaultAuthenticator);
    }

    /**
     * Executes a HTTP GET and returns a typed input stream
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
     * @param httpClient
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     * @return Typed Input Stream, null if the URL returns 404
     */
    public static TypedInputStreamHttp execHttpGet(String url, String acceptHeader, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            long id = counter.incrementAndGet();
            String requestURI = determineRequestURI(url);
            //String baseIRI = determineBaseIRI(requestURI);

            HttpGet httpget = new HttpGet(requestURI);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, httpget.getMethod(), httpget.getURI().toString()));
            // Accept
            if (acceptHeader != null)
                httpget.addHeader(HttpNames.hAccept, acceptHeader);

            // Prepare and execute
            httpClient = ensureClient(httpClient);
            httpContext = ensureContext(httpContext);
            applyAuthentication(asAbstractClient(httpClient), url, httpContext, authenticator);
            HttpResponse response = httpClient.execute(httpget, httpContext);

            // Response
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == 404) {
                log.debug(format("[%d] %s %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                return null;
            }
            if (statusLine.getStatusCode() >= 400) {
                log.debug(format("[%d] %s %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                throw new HttpException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                // No content in the return. Probably a mistake, but not
                // guaranteed.
                if (log.isDebugEnabled())
                    log.debug(format("[%d] %d %s :: (empty)", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                return null;
            }

            MediaType mt = MediaType.create(entity.getContentType().getValue());
            if (log.isDebugEnabled())
                log.debug(format("[%d] %d %s :: %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase(), mt));

            return new TypedInputStreamHttp(entity.getContent(), mt, httpClient.getConnectionManager());
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    /**
     * Executes a simple GET with no content negotiation and returning the
     * response as a string
     * 
     * @param url
     *            URL
     * @param httpContext
     *            HTTP Context
     * @return Response as a string
     */
    public static String execHttpGet(String url, HttpContext httpContext) {
        return execHttpGet(url, httpContext, defaultAuthenticator);
    }

    /**
     * Executes a simple GET with no content negotiation and returning the
     * response as a string
     * 
     * @param url
     *            URL
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     * @return Response as a string
     */
    public static String execHttpGet(String url, HttpContext httpContext, HttpAuthenticator authenticator) {
        HttpUriRequest httpGet = new HttpGet(url);
        DefaultHttpClient httpclient = new SystemDefaultHttpClient();

        // Authentication
        httpContext = ensureContext(httpContext);
        applyAuthentication(httpclient, url, httpContext, authenticator);
        try {
            HttpResponse response = httpclient.execute(httpGet, httpContext);
            int responseCode = response.getStatusLine().getStatusCode();
            String responseMessage = response.getStatusLine().getReasonPhrase();
            if (200 != responseCode)
                throw JenaHttpException.create(responseCode, responseMessage);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            String string = IO.readWholeFileAsUTF8(instream);
            instream.close();
            return string;
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    /**
     * Executes a HTTP POST with the given string as the request body and throws
     * away success responses, failure responses will throw an error.
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param content
     *            Content to POST
     */
    public static void execHttpPost(String url, String contentType, String content) {
        // TODO Use MediaType
        execHttpPost(url, contentType, content, null, null, null, defaultAuthenticator);
    }

    /**
     * Executes a simple POST with the given string as the request body and
     * throws away success responses, failure responses will throw an error.
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param content
     *            Content to POST
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, String content, HttpAuthenticator authenticator) {
        // TODO Use MediaType
        execHttpPost(url, contentType, content, null, null, null, authenticator);
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
    // TODO Use MediaType
    public static void execHttpPost(String url, String contentType, InputStream input, long length) {
        execHttpPost(url, contentType, input, length, null, null, defaultAuthenticator);
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
     * @param authenticator
     *            HTTP Authenticator
     * 
     */
    // TODO Use MediaType
    public static void execHttpPost(String url, String contentType, InputStream input, long length,
            HttpAuthenticator authenticator) {
        execHttpPost(url, contentType, input, length, null, null, authenticator);
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
     * @param handlers
     *            Response Handlers
     * @param httpContext
     *            HTTP Context
     */
    public static void execHttpPost(String url, String contentType, String content, String acceptType,
            Map<String, HttpResponseHandler> handlers, HttpContext httpContext) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, "UTF-8");
            e.setContentType(contentType);
            execHttpPost(url, e, acceptType, handlers, httpContext, defaultAuthenticator);
        } catch (UnsupportedEncodingException e1) {
            throw new ARQInternalErrorException("Platform does not support required UTF-8");
        } finally {
            closeEntity(e);
        }
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
     * @param handlers
     *            Response Handlers
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, String content, String acceptType,
            Map<String, HttpResponseHandler> handlers, HttpContext httpContext, HttpAuthenticator authenticator) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, "UTF-8");
            e.setContentType(contentType);
            execHttpPost(url, e, acceptType, handlers, httpContext, authenticator);
        } catch (UnsupportedEncodingException e1) {
            throw new ARQInternalErrorException("Platform does not support required UTF-8");
        } finally {
            closeEntity(e);
        }
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
     * @param handlers
     *            Response Handlers
     */
    public static void execHttpPost(String url, String contentType, InputStream input, long length, String acceptType,
            Map<String, HttpResponseHandler> handlers) {

        InputStreamEntity e = new InputStreamEntity(input, length);
        e.setContentType(contentType);
        e.setContentEncoding("UTF-8");
        execHttpPost(url, e, acceptType, handlers, null, defaultAuthenticator);
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
     * @param handlers
     *            Response Handlers
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, InputStream input, long length, String acceptType,
            Map<String, HttpResponseHandler> handlers, HttpAuthenticator authenticator) {

        InputStreamEntity e = new InputStreamEntity(input, length);
        e.setContentType(contentType);
        e.setContentEncoding("UTF-8");
        execHttpPost(url, e, acceptType, handlers, null, authenticator);
    }

    /**
     * Executes a HTTP POST with a request body and response handling
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param provider
     *            Provider of the POST content
     * @param acceptType
     *            Accept Type
     * @param handlers
     *            Response Handlers
     */
    public static void execHttpPost(String url, String contentType, ContentProducer provider, String acceptType,
            Map<String, HttpResponseHandler> handlers) {
        EntityTemplate entity = new EntityTemplate(provider);
        entity.setContentType(contentType);
        try {
            execHttpPost(url, entity, acceptType, handlers, null, defaultAuthenticator);
        } finally {
            closeEntity(entity);
        }
    }

    /**
     * Executes a HTTP POST with a request body and response handling
     * 
     * @param url
     *            URL
     * @param contentType
     *            Content Type to POST
     * @param provider
     *            Provider of the POST content
     * @param acceptType
     *            Accept Type
     * @param handlers
     *            Response Handlers
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, String contentType, ContentProducer provider, String acceptType,
            Map<String, HttpResponseHandler> handlers, HttpAuthenticator authenticator) {
        EntityTemplate entity = new EntityTemplate(provider);
        entity.setContentType(contentType);
        try {
            execHttpPost(url, entity, acceptType, handlers, null, authenticator);
        } finally {
            closeEntity(entity);
        }
    }

    /**
     * POST with response body.
     * <p>
     * The content for the POST body comes from the HttpEntity.
     * <p>
     * The response is handled by the handler map, as per
     * {@link #execHttpGet(String, String, Map, HttpContext)}
     * <p>
     * Additional headers e.g. for authentication can be injected through an
     * {@link HttpContext}
     * 
     * @param url
     *            URL
     * @param provider
     *            Entity to POST
     * @param acceptType
     *            Accept Type
     * @param handlers
     *            Response Handlers
     * @param context
     *            HTTP Context
     */
    public static void execHttpPost(String url, HttpEntity provider, String acceptType,
            Map<String, HttpResponseHandler> handlers, HttpContext context) {
        execHttpPost(url, provider, acceptType, handlers, context, defaultAuthenticator);
    }

    /**
     * POST with response body.
     * <p>
     * The content for the POST body comes from the HttpEntity.
     * <p>
     * The response is handled by the handler map, as per
     * {@link #execHttpGet(String, String, Map, HttpContext)}
     * <p>
     * Additional headers e.g. for authentication can be injected through an
     * {@link HttpContext}
     * 
     * @param url
     *            URL
     * @param provider
     *            Entity to POST
     * @param acceptType
     *            Accept Type
     * @param handlers
     *            Response Handlers
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPost(String url, HttpEntity provider, String acceptType,
            Map<String, HttpResponseHandler> handlers, HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            long id = counter.incrementAndGet();
            String requestURI = determineRequestURI(url);
            String baseIRI = determineBaseIRI(requestURI);

            HttpPost httppost = new HttpPost(requestURI);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, httppost.getMethod(), httppost.getURI().toString()));

            if (provider.getContentType() == null)
                log.debug(format("[%d] No content type"));

            // Prepare and Execute
            DefaultHttpClient httpclient = new SystemDefaultHttpClient();
            httpContext = ensureContext(httpContext);
            applyAuthentication(httpclient, url, httpContext, authenticator);
            httppost.setEntity(provider);
            HttpResponse response = httpclient.execute(httppost, httpContext);
            httpResponse(id, response, baseIRI, handlers);

            httpclient.getConnectionManager().shutdown();
        } catch (IOException ex) {
            throw new HttpException(ex);
        } finally {
            closeEntity(provider);
        }
    }

    /**
     * Executes a HTTP GET and returns a typed input stream
     * <p>
     * The acceptHeader string is any legal value for HTTP Accept: field.
     * </p>
     * 
     * @param url
     *            URL
     * @param acceptHeader
     *            Accept Header
     * @param params
     *            Parameters to POST
     * @param httpContext
     *            HTTP Context
     * @return Typed Input Stream
     */
    public static TypedInputStreamHttp execHttpPostForm(String url, String acceptHeader, Params params, HttpContext httpContext) {
        return execHttpPostForm(url, acceptHeader, params, null, httpContext, defaultAuthenticator);
    }

    /**
     * Executes a HTTP GET and returns a typed input stream
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
     * @return Typed Input Stream, null if the URL returns 404
     */
    public static TypedInputStreamHttp execHttpPostForm(String url, String acceptHeader, Params params, HttpClient httpClient,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            long id = counter.incrementAndGet();
            String requestURI = determineRequestURI(url);
            //String baseIRI = determineBaseIRI(requestURI);

            HttpPost httppost = new HttpPost(requestURI);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, httppost.getMethod(), httppost.getURI().toString()));
            // Accept
            if (acceptHeader != null)
                httppost.addHeader(HttpNames.hAccept, acceptHeader);
            httppost.setEntity(convertFormParams(params));

            // Prepare and execute
            httpClient = ensureClient(httpClient);
            httpContext = ensureContext(httpContext);
            applyAuthentication(asAbstractClient(httpClient), url, httpContext, authenticator);
            HttpResponse response = httpClient.execute(httppost, httpContext);

            // Response
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == 404) {
                log.debug(format("[%d] %s %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                return null;
            }
            if (statusLine.getStatusCode() >= 400) {
                log.debug(format("[%d] %s %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                throw new HttpException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                // No content in the return. Probably a mistake, but not
                // guaranteed.
                if (log.isDebugEnabled())
                    log.debug(format("[%d] %d %s :: (empty)", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                return null;
            }

            MediaType mt = MediaType.create(entity.getContentType().getValue());
            if (log.isDebugEnabled())
                log.debug(format("[%d] %d %s :: %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase(), mt));

            return new TypedInputStreamHttp(entity.getContent(), mt, httpClient.getConnectionManager());
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    /**
     * Executes a HTTP POST form operation
     * 
     * @param url
     *            URL
     * @param params
     *            Form parameters to POST
     * @param handlers
     *            Response Handlers
     */
    public static void execHttpPostForm(String url, Params params, Map<String, HttpResponseHandler> handlers) {
        execHttpPostForm(url, params, handlers, null, defaultAuthenticator);
    }

    /**
     * Executes a HTTP POST form operation
     * 
     * @param url
     *            URL
     * @param params
     *            Form parameters to POST
     * @param handlers
     *            Response Handlers
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPostForm(String url, Params params, Map<String, HttpResponseHandler> handlers,
            HttpAuthenticator authenticator) {
        execHttpPostForm(url, params, handlers, null, authenticator);
    }

    /**
     * Execute a HTTP POST form operation
     * 
     * @param url
     *            URL
     * @param params
     *            Form parameters to POST
     * @param handlers
     *            Response Handlers
     * @param httpContext
     *            HTTP Context
     */
    public static void execHttpPostForm(String url, Params params, Map<String, HttpResponseHandler> handlers,
            HttpContext httpContext) {
        execHttpPostForm(url, params, handlers, httpContext, defaultAuthenticator);
    }

    /**
     * Executes a HTTP POST form operation
     * 
     * @param url
     *            URL
     * @param params
     *            Form parameters to POST
     * @param handlers
     *            Response Handlers
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPostForm(String url, Params params, Map<String, HttpResponseHandler> handlers,
            HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            long id = counter.incrementAndGet();
            String requestURI = url;
            String baseIRI = determineBaseIRI(requestURI);
            HttpPost httppost = new HttpPost(requestURI);
            httppost.setEntity(convertFormParams(params));
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, httppost.getMethod(), httppost.getURI().toString()));

            // Prepare and Execute
            DefaultHttpClient httpclient = new SystemDefaultHttpClient();
            httpContext = ensureContext(httpContext);
            applyAuthentication(httpclient, url, httpContext, authenticator);

            HttpResponse response = httpclient.execute(httppost, httpContext);
            httpResponse(id, response, baseIRI, handlers);
            httpclient.getConnectionManager().shutdown();
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
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
     * @param httpContext
     *            HTTP Context
     */
    public static void execHttpPut(String url, String contentType, String content, HttpContext httpContext) {
        execHttpPut(url, contentType, content, httpContext, defaultAuthenticator);
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
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPut(String url, String contentType, String content, HttpContext httpContext,
            HttpAuthenticator authenticator) {
        StringEntity e = null;
        try {
            e = new StringEntity(content, "UTF-8");
            e.setContentType(contentType.toString());
            execHttpPut(url, e, httpContext, authenticator);
        } catch (UnsupportedEncodingException e1) {
            throw new ARQInternalErrorException("Platform does not support required UTF-8");
        } finally {
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
     * @param httpContext
     *            HTTP Context
     */
    public static void execHttpPut(String url, String contentType, InputStream input, long length, HttpContext httpContext) {
        execHttpPut(url, contentType, input, length, httpContext, defaultAuthenticator);
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
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPut(String url, String contentType, InputStream input, long length, HttpContext httpContext,
            HttpAuthenticator authenticator) {
        InputStreamEntity e = new InputStreamEntity(input, length);
        e.setContentType(contentType);
        e.setContentEncoding("UTF-8");
        try {
            execHttpPut(url, e, httpContext, authenticator);
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
     * @param httpContext
     *            HTTP Context
     */
    public static void execHttpPut(String url, HttpEntity entity, HttpContext httpContext) {
        execHttpPut(url, entity, httpContext, defaultAuthenticator);
    }

    /**
     * Executes a HTTP PUT operation
     * 
     * @param url
     *            URL
     * @param entity
     *            HTTP Entity to PUT
     * @param httpContext
     *            HTTP Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public static void execHttpPut(String url, HttpEntity entity, HttpContext httpContext, HttpAuthenticator authenticator) {
        try {
            long id = counter.incrementAndGet();
            String requestURI = url;
            String baseIRI = determineBaseIRI(requestURI);
            HttpPut httpput = new HttpPut(requestURI);
            if (log.isDebugEnabled())
                log.debug(format("[%d] %s %s", id, httpput.getMethod(), httpput.getURI().toString()));

            httpput.setEntity(entity);

            // Prepare and Execute
            DefaultHttpClient httpclient = new SystemDefaultHttpClient();
            httpContext = ensureContext(httpContext);
            applyAuthentication(httpclient, url, httpContext, authenticator);
            HttpResponse response = httpclient.execute(httpput, httpContext);

            httpResponse(id, response, baseIRI, null);
            httpclient.getConnectionManager().shutdown();
        } catch (IOException ex) {
            throw new HttpException(ex);
        }
    }

    /**
     * Ensures that a HTTP Client is non-null
     * 
     * @param client
     *            HTTP Client
     * @return HTTP Client
     */
    private static HttpClient ensureClient(HttpClient client) {
        return client != null ? client : new SystemDefaultHttpClient();
    }

    private static AbstractHttpClient asAbstractClient(HttpClient client) {
        if (AbstractHttpClient.class.isAssignableFrom(client.getClass())) {
            return (AbstractHttpClient) client;
        }
        return null;
    }

    /**
     * Ensures that a context is non-null
     * 
     * @param context
     *            HTTP Context
     * @return Non-null HTTP Context
     */
    private static HttpContext ensureContext(HttpContext context) {
        return context != null ? context : new BasicHttpContext();
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

        URI uri;
        try {
            // Apply the authenticator
            uri = new URI(target);
            authenticator.apply(client, context, uri);
        } catch (URISyntaxException e) {
            throw new ARQException("Invalid request URI", e);
        } catch (NullPointerException e) {
            throw new ARQException("Null request URI", e);
        }
    }

    private static HttpEntity convertFormParams(Params params) {
        try {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Pair p : params.pairs())
                nvps.add(new BasicNameValuePair(p.getName(), p.getValue()));
            HttpEntity e = new UrlEncodedFormEntity(nvps, "UTF-8");
            return e;
        } catch (UnsupportedEncodingException e) {
            throw new ARQInternalErrorException("Platform does not support required UTF-8");
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

    private static String determineRequestURI(String url) {
        String requestURI = url;
        if (requestURI.contains("#")) {
            // No frag ids.
            int i = requestURI.indexOf('#');
            requestURI = requestURI.substring(0, i);
        }
        return requestURI;
    }

    private static String determineBaseIRI(String requestURI) {
        // Technically wrong, but including the query string is "unhelpful"
        String baseIRI = requestURI;
        if (requestURI.contains("?")) {
            // No frag ids.
            int i = requestURI.indexOf('?');
            baseIRI = requestURI.substring(0, i);
        }
        return baseIRI;
    }

    private static void httpResponse(long id, HttpResponse response, String baseIRI, Map<String, HttpResponseHandler> handlers)
            throws IllegalStateException, IOException {
        if (response == null)
            return;
        if (handlers == null)
            handlers = noActionHandlers;
        try {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 400) {
                log.debug(format("[%d] %s %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                throw new HttpException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
            }

            String ct = "*";
            MediaType mt = null;
            if (statusLine.getStatusCode() == 200) {
                String contentType = null;
                Header ctHeader = response.getFirstHeader(HttpNames.hContentType);
                if (ctHeader == null)
                    log.info(format("[%d] %d %s :: No Content-Type in response", id, statusLine.getStatusCode(),
                            statusLine.getReasonPhrase()));
                else {
                    contentType = ctHeader.getValue();
                    if (contentType != null) {
                        mt = MediaType.create(contentType);
                        ct = mt.getContentType();
                        if (log.isDebugEnabled())
                            log.debug(format("[%d] %d %s :: %s", id, statusLine.getStatusCode(), statusLine.getReasonPhrase(), mt));
                    } else {
                        if (log.isDebugEnabled())
                            log.debug(format("[%d] %d %s :: (no content type: header but no value)", id,
                                    statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                    }
                }

                HttpResponseHandler handler = handlers.get(ct);
                if (handler == null)
                    // backstop
                    handler = handlers.get("*");
                if (handler != null)
                    handler.handle(ct, baseIRI, response);
                else
                    log.warn(format("[%d] No handler found for %s", id, ct));
            }
        } finally {
            closeEntity(response.getEntity());
        }
    }
}
