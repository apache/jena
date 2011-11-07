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

package com.hp.hpl.jena.util;

import java.io.InputStream;

public class TypedStream
{ 
    private InputStream input ;
    private String mimeType = null ;
    private String charset = null ;
    
    public TypedStream(InputStream in)
    { this(in, null, null) ; }
    
    public TypedStream(InputStream in, String header)
    { 
        input = in ;
        // Parse header.
//        mimeType = t ; 
//        charset = c ;
    }
    
    public TypedStream(InputStream in, String t, String c)
    { input = in ; mimeType = t ; charset = c ; }
    
    public InputStream getInput()               { return input ; }
    public String getMimeType()                 { return mimeType ; }
    public String getCharset()                  { return charset ; }
    
    // Parser for content-type header
     
}
