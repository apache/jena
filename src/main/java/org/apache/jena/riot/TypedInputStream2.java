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

package org.apache.jena.riot;

import java.io.InputStream;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.web.ContentType ;

public class TypedInputStream2
{ 
    private InputStream input ;
    private ContentType mediaType ;
    // The URI to use when parsing.
    // May be different from the URI used to access the resource 
    // e.g. 303 redirection, mapped URI redirection 
    private String baseURI ;
    
    public TypedInputStream2(InputStream in)
    { this(in, null, null) ; }
    
    public TypedInputStream2(InputStream in, String mediaType, String charset, String baseURI)
    {
        this(in, ContentType.create(mediaType, charset), baseURI) ;
    }
    
    public TypedInputStream2(InputStream in, ContentType ct, String baseURI)
    {
        this.input = in ;
        this.mediaType = ct ;
        this.baseURI = baseURI ;
    }
    
    public InputStream getInput()           { return input ; }
    
    /** @deprecated Use {@link #getContentType} */
    @Deprecated 
    public String getMimeType()                 { return getContentType()  ; }

    
    public String getContentType()          { return mediaType == null ? null : mediaType.getContentType() ; }
    public String getCharset()              { return mediaType == null ? null : mediaType.getCharset() ; }
    public ContentType getMediaType()       { return mediaType ; }
    public String getBaseURI()              { return baseURI ; }
    
    public void close()                     { IO.close(input) ; }
}
