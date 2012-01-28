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

package org.openjena.atlas.web;

import java.io.InputStream;

import org.openjena.atlas.io.IO ;
import org.openjena.riot.ContentType ;

public class TypedStream
{ 
    private InputStream input ;
    private ContentType mediaType ;
    
    public TypedStream(InputStream in)
    { this(in, null) ; }
    
    public TypedStream(InputStream in, String mediaType, String charset)
    {
        this(in, ContentType.create(mediaType, charset)) ;
    }
    
    public TypedStream(InputStream in, ContentType ct)
    {
        input = in ;
        mediaType = ct ;
    }
    
    public InputStream getInput()           { return input ; }
    public String getContentType()          { return mediaType.getContentType() ; }
    public String getCharset()              { return mediaType.getCharset() ; }
    public ContentType getMediaType()       { return mediaType ; }
    
    public void close()                     { IO.close(input) ; }
}
