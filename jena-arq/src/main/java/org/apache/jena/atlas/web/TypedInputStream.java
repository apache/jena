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

package org.apache.jena.atlas.web;

import java.io.FilterInputStream ;
import java.io.IOException ;
import java.io.InputStream ;

import org.apache.jena.atlas.io.IO ;

public class TypedInputStream extends FilterInputStream
{ 
    private ContentType mediaType ;
    // The URI to use when parsing.
    // May be different from the URI used to access the resource 
    // e.g. 303 redirection, mapped URI redirection 
    private String baseURI ;
    
    public TypedInputStream(InputStream in)
    { this(in, (ContentType)null, null) ; }
    
    public TypedInputStream(InputStream in, String contentType)
    { this(in, ContentType.create(contentType), null) ; }

    public TypedInputStream(InputStream in, String mediaType, String charset)
    { this(in, mediaType, charset, null) ; }
    
    public TypedInputStream(InputStream in, String mediaType, String charset, String baseURI)
    { this(in, ContentType.create(mediaType, charset), baseURI) ; }
    
    public TypedInputStream(InputStream in, ContentType ct)
    { this(in, ct, null) ; }
    
    public TypedInputStream(InputStream in, ContentType ct, String baseURI)
    {
        super(in) ;
        this.mediaType = ct ;
        this.baseURI = baseURI ;
    }
    
    public String getContentType()          { return mediaType == null ? null : mediaType.getContentType() ; }
    public String getCharset()              { return mediaType == null ? null : mediaType.getCharset() ; }
    public ContentType getMediaType()       { return mediaType ; }
    public String getBaseURI()              { return baseURI ; }
    
    @Override
    public void close() {
        try { super.close() ; }
        catch (IOException ex) { IO.exception(ex) ; }
    }
}