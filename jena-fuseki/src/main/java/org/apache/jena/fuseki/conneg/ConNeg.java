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

package org.apache.jena.fuseki.conneg;

import static org.apache.jena.fuseki.HttpNames.hAcceptCharset ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaRange ;
import org.apache.jena.atlas.web.MediaType ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class ConNeg
{
    private static Logger log = LoggerFactory.getLogger(ConNeg.class) ;
    // See riot.ContentNeg (client side).
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1

    static public MediaType parse(String contentType)
    {
        try {
            return MediaType.create(contentType) ;
        } catch (RuntimeException ex) { return null ; }
    }
    
    static public MediaType match(String headerString, AcceptList offerList)
    {
        AcceptList l = new AcceptList(headerString) ;
        return AcceptList.match(l, offerList) ;
    }

    /** Match a single media type against a header string */
    public static String match(String headerString, String mediaRangeStr)
    {
        AcceptList l = new AcceptList(headerString) ;
        MediaRange aItem = new MediaRange(mediaRangeStr) ;  // MediaType
        MediaType m = l.match(aItem) ;
        if ( m == null )
            return null ;
        return m.toHeaderString() ;
    }
    
    /*package*/ static String[] split(String s, String splitStr)
    {
        String[] x = s.split(splitStr,2) ;
        for ( int i = 0 ; i < x.length ; i++ )
        {
            x[i] = x[i].trim() ;
        }
        return x ;
    }

    public static MediaType chooseCharset(HttpServletRequest httpRequest,
                                          AcceptList myPrefs,
                                          MediaType defaultMediaType)
    {
        String a = httpRequest.getHeader(hAcceptCharset) ;
        if ( log.isDebugEnabled() )
            log.debug("Accept-Charset request: "+a) ;
        
        MediaType item = choose(a, myPrefs, defaultMediaType) ;
        
        if ( log.isDebugEnabled() )
            log.debug("Charset chosen: "+item) ;
    
        return item ;
    }

    public static MediaType chooseContentType(HttpServletRequest httpRequest,
                                              AcceptList myPrefs,
                                              MediaType defaultMediaType)
    {
        String a = WebLib.getAccept(httpRequest) ;
        if ( log.isDebugEnabled() )
            log.debug("Accept request: "+a) ;
        
        MediaType item = choose(a, myPrefs, defaultMediaType) ;
    
        if ( log.isDebugEnabled() )
            log.debug("Content type chosen: "+item) ;
    
        return item ;
    }

    private static MediaType choose(String headerString, AcceptList myPrefs,
                                    MediaType defaultMediaType)
    {
        if ( headerString == null )
            return defaultMediaType ;
        
        AcceptList headerList = new AcceptList(headerString) ;
        
        if ( myPrefs == null )
        {
            MediaType i = headerList.first() ;
            if ( i == null ) return defaultMediaType ;
            return i ;
        }
    
        MediaType i = AcceptList.match(headerList, myPrefs) ;
        if ( i == null )
            return defaultMediaType ;
        return i ;
    }
}
