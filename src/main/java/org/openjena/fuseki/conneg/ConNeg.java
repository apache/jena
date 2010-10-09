/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.conneg;

import static org.openjena.fuseki.HttpNames.hAcceptCharset ;

import javax.servlet.http.HttpServletRequest ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class ConNeg
{
    private static Logger log = LoggerFactory.getLogger(ConNeg.class) ;
    // See riot.ContentNeg (client side).
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1

    static public MediaType match(String headerString, AcceptList offerList)
    {
        AcceptList l = new AcceptList(headerString) ;
        return AcceptList.match(l, offerList) ;
    }

    /** Match a single media type against a header string */
    public static String match_X(String headerString, String mediaRangeStr)
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
        String a = httpRequest.getHeader("Accept") ;
        if ( log.isDebugEnabled() )
            log.debug("Accept request: "+a) ;
        
        // Bag Q
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
            return headerList.first() ;
    
        MediaType i = AcceptList.match(headerList, myPrefs) ;
        if ( i == null )
            return defaultMediaType ;
        return i ;
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