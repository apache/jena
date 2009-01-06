/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;



/**
 * Handle HTTP content type
 * 
 * @author Andy Seaborne
 */

public class HttpContentType
{

    String mediaType = null;
    String params[] = null;
    String charset = null;

    public HttpContentType(String s)
    {
        parse(s);
    }

    public HttpContentType(String s, String defaultMediaType, String defaultCharset)
    {
        this(s);
        if (mediaType == null)
            mediaType = defaultMediaType;
        if (charset == null)
            charset = defaultCharset;
    }

    /**
     * @return Media type as string
     */
    public String getMediaType() { return mediaType; }

    /**
     * @param charset The charset to set.
     */
    public void setCharset(String charset) { this.charset = charset; }
    
    /**
     * @return charset as string
     */
    public String getCharset() { return charset; }

    /**
     * @param mediaType The mediaType to set.
     */
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    
    
    // Ignore misc params.
    @Override
    public String toString()
    {
        StringBuffer sbuff = new StringBuffer() ;
        if ( mediaType != null )
            sbuff.append(mediaType) ;
        if ( charset != null )
        {    
            sbuff.append("; charset=") ;
            sbuff.append(charset) ;
        }
        return sbuff.toString() ;
    }
    
    private void parse(String s)
    {
        if (s == null)
            return;

//        int j = s.indexOf(';');
//        if (j == -1)
//        {
//            mediaType = s.trim() ;
//            return ;
//        }
//            
//        mediaType = s.substring(0, j).trim();
//        String sParam = s.substring(j + 1) ;
        
        params = s.split(";") ;
        for ( int i = 0 ; i < params.length ; i++ )
        {
            params[i] = params[i].trim();

            if ( params[i].matches("charset\\s*=.*") )
            {    
                int k = params[i].indexOf('=') ;
                charset = params[i].substring(k+1).trim() ;
            }
            else
                mediaType = params[i] ;
        }
    }
}

/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
