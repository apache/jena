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

import java.io.FilterInputStream ;
import java.io.IOException ;
import java.io.InputStream;

import org.openjena.atlas.io.IO ;

public class TypedInputStream extends FilterInputStream
{ 
    private final MediaType mediaType ;
    
    public TypedInputStream(InputStream in)
    { this(in, null, null) ; }
    
    public TypedInputStream(InputStream in, MediaType mediaType)
    {
        super(in) ;
        this.mediaType = mediaType ;
    }
    
    public TypedInputStream(InputStream in, String mediaType, String charset)
    {
        this(in, MediaType.create(mediaType, charset)) ;
    }
    
    public String getMediaType()                { return mediaType.getContentType() ; }
    public String getCharset()                  { return mediaType.getCharset() ; }
    
    @Override
    public void close()
    {
        try { super.close() ; } catch (IOException ex) { IO.exception(ex) ; }
    }
}
