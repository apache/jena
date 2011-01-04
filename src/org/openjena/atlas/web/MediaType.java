/*
 * (c ) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.web;

import static org.openjena.atlas.lib.Lib.equal ;
import static org.openjena.atlas.lib.Lib.hashCodeObject ;

import java.util.Iterator ;
import java.util.LinkedHashMap ;
import java.util.Map ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** A structure to represent a <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">media type</a>.
 * Se also the <a href="http://httpd.apache.org/docs/current/content-negotiation.html">Apache httpd documentation</a>.
 */
public class MediaType
{
    private static Logger log = LoggerFactory.getLogger(MediaType.class) ; 

    private String type = null ;
    private String subType = null ;
    private String charset = null ;
    public static final String strCharset              = "charset" ;
    // Keys in insertion order.
    private Map<String, String> params = new LinkedHashMap<String, String>() ;
   
    private MediaType() {}
    
    public MediaType(MediaType other)
    {
        this.type = other.type ;
        this.subType = other.subType ;
        // Order preserving copy.
        this.params = new LinkedHashMap<String, String>(other.params) ;
    }
    
    public MediaType(String string)
    {
        parseOneEntry(string) ;
    }
    
    /** Create a media type from type and subType */
    protected MediaType(String type, String subType)
    {
        this.type = type ;
        this.subType = subType ;
    }

    public static MediaType create(String contentType, String charset)
    {
        MediaType mediaType = new MediaType(contentType) ;
        mediaType.setParameter(strCharset, charset) ;
        return mediaType ;
    }
    
    public static MediaType create(String contentType, String subType, String charset)
    {
        MediaType mediaType = new MediaType() ;
        mediaType.type = contentType ;
        mediaType.subType = subType ;
        mediaType.setParameter(strCharset, charset) ;
        return mediaType ;
    }
    
    private void parseOneEntry(String s)
    {
        String[] x = WebLib.split(s, ";") ;
        parseAndSetType(x[0]) ;
        
        for ( int i = 1 ; i < x.length ; i++ )
        {
            // Each a parameter
            String z[] = WebLib.split(x[i], "=") ;
            if ( z.length == 2 )
                this.params.put(z[0], z[1]) ;
            else
                log.warn("Duff parameter: "+x[i]+" in "+s) ;
        }
        strContentType = null ; 
    }
    
    private void parseAndSetType(String s)
    {
        String[] t = WebLib.split(s, "/") ;
        type = t[0] ;
        if ( t.length > 1 )
            subType = t[1] ;
    }
    
    /** Format for use in HTTP header */
    
    public String toHeaderString()
    {
        StringBuilder b = new StringBuilder() ;
        b.append(type) ;
        if ( subType != null )
            b.append("/").append(subType) ;

        for ( Map.Entry<String, String> entry: params.entrySet() )
        {
            b.append(";") ;
            b.append(entry.getKey()) ;
            b.append("=") ;
            b.append(entry.getValue()) ;
        }
        return b.toString() ;
    }
    
    /** Format to show structure - intentionally different from header
     *  form so you can tell parsing happened correctly
     */  
    
    @Override
    public String toString()
    {
        StringBuffer b = new StringBuffer() ;
        b.append("[") ;
        b.append(type) ;
        if ( subType != null )
            b.append("/").append(subType) ;
        for ( Iterator<String> iter = params.keySet().iterator() ; iter.hasNext() ; )
        {
            String k = iter.next() ;
            String v = params.get(k) ;
            b.append(" ") ;
            b.append(k) ;
            b.append("=") ;
            b.append(v) ;
        }
        b.append("]") ;
        return b.toString() ;
    }
    
//    private String type = null ;
//    private String subType = null ;
//    // Keys in insertion order.
//    private Map<String, String> params = new LinkedHashMap<String, String>() ;
    
    @Override
    public int hashCode() 
    {
        return hashCodeObject(type, 1)^hashCodeObject(subType, 2)^hashCodeObject(params, 3) ;
    }
    
    @Override
    public boolean equals(Object object) 
    {
        if (this == object) return true ;
        if (!(object instanceof MediaType)) return false ;
        MediaType mt = (MediaType)object ;
        return equal(type, mt.type) && equal(subType, mt.subType) && equal(params, mt.params) ;
    }

    public String getParameter(String name)             { return params.get(name) ; }
    public void setParameter(String name, String value) { params.put(name, value) ; strContentType = null ; }
    
    private String strContentType = null ;
    public String getContentType()
    {
        if ( strContentType != null )
            return strContentType ;
        if ( subType == null )
            return type ;
        return type+"/"+subType ;
    }
    
    public String getCharset()              { return getParameter(strCharset) ; }

    public String getSubType()              { return subType ; }
    public void setSubType(String subType)  { this.subType = subType ; strContentType = null ; }
    public String getType()                 { return type ; }
    public void setType(String type)        { this.type = type ; strContentType = null ; }
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