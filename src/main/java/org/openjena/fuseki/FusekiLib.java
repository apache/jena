/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;

import org.openjena.atlas.lib.MultiMap ;
import org.openjena.fuseki.conneg.ContentType ;
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

    
    public static RDFWriter chooseWriter(Lang lang)        
    {
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