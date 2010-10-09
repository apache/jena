/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.lib.MultiMap ;
import org.openjena.fuseki.conneg.ContentType ;
import org.openjena.fuseki.conneg.TypedStream ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.sparql.util.Convert ;

public class FusekiLib
{
    // See also HttpUtils.
    
    // Much of this ought to be elsewhere but while we develop it, it's useful to have close to first use.
    // ---- To riot.WebContent
    public static Lang langFromContentType(String mimeType)
    { 
        if ( mimeType == null )
            return null ;
        return mapContentTypeToLang.get(mimeType.toLowerCase()) ;
    }
    
    private static Model dummy = ModelFactory.createDefaultModel() ;
    private static Map<String, Lang> mapContentTypeToLang = new HashMap<String, Lang>() ;
    // To riot.WebContent
    static {
        mapContentTypeToLang.put(WebContent.contentTypeRDFXML, Lang.RDFXML) ;
        mapContentTypeToLang.put(WebContent.contentTypeTurtle1, Lang.TURTLE) ;
        mapContentTypeToLang.put(WebContent.contentTypeTurtle2, Lang.TURTLE) ;
        mapContentTypeToLang.put(WebContent.contentTypeTurtle3, Lang.TURTLE) ;
        mapContentTypeToLang.put(WebContent.contentTypeNTriples, Lang.NTRIPLES) ;   // text/plain
        mapContentTypeToLang.put(WebContent.contentTypeNTriplesAlt, Lang.NTRIPLES) ;
    }
    // ---- 

    // TODO Replace with a MediaType parser.
    
    public static ContentType contentType(HttpServletRequest request)
    {
        return contentType(request.getHeader(HttpNames.hContentType)) ;
    }

    /** Split Content-Type into MIME type and charset */ 
    public static ContentType contentType(String x)
    {
        if ( x == null )
            return null ;
        String y[] = x.split(";") ;
        if ( y.length == 0 )
            return null ;
        
        String contentType = null ;
        if ( y[0] != null )
            contentType = y[0].trim();
        
        String charset = null ;
        if ( y.length == 2 && y[1] != null && y[1].contains("=") )
        {
            String[] z = y[1].split("=") ;
            if ( z[0].toLowerCase().startsWith(HttpNames.charset) )
                charset=z[1].trim() ;
        }
        
        return new ContentType(contentType, charset) ;
    }

    
    // ---- To riot.Webreader // RIOT writers
    public static RDFWriter chooseWriter(TypedStream stream)
    {
        Lang lang = FusekiLib.langFromContentType(stream.getMediaType()) ;
        if ( lang == null )
            lang = Lang.RDFXML ;
        
        switch (lang)
        {
            case N3 :
            case TURTLE :
            case NTRIPLES :
            case RDFXML :
                return dummy.getWriter(lang.getName()) ;
            default:
                throw new RiotException("Not a triples language: "+lang) ;
//            case NQUADS :
//            case TRIG :
//                  throw new RiotException("Not a triples language: "+lang) ;
        }
    }

//    static public MediaType match(String headerString, AcceptList offerList)
//    {
//        AcceptList l = new AcceptList(headerString) ;
//        return AcceptList.match(l, offerList) ;
//    }
//
//    public static String match(String headerString, String str)
//    {
//        AcceptList l = new AcceptList(headerString) ;
//        MediaType aItem = new MediaType(str) ;   
//    
//        MediaType m = l.match(aItem) ;
//        if ( m == null )
//            return null ;
//        return m.toHeaderString() ;
//    }
//
//    public static boolean accept(String headerString, String str)
//    {
//        AcceptList l = new AcceptList(headerString) ;
//        MediaType aItem = new MediaType(str) ;
//        return l.accepts(aItem) ;
//    }

    static String fmtRequest(HttpServletRequest request)
    {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(request.getMethod()) ;
        sbuff.append(" ") ;
        sbuff.append(Convert.decWWWForm(request.getRequestURL()));
        
        String qs = request.getQueryString();
        if (qs != null)
        {
            String tmp = request.getQueryString() ;
            tmp = Convert.decWWWForm(tmp) ;
            tmp = tmp.replace('\n', ' ') ;
            tmp = tmp.replace('\r', ' ') ;
            sbuff.append("?").append(tmp);
        }
        return sbuff.toString() ;
    }

