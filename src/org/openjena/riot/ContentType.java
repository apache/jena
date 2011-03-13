/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot;


/** Content Type - Parsed version of content type / MIME type */
public class ContentType
{
    private static String nameCharset = "charset" ; // HttpNames.charset
    
    /** Split Content-Type into MIME type and charset */ 
    public static ContentType parse(String x)
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
            if ( z[0].toLowerCase().startsWith(nameCharset) )
                charset=z[1].trim() ;
        }
        
        if ( contentType != null ) contentType = contentType.toLowerCase() ;
        if ( charset != null ) charset = charset.toLowerCase() ;
        return new ContentType(contentType, charset) ;
    }
    
    // => MediaType and add a map.
    public final String contentType ;
    public final String charset ;
    public ContentType(String contentType, String charset)
    {
        this.contentType = contentType ;
        this.charset = charset ;
    }
    
//    public ContentType(MediaType mediaType)
//    {
//        contentType = mediaType.getContentType() ;
//        charset = mediaType.getCharset() ;
//    }

    @Override
    public String toString()
    {
        String x = contentType ;
        if ( charset != null )
            x = x + ";" + nameCharset + "=" + charset ;
        return x ;
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