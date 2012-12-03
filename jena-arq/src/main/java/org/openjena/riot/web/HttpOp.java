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

package org.openjena.riot.web;

import static java.lang.String.format ;

import java.io.IOException ;
import java.io.InputStream ;
import java.io.UnsupportedEncodingException ;
import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.http.HttpEntity ;
import org.apache.http.HttpResponse ;
import org.apache.http.NameValuePair ;
import org.apache.http.StatusLine ;
import org.apache.http.client.HttpClient ;
import org.apache.http.client.entity.UrlEncodedFormEntity ;
import org.apache.http.client.methods.HttpGet ;
import org.apache.http.client.methods.HttpPost ;
import org.apache.http.client.methods.HttpPut ;
import org.apache.http.entity.EntityTemplate ;
import org.apache.http.entity.InputStreamEntity ;
import org.apache.http.entity.StringEntity ;
import org.apache.http.message.BasicNameValuePair ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.web.HttpException ;
import org.apache.jena.atlas.web.MediaType ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import org.apache.http.impl.client.SystemDefaultHttpClient;

/** Simplified HTTP operations; simplification means only supporting certain uses of HTTP.
 * The expectation is that the simplified operations in this class can be used by other code to
 * generate more application specific HTTP interactions (e.g. SPARQL queries).
 * For more complictaed requirments of HTTP, then the application wil need to use
 * org.apache.http.client directly.
 * 
 * <p>
 * For HTTP GET, the application supplies a URL, the accept header string, and a 
 * list of handlers to deal with different content type responses. 
 * <p>
 * For HTTP POST, the application supplies a URL, content, 
 * the accept header string, and a list of handlers to deal with different content type responses,
 * or no response is expected.
 * <p>
 * For HTTP PUT, the application supplies a URL, content, 
 * the accept header string
 * </p>
 * @see HttpNames HttpNames, for HTTP related constants
 * @see WebContent WebContent, for content type name constants
 */
public class HttpOp
{
    // See also:
    //   Fluent API in HttpClient from v4.2
    static private Logger log = LoggerFactory.getLogger(HttpOp.class) ;
    
    static private AtomicLong counter = new AtomicLong(0) ;
    
