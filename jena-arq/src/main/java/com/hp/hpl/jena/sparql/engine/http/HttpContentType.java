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

package com.hp.hpl.jena.sparql.engine.http;



/**
 * Handle HTTP content type */

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
        StringBuilder sbuff = new StringBuilder() ;
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
