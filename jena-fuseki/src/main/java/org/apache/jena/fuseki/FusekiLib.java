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

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;

import org.openjena.atlas.lib.MultiMap ;
import org.openjena.atlas.web.MediaType ;
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
    // Use riot.WebContent on next ARQ update.
    static {
        mapContentTypeToLang.put(WebContent.contentTypeRDFXML, Lang.RDFXML) ;
        mapContentTypeToLang.put(WebContent.contentTypeRDFJSON, Lang.RDFJSON) ;
        mapContentTypeToLang.put(WebContent.contentTypeTurtle, Lang.TURTLE) ;
        mapContentTypeToLang.put(WebContent.contentTypeTurtleAlt1, Lang.TURTLE) ;
        mapContentTypeToLang.put(WebContent.contentTypeTurtleAlt2, Lang.TURTLE) ;
        mapContentTypeToLang.put(WebContent.contentTypeNTriples, Lang.NTRIPLES) ;   // text/plain
        mapContentTypeToLang.put(WebContent.contentTypeNTriplesAlt, Lang.NTRIPLES) ;
    }
    // ---- 

    public static MediaType contentType(HttpServletRequest request)
    {
        String x = request.getHeader(HttpNames.hContentType) ;
        if ( x == null )
            return null ;
        return MediaType.create(x) ;
    }

    private static Map<Lang, String> mapLangToWriterName =  new HashMap<Lang, String>() ;
    static {
        mapLangToWriterName.put(Lang.N3, WebContent.langN3) ;
        mapLangToWriterName.put(Lang.RDFJSON, WebContent.langRdfJson) ;
        mapLangToWriterName.put(Lang.TURTLE, WebContent.langTurtle) ;
        mapLangToWriterName.put(Lang.NTRIPLES, WebContent.langNTriples) ;
        mapLangToWriterName.put(Lang.RDFXML, WebContent.langRDFXML) ;
    }
    
    public static RDFWriter chooseWriter(Lang lang)        
    {
        if ( lang == null )
            lang = Lang.RDFXML ;
        String name = mapLangToWriterName.get(lang) ;
        
        if ( name == null )
            throw new RiotException("Not a triples language: "+lang) ;
        return dummy.getWriter(name) ;
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
