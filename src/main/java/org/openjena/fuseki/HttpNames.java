/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

public class HttpNames
{
    public static final String hAccept              = "Accept" ;
    public static final String hAllow               = "Allow" ;
    public static final String hAcceptEncoding      = "Accept-Encoding" ;
    public static final String hAcceptCharset       = "Accept-Charset" ;
    public static final String hAcceptRanges        = "Accept-Ranges" ;
    
    public static final String hContentEncoding     = "Content-Encoding" ;
    public static final String hContentLengh        = "Content-Length" ;
    public static final String hContentLocation     = "Content-Location" ;
    public static final String hContentRange        = "Content-Range" ;
    public static final String hContentType         = "Content-Type" ;
    public static final String hServer              = "Server" ;
    public static final String hLocation            = "Location" ; 
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
    
    public static final String paramUpdate          = "update" ;
    public static final String paramRequest         = "request" ;       // Alternative name. 
    
    
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
    
    public static final String ServiceQuery     = "/query" ;
    public static final String ServiceQueryAlt  = "/sparql" ;
    public static final String ServiceUpdate    = "/update" ;
    public static final String ServiceData      = "/data" ;
    public static final String ServiceUpload    = "/upload" ;
    
    // Posisble values of fields.
    // TODO Pull in from results writer.
    public static final String valueDefault    = "default" ;
    
}

/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */