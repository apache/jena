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

public class HttpNames
{
    // See also Guava com.google.common.net.HttpHeaders
    // (org.apache.jena.ext.com.google.common.net.HttpHeaders)

    public static final String hAccept              = "Accept" ;
    public static final String hAcceptCharset       = "Accept-Charset" ;
    public static final String hAcceptRanges        = "Accept-Ranges" ;
    public static final String hAcceptEncoding      = "Accept-Encoding" ;

    public static final String hAllow               = "Allow" ;
    public static final String hAuthorization       = "Authorization";
    public static final String hWWWAuthenticate     = "WWW-Authenticate";
    public static final String hContentEncoding     = "Content-Encoding" ;
    public static final String hContentLength       = "Content-Length" ;
    public static final String hContentLocation     = "Content-Location" ;
    public static final String hContentRange        = "Content-Range" ;
    public static final String hContentType         = "Content-Type" ;
    public static final String hPragma              = "Pragma" ;
    public static final String hCacheControl        = "Cache-Control" ;
    public static final String hRetryAfter          = "Retry-After" ;
    public static final String hServer              = "Server" ;
    public static final String hLocation            = "Location" ;
    public static final String hVary                = "Vary" ;
    public static final String charset              = "charset" ;

    // CORS:
    //   http://www.w3.org/TR/cors/  http://esw.w3.org/CORS_Enabled
    public static final String hAccessControlAllowOrigin        = "Access-Control-Allow-Origin" ;
    public static final String hAccessControlExposeHeaders      = "Access-Control-Expose-Headers" ;
    public static final String hAccessControlMaxAge             = "Access-Control-Max-Age" ;
    public static final String hAccessControlAllowCredentials   = "Access-Control-Allow-Credentials" ;
    public static final String hAccessControlAllowMethods       = "Access-Control-Allow-Methods" ;
    public static final String hAccessControlAllowHeaders       = "Access-Control-Allow-Headers" ;
    public static final String hOrigin                          = "Origin" ;
    public static final String hAccessControlRequestMethod      = "Access-Control-Request-Method" ;
    public static final String hAccessControlRequestHeaders     = "Access-Control-Request-Headers" ;

    // GSP parameter names
    public static final String paramGraph           = "graph" ;
    public static final String paramGraphDefault    = "default" ;

    // Special names for GSP targets (use in ?graph=)
    public static final String graphTargetDefault   = "default" ;
    public static final String graphTargetUnion     = "union" ;

    public static final String paramUpdate          = "update" ;
    public static final String paramRequest         = "request" ;
    public static final String paramUsingGraphURI        = "using-graph-uri" ;
    public static final String paramUsingNamedGraphURI   = "using-named-graph-uri" ;

    // SPARQL parameter names
    public static final String paramQuery           = "query" ;
    public static final String paramQueryRef        = "query-ref" ;
    public static final String paramDefaultGraphURI = "default-graph-uri" ;
    public static final String paramNamedGraphURI   = "named-graph-uri" ;
    public static final String paramTarget          = "target" ;

    // Jena parameter names (SPARQL protocol extensions)
    public static final String paramStyleSheet      = "stylesheet" ;
    public static final String paramLang            = "lang" ;
    public static final String paramAccept          = "accept" ;
    public static final String paramOutput1         = "output" ;
    public static final String paramOutput2         = "format" ;        // Alternative name
    public static final String paramOutput3         = "results" ;       // Alternative name
    public static final String paramCallback        = "callback" ;
    public static final String paramForceAccept     = "force-accept" ;  // Force the accept header at the last moment
    public static final String paramTimeout         = "timeout" ;

    public static final String METHOD_DELETE        = "DELETE";
    public static final String METHOD_HEAD          = "HEAD";
    public static final String METHOD_GET           = "GET";
    public static final String METHOD_QUERY         = "QUERY" ;
    public static final String METHOD_OPTIONS       = "OPTIONS";
    public static final String METHOD_PATCH         = "PATCH" ;
    public static final String METHOD_POST          = "POST";
    public static final String METHOD_PUT           = "PUT";
    public static final String METHOD_TRACE         = "TRACE";

    public static final String HEADER_IFMODSINCE    = "If-Modified-Since";
    public static final String HEADER_LASTMOD       = "Last-Modified";
}
