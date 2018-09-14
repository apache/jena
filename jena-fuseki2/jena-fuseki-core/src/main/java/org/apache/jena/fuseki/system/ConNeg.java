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

package org.apache.jena.fuseki.system;

import static org.apache.jena.riot.web.HttpNames.hAcceptCharset ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaRange ;
import org.apache.jena.atlas.web.MediaType ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * <p>Content negotiation is a mechanism defined in the HTTP specification
 * that makes it possible to serve different versions of a document
 * (or more generally, a resource representation) at the same URI, so that
 * user agents can specify which version fit their capabilities the best.</p>
 *
 * <p>ConNeg is used in Fuseki to help matching the content media type requested
 * by the user, against the list of offered media types.</p>
 *
 * @see <a href="http://en.wikipedia.org/wiki/Content_negotiation">http://en.wikipedia.org/wiki/Content_negotiation</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1</a>
 */
public class ConNeg
{
    // Logger
    private static Logger log = LoggerFactory.getLogger(ConNeg.class) ;
    // See riot.ContentNeg (client side).
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1

    /**
     * Parses the content type. It splits the string by semi-colon and finds the
     * other features such as the "q" quality factor.  For a complete documentation
     * on how the parsing happens, see
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1</a>.
     *
     * @param contentType content type string
     * @return parsed media type
     */
    static public MediaType parse(String contentType)
    {
        try {
            return MediaType.create(contentType) ;
        } catch (RuntimeException ex) { return null ; }
    }

    /**
     * <p>Creates a {@link AcceptList} with the given HTTP header string and
     * uses the {@link AcceptList#match(MediaType)} method to decide which
     * media type matches the HTTP header string.</p>
     *
     * <p>The <em>q</em> quality factor is used to decide which choice is the best
     * match.</p>
     *
     * @param headerString HTTP header string
     * @param offerList accept list
     * @return matched media type
     */
    static public MediaType match(String headerString, AcceptList offerList)
    {
        AcceptList l = new AcceptList(headerString) ;
        return AcceptList.match(l, offerList) ;
    }

    /**
     * Match a single media type against a header string.
     *
     * @param headerString HTTP header string
     * @param mediaRangeStr Semi-colon separated list of media types
     * @return the matched media type or <code>null</code> if there was no match
     */
    public static String match(String headerString, String mediaRangeStr)
    {
        AcceptList l = new AcceptList(headerString) ;
        MediaRange aItem = new MediaRange(mediaRangeStr) ;  // MediaType
        MediaType m = l.match(aItem) ;
        if ( m == null )
            return null ;
        return m.toHeaderString() ;
    }

    /**
     * Split and trims a string using a given regex.
     *
     * @param s string
     * @param splitStr given regex
     * @return an array with the trimmed strings found
     */
    /*package*/ static String[] split(String s, String splitStr)
    {
        String[] x = s.split(splitStr,2) ;
        for ( int i = 0 ; i < x.length ; i++ )
        {
            x[i] = x[i].trim() ;
        }
        return x ;
    }

    /**
     * <p>Chooses the charset by using the Accept-Charset HTTP header.</p>
     *
     * <p>See {@link ConNeg#choose(String, AcceptList, MediaType)}.</p>
     *
     * @param httpRequest HTTP request
     * @param myPrefs accept list
     * @param defaultMediaType default media type
     * @return media type chosen
     */
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

    /**
     * <p>Choose the content media type by extracting the Accept HTTP header from
     * the HTTP request and choosing
     * (see {@link ConNeg#choose(String, AcceptList, MediaType)}) a content media
     * type that matches the header.</p>
     *
     * @param httpRequest HTTP request
     * @param myPrefs accept list
     * @param defaultMediaType default media type
     * @return media type chosen
     */
    public static MediaType chooseContentType(HttpServletRequest httpRequest,
                                              AcceptList myPrefs,
                                              MediaType defaultMediaType)
    {
        String a = FusekiNetLib.getAccept(httpRequest) ;
        if ( log.isDebugEnabled() )
            log.debug("Accept request: "+a) ;
        
        MediaType item = choose(a, myPrefs, defaultMediaType) ;
    
        if ( log.isDebugEnabled() )
            log.debug("Content type chosen: "+item) ;
    
        return item ;
    }

    /**
     * <p>This method receives a HTTP header string, an {@link AcceptList} and a
     * default {@link MediaType}.</p>
     *
     * <p>If the header string is null, it returns the given default MediaType.</p>
     *
     * <p>Otherwise it builds an {@link AcceptList} object with the header string
     * and uses it to match against the given MediaType.</p>
     *
     * @param headerString HTTP header string
     * @param myPrefs accept list
     * @param defaultMediaType default media type
     * @return a media type or <code>null</code> if none matched or if the
     *          HTTP header string and the default media type are <code>null</code>.
     */
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
