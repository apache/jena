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

package org.apache.jena.atlas.json.io.parser;

import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.json.io.JSONHandler ;
import org.apache.jena.atlas.json.io.parserjavacc.JSONPrinter ;

/** JSON parser */
public class JSONParser
{    
    /** Parse from an input stream to get a Json object */ 
    public static void parse(InputStream input)
    { parse(input, new JSONPrinter()) ; }

    /** Parse to get a Json object */ 
    public static void parse(InputStream input, JSONHandler handler)
    {
        PeekReader r = PeekReader.makeUTF8(input) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parse(t, handler) ;
    }

    /** Parse from a reader to get a Json object */ 
    public static void parse(Reader reader)
    { parse(reader, new JSONPrinter()) ; }
    
    /** Parse to get a Json object */ 
    public static void parse(Reader reader, JSONHandler handler)
    {
        PeekReader r = PeekReader.make(reader) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parse(t, handler) ;
    }
    
    private static void parse(TokenizerJSON t, JSONHandler handler)
    {
        JSONP p = new JSONP(t , handler) ;
        p.parse() ;
    }

    /** Parse from a reader to get an Json value */ 
    public static void parseAny(Reader reader)
    { parseAny(reader, new JSONPrinter()) ; }

    /** Parse to get a Json primitive */ 
    public static void parseAny(Reader reader, JSONHandler handler)
    {
        PeekReader r = PeekReader.make(reader) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parseAny(t, handler) ;
    }
        
    /** Parse from a reader to get an Json value */ 
    public static void parseAny(InputStream input)
    { parseAny(input, new JSONPrinter()) ; }

    /** Parse to get a Json primitive */ 
    public static void parseAny(InputStream input, JSONHandler handler)
    {
        PeekReader r = PeekReader.makeUTF8(input) ;
        TokenizerJSON t = new TokenizerJSON(r) ;
        parseAny(t, handler) ;
    }
        
    private static void parseAny(TokenizerJSON t, JSONHandler handler)
    {
        JSONP p = new JSONP(t , handler) ;
        p.parseAny() ;
    }

}
