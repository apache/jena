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

package org.apache.jena.fuseki;

public class HttpNames
{
    // Request
    public static final String hAccept              = "Accept" ;
    public static final String hAcceptEncoding      = "Accept-Encoding" ;
    public static final String hAcceptCharset       = "Accept-Charset" ;
    //public static final String hAcceptLanguage      = "Accept-Language" ;

    // Response
    public static final String xhAcceptRanges        = "Accept-Ranges" ;
    public static final String hAllow               = "Allow" ;
    public static final String hContentEncoding     = "Content-Encoding" ;
    public static final String hContentLengh        = "Content-Length" ;
    public static final String hContentLocation     = "Content-Location" ;
    public static final String hContentRange        = "Content-Range" ;
    public static final String hContentType         = "Content-Type" ;
    public static final String hRetryAfter          = "Retry-After" ;
    public static final String hServer              = "Server" ;
    public static final String hLocation            = "Location" ; 
    public static final String hVary                = "Vary" ;
    public static final String charset              = "charset" ;
    
    // CORS: 
    //   http://www.w3.org/TR/cors/  http://esw.w3.org/CORS_Enabled
    public static final String hAccessControlAllowOrigin = "Access-Control-Allow-Origin" ;
    
    // Fuseki parameter names 
    public static final String paramGraph           = "graph" ;
    public static final String paramGraphDefault    = "default" ;

    public static final String paramQuery           = "query" ;
    public static final String paramQueryRef        = "query-ref" ;
    public static final String paramDefaultGraphURI = "default-graph-uri" ;
    public static final String paramNamedGraphURI   = "named-graph-uri" ;
    
    public static final String paramStyleSheet      = "stylesheet" ;
    public static final String paramAccept          = "accept" ;
    public static final String paramOutput1         = "output" ;        // See Yahoo! developer: http://developer.yahoo.net/common/json.html 
    public static final String paramOutput2         = "format" ;        // Alternative name 
    public static final String paramCallback        = "callback" ;
    public static final String paramForceAccept     = "force-accept" ;  // Force the accept header at the last moment
    public static final String paramTimeout         = "timeout" ;
    
    public static final String paramUpdate          = "update" ;
    public static final String paramRequest         = "request" ; 
    public static final String paramUsingGraphURI        = "using-graph-uri" ;
    public static final String paramUsingNamedGraphURI   = "using-named-graph-uri" ;
    
    public static final String METHOD_DELETE        = "DELETE";
    public static final String METHOD_HEAD          = "HEAD";
    public static final String METHOD_GET           = "GET";
    public static final String METHOD_OPTIONS       = "OPTIONS";
    public static final String METHOD_PATCH         = "PATCH" ;
    public static final String METHOD_POST          = "POST";
    public static final String METHOD_PUT           = "PUT";
    public static final String METHOD_TRACE         = "TRACE";

    public static final String HEADER_IFMODSINCE    = "If-Modified-Since";
    public static final String HEADER_LASTMOD       = "Last-Modified";
    
    // Names for services in the default configuration
    public static final String ServiceQuery         = "query" ;
    public static final String ServiceQueryAlt      = "sparql" ;
    public static final String ServiceUpdate        = "update" ;
    public static final String ServiceData          = "data" ;
    public static final String ServiceUpload        = "upload" ;
    public static final String ServiceGeneralQuery  = "/sparql" ;
    
    // Posisble values of fields.
    // TODO Pull in from results writer.
    public static final String valueDefault    = "default" ;
    
}
