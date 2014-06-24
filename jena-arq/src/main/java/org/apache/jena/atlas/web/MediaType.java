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

package org.apache.jena.atlas.web ;

import static org.apache.jena.atlas.lib.Lib.equal ;
import static org.apache.jena.atlas.lib.Lib.hashCodeObject ;

import java.util.LinkedHashMap ;
import java.util.Map ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * A structure to represent a <a
 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">media
 * type</a>. See also the <a
 * href="http://httpd.apache.org/docs/current/content-negotiation.html">Apache
 * httpd documentation</a>.
 */
public class MediaType {
    private static Logger             log        = LoggerFactory.getLogger(MediaType.class) ;

    private static final String       strCharset = "charset" ;

    private final String              type ;
    private final String              subType ;
    // Keys in insertion order.
    private final Map<String, String> params ;

    protected MediaType(ParsedMediaType parser) {
        this.type = parser.type ;
        this.subType = parser.subType ;
        this.params = parser.params ;
    }

    public MediaType(MediaType other) {
        this.type = other.type ;
        this.subType = other.subType ;
        // Order preserving copy.
        this.params = new LinkedHashMap<>(other.params) ;
    }

    /** Create a media type from type and subType */
    public MediaType(String type, String subType) {
        this(type, subType, null) ;
    }

    /** Create a media type from type and subType */
    public MediaType(String type, String subType, String charset) {
        this.type = type ;
        this.subType = subType ;
        this.params = new LinkedHashMap<>() ;
        if ( charset != null )
            setParameter(strCharset, charset) ;
    }

    public static MediaType create(String contentType, String charset) {
        ParsedMediaType mediaType = parse(contentType) ;
        if ( charset != null )
            mediaType.params.put(strCharset, charset) ;
        return new MediaType(mediaType) ;
    }

    public static MediaType createFromContentType(String string) {
        return new MediaType(parse(string)) ;
    }

    public static MediaType create(String contentType, String subType, String charset) {
        return new MediaType(contentType, subType, charset) ;
    }

    public static MediaType create(String string) {
        if ( string == null )
            return null ;
        return new MediaType(parse(string)) ;
    }

    public static ParsedMediaType parse(String string) {
        ParsedMediaType mt = new ParsedMediaType() ;

        String[] x = WebLib.split(string, ";") ;
        String[] t = WebLib.split(x[0], "/") ;
        mt.type = t[0] ;
        if ( t.length > 1 )
            mt.subType = t[1] ;

        for (int i = 1; i < x.length; i++) {
            // Each a parameter
            String z[] = WebLib.split(x[i], "=") ;
            if ( z.length == 2 )
                mt.params.put(z[0], z[1]) ;
            else
                log.warn("Duff parameter: " + x[i] + " in " + string) ;
        }
        return mt ;
    }

    /** Format for use in HTTP header */

    public String toHeaderString() {
        StringBuilder b = new StringBuilder() ;
        b.append(type) ;
        if ( subType != null )
            b.append("/").append(subType) ;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            b.append(";") ;
            b.append(entry.getKey()) ;
            b.append("=") ;
            b.append(entry.getValue()) ;
        }
        return b.toString() ;
    }

    /**
     * Format to show structure - intentionally different from header form so
     * you can tell parsing happened correctly
     */

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder() ;
        b.append("[") ;
        b.append(type) ;
        if ( subType != null )
            b.append("/").append(subType) ;
        for (String k : params.keySet()) {
            if ( k.equals("boundary") )
                continue ;
            String v = params.get(k) ;
            b.append(" ") ;
            b.append(k) ;
            b.append("=") ;
            b.append(v) ;
        }
        b.append("]") ;
        return b.toString() ;
    }

    // private String type = null ;
    // private String subType = null ;
    // // Keys in insertion order.
    // private Map<String, String> params = new LinkedHashMap<String, String>()
    // ;

    @Override
    public int hashCode() {
        return hashCodeObject(type, 1) ^ hashCodeObject(subType, 2) ^ hashCodeObject(params, 3) ;
    }

    @Override
    public boolean equals(Object object) {
        if ( this == object )
            return true ;
        if ( !(object instanceof MediaType) )
            return false ;
        MediaType mt = (MediaType)object ;
        return equal(type, mt.type) && equal(subType, mt.subType) && equal(params, mt.params) ;
    }

    public String getParameter(String name) {
        return params.get(name) ;
    }

    private void setParameter(String name, String value) {
        params.put(name, value) ;
        strContentType = null ;
    }

    // A cache.
    private String strContentType = null ;

    public String getContentType() {
        if ( strContentType != null )
            return strContentType ;
        if ( subType == null )
            return type ;
        return type + "/" + subType ;
    }

    public String getCharset() {
        return getParameter(strCharset) ;
    }

    public String getSubType() {
        return subType ;
    }

    // public void setSubType(String subType) { this.subType = subType ;
    // strContentType = null ; }
    public String getType() {
        return type ;
    }
    // public void setType(String type) { this.type = type ; strContentType =
    // null ; }

    /**
     * The outcome of parsing
     * 
     * @see MediaType#parse
     */
    /* package */static class ParsedMediaType {
        public String              type ;
        public String              subType ;
        public Map<String, String> params = new LinkedHashMap<>() ;
    }
}
