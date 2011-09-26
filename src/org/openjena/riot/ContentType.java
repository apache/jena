/**
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

package org.openjena.riot;


/** Content Type - Parsed version of content type / MIME type */
public class ContentType
{
    // Unify with MediaType
    
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
        return new ContentType(contentType, charset, null) ;
    }
    
    private final String contentType ;
    // Split into type/substype.
    private final String charset ;
    private final String dftCharset ;
    
    /** Create a media type, with a default charset */ 
    public static ContentType createConst(String contentType, String dftCharset)
    {
        return new ContentType(contentType, null, dftCharset) ;
    }
    
    public static ContentType create(String contentType, String charset)
    {
        return new ContentType(contentType, charset, null) ;
    }
    
    public static ContentType create(String contentType)
    {
        return new ContentType(contentType, null, null) ;
    }

    private ContentType(String contentType, String charset, String dftCharset)
    {
        this.contentType = contentType ;
        this.charset = charset ;
        this.dftCharset = dftCharset ;
    }
    
    
    @Override
    public String toString()
    {
        String x = contentType ;
        if ( charset != null )
            x = x + ";" + nameCharset + "=" + charset ;
        return x ;
    }

    public String getContentType()
    {
        return contentType ;
    }

    public String getCharset()
    {
        return charset ;
    }

    public String getDftCharset()
    {
        return dftCharset ;
    }
}