    static Map<String, HttpResponseHandler> noActionHandlers = new HashMap<String, HttpResponseHandler>() ;
    static {
        noActionHandlers.put("*", HttpResponseLib.nullResponse) ;
    }
    /** GET
     *  <p>The acceptHeader string is any legal value for HTTP Accept: field.
     *  <p>The handlers are the set of content types (without charset),
     *  used to dispatch the response body for handling.
     *  <p>A Map entry of ("*",....) is used "no handler found".
     *  <p>HTTP responses 400 and 500 become exceptions.   
     */
    public static void execHttpGet(String url, String acceptHeader, Map<String, HttpResponseHandler> handlers)
    {
        try {
            long id = counter.incrementAndGet() ;
            String requestURI = determineRequestURI(url) ;
            String baseIRI = determineBaseIRI(requestURI) ;

            HttpGet httpget = new HttpGet(requestURI);
            if ( log.isDebugEnabled() )
                log.debug(format("[%d] %s %s",id ,httpget.getMethod(),httpget.getURI().toString())) ;
            // Accept
            if ( acceptHeader != null )
                httpget.addHeader(HttpNames.hAccept, acceptHeader) ;
            
            // Execute
            HttpClient httpclient = new SystemDefaultHttpClient();
            HttpResponse response = httpclient.execute(httpget) ;
            // Handle response
            httpResponse(id, response, baseIRI, handlers) ;
            httpclient.getConnectionManager().shutdown(); 
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    /** POST a string without response body.
     * <p>Execute an HTTP POST, with the string as content.
     * <p>No response content expected or processed. 
     */
    //TODO Use MediaType
    public static void execHttpPost(String url, String contentType, String content)
    {
        execHttpPost(url, contentType, content, null, null) ;
    }
    
    /** POST without response body.
     * Content read from the the input stream.
     * <p>Execute an HTTP POST, with the string as content.
     * <p>No response content expected or processed. 
     */
   //TODO Use MediaType
    public static void execHttpPost(String url, String contentType, 
                                    InputStream input, long length)
    {
        execHttpPost(url, contentType, input, length, null, null ) ;
    }
    

    /** POST a string, expect a response body.*/
    public static void execHttpPost(String url, 
                                    String contentType, String content,
                                    String acceptType, Map<String, HttpResponseHandler> handlers)
    {
        StringEntity e = null ;
        try
        {
            e = new StringEntity(content, "UTF-8") ;
            e.setContentType(contentType) ;
            execHttpPost(url, e, acceptType, handlers) ;
        } catch (UnsupportedEncodingException e1)
        { throw new ARQInternalErrorException("Platform does not support required UTF-8") ; }
        finally { closeEntity(e) ; }
    }
    
    /** POST with response body.
     * The input stream is assumed to be UTF-8.
     */
    public static void execHttpPost(String url, String contentType, 
                                    InputStream input, long length,
                                    String acceptType, Map<String, HttpResponseHandler> handlers)
    {
        
        InputStreamEntity e = new InputStreamEntity(input, length) ;
        e.setContentType(contentType) ;
        e.setContentEncoding("UTF-8") ;
        execHttpPost(url, e, acceptType, handlers) ;
    }
    
    /** POST with response body */
    public static void execHttpPost(String url, 
                                    String contentType, ContentProducer provider,
                                    String acceptType, Map<String, HttpResponseHandler> handlers)
    {
        EntityTemplate entity = new EntityTemplate(provider) ;
        entity.setContentType(contentType) ;
        try {
            execHttpPost(url, entity, acceptType, handlers) ;
        } finally { closeEntity(entity) ; } 
    }
                             
    /** POST with response body.
     * <p>The content for the POST body comes from the HttpEntity.
     * <p>The response is handled bythe handler map, as per {@link #execHttpGet(String, String, Map)}
     */
    public static void execHttpPost(String url, 
                                    HttpEntity provider,
                                    String acceptType, Map<String, HttpResponseHandler> handlers)
    {
        try {
            long id = counter.incrementAndGet() ;
            String requestURI = determineBaseIRI(url) ;
            String baseIRI = determineBaseIRI(requestURI) ;
            
            HttpPost httppost = new HttpPost(requestURI);
            if ( log.isDebugEnabled() )
                log.debug(format("[%d] %s %s",id ,httppost.getMethod(),httppost.getURI().toString())) ;
            
            if ( provider.getContentType() == null )
                log.debug(format("[%d] No content type")) ;

            // Execute
            HttpClient httpclient = new SystemDefaultHttpClient();
            httppost.setEntity(provider) ;
            HttpResponse response = httpclient.execute(httppost) ;
            httpResponse(id, response, baseIRI, handlers) ;
            
            httpclient.getConnectionManager().shutdown(); 
        } catch (IOException ex) { IO.exception(ex) ; }
        finally { closeEntity(provider) ; }
    }
    
    /** Execute an HTTP POST form operation */
    public static void execHttpPostForm(String url, List<Pair<String, String>> params, Map<String, HttpResponseHandler> handlers)
    {
        try {
            long id = counter.incrementAndGet() ;
            String requestURI = url ;// determineBaseIRI(url) ;
            String baseIRI = determineBaseIRI(requestURI) ;
            //A rat walked over the keyboard:   >n'e q5tas5aaas v            
            HttpPost httppost = new HttpPost(requestURI);
            httppost.setEntity(convertFormParams(params));
            if ( log.isDebugEnabled() )
                log.debug(format("[%d] %s %s",id ,httppost.getMethod(),httppost.getURI().toString())) ;

            HttpClient httpclient = new SystemDefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost) ;
            httpResponse(id, response, baseIRI, handlers) ;
            httpclient.getConnectionManager().shutdown(); 
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    /** Execute an HTTP PUT operation */
    public static void execHttpPut(String url, String contentType, String content)
    {
            StringEntity e = null ;
            try
            {
                e = new StringEntity(content, "UTF-8") ;
                e.setContentType(contentType.toString()) ;
                execHttpPut(url, e) ;
            } catch (UnsupportedEncodingException e1)
            { throw new ARQInternalErrorException("Platform does not support required UTF-8") ; }
            finally { closeEntity(e) ; }
    }
    
    /** Execute an HTTP PUT operation */
    public static void execHttpPut(String url, String contentType, InputStream input, long length)
    {
        InputStreamEntity e = new InputStreamEntity(input, length) ;
        e.setContentType(contentType) ;
        e.setContentEncoding("UTF-8") ;
        try { execHttpPut(url, e) ; }
        finally { closeEntity(e) ; }
    }
    
    /** Execute an HTTP PUT operation */
    public static void execHttpPut(String url, HttpEntity entity)
    {
        try {
            long id = counter.incrementAndGet() ;
            String requestURI = url ;
            String baseIRI = determineBaseIRI(requestURI) ;
            HttpPut httpput = new HttpPut(requestURI);
            if ( log.isDebugEnabled() )
                log.debug(format("[%d] %s %s",id , httpput.getMethod(), httpput.getURI().toString())) ;
            
            httpput.setEntity(entity) ;
            HttpClient httpclient = new SystemDefaultHttpClient();
            HttpResponse response = httpclient.execute(httpput) ;
            httpResponse(id, response, baseIRI, null) ;
            httpclient.getConnectionManager().shutdown(); 
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    /*
InputStreamEntity e = new InputStreamEntity(input, length) ;
        e.setContentType(contentType) ;
        e.setContentEncoding("UTF-8") ;     */
    
    private static HttpEntity convertFormParams(List<Pair<String, String>> params)
    {
        try {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>() ;
            for (Pair<String, String> p : params)
                nvps.add(new BasicNameValuePair(p.getLeft(), p.getRight())) ;
            HttpEntity e = new UrlEncodedFormEntity(nvps, "UTF-8") ;
            return e ;
        } catch (UnsupportedEncodingException e)
        { throw new ARQInternalErrorException("Platform does not support required UTF-8") ; }
    }
    

    private static void closeEntity(HttpEntity entity)
    {
        if ( entity == null )
            return ;
        try { entity.getContent().close() ; } catch (Exception e) {}
    }

    private static String determineRequestURI(String url)
    {
        String requestURI = url ;
        if ( requestURI.contains("#") )
        {
            // No frag ids.
            int i = requestURI.indexOf('#') ;
            requestURI = requestURI.substring(0,i) ;
        }
        return requestURI ;
    }

    private static String determineBaseIRI(String requestURI)
    {
        // Technically wrong, but including the query string is "unhelpful"
        String baseIRI = requestURI ;
        if ( requestURI.contains("?") )
        {
            // No frag ids.
            int i = requestURI.indexOf('?') ;
            baseIRI = requestURI.substring(0,i) ;
        }
        return baseIRI ;
    }

    private static void httpResponse(long id, HttpResponse response, String baseIRI, Map<String, HttpResponseHandler> handlers) throws IllegalStateException, IOException
    {
        if ( response == null )
            return ;
        if ( handlers == null )
            handlers = noActionHandlers ;
        try {
            StatusLine statusLine = response.getStatusLine() ;
            if ( statusLine.getStatusCode() >= 400 )
            {
                log.debug(format("[%d] %s %s",id, statusLine.getStatusCode(), statusLine.getReasonPhrase())) ;
                throw new HttpException(statusLine.getStatusCode()+" "+statusLine.getReasonPhrase()) ;
            }

            String ct = "*" ;
            MediaType mt = null ;
            if ( statusLine.getStatusCode() == 200 )
            {
                String contentType = response.getFirstHeader(HttpNames.hContentType).getValue() ;
                if ( contentType != null )
                {
                    mt = MediaType.create(contentType) ;
                    ct = mt.getContentType() ;
                    if ( log.isDebugEnabled() )
                        log.debug(format("[%d] %d %s :: %s",id, statusLine.getStatusCode(), statusLine.getReasonPhrase() , mt)) ;
                }
                else
                {
                    if ( log.isDebugEnabled() )
                        log.debug(format("[%d] %d %s :: (no content type)",id, statusLine.getStatusCode(), statusLine.getReasonPhrase())) ;
                }
                HttpResponseHandler handler = handlers.get(ct) ;
                if ( handler == null )
                    // backstop
                    handler = handlers.get("*") ;
                if ( handler != null )
                    handler.handle(ct, baseIRI, response) ;
                else
                    log.warn(format("[%d] No handler found for %s", id, ct));
            }
//            else
//            {
//                if ( handlers != null )
//                    log.warn(format("[%d] No content returned but handlers provided", id));
//            }
        } finally { closeEntity(response.getEntity()) ; }
    }
}