    public static String httpResponseCode(int responseCode)
    {
        switch (responseCode)
        {
        case HttpServletResponse.SC_CONTINUE: return "SC_CONTINUE" ;
        case HttpServletResponse.SC_SWITCHING_PROTOCOLS: return "SC_SWITCHING_PROTOCOLS" ;
        case HttpServletResponse.SC_OK: return "SC_OK" ;
        case HttpServletResponse.SC_CREATED: return "SC_CREATED" ;
        case HttpServletResponse.SC_ACCEPTED: return "SC_ACCEPTED" ;
        case HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION: return "SC_NON_AUTHORITATIVE_INFORMATION" ;
        case HttpServletResponse.SC_NO_CONTENT: return "SC_NO_CONTENT" ;
        case HttpServletResponse.SC_RESET_CONTENT: return "SC_RESET_CONTENT" ;
        case HttpServletResponse.SC_PARTIAL_CONTENT: return "SC_PARTIAL_CONTENT" ;
        case HttpServletResponse.SC_MULTIPLE_CHOICES: return "SC_MULTIPLE_CHOICES" ;
        case HttpServletResponse.SC_MOVED_PERMANENTLY: return "SC_MOVED_PERMANENTLY" ;
        case HttpServletResponse.SC_MOVED_TEMPORARILY: return "SC_MOVED_TEMPORARILY" ;
        case HttpServletResponse.SC_SEE_OTHER: return "SC_SEE_OTHER" ;
        case HttpServletResponse.SC_NOT_MODIFIED: return "SC_NOT_MODIFIED" ;
        case HttpServletResponse.SC_USE_PROXY: return "SC_USE_PROXY" ;
        case HttpServletResponse.SC_TEMPORARY_REDIRECT: return "SC_TEMPORARY_REDIRECT" ;
        case HttpServletResponse.SC_BAD_REQUEST: return "SC_BAD_REQUEST" ;
        case HttpServletResponse.SC_UNAUTHORIZED: return "SC_UNAUTHORIZED" ;
        case HttpServletResponse.SC_PAYMENT_REQUIRED: return "SC_PAYMENT_REQUIRED" ;
        case HttpServletResponse.SC_FORBIDDEN: return "SC_FORBIDDEN" ;
        case HttpServletResponse.SC_NOT_FOUND: return "SC_NOT_FOUND" ;
        case HttpServletResponse.SC_METHOD_NOT_ALLOWED: return "SC_METHOD_NOT_ALLOWED" ;
        case HttpServletResponse.SC_NOT_ACCEPTABLE: return "SC_NOT_ACCEPTABLE" ;
        case HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED: return "SC_PROXY_AUTHENTICATION_REQUIRED" ;
        case HttpServletResponse.SC_REQUEST_TIMEOUT: return "SC_REQUEST_TIMEOUT" ;
        case HttpServletResponse.SC_CONFLICT: return "SC_CONFLICT" ;
        case HttpServletResponse.SC_GONE: return "SC_GONE" ;
        case HttpServletResponse.SC_LENGTH_REQUIRED: return "SC_LENGTH_REQUIRED" ;
        case HttpServletResponse.SC_PRECONDITION_FAILED: return "SC_PRECONDITION_FAILED" ;
        case HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE: return "SC_REQUEST_ENTITY_TOO_LARGE" ;
        case HttpServletResponse.SC_REQUEST_URI_TOO_LONG: return "SC_REQUEST_URI_TOO_LONG" ;
        case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE: return "SC_UNSUPPORTED_MEDIA_TYPE" ;
        case HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE: return "SC_REQUESTED_RANGE_NOT_SATISFIABLE" ;
        case HttpServletResponse.SC_EXPECTATION_FAILED: return "SC_EXPECTATION_FAILED" ;
        case HttpServletResponse.SC_INTERNAL_SERVER_ERROR: return "SC_INTERNAL_SERVER_ERROR" ;
        case HttpServletResponse.SC_NOT_IMPLEMENTED: return "SC_NOT_IMPLEMENTED" ;
        case HttpServletResponse.SC_BAD_GATEWAY: return "SC_BAD_GATEWAY" ;
        case HttpServletResponse.SC_SERVICE_UNAVAILABLE: return "SC_SERVICE_UNAVAILABLE" ;
        case HttpServletResponse.SC_GATEWAY_TIMEOUT: return "SC_GATEWAY_TIMEOUT" ;
        case HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED: return "SC_HTTP_VERSION_NOT_SUPPORTED" ;
        default: return "Unknown HTTP response code: "+responseCode ;
        }        
    }

    /** Parse the query string - do not process the body even for a form */  
    public static MultiMap<String, String> parseQueryString(HttpServletRequest req)
    {
        MultiMap<String, String> map = MultiMap.createMapList() ;
        
        // Don't use ServletRequest.getParameter or getParamterNames
        // as that reads form data.  This code parses just the query string.
        if ( req.getQueryString() != null )
        {
            String[] params = req.getQueryString().split("&") ;
            for ( int i = 0 ; i < params.length ; i++ )
            {
                String p = params[i] ;
                String[] x = p.split("=",2) ;
                String name = null ;
                String value = null ;
    
                if ( x.length == 0 )
                {   // No "="
                    name = p ;
                    value = "" ;
                }
                else if ( x.length == 1 )
                {   // param=
                    name = x[0] ;
                    value = "" ;
                }
                else
                {   // param=value
                    name = x[0] ;
                    value = x[1] ;
                }
                map.put(name, value) ;
            }
        }
        return map ;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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